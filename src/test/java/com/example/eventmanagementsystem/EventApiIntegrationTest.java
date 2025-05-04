package com.example.eventmanagementsystem;

import com.example.eventmanagementsystem.dto.EventDTO;
import com.example.eventmanagementsystem.dto.JwtAuthResponse;
import com.example.eventmanagementsystem.dto.LoginRequest;
import com.example.eventmanagementsystem.dto.RegistrationDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDateTime;

import static org.hamcrest.Matchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("Event API Integration Tests")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class EventApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private static String adminToken;
    private static String organizerToken;
    private static String userToken;
    private static Long eventId;
    private static Long userId;
    private static Long registrationId;

    @BeforeAll
    static void setup() {
        // Test data will be initialized by DataInitializer in the application
    }

    @Test
    @Order(1)
    @DisplayName("Admin should be able to login")
    void adminShouldBeAbleToLogin() throws Exception {
        // Given
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("admin");
        loginRequest.setPassword("admin123");

        // When
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists())
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.username").value("admin"))
                .andReturn();

        // Then
        JwtAuthResponse response = objectMapper.readValue(
                result.getResponse().getContentAsString(), JwtAuthResponse.class);
        adminToken = response.getAccessToken();
    }

    @Test
    @Order(2)
    @DisplayName("Organizer should be able to login")
    void organizerShouldBeAbleToLogin() throws Exception {
        // Given
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("organizer");
        loginRequest.setPassword("organizer123");

        // When
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists())
                .andReturn();

        // Then
        JwtAuthResponse response = objectMapper.readValue(
                result.getResponse().getContentAsString(), JwtAuthResponse.class);
        organizerToken = response.getAccessToken();
    }

    @Test
    @Order(3)
    @DisplayName("Regular user should be able to login")
    void regularUserShouldBeAbleToLogin() throws Exception {
        // Given
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("user");
        loginRequest.setPassword("user123");

        // When
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists())
                .andReturn();

        // Then
        JwtAuthResponse response = objectMapper.readValue(
                result.getResponse().getContentAsString(), JwtAuthResponse.class);
        userToken = response.getAccessToken();
        userId = response.getUserId();
    }

    @Test
    @Order(4)
    @DisplayName("Organizer should be able to create a new event")
    void organizerShouldBeAbleToCreateNewEvent() throws Exception {
        // Given
        LocalDateTime startTime = LocalDateTime.now().plusDays(30);
        LocalDateTime endTime = startTime.plusHours(3);

        EventDTO eventDTO = EventDTO.builder()
                .name("Integration Test Event")
                .description("This is a test event created during integration testing")
                .startTime(startTime)
                .endTime(endTime)
                .location("Test Location")
                .category("Test Category")
                .capacity(50)
                .organizerId(2L) // Organizer's ID (from DataInitializer)
                .build();

        // When
        MvcResult result = mockMvc.perform(post("/api/events")
                        .with(csrf())
                        .header("Authorization", "Bearer " + organizerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(eventDTO)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Integration Test Event"))
                .andExpect(jsonPath("$.description").value("This is a test event created during integration testing"))
                .andExpect(jsonPath("$.published").value(false))
                .andReturn();

        // Then
        EventDTO createdEvent = objectMapper.readValue(
                result.getResponse().getContentAsString(), EventDTO.class);
        eventId = createdEvent.getId();
    }

    @Test
    @Order(5)
    @DisplayName("Organizer should be able to publish the event")
    void organizerShouldBeAbleToPublishEvent() throws Exception {
        // When
        mockMvc.perform(patch("/api/events/{id}/publish", eventId)
                        .with(csrf())
                        .header("Authorization", "Bearer " + organizerToken))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.published").value(true));
    }

    @Test
    @Order(6)
    @DisplayName("User should be able to view published events")
    void userShouldBeAbleToViewPublishedEvents() throws Exception {
        // When
        mockMvc.perform(get("/api/events")
                        .param("publishedOnly", "true")
                        .header("Authorization", "Bearer " + userToken))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$[*].name", hasItem("Integration Test Event")));
    }

    @Test
    @Order(7)
    @DisplayName("User should be able to register for event")
    void userShouldBeAbleToRegisterForEvent() throws Exception {
        // When
        MvcResult result = mockMvc.perform(post("/api/registrations/user/{userId}/event/{eventId}", userId, eventId)
                        .with(csrf())
                        .header("Authorization", "Bearer " + userToken))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.userId").value(userId))
                .andExpect(jsonPath("$.eventId").value(eventId))
                .andExpect(jsonPath("$.registrationStatus").value("CONFIRMED"))
                .andReturn();

        // Then
        RegistrationDTO registration = objectMapper.readValue(
                result.getResponse().getContentAsString(), RegistrationDTO.class);
        registrationId = registration.getId();
    }

    @Test
    @Order(8)
    @DisplayName("User should be able to view their registrations")
    void userShouldBeAbleToViewTheirRegistrations() throws Exception {
        // When
        mockMvc.perform(get("/api/registrations/user/{userId}", userId)
                        .header("Authorization", "Bearer " + userToken))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$[*].eventId", hasItem(eventId.intValue())));
    }

    @Test
    @Order(9)
    @DisplayName("Organizer should be able to view registrations for their event")
    void organizerShouldBeAbleToViewRegistrationsForTheirEvent() throws Exception {
        // When
        mockMvc.perform(get("/api/registrations/event/{eventId}", eventId)
                        .header("Authorization", "Bearer " + organizerToken))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$[*].userId", hasItem(userId.intValue())));
    }

    @Test
    @Order(10)
    @DisplayName("User should be able to cancel registration")
    void userShouldBeAbleToCancelRegistration() throws Exception {
        // When
        mockMvc.perform(patch("/api/registrations/user/{userId}/event/{eventId}/cancel", userId, eventId)
                        .with(csrf())
                        .header("Authorization", "Bearer " + userToken))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.registrationStatus").value("CANCELLED"));
    }

    @Test
    @Order(11)
    @DisplayName("Organizer should be able to unpublish event")
    void organizerShouldBeAbleToUnpublishEvent() throws Exception {
        // When
        mockMvc.perform(patch("/api/events/{id}/unpublish", eventId)
                        .with(csrf())
                        .header("Authorization", "Bearer " + organizerToken))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.published").value(false));
    }

    @Test
    @Order(12)
    @DisplayName("Admin should be able to delete registration")
    void adminShouldBeAbleToDeleteRegistration() throws Exception {
        // When
        mockMvc.perform(delete("/api/registrations/{id}", registrationId)
                        .with(csrf())
                        .header("Authorization", "Bearer " + adminToken))
                .andDo(print())
                .andExpect(status().isNoContent());

        // Then - Verify registration is deleted
        mockMvc.perform(get("/api/registrations/{id}", registrationId)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNotFound());
    }

    @Test
    @Order(13)
    @DisplayName("Admin should be able to delete event")
    void adminShouldBeAbleToDeleteEvent() throws Exception {
        // First, verify no more registrations exist for this event
        mockMvc.perform(get("/api/registrations/event/{eventId}", eventId)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0))); // Verify no registrations

        // When
        mockMvc.perform(delete("/api/events/{id}", eventId)
                        .with(csrf())
                        .header("Authorization", "Bearer " + adminToken))
                .andDo(print())
                .andExpect(status().isNoContent());

        // Then - Verify event is deleted
        mockMvc.perform(get("/api/events/{id}", eventId)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNotFound());
    }
}