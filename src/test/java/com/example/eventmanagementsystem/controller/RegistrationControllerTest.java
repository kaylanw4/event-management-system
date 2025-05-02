package com.example.eventmanagementsystem.controller;

import com.example.eventmanagementsystem.config.TestWebConfig;
import com.example.eventmanagementsystem.dto.RegistrationDTO;
import com.example.eventmanagementsystem.exception.ApiException;
import com.example.eventmanagementsystem.exception.ResourceNotFoundException;
import com.example.eventmanagementsystem.security.EventSecurity;
import com.example.eventmanagementsystem.security.RegistrationSecurity;
import com.example.eventmanagementsystem.security.UserSecurity;
import com.example.eventmanagementsystem.service.RegistrationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(RegistrationController.class)
@Import(TestWebConfig.class)
@ActiveProfiles("test")
@DisplayName("Registration Controller Tests")
public class RegistrationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private RegistrationService registrationService;

    @MockBean
    private UserSecurity userSecurity;

    @MockBean
    private EventSecurity eventSecurity;

    @MockBean
    private RegistrationSecurity registrationSecurity;

    private RegistrationDTO registration1;
    private RegistrationDTO registration2;
    private RegistrationDTO registration3;
    private List<RegistrationDTO> allRegistrations;
    private List<RegistrationDTO> user1Registrations;
    private List<RegistrationDTO> event1Registrations;

    @BeforeEach
    void setUp() {
        // Setup test data
        LocalDateTime now = LocalDateTime.now();

        // Registration for User 1, Event 1
        registration1 = RegistrationDTO.builder()
                .id(1L)
                .userId(1L)
                .username("user1")
                .eventId(1L)
                .eventName("Tech Conference")
                .registrationTime(now.minusDays(7))
                .registrationStatus("CONFIRMED")
                .build();

        // Registration for User 1, Event 2
        registration2 = RegistrationDTO.builder()
                .id(2L)
                .userId(1L)
                .username("user1")
                .eventId(2L)
                .eventName("Coding Workshop")
                .registrationTime(now.minusDays(5))
                .registrationStatus("CONFIRMED")
                .build();

        // Registration for User 2, Event 1
        registration3 = RegistrationDTO.builder()
                .id(3L)
                .userId(2L)
                .username("user2")
                .eventId(1L)
                .eventName("Tech Conference")
                .registrationTime(now.minusDays(3))
                .registrationStatus("CONFIRMED")
                .build();

        // Collections of registrations
        allRegistrations = Arrays.asList(registration1, registration2, registration3);
        user1Registrations = Arrays.asList(registration1, registration2);
        event1Registrations = Arrays.asList(registration1, registration3);

        // Setup security mocks
        when(userSecurity.isSameUser(anyLong(), any())).thenReturn(true);
        when(eventSecurity.isOrganizerOrAdmin(anyLong(), any())).thenReturn(true);
        when(registrationSecurity.isUserOrEventOrganizer(anyLong(), any())).thenReturn(true);
    }

    @Nested
    @DisplayName("GET /api/registrations Tests")
    class GetAllRegistrationsTests {

        @Test
        @WithMockUser(roles = {"ADMIN"})
        @DisplayName("Admin should be able to get all registrations")
        void adminShouldBeAbleToGetAllRegistrations() throws Exception {
            // Given
            given(registrationService.findAllRegistrations()).willReturn(allRegistrations);

            // When & Then
            mockMvc.perform(get("/api/registrations"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(3)))
                    .andExpect(jsonPath("$[0].id").value(1L))
                    .andExpect(jsonPath("$[0].username").value("user1"))
                    .andExpect(jsonPath("$[0].eventName").value("Tech Conference"))
                    .andExpect(jsonPath("$[1].id").value(2L))
                    .andExpect(jsonPath("$[2].id").value(3L));

            verify(registrationService).findAllRegistrations();
        }

        @Test
        @WithMockUser(roles = {"USER"})
        @DisplayName("Regular user should not be able to get all registrations")
        void regularUserShouldNotBeAbleToGetAllRegistrations() throws Exception {
            // When & Then
            mockMvc.perform(get("/api/registrations"))
                    .andDo(print())
                    .andExpect(status().isForbidden());

            verify(registrationService, never()).findAllRegistrations();
        }
    }

    @Nested
    @DisplayName("GET /api/registrations/{id} Tests")
    class GetRegistrationByIdTests {

        @Test
        @WithMockUser(roles = {"ADMIN"})
        @DisplayName("Admin should be able to get registration by ID")
        void adminShouldBeAbleToGetRegistrationById() throws Exception {
            // Given
            given(registrationService.findRegistrationById(1L)).willReturn(registration1);

            // When & Then
            mockMvc.perform(get("/api/registrations/{id}", 1L))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1L))
                    .andExpect(jsonPath("$.username").value("user1"))
                    .andExpect(jsonPath("$.eventName").value("Tech Conference"));

            verify(registrationService).findRegistrationById(1L);
        }

        @Test
        @WithMockUser(username = "user1")
        @DisplayName("User should be able to get their own registration")
        void userShouldBeAbleToGetTheirOwnRegistration() throws Exception {
            // Given
            given(registrationSecurity.isUserOrEventOrganizer(1L, null)).willReturn(true);
            given(registrationService.findRegistrationById(1L)).willReturn(registration1);

            // When & Then
            mockMvc.perform(get("/api/registrations/{id}", 1L))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1L))
                    .andExpect(jsonPath("$.username").value("user1"));

            verify(registrationSecurity).isUserOrEventOrganizer(eq(1L), any());
            verify(registrationService).findRegistrationById(1L);
        }

        @Test
        @WithMockUser(username = "organizer")
        @DisplayName("Event organizer should be able to get registration for their event")
        void eventOrganizerShouldBeAbleToGetRegistrationForTheirEvent() throws Exception {
            // Given
            given(registrationSecurity.isUserOrEventOrganizer(1L, null)).willReturn(true);
            given(registrationService.findRegistrationById(1L)).willReturn(registration1);

            // When & Then
            mockMvc.perform(get("/api/registrations/{id}", 1L))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1L))
                    .andExpect(jsonPath("$.eventName").value("Tech Conference"));

            verify(registrationSecurity).isUserOrEventOrganizer(eq(1L), any());
            verify(registrationService).findRegistrationById(1L);
        }

        @Test
        @WithMockUser(username = "other_user")
        @DisplayName("Other users should not be able to get registration")
        void otherUsersShouldNotBeAbleToGetRegistration() throws Exception {
            // Given
            given(registrationSecurity.isUserOrEventOrganizer(1L, null)).willReturn(false);

            // When & Then
            mockMvc.perform(get("/api/registrations/{id}", 1L))
                    .andDo(print())
                    .andExpect(status().isForbidden());

            verify(registrationSecurity).isUserOrEventOrganizer(eq(1L), any());
            verify(registrationService, never()).findRegistrationById(anyLong());
        }

        @Test
        @WithMockUser(roles = {"ADMIN"})
        @DisplayName("Should return 404 when registration not found")
        void shouldReturn404WhenRegistrationNotFound() throws Exception {
            // Given
            given(registrationService.findRegistrationById(999L))
                    .willThrow(new ResourceNotFoundException("Registration", "id", 999L));

            // When & Then
            mockMvc.perform(get("/api/registrations/{id}", 999L))
                    .andDo(print())
                    .andExpect(status().isNotFound());

            verify(registrationService).findRegistrationById(999L);
        }
    }

    @Nested
    @DisplayName("GET /api/registrations/user/{userId} Tests")
    class GetRegistrationsByUserIdTests {

        @Test
        @WithMockUser(roles = {"ADMIN"})
        @DisplayName("Admin should be able to get registrations by user ID")
        void adminShouldBeAbleToGetRegistrationsByUserId() throws Exception {
            // Given
            given(registrationService.findRegistrationsByUser(1L)).willReturn(user1Registrations);

            // When & Then
            mockMvc.perform(get("/api/registrations/user/{userId}", 1L))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(2)))
                    .andExpect(jsonPath("$[0].id").value(1L))
                    .andExpect(jsonPath("$[0].username").value("user1"))
                    .andExpect(jsonPath("$[1].id").value(2L))
                    .andExpect(jsonPath("$[1].username").value("user1"));

            verify(registrationService).findRegistrationsByUser(1L);
        }

        @Test
        @WithMockUser(username = "user1")
        @DisplayName("User should be able to get their own registrations")
        void userShouldBeAbleToGetTheirOwnRegistrations() throws Exception {
            // Given
            given(userSecurity.isSameUser(eq(1L), any())).willReturn(true);
            given(registrationService.findRegistrationsByUser(1L)).willReturn(user1Registrations);

            // When & Then
            mockMvc.perform(get("/api/registrations/user/{userId}", 1L))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(2)))
                    .andExpect(jsonPath("$[0].username").value("user1"))
                    .andExpect(jsonPath("$[1].username").value("user1"));

            verify(userSecurity).isSameUser(eq(1L), any());
            verify(registrationService).findRegistrationsByUser(1L);
        }

        @Test
        @WithMockUser(username = "other_user")
        @DisplayName("Other users should not be able to get user's registrations")
        void otherUsersShouldNotBeAbleToGetUsersRegistrations() throws Exception {
            // Given
            given(userSecurity.isSameUser(eq(1L), any())).willReturn(false);

            // When & Then
            mockMvc.perform(get("/api/registrations/user/{userId}", 1L))
                    .andDo(print())
                    .andExpect(status().isForbidden());

            verify(userSecurity).isSameUser(eq(1L), any());
            verify(registrationService, never()).findRegistrationsByUser(anyLong());
        }
    }

    @Nested
    @DisplayName("GET /api/registrations/event/{eventId} Tests")
    class GetRegistrationsByEventIdTests {

        @Test
        @WithMockUser(roles = {"ADMIN"})
        @DisplayName("Admin should be able to get registrations by event ID")
        void adminShouldBeAbleToGetRegistrationsByEventId() throws Exception {
            // Given
            given(registrationService.findRegistrationsByEvent(1L)).willReturn(event1Registrations);

            // When & Then
            mockMvc.perform(get("/api/registrations/event/{eventId}", 1L))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(2)))
                    .andExpect(jsonPath("$[0].id").value(1L))
                    .andExpect(jsonPath("$[0].eventName").value("Tech Conference"))
                    .andExpect(jsonPath("$[1].id").value(3L))
                    .andExpect(jsonPath("$[1].eventName").value("Tech Conference"));

            verify(registrationService).findRegistrationsByEvent(1L);
        }

        @Test
        @WithMockUser(username = "organizer")
        @DisplayName("Event organizer should be able to get registrations for their event")
        void eventOrganizerShouldBeAbleToGetRegistrationsForTheirEvent() throws Exception {
            // Given
            given(eventSecurity.isOrganizerOrAdmin(eq(1L), any())).willReturn(true);
            given(registrationService.findRegistrationsByEvent(1L)).willReturn(event1Registrations);

            // When & Then
            mockMvc.perform(get("/api/registrations/event/{eventId}", 1L))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(2)))
                    .andExpect(jsonPath("$[0].eventName").value("Tech Conference"))
                    .andExpect(jsonPath("$[1].eventName").value("Tech Conference"));

            verify(eventSecurity).isOrganizerOrAdmin(eq(1L), any());
            verify(registrationService).findRegistrationsByEvent(1L);
        }

        @Test
        @WithMockUser(username = "other_user")
        @DisplayName("Other users should not be able to get event registrations")
        void otherUsersShouldNotBeAbleToGetEventRegistrations() throws Exception {
            // Given
            given(eventSecurity.isOrganizerOrAdmin(eq(1L), any())).willReturn(false);

            // When & Then
            mockMvc.perform(get("/api/registrations/event/{eventId}", 1L))
                    .andDo(print())
                    .andExpect(status().isForbidden());

            verify(eventSecurity).isOrganizerOrAdmin(eq(1L), any());
            verify(registrationService, never()).findRegistrationsByEvent(anyLong());
        }
    }

    @Nested
    @DisplayName("POST /api/registrations/user/{userId}/event/{eventId} Tests")
    class RegisterForEventTests {

        @Test
        @WithMockUser(username = "user1")
        @DisplayName("User should be able to register themselves for an event")
        void userShouldBeAbleToRegisterThemselvesForAnEvent() throws Exception {
            // Given
            given(userSecurity.isSameUser(eq(1L), any())).willReturn(true);
            given(registrationService.registerForEvent(1L, 1L)).willReturn(registration1);

            // When & Then
            mockMvc.perform(post("/api/registrations/user/{userId}/event/{eventId}", 1L, 1L)
                            .with(csrf()))
                    .andDo(print())
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").value(1L))
                    .andExpect(jsonPath("$.userId").value(1L))
                    .andExpect(jsonPath("$.eventId").value(1L))
                    .andExpect(jsonPath("$.registrationStatus").value("CONFIRMED"));

            verify(userSecurity).isSameUser(eq(1L), any());
            verify(registrationService).registerForEvent(1L, 1L);
        }

        @Test
        @WithMockUser(roles = {"ADMIN"})
        @DisplayName("Admin should be able to register any user for an event")
        void adminShouldBeAbleToRegisterAnyUserForAnEvent() throws Exception {
            // Given
            given(registrationService.registerForEvent(1L, 2L)).willReturn(
                    RegistrationDTO.builder()
                            .id(4L)
                            .userId(1L)
                            .username("user1")
                            .eventId(2L)
                            .eventName("Coding Workshop")
                            .registrationStatus("CONFIRMED")
                            .build()
            );

            // When & Then
            mockMvc.perform(post("/api/registrations/user/{userId}/event/{eventId}", 1L, 2L)
                            .with(csrf()))
                    .andDo(print())
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.userId").value(1L))
                    .andExpect(jsonPath("$.eventId").value(2L));

            verify(registrationService).registerForEvent(1L, 2L);
        }

        @Test
        @WithMockUser(username = "other_user")
        @DisplayName("Other users should not be able to register someone else")
        void otherUsersShouldNotBeAbleToRegisterSomeoneElse() throws Exception {
            // Given
            given(userSecurity.isSameUser(eq(1L), any())).willReturn(false);

            // When & Then
            mockMvc.perform(post("/api/registrations/user/{userId}/event/{eventId}", 1L, 1L)
                            .with(csrf()))
                    .andDo(print())
                    .andExpect(status().isForbidden());

            verify(userSecurity).isSameUser(eq(1L), any());
            verify(registrationService, never()).registerForEvent(anyLong(), anyLong());
        }

        @Test
        @WithMockUser(username = "user1")
        @DisplayName("Should return 400 when event is at capacity")
        void shouldReturn400WhenEventIsAtCapacity() throws Exception {
            // Given
            given(userSecurity.isSameUser(eq(1L), any())).willReturn(true);
            given(registrationService.registerForEvent(1L, 3L))
                    .willThrow(new ApiException(HttpStatus.BAD_REQUEST, "Event is at full capacity"));

            // When & Then
            mockMvc.perform(post("/api/registrations/user/{userId}/event/{eventId}", 1L, 3L)
                            .with(csrf()))
                    .andDo(print())
                    .andExpect(status().isBadRequest());

            verify(userSecurity).isSameUser(eq(1L), any());
            verify(registrationService).registerForEvent(1L, 3L);
        }

        @Test
        @WithMockUser(username = "user1")
        @DisplayName("Should return 409 when user is already registered")
        void shouldReturn409WhenUserIsAlreadyRegistered() throws Exception {
            // Given
            given(userSecurity.isSameUser(eq(1L), any())).willReturn(true);
            given(registrationService.registerForEvent(1L, 1L))
                    .willThrow(new ApiException(HttpStatus.CONFLICT, "User is already registered for this event"));

            // When & Then
            mockMvc.perform(post("/api/registrations/user/{userId}/event/{eventId}", 1L, 1L)
                            .with(csrf()))
                    .andDo(print())
                    .andExpect(status().isConflict());

            verify(userSecurity).isSameUser(eq(1L), any());
            verify(registrationService).registerForEvent(1L, 1L);
        }
    }

    @Nested
    @DisplayName("PATCH /api/registrations/user/{userId}/event/{eventId}/cancel Tests")
    class CancelRegistrationTests {

        @Test
        @WithMockUser(username = "user1")
        @DisplayName("User should be able to cancel their own registration")
        void userShouldBeAbleToCancelTheirOwnRegistration() throws Exception {
            // Given
            RegistrationDTO cancelledRegistration = RegistrationDTO.builder()
                    .id(1L)
                    .userId(1L)
                    .username("user1")
                    .eventId(1L)
                    .eventName("Tech Conference")
                    .registrationStatus("CANCELLED")
                    .build();

            given(userSecurity.isSameUser(eq(1L), any())).willReturn(true);
            given(registrationService.cancelRegistration(1L, 1L)).willReturn(cancelledRegistration);

            // When & Then
            mockMvc.perform(patch("/api/registrations/user/{userId}/event/{eventId}/cancel", 1L, 1L)
                            .with(csrf()))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1L))
                    .andExpect(jsonPath("$.userId").value(1L))
                    .andExpect(jsonPath("$.registrationStatus").value("CANCELLED"));

            verify(userSecurity).isSameUser(eq(1L), any());
            verify(registrationService).cancelRegistration(1L, 1L);
        }

        @Test
        @WithMockUser(roles = {"ADMIN"})
        @DisplayName("Admin should be able to cancel any registration")
        void adminShouldBeAbleToCancelAnyRegistration() throws Exception {
            // Given
            RegistrationDTO cancelledRegistration = RegistrationDTO.builder()
                    .id(2L)
                    .userId(1L)
                    .username("user1")
                    .eventId(2L)
                    .eventName("Coding Workshop")
                    .registrationStatus("CANCELLED")
                    .build();

            given(registrationService.cancelRegistration(1L, 2L)).willReturn(cancelledRegistration);

            // When & Then
            mockMvc.perform(patch("/api/registrations/user/{userId}/event/{eventId}/cancel", 1L, 2L)
                            .with(csrf()))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.registrationStatus").value("CANCELLED"));

            verify(registrationService).cancelRegistration(1L, 2L);
        }

        @Test
        @WithMockUser(username = "other_user")
        @DisplayName("Other users should not be able to cancel someone else's registration")
        void otherUsersShouldNotBeAbleToCancelSomeoneElsesRegistration() throws Exception {
            // Given
            given(userSecurity.isSameUser(eq(1L), any())).willReturn(false);

            // When & Then
            mockMvc.perform(patch("/api/registrations/user/{userId}/event/{eventId}/cancel", 1L, 1L)
                            .with(csrf()))
                    .andDo(print())
                    .andExpect(status().isForbidden());

            verify(userSecurity).isSameUser(eq(1L), any());
            verify(registrationService, never()).cancelRegistration(anyLong(), anyLong());
        }

        @Test
        @WithMockUser(username = "user1")
        @DisplayName("Should return 400 when trying to cancel registration for already started event")
        void shouldReturn400WhenTryingToCancelRegistrationForAlreadyStartedEvent() throws Exception {
            // Given
            given(userSecurity.isSameUser(eq(1L), any())).willReturn(true);
            given(registrationService.cancelRegistration(1L, 1L))
                    .willThrow(new ApiException(HttpStatus.BAD_REQUEST,
                            "Cannot cancel registration for events that have already started"));

            // When & Then
            mockMvc.perform(patch("/api/registrations/user/{userId}/event/{eventId}/cancel", 1L, 1L)
                            .with(csrf()))
                    .andDo(print())
                    .andExpect(status().isBadRequest());

            verify(userSecurity).isSameUser(eq(1L), any());
            verify(registrationService).cancelRegistration(1L, 1L);
        }
    }

    @Nested
    @DisplayName("DELETE /api/registrations/{id} Tests")
    class DeleteRegistrationTests {

        @Test
        @WithMockUser(roles = {"ADMIN"})
        @DisplayName("Admin should be able to delete registration")
        void adminShouldBeAbleToDeleteRegistration() throws Exception {
            // Given
            doNothing().when(registrationService).deleteRegistration(1L);

            // When & Then
            mockMvc.perform(delete("/api/registrations/{id}", 1L)
                            .with(csrf()))
                    .andDo(print())
                    .andExpect(status().isNoContent());

            verify(registrationService).deleteRegistration(1L);
        }

        @Test
        @WithMockUser(roles = {"USER"})
        @DisplayName("Regular user should not be able to delete registration")
        void regularUserShouldNotBeAbleToDeleteRegistration() throws Exception {
            // When & Then
            mockMvc.perform(delete("/api/registrations/{id}", 1L)
                            .with(csrf()))
                    .andDo(print())
                    .andExpect(status().isForbidden());

            verify(registrationService, never()).deleteRegistration(anyLong());
        }

        @Test
        @WithMockUser(roles = {"ADMIN"})
        @DisplayName("Should return 404 when registration not found")
        void shouldReturn404WhenRegistrationNotFound() throws Exception {
            // Given
            doThrow(new ResourceNotFoundException("Registration", "id", 999L))
                    .when(registrationService).deleteRegistration(999L);

            // When & Then
            mockMvc.perform(delete("/api/registrations/{id}", 999L)
                            .with(csrf()))
                    .andDo(print())
                    .andExpect(status().isNotFound());

            verify(registrationService).deleteRegistration(999L);
        }
    }
}