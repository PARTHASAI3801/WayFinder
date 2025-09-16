package org.uol.crowdsourcerouteplan.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.uol.crowdsourcerouteplan.config.GeminiProperties;
import reactor.core.publisher.Mono;

import java.util.Map;

@Component
public class GeminiClient {
    private final GeminiProperties props;
    private final WebClient http;
    private final ObjectMapper mapper = new ObjectMapper();

    public GeminiClient(GeminiProperties props) {
        this.props = props;
        this.http = WebClient.builder()
                .baseUrl(props.getUrl())
                .build();
    }

    /** Single-shot JSON: instruct + userPrompt + inline schema, returns raw JSON string. */
    public Mono<String> jsonExtract(String systemRules, String userPrompt, Map<String,Object> schema) {
        try {
            String prompt = systemRules +
                    "\n\nJSON Schema (follow EXACTLY):\n" + mapper.writeValueAsString(schema) +
                    "\n\nUSER PROMPT:\n" + userPrompt +
                    "\n\nReturn ONLY JSON (no prose).";
            Map<String,Object> body = Map.of(
                    "model", props.getModel(),
                    "generationConfig", Map.of(
                            "temperature", 0,
                            "response_mime_type", "application/json"
                    ),
                    "contents", new Object[] {
                            Map.of("role","user","parts", new Object[] { Map.of("text", prompt) })
                    }
            );
            return http.post()
                    .uri(uriBuilder -> uriBuilder
                            .path("/models/" + props.getModel() + ":generateContent")
                            .queryParam("key", props.getApiKey())
                            .build())
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(body)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .map(resp -> {
                        var cands = (java.util.List<Map<String,Object>>) resp.get("candidates");
                        if (cands == null || cands.isEmpty()) throw new RuntimeException("No candidates: " + resp);
                        var content = (Map<String,Object>) cands.get(0).get("content");
                        var parts = (java.util.List<Map<String,Object>>) content.get("parts");
                        String text = (String) parts.get(0).get("text");
                        if (text == null || text.isBlank()) throw new RuntimeException("Empty text: " + resp);
                        return text; // JSON as string
                    });
        } catch (Exception e) {
            return Mono.error(e);
        }
    }

    /** Repair pass: send invalid JSON + errors, ask for corrected JSON that satisfies schema. */
    public Mono<String> jsonRepair(String systemRules, String userPrompt, Map<String,Object> schema,
                                   String invalidJson, String validationErrors) {
        try {
            String prompt = systemRules +
                    "\n\nJSON Schema (follow EXACTLY):\n" + new ObjectMapper().writeValueAsString(schema) +
                    "\n\nORIGINAL USER PROMPT:\n" + userPrompt +
                    "\n\nPREVIOUS JSON (INVALID):\n" + invalidJson +
                    "\n\nVALIDATION ERRORS:\n" + validationErrors +
                    "\n\nReturn ONLY the corrected JSON that satisfies the schema.";
            Map<String,Object> body = Map.of(
                    "model", props.getModel(),
                    "generationConfig", Map.of(
                            "temperature", 0,
                            "response_mime_type", "application/json"
                    ),
                    "contents", new Object[] {
                            Map.of("role","user","parts", new Object[] { Map.of("text", prompt) })
                    }
            );
            return http.post()
                    .uri(uriBuilder -> uriBuilder
                            .path("/models/" + props.getModel() + ":generateContent")
                            .queryParam("key", props.getApiKey())
                            .build())
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(body)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .map(resp -> {
                        var cands = (java.util.List<Map<String,Object>>) resp.get("candidates");
                        if (cands == null || cands.isEmpty()) throw new RuntimeException("No candidates: " + resp);
                        var content = (Map<String,Object>) cands.get(0).get("content");
                        var parts = (java.util.List<Map<String,Object>>) content.get("parts");
                        String text = (String) parts.get(0).get("text");
                        if (text == null || text.isBlank()) throw new RuntimeException("Empty text: " + resp);
                        return text; // JSON as string
                    });
        } catch (Exception e) {
            return Mono.error(e);
        }
    }
}
