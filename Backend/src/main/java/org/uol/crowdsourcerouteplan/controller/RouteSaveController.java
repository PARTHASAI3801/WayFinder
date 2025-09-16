package org.uol.crowdsourcerouteplan.controller;


import org.apache.hc.core5.http.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.uol.crowdsourcerouteplan.dto.ActivityFeedItem;
import org.uol.crowdsourcerouteplan.dto.SavedRouteRequest;
import org.uol.crowdsourcerouteplan.service.RoutePlanService;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/userRouteSave")
public class RouteSaveController {

    @Autowired
    private final RoutePlanService routePlanService;


    public RouteSaveController(RoutePlanService routePlanService) {
        this.routePlanService = routePlanService;
    }


    @PostMapping("/saveUserRoute")
    public ResponseEntity<?> saveUserRoute(@RequestBody SavedRouteRequest request){
        Integer id = routePlanService.saveUserRoute(request);
        return ResponseEntity.ok(id);
    }

    @GetMapping("/listOfSavedRoutes")
    public ResponseEntity<?> savedListSummary(@RequestParam Integer userId,
                                              @RequestParam Integer savedId) throws IOException, ParseException {
        return ResponseEntity.ok(routePlanService.savedListSummary(userId,savedId));
    }


    @GetMapping("/getAllUserSavedRoutes")
    public ResponseEntity<?> getAllUserSavedRoutes(@RequestParam Integer userId) {
        return ResponseEntity.ok(routePlanService.getAllUserSavedRoutes(userId));
    }

    @DeleteMapping("/deleteUserRoute")
    public ResponseEntity<Void> deleteUserRoute(@RequestParam Integer savedId, @RequestParam Integer userId) {
        routePlanService.deleteUserRoute(savedId, userId);
        return ResponseEntity.noContent().build();
    }


    @GetMapping("/materializeUserRoute")
    public ResponseEntity<?> materializeUserRoute(@RequestParam Integer savedId) {
        var saved = routePlanService.getUserRoute(savedId);
        return ResponseEntity.notFound().build();
    }


    @GetMapping("/userActivity")
    public List<ActivityFeedItem> getUserActivity(@RequestParam int userId){
        return routePlanService.getAllForUser(userId);
    }


}
