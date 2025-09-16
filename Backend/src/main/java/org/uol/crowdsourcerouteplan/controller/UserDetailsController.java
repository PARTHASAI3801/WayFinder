package org.uol.crowdsourcerouteplan.controller;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.uol.crowdsourcerouteplan.dto.UserDetailsDTO;
import org.uol.crowdsourcerouteplan.dto.UserDetailsRequestDTO;
import org.uol.crowdsourcerouteplan.model.UserDetailsMgmt;
import org.uol.crowdsourcerouteplan.service.RoutePlanService;

import java.util.Map;

@RestController
@RequestMapping("/userMgmt")
public class UserDetailsController {

    @Autowired
    private final RoutePlanService routePlanService;


    public UserDetailsController(RoutePlanService routePlanService) {
        this.routePlanService = routePlanService;
    }


    @PostMapping("/saveUserDetails")
    public ResponseEntity<?> saveUserDetails(@RequestBody UserDetailsRequestDTO userDetailsRequestDTO){
        try{
            String msg=routePlanService.saveUserDetails(userDetailsRequestDTO);
            return ResponseEntity.ok(msg);
        } catch (Exception e){
            return ResponseEntity.badRequest().body("Error saving details: " + e.getMessage());
        }
    }

    @GetMapping("/getUserDetails")
    public ResponseEntity<?> getUserDetails(@RequestParam int userId) {
        try {
            UserDetailsDTO dto = routePlanService.getUserDetails(userId);
            return ResponseEntity.ok(dto);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Error fetching profile: " + e.getMessage());
        }
    }

    @PatchMapping("/updateUserDetails")
    public ResponseEntity<?> patchUserDetails(
            @RequestBody UserDetailsDTO updateDto) {

        try {
            UserDetailsMgmt updatedDetails = routePlanService.patchUserDetails(updateDto);
            return ResponseEntity.ok(updatedDetails);
        } catch (RuntimeException e) {
            String errorMessage = e.getMessage() != null ? e.getMessage() : "Failed to update user details";
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", errorMessage));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Server error occurred"));
        }
    }


}
