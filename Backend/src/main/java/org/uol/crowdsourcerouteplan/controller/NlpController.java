package org.uol.crowdsourcerouteplan.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.uol.crowdsourcerouteplan.dto.NlpParseRequest;
import org.uol.crowdsourcerouteplan.dto.RouteIntent;
import org.uol.crowdsourcerouteplan.service.IntentExtractionService;
import org.uol.crowdsourcerouteplan.service.RoutingService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

@RestController
@RequestMapping("/nlp")
public class NlpController {

    private final IntentExtractionService intentService;
    private final RoutingService routingService;

    // Regex helpers to extract user-typed candidates for nicer "place not found" message
    private static final Pattern FROM_CAND = Pattern.compile(
            "\\bfrom\\s+([^,;]+?)(?=\\s+(to|via|using|by|with|on)\\b|[\\.,;]|$)",
            Pattern.CASE_INSENSITIVE);

    private static final Pattern TO_CAND = Pattern.compile(
            "\\bto\\s+([^,;]+?)(?=\\s+(via|using|by|with|on)\\b|[\\.,;]|$)",
            Pattern.CASE_INSENSITIVE);

    private static String extract(Pattern p, String text) {
        if (text == null) return null;
        var m = p.matcher(text);
        if (!m.find()) return null;
        return m.group(1).trim();
    }

    public NlpController(IntentExtractionService intentService, RoutingService routingService) {
        this.intentService = intentService;
        this.routingService = routingService;
    }

    @PostMapping("/parse")
    public ResponseEntity<Map<String, Object>> parse(@RequestBody NlpParseRequest req) {
        Map<String, Object> resp = new HashMap<>();
        try {
            RouteIntent intent = intentService.extract(req);
            List<String> missing = intentService.computeMissing(intent, req.getContext());

            String ask = null;
            Map<String, Object> cta = null;
            String nextAction;
            Object route = null;

            if (!missing.isEmpty()) {
                // user asked for current location but no coords
                if (missing.contains("userLocationPermissionOrCoords")) {
                    nextAction = "ASK_LOCATION";
                    ask = "To start from your current location, please share your location.";
                    cta = Map.of("type", "requestLocation", "label", "I'm here");
                }
                // Both from & to missing
                else if (missing.contains("from") && missing.contains("to")) {
                    String candFrom = extract(FROM_CAND, req.getPrompt());
                    String candTo = extract(TO_CAND, req.getPrompt());

                    if (candFrom != null && !candFrom.equalsIgnoreCase("my location") && candTo != null) {
                        ask = "Places not found: '" + candFrom + "' (from), '" + candTo + "' (to). " +
                                "Please enter both, or tap 'I'm here' for your starting point.";
                    } else {
                        ask = "What are your start and destination? You can also tap 'I'm here' for your start.";
                    }
                    cta = Map.of("type", "requestLocation", "label", "I'm here");
                    nextAction = "ASK_FROM_TO"; // tell FE to show two inputs
                }
                // Only from missing
                else if (missing.contains("from")) {
                    String candFrom = extract(FROM_CAND, req.getPrompt());
                    if (candFrom != null && !candFrom.equalsIgnoreCase("my location")) {
                        ask = "Place not found: '" + candFrom + "'. Please type your starting point or tap 'I'm here'.";
                    } else {
                        ask = "What’s your starting point? You can also tap 'I'm here' to use your current location.";
                    }
                    cta = Map.of("type", "requestLocation", "label", "I'm here");
                    nextAction = "ASK_FROM";
                }
                // Only to missing
                else if (missing.contains("to")) {
                    String candTo = extract(TO_CAND, req.getPrompt());
                    if (candTo != null) {
                        ask = "Place not found: '" + candTo + "'. Where are you heading?";
                    } else {
                        ask = "Where are you heading?";
                    }
                    nextAction = "ASK_TO";
                }
                // Fallback
                else {
                    ask = "Please provide: " + String.join(", ", missing);
                    nextAction = "ASK_MORE";
                }
            } else {
                // if all good then fetch route on backend
                try {
                    route = routingService.getRoute(intent, req.getContext());
                    nextAction = "SHOW_ROUTE";
                } catch (Exception ex) {
                    nextAction = "ERROR_ROUTE";
                    resp.put("routeError", ex.getMessage());
                }
            }

            resp.put("status", "OK");
            resp.put("data", intent);
            resp.put("missingFields", missing);
            resp.put("ask", ask);
            if (cta != null) resp.put("cta", cta);
            resp.put("nextAction", nextAction);
            resp.put("route", route);
            resp.put("error", null);
            return ResponseEntity.ok(resp);

        } catch (Exception e) {
            resp.put("status", "ERROR");
            resp.put("data", null);
            resp.put("missingFields", null);
            resp.put("ask", null);
            resp.put("error", e.getMessage());
            return ResponseEntity.status(500).body(resp);
        }
    }
}
