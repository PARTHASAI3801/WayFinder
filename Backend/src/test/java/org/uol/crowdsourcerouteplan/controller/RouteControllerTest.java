package org.uol.crowdsourcerouteplan.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.uol.crowdsourcerouteplan.dto.FeedbackDTO;
import org.uol.crowdsourcerouteplan.dto.RouteDTO;
import org.uol.crowdsourcerouteplan.dto.RouteWithViaRequestDTO;
import org.uol.crowdsourcerouteplan.dto.UserContributedRouteDTO;
import org.uol.crowdsourcerouteplan.service.RoutePlanServiceImpl;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RouteControllerTest {

    @Mock
    private RoutePlanServiceImpl routePlanService;

    private RouteController routeController;

    private final ObjectMapper mapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        routeController = new RouteController(routePlanService);
    }

    @Test
    @DisplayName("getRoute: returns 200 OK with geometry from service")
    void getRoute_returnsOkWithGeometry() throws Exception {
        ObjectNode node = mapper.createObjectNode().put("geometry", "encoded_polyline_here");
        when(routePlanService.fetchRouteReal("Leicester", "Nottingham", "driving-car"))
                .thenReturn(node);

        ResponseEntity<?> resp = routeController.getRoute("Leicester", "Nottingham", "driving-car");

        System.out.println("Status: " + resp.getStatusCode());
        System.out.println("Body  : " + resp.getBody());

        assertEquals(HttpStatus.OK, resp.getStatusCode());
        assertTrue(resp.getBody() instanceof JsonNode);
        assertEquals("encoded_polyline_here", ((JsonNode) resp.getBody()).get("geometry").asText());
        verify(routePlanService).fetchRouteReal("Leicester", "Nottingham", "driving-car");
    }

    @Test
    @DisplayName("getRoute: when service throws  500 with message")
    void getRoute_whenServiceThrows_returns500WithMessage() throws Exception {
        when(routePlanService.fetchRouteReal(anyString(), anyString(), anyString()))
                .thenThrow(new RuntimeException("boom"));

        ResponseEntity<?> resp = routeController.getRoute("A", "B", "driving-car");

        System.out.println("Status: " + resp.getStatusCode());
        System.out.println("Body  : " + resp.getBody());

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, resp.getStatusCode());
        assertEquals("boom", resp.getBody());
        verify(routePlanService).fetchRouteReal("A", "B", "driving-car");
    }

    @Test
    void getRouteWithViaStopsORS_returnsOk_asMap() {
        RouteWithViaRequestDTO req = new RouteWithViaRequestDTO();
        req.setStartLocation("Leicester");
        req.setViaStops(List.of("Loughborough"));
        req.setEndLocation("Nottingham");
        req.setTransportMode("cycling-regular");

        Map<String,Object> payload = new LinkedHashMap<>();
        payload.put("totalDistance", 12345);
        payload.put("duration", 3600);
        payload.put("segments", List.of(Map.of("geometry","abc123")));

        when(routePlanService.getRouteWithViaStopsORS(any())).thenReturn(payload);

        ResponseEntity<?> resp = routeController.getRouteWithViaStopsORS(req);

        System.out.println("Status: " + resp.getStatusCode());
        System.out.println("Body  : " + resp.getBody());
        assertEquals(HttpStatus.OK, resp.getStatusCode());
        @SuppressWarnings("unchecked")
        Map<String,Object> body = (Map<String,Object>) resp.getBody();
        assertEquals(12345, body.get("totalDistance"));
        List<?> segs = (List<?>) body.get("segments");
        assertEquals("abc123", ((Map<?,?>)segs.get(0)).get("geometry"));

        verify(routePlanService).getRouteWithViaStopsORS(any());
    }


    @Test
    @DisplayName("getRouteWithViaStopsORS: when service throws → 500 with error message")
    void getRouteWithViaStopsORS_whenServiceThrows_returns500() {
        RouteWithViaRequestDTO req = new RouteWithViaRequestDTO();
        req.setStartLocation("A");
        req.setViaStops(List.of("B"));
        req.setEndLocation("C");
        req.setTransportMode("driving-car");

        when(routePlanService.getRouteWithViaStopsORS(any()))
                .thenThrow(new RuntimeException("ORS down"));

        ResponseEntity<?> resp = routeController.getRouteWithViaStopsORS(req);

        System.out.println("Status: " + resp.getStatusCode());
        System.out.println("Body  : " + resp.getBody());

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, resp.getStatusCode());
        String msg = String.valueOf(resp.getBody());
        assertTrue(msg.contains("Error fetching ORS route with via stops"));
        assertTrue(msg.contains("ORS down"));

        verify(routePlanService).getRouteWithViaStopsORS(same(req));
    }



    // --- submitFeedback ----------------------------------------------------------

    @Test
    @DisplayName("submitFeedback: returns 200 OK with success message")
    void submitFeedback_returnsOk() throws Exception {
        FeedbackDTO dto = new FeedbackDTO();

        ResponseEntity<?> resp = routeController.submitFeedback(dto);
        System.out.println("Status: " + resp.getStatusCode());
        System.out.println("Body  : " + resp.getBody());

        // Assert
        assertEquals(HttpStatus.OK, resp.getStatusCode());
        assertEquals("Feedback submitted successfully.", resp.getBody());

        verify(routePlanService).submitFeedback(dto);
    }

    @Test
    @DisplayName("submitFeedback: when service throws 500 with error message")
    void submitFeedback_whenServiceThrows_returns500() throws Exception {
        FeedbackDTO dto = new FeedbackDTO();
        doThrow(new RuntimeException("DB down")).when(routePlanService).submitFeedback(any());
        ResponseEntity<?> resp = routeController.submitFeedback(dto);
        System.out.println("Status: " + resp.getStatusCode());
        System.out.println("Body  : " + resp.getBody());
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, resp.getStatusCode());
        String msg = String.valueOf(resp.getBody());
        assertTrue(msg.contains("An error occurred while submitting feedback"));
        assertTrue(msg.contains("DB down"));
        verify(routePlanService).submitFeedback(dto);
    }

// --- getUserContributedRoutes -----------------------------------------------

    @Test
    @DisplayName("getUserContributedRoutes: returns 200 OK with list from service")
    void getUserContributedRoutes_returnsOk() throws Exception {
        String startLat = "52.62";
        String startLng = "-1.13";
        String endLat   = "52.95";
        String endLng   = "-1.15";
        String mode     = "driving-car";

        List<UserContributedRouteDTO> routes = List.of(new UserContributedRouteDTO(),
                new UserContributedRouteDTO());

        when(routePlanService.findMatchingRoutes(startLat, startLng, endLat, endLng, mode))
                .thenReturn(routes);

        ResponseEntity<List<UserContributedRouteDTO>> resp =
                routeController.getUserContributedRoutes(startLat, startLng, endLat, endLng, mode);
        ObjectMapper om = new ObjectMapper();
        System.out.println("Status: " + resp.getStatusCode());
        System.out.println("Body  : " + om.writeValueAsString(resp.getBody()));

        // Assert
        assertEquals(HttpStatus.OK, resp.getStatusCode());
        assertNotNull(resp.getBody());
        assertEquals(2, resp.getBody().size());
        verify(routePlanService).findMatchingRoutes(startLat, startLng, endLat, endLng, mode);
    }

// --- saveRoute ---------------------------------------------------------------

    @Test
    @DisplayName("saveRoute: returns 200 OK with success message")
    void saveRoute_returnsOk() throws Exception {
        RouteDTO dto = new RouteDTO(); // fields not needed for controller test
        ResponseEntity<?> resp = routeController.saveRoute(dto);
        System.out.println("Status: " + resp.getStatusCode());
        System.out.println("Body  : " + resp.getBody());
        // Assert
        assertEquals(HttpStatus.OK, resp.getStatusCode());
        assertEquals("RoutePath Submitted Successfully", resp.getBody());
        // Verify delegation
        verify(routePlanService).saveRoute(dto);
    }




}
