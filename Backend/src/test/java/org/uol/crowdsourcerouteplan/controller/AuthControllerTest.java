package org.uol.crowdsourcerouteplan.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.uol.crowdsourcerouteplan.dto.LoginResponseDTO;
import org.uol.crowdsourcerouteplan.dto.registerdto;
import org.uol.crowdsourcerouteplan.service.RoutePlanServiceImpl;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private RoutePlanServiceImpl routePlanService;

    private AuthController controller;

    @BeforeEach
    void setUp() {
        controller = new AuthController(routePlanService);
    }

    // --- Register -------------------------------------------------------

    @Test
    @DisplayName("registerUser: 400 when username empty")
    void register_emptyUsername() {
        registerdto dto = new registerdto();
        dto.setUname("");
        dto.setPassword("pass");

        ResponseEntity<?> resp = controller.registerUser(dto);

        System.out.println("Status: " + resp.getStatusCode());
        System.out.println("Body  : " + resp.getBody());

        assertEquals(HttpStatus.BAD_REQUEST, resp.getStatusCode());
        assertTrue(((Map<?, ?>) resp.getBody()).get("message")
                .toString().contains("must not be empty"));
    }

    @Test
    @DisplayName("registerUser: 200 when success")
    void register_success() {
        registerdto dto = new registerdto();
        dto.setUname("user");
        dto.setPassword("pass");

        doNothing().when(routePlanService).registerUser(dto);

        ResponseEntity<?> resp = controller.registerUser(dto);

        System.out.println("Status: " + resp.getStatusCode());
        System.out.println("Body  : " + resp.getBody());

        assertEquals(HttpStatus.OK, resp.getStatusCode());
        assertEquals("User registered successfully", ((Map<?, ?>) resp.getBody()).get("message"));
        verify(routePlanService).registerUser(dto);
    }

    @Test
    @DisplayName("registerUser: 400 when service throws RuntimeException")
    void register_serviceError() {
        registerdto dto = new registerdto();
        dto.setUname("user");
        dto.setPassword("pass");

        doThrow(new RuntimeException("User already exists"))
                .when(routePlanService).registerUser(dto);

        ResponseEntity<?> resp = controller.registerUser(dto);

        System.out.println("Status: " + resp.getStatusCode());
        System.out.println("Body  : " + resp.getBody());

        assertEquals(HttpStatus.BAD_REQUEST, resp.getStatusCode());
        assertEquals("User already exists", ((Map<?, ?>) resp.getBody()).get("message"));
        verify(routePlanService).registerUser(dto);
    }

    // --- Login ----------------------------------------------------------

    @Test
    @DisplayName("login: 200 with token and message")
    void login_success() {
        registerdto dto = new registerdto();
        dto.setUname("user");
        dto.setPassword("pass");

        LoginResponseDTO loginResp = new LoginResponseDTO("jwtToken", "user", 123);
        when(routePlanService.verify(dto)).thenReturn(loginResp);

        ResponseEntity<?> resp = controller.login(dto);

        System.out.println("Status: " + resp.getStatusCode());
        System.out.println("Body  : " + resp.getBody());

        assertEquals(HttpStatus.OK, resp.getStatusCode());
        Map<?, ?> body = (Map<?, ?>) resp.getBody();
        assertEquals("Login successful", body.get("message"));
        assertEquals("jwtToken", body.get("token"));
        assertEquals("user", body.get("username"));
        assertEquals(123, body.get("userId"));
        verify(routePlanService).verify(dto);
    }

    @Test
    @DisplayName("login: 401 when invalid credentials")
    void login_invalid() {
        registerdto dto = new registerdto();
        dto.setUname("wrong");
        dto.setPassword("wrong");

        when(routePlanService.verify(dto)).thenReturn(null);

        ResponseEntity<?> resp = controller.login(dto);

        System.out.println("Status: " + resp.getStatusCode());
        System.out.println("Body  : " + resp.getBody());

        assertEquals(HttpStatus.UNAUTHORIZED, resp.getStatusCode());
        assertEquals("Invalid username or password", ((Map<?, ?>) resp.getBody()).get("message"));
        verify(routePlanService).verify(dto);
    }

    @Test
    @DisplayName("login: 401 when AuthenticationException")
    void login_authException() {
        registerdto dto = new registerdto();
        dto.setUname("user");
        dto.setPassword("pass");

        when(routePlanService.verify(dto))
                .thenThrow(new org.springframework.security.core.AuthenticationException("Bad creds") {});

        ResponseEntity<?> resp = controller.login(dto);

        System.out.println("Status: " + resp.getStatusCode());
        System.out.println("Body  : " + resp.getBody());

        assertEquals(HttpStatus.UNAUTHORIZED, resp.getStatusCode());
        assertEquals("Invalid username or password", ((Map<?, ?>) resp.getBody()).get("message"));
        verify(routePlanService).verify(dto);
    }

    @Test
    @DisplayName("login: 500 when unexpected error")
    void login_unexpected() {
        registerdto dto = new registerdto();
        dto.setUname("user");
        dto.setPassword("pass");

        when(routePlanService.verify(dto)).thenThrow(new RuntimeException("DB down"));

        ResponseEntity<?> resp = controller.login(dto);

        System.out.println("Status: " + resp.getStatusCode());
        System.out.println("Body  : " + resp.getBody());

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, resp.getStatusCode());
        assertEquals("An error occurred", ((Map<?, ?>) resp.getBody()).get("message"));
        verify(routePlanService).verify(dto);
    }
}
