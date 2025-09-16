package org.uol.crowdsourcerouteplan.service;


import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.reactive.function.client.WebClient;
import org.uol.crowdsourcerouteplan.config.LocationsProperties;
import org.uol.crowdsourcerouteplan.dto.NlpParseRequest;
import org.uol.crowdsourcerouteplan.dto.RouteIntent;

import java.util.HashMap;
import java.util.Map;

@Service
public class RoutingService {
    private final LocationsProperties locations;
    private final WebClient webClient = WebClient.builder().build();

    public RoutingService(LocationsProperties locations) {
        this.locations = locations;
    }

    public Object getRoute(RouteIntent intent, NlpParseRequest.ParseContext ctx) {
        String base = locations.getBaseUrl();

        String startLocation;
        if (Boolean.TRUE.equals(intent.getUseCurrentLocation())) {
            if (ctx == null || ctx.getUserLat() == null || ctx.getUserLon() == null) {
                throw new IllegalArgumentException("Current location requested but user coordinates not provided.");
            }
            startLocation = ctx.getUserLat() + "," + ctx.getUserLon();
        } else {
            startLocation = intent.getFrom();
        }

        String endLocation = intent.getTo();
        String mode = intent.getMode();

        if (intent.getVia() != null && !intent.getVia().isEmpty()) {
            Map<String, Object> payload = new HashMap<>();
            payload.put("startLocation", startLocation);
            payload.put("viaStops", intent.getVia());
            payload.put("endLocation", endLocation);
            payload.put("transportMode", mode);

            return webClient.post()
                    .uri(base + "/locations/getRouteWithViaStopsORS")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(payload)
                    .retrieve()
                    .bodyToMono(Object.class)
                    .block();
        } else {
            LinkedMultiValueMap<String, String> q = new LinkedMultiValueMap<>();
            q.add("source", startLocation);
            q.add("destination", endLocation);
            q.add("mode", mode);

            return webClient.get()
                    .uri(base + "/locations/getRoutePath?source={s}&destination={d}&mode={m}",
                            startLocation, endLocation, mode)
                    .retrieve()
                    .bodyToMono(Object.class)
                    .block();

        }
    }
}
