package org.uol.crowdsourcerouteplan.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.uol.crowdsourcerouteplan.dto.UserDetailsDTO;
import org.uol.crowdsourcerouteplan.dto.UserDetailsRequestDTO;
import org.uol.crowdsourcerouteplan.model.UserDetailsMgmt;
import org.uol.crowdsourcerouteplan.service.RoutePlanService;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserDetailsControllerTest {

    @Mock
    private RoutePlanService routePlanService;

    private UserDetailsController controller;

    @BeforeEach
    void setUp() {
        controller = new UserDetailsController(routePlanService);
    }

    // --- saveUserDetails ---------------------------------------------------
    @Test
    @DisplayName("saveUserDetails: returns 200 OK with success message")
    void saveUserDetails_success() {
        UserDetailsRequestDTO dto = new UserDetailsRequestDTO();
        when(routePlanService.saveUserDetails(dto)).thenReturn("Saved successfully");

        ResponseEntity<?> resp = controller.saveUserDetails(dto);

        System.out.println("Status: " + resp.getStatusCode());
        System.out.println("Body  : " + resp.getBody());

        assertEquals(HttpStatus.OK, resp.getStatusCode());
        assertEquals("Saved successfully", resp.getBody());
        verify(routePlanService).saveUserDetails(dto);
    }

    @Test
    @DisplayName("saveUserDetails: when service throws to 400 with error message")
    void saveUserDetails_failure() {
        UserDetailsRequestDTO dto = new UserDetailsRequestDTO();
        when(routePlanService.saveUserDetails(dto)).thenThrow(new RuntimeException("DB fail"));

        ResponseEntity<?> resp = controller.saveUserDetails(dto);

        System.out.println("Status: " + resp.getStatusCode());
        System.out.println("Body  : " + resp.getBody());

        assertEquals(HttpStatus.BAD_REQUEST, resp.getStatusCode());
        String msg = String.valueOf(resp.getBody());
        assertTrue(msg.contains("Error saving details"));
        assertTrue(msg.contains("DB fail"));
        verify(routePlanService).saveUserDetails(dto);
    }

    // --- getUserDetails ----------------------------------------------------
    @Test
    @DisplayName("getUserDetails: returns 200 OK with DTO")
    void getUserDetails_success() {
        UserDetailsDTO dto = new UserDetailsDTO();
        dto.setUserId(42);
        when(routePlanService.getUserDetails(42)).thenReturn(dto);

        ResponseEntity<?> resp = controller.getUserDetails(42);

        System.out.println("Status: " + resp.getStatusCode());
        System.out.println("Body  : " + resp.getBody());

        assertEquals(HttpStatus.OK, resp.getStatusCode());
        assertTrue(resp.getBody() instanceof UserDetailsDTO);
        assertEquals(42, ((UserDetailsDTO) resp.getBody()).getUserId());
        verify(routePlanService).getUserDetails(42);
    }

    @Test
    @DisplayName("getUserDetails: when service throws to 404 with error message")
    void getUserDetails_notFound() {
        when(routePlanService.getUserDetails(99)).thenThrow(new RuntimeException("User not found"));

        ResponseEntity<?> resp = controller.getUserDetails(99);

        System.out.println("Status: " + resp.getStatusCode());
        System.out.println("Body  : " + resp.getBody());

        assertEquals(HttpStatus.NOT_FOUND, resp.getStatusCode());
        String msg = String.valueOf(resp.getBody());
        assertTrue(msg.contains("Error fetching profile"));
        assertTrue(msg.contains("User not found"));
        verify(routePlanService).getUserDetails(99);
    }

    // --- patchUserDetails --------------------------------------------------
    @Test
    @DisplayName("patchUserDetails: returns 200 OK with updated entity")
    void patchUserDetails_success() {
        UserDetailsDTO dto = new UserDetailsDTO();
        dto.setUserId(7);

        UserDetailsMgmt updated = new UserDetailsMgmt();
        updated.setUserId(7);

        when(routePlanService.patchUserDetails(dto)).thenReturn(updated);

        ResponseEntity<?> resp = controller.patchUserDetails(dto);

        System.out.println("Status: " + resp.getStatusCode());
        System.out.println("Body  : " + resp.getBody());

        assertEquals(HttpStatus.OK, resp.getStatusCode());
        assertTrue(resp.getBody() instanceof UserDetailsMgmt);
        assertEquals(7, ((UserDetailsMgmt) resp.getBody()).getUserId());
        verify(routePlanService).patchUserDetails(dto);
    }

    @Test
    @DisplayName("patchUserDetails: when service throws RuntimeException to 404 with error map")
    void patchUserDetails_runtimeError() {
        UserDetailsDTO dto = new UserDetailsDTO();
        dto.setUserId(8);

        when(routePlanService.patchUserDetails(dto))
                .thenThrow(new RuntimeException("No profile to update"));

        ResponseEntity<?> resp = controller.patchUserDetails(dto);

        System.out.println("Status: " + resp.getStatusCode());
        System.out.println("Body  : " + resp.getBody());

        assertEquals(HttpStatus.NOT_FOUND, resp.getStatusCode());
        String msg = String.valueOf(resp.getBody());
        assertTrue(msg.contains("No profile to update"));
        verify(routePlanService).patchUserDetails(dto);
    }


}
