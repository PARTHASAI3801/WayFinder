package org.uol.crowdsourcerouteplan.controller;


import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.uol.crowdsourcerouteplan.dto.*;
import org.uol.crowdsourcerouteplan.model.ContributeRoute;
import org.uol.crowdsourcerouteplan.model.Location;
import org.uol.crowdsourcerouteplan.service.RoutePlanServiceImpl;

import java.util.List;


@RestController
@RequestMapping("/locations")
public class RouteController {

    @Autowired
    private RoutePlanServiceImpl routePlanService;

    public RouteController(RoutePlanServiceImpl routePlanService) {
        this.routePlanService = routePlanService;
    }


    @PostMapping("/addLocation")
    public ResponseEntity<?> addLocation(@RequestParam String place){
        try{
            Location location = routePlanService.fetchAndSaveLocation(place);
            return ResponseEntity.ok(location);
        } catch (Exception e){
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/getCoordinatesForLocation")
    public ResponseEntity<?> getCoordinatesForLoc(@RequestParam String place){
        try{
            CoordinatesDTO location = routePlanService.getCoordinatesForLoc(place);
            return ResponseEntity.ok(location);
        } catch (Exception e){
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/getCoordinates")
    ResponseEntity<?> getCoOrdinatesByName(@RequestParam String place){
        try{
            CoordinatesDTO coordinatesDTO = routePlanService.getLocationByName(place);
            return ResponseEntity.ok(coordinatesDTO);
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body("Place not found" + place);
        }
    }

    @GetMapping("/getRoutePath")
    public ResponseEntity<?> getRoute(
            @RequestParam String source,
            @RequestParam String destination,
            @RequestParam(required = false, defaultValue = "driving-car") String mode
    ){
        try {
            JsonNode routeData = routePlanService.fetchRouteReal(source,destination,mode);
            return ResponseEntity.ok(routeData);
        } catch (Exception e){
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }

    @PostMapping("/generate")
    public ResponseEntity<String> generateEdges(@RequestParam String location) {
        routePlanService.generateAllDistancesFromNode(location);
        return ResponseEntity.ok("Edges generated from node " + location);
    }

    @PostMapping("/generate/all")
    public ResponseEntity<String> generateEdgesForAllNodes() {
        List<Location> allLocations = routePlanService.allLocations();
        return ResponseEntity.ok("Edges generated for all nodes");
    }


    @PostMapping("/saveRoute")
    public ResponseEntity<?> saveRoute(@RequestBody RouteDTO routeDTO){
        routePlanService.saveRoute(routeDTO);
        return ResponseEntity.ok("RoutePath Submitted Successfully");
    }


    @GetMapping("/allRoutes")
    public ResponseEntity<?> getAllRoutes(@RequestParam String from,@RequestParam String to){
        try{
            List<RouteResponseDTO> routes = routePlanService.getAllRoutesByNames(from, to);
            if (routes.isEmpty()) {
                return ResponseEntity.status(404).body("No routes found between " + from + " and " + to);
            }
            return ResponseEntity.ok(routes);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Internal server error." +e.getMessage());
        }
    }


    @GetMapping("/generateEdges20")
    public ResponseEntity<String> generateEdges(){
        routePlanService.generateTop20Edges();
        return ResponseEntity.ok("Top 20 edges gnerated");
    }



    @GetMapping("/getOptimizedPath")
    public ResponseEntity<?> getOptimizedPath(@RequestParam String startName,@RequestParam String endName,String mode){
        try {
            RoutePathWithSegmentsDTO route = routePlanService.findOptimizedRoute(startName, endName,mode);
            return ResponseEntity.ok(route);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Internal server error: " + e.getMessage());
        }
    }


    @PostMapping("/submitFeedback")
    public ResponseEntity<?> submitFeedback(@RequestBody FeedbackDTO feedbackDTO){
        try {
            routePlanService.submitFeedback(feedbackDTO);
            return ResponseEntity.ok("Feedback submitted successfully.");
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An error occurred while submitting feedback: " + e.getMessage());
        }
    }


    @GetMapping("/feedbacks")
    public ResponseEntity<List<feedbackshowdto>> getFeedBacks(@RequestParam String routeType,@RequestParam int routeId){
        List<feedbackshowdto> feedbacks = routePlanService.getFeedbacks(routeType,routeId);
        return ResponseEntity.ok(feedbacks);
    }


    @PostMapping("/ExternalRouteId")
    public ResponseEntity<Integer> getRouteByCoordinates(@RequestBody ExternalRouteLocationDTO externalRouteLocationDTO){
        try {
            Integer routeID = routePlanService.findOrCreateRouteIdByLocationNames(
                    externalRouteLocationDTO.getStartLocation(), externalRouteLocationDTO.getEndLocation(),externalRouteLocationDTO.getTransportMode(),
                    externalRouteLocationDTO.getRouteType()
            );

            if (routeID != null) {
                return ResponseEntity.ok(routeID);
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }





//    @PostMapping("/getOrCreateExternalRoute")
//    public ResponseEntity<Integer> getOrCreateExternalRoute(@RequestBody ExternalRouteLocationDTO externalRouteLocationDTO){
//        Integer routeId=routePlanService.getOrCreateExternalRoute(externalRouteLocationDTO.getStartLocation(),externalRouteLocationDTO.getEndLocation());
//        return ResponseEntity.ok(routeId);
//    }

    @PostMapping("/contributeRoute")
    public ResponseEntity<?> contributeRoute(@RequestBody ContributeRouteRequest request){
        try{
            ContributeRoute savedRoute = routePlanService.saveContributedRoute(request);
            return ResponseEntity.ok("Route Contributed Successfully!" + savedRoute.getId());
        } catch (Exception e){
            return ResponseEntity.internalServerError().body("Failed to save route: " + e.getMessage());
        }
    }



    @GetMapping("/userContributedRoutes")
    public ResponseEntity<List<UserContributedRouteDTO>> getUserContributedRoutes(@RequestParam String startLat,
                                                                                  @RequestParam String startLng,
                                                                                  @RequestParam String endLat,
                                                                                  @RequestParam String endLng,
                                                                                  @RequestParam String mode){
        List<UserContributedRouteDTO> routes = routePlanService.findMatchingRoutes(startLat,startLng,endLat,endLng,mode);
        return ResponseEntity.ok(routes);
    }



    @PostMapping("/routeWithViaStops")
    public ResponseEntity<RoutePathWithSegmentsDTO> getRouteWithViaStops(@RequestBody RouteWithViaRequestDTO request){
        try{
            RoutePathWithSegmentsDTO route = routePlanService.findOptimizedRouteWithViaStops(
                    request.getStartLocation(),
                    request.getViaStops(),
                    request.getEndLocation(),
                    request.getTransportMode()
            );
            return ResponseEntity.ok(route);
        } catch (Exception e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }


    @PostMapping("/getRouteWithViaStopsORS")
    public ResponseEntity<?> getRouteWithViaStopsORS(@RequestBody RouteWithViaRequestDTO request){
        try {
            return ResponseEntity.ok(routePlanService.getRouteWithViaStopsORS(request));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error fetching ORS route with via stops: " + e.getMessage());
        }
    }


    @PostMapping("/ExternalViaRouteId")
    public ResponseEntity<?> getExternalViaRouteId(@RequestBody RouteWithViaRequestDTO request){
        Integer id = routePlanService.getOrCreateEcternalViaRouteId(request);
        return ResponseEntity.ok(id);
    }






}
