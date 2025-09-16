package org.uol.crowdsourcerouteplan.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion;
import com.networknt.schema.ValidationMessage;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.uol.crowdsourcerouteplan.dto.NlpParseRequest;
import org.uol.crowdsourcerouteplan.dto.PreferencesDto;
import org.uol.crowdsourcerouteplan.dto.RouteIntent;
import org.uol.crowdsourcerouteplan.util.ModeNormalizer;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class IntentExtractionService {

    private final GeminiClient gemini;
    private final ObjectMapper mapper = new ObjectMapper();

    private final JsonSchema validatorSchema = loadValidatorSchema();
    private final Map<String, Object> llmSchema = loadSchemaMap();

    public IntentExtractionService(GeminiClient gemini) {
        this.gemini = gemini;
    }

    public RouteIntent extract(NlpParseRequest req) throws Exception {
        String sys = systemPrompt();
        String usr = userPrompt(req);

        // 1) First pass with JSON-only output
        String content = gemini.jsonExtract(sys, usr, llmSchema).block();
        if (content == null || content.isBlank()) {
            throw new IllegalStateException("Empty response from Gemini.");
        }

        RouteIntent intent = mapper.readValue(content.trim(), RouteIntent.class);

        // 2) Validating locally
        var node = mapper.valueToTree(intent);
        Set<ValidationMessage> errors = validatorSchema.validate(node);

        // 3) One repair pass if needed
        if (!errors.isEmpty()) {
            String invalidJson = mapper.writeValueAsString(intent);
            String errList = errors.stream().map(ValidationMessage::getMessage)
                    .collect(Collectors.joining(" | "));
            String fixed = gemini.jsonRepair(repairSystemPrompt(), req.getPrompt(), llmSchema, invalidJson, errList).block();

            if (fixed == null || fixed.isBlank()) {
                throw new IllegalArgumentException("Schema repair failed: " + errors);
            }
            intent = mapper.readValue(fixed.trim(), RouteIntent.class);
            node = mapper.valueToTree(intent);
            errors = validatorSchema.validate(node);
            if (!errors.isEmpty()) {
                throw new IllegalArgumentException("Schema validation failed: " + errors);
            }
        }

        // 4) Defaults + normalization
        validateAndDefault(intent, req.getContext());
        return intent;
    }



    public List<String> computeMissing(RouteIntent intent, NlpParseRequest.ParseContext ctx) {
        List<String> missing = new ArrayList<>();
        if (isBlank(intent.getTo())) missing.add("to");

        boolean wantsCurrent = Boolean.TRUE.equals(intent.getUseCurrentLocation());
        boolean hasFrom = !isBlank(intent.getFrom());
        boolean hasUserCoords = ctx != null && ctx.getUserLat() != null && ctx.getUserLon() != null;

        if (!wantsCurrent && !hasFrom) {
            missing.add("from");
        } else if (wantsCurrent && !hasUserCoords) {
            missing.add("userLocationPermissionOrCoords");
        }
        return missing;
    }

    private void validateAndDefault(RouteIntent intent, NlpParseRequest.ParseContext ctx) {
        var node = mapper.valueToTree(intent);
        Set<ValidationMessage> errors = validatorSchema.validate(node);
        if (!errors.isEmpty()) {
            throw new IllegalArgumentException("Schema validation failed: " + errors);
        }

        if (intent.getVia() == null) intent.setVia(new ArrayList<>());
        if (intent.getPreferences() == null) intent.setPreferences(new PreferencesDto());

        String def = (ctx != null && ctx.getDefaultMode() != null) ? ctx.getDefaultMode() : "driving-car";
        intent.setMode(ModeNormalizer.normalize(intent.getMode(), def));
    }

    private static boolean isBlank(String s) { return s == null || s.trim().isEmpty(); }

    //schema loaders
    private static JsonSchema loadValidatorSchema() {
        try {
            var res = new ClassPathResource("nlp/route-intent.schema.json");
            String json = new String(res.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
            var factory = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V202012);
            return factory.getSchema(json);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load JSON schema (validator)", e);
        }
    }
    @SuppressWarnings("unchecked")
    private static Map<String, Object> loadSchemaMap() {
        try {
            var res = new ClassPathResource("nlp/route-intent.schema.json");
            String json = new String(res.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
            return new ObjectMapper().readValue(json, Map.class);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load JSON schema (for LLM)", e);
        }
    }

    // prompts
    private String systemPrompt() {
        return """
You output STRICT JSON that conforms to the provided JSON Schema.
Rules:
- Output ONLY JSON (no prose, no code fences).
- In "from", "to", and each "via" item, return ONLY bare UK place names. Do NOT include descriptors
  like "on a scenic route", "with my car", "using bike".
- If the user says "my location" (or similar), set "useCurrentLocation": true and "from": null.
- Transport synonyms:
    * car/drive/taxi → "driving-car"
    * lorry/truck/HGV → "driving-hgv"
    * walk/foot/legs/on foot → "foot-walking"
    * cycle/bike/bicycle → "cycling-regular"
- Preferences:
    * "scenic": true if scenic/nature/coastal/pretty route requested
    * "avoidMotorways": true if user asks to avoid motorways/highways
- Do not invent places; use null if unknown.

Examples:
A) "drive from leeds to london via york and oxford on a scenic route"
   → {"from":"leeds","to":"london","via":["york","oxford"],"mode":"driving-car",
      "preferences":{"scenic":true,"avoidMotorways":false,"leaveAfter":null},
      "useCurrentLocation":false}

B) "how can i go from leicester to edinburgh via stops liverpool and derby with my car"
   → {"from":"Leicester","to":"Edinburgh","via":["Liverpool","Derby"],"mode":"driving-car",
      "preferences":{"scenic":false,"avoidMotorways":false,"leaveAfter":null},
      "useCurrentLocation":false}
""";
    }

    private String repairSystemPrompt() {
        return """
Return ONLY JSON that satisfies the provided JSON Schema.
You are repairing a previously extracted object that failed validation.
Modify ONLY invalid fields so the JSON passes the schema.
Keep bare UK place names in from/to/via; no descriptors.
""";
    }

    private String userPrompt(NlpParseRequest req) {
        String country = (req.getContext() != null && req.getContext().getCountry() != null)
                ? req.getContext().getCountry() : "UK";
        return """
Country context: %s
Only UK places should be returned in from/to/via (correct casing is fine but not required).
USER PROMPT:
%s
""".formatted(country, req.getPrompt());
    }
}
