package org.uol.crowdsourcerouteplan.controller;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.*;
import org.uol.crowdsourcerouteplan.dto.LoginResponseDTO;
import org.uol.crowdsourcerouteplan.dto.registerdto;
import org.uol.crowdsourcerouteplan.model.User;
import org.uol.crowdsourcerouteplan.service.RoutePlanServiceImpl;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {



    @Autowired
    private final RoutePlanServiceImpl routePlanService;

    public AuthController(RoutePlanServiceImpl routePlanService) {
        this.routePlanService = routePlanService;
    }


    @GetMapping("/getAllUsers")
    public List<User> getAllUsers() {
        return routePlanService.getAllUsers();
    }


    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody registerdto request) {
        if (request.getUname() == null || request.getUname().isEmpty() || request.getPassword() == null || request.getPassword().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Username and password must not be empty."));
        }

        try {
            routePlanService.registerUser(request);
            return ResponseEntity.ok(Map.of("message", "User registered successfully"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }



    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody registerdto registerdto) {
        try {
            LoginResponseDTO loginResponse = routePlanService.verify(registerdto);

            if (loginResponse == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("message", "Invalid username or password"));
            }

            // token, username, userId, message in JSON
            return ResponseEntity.ok(Map.of(
                    "token", loginResponse.getToken(),
                    "message", "Login successful",
                    "username", loginResponse.getUsername(),
                    "userId", loginResponse.getUserId()
            ));
        } catch (AuthenticationException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Invalid username or password"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "An error occurred"));
        }
    }
}
