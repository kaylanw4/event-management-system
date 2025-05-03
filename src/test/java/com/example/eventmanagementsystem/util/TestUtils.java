package com.example.eventmanagementsystem.util;

import com.example.eventmanagementsystem.dto.EventDTO;
import com.example.eventmanagementsystem.dto.RegistrationDTO;
import com.example.eventmanagementsystem.dto.UserDTO;
import com.example.eventmanagementsystem.model.Event;
import com.example.eventmanagementsystem.model.Registration;
import com.example.eventmanagementsystem.model.User;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class TestUtils {

    // User test data
    public static User createTestUser() {
        return User.builder()
                .id(1L)
                .username("testuser")
                .password("password123")
                .email("test@example.com")
                .fullName("Test User")
                .roles(new HashSet<>(Set.of("USER")))
                .organizedEvents(new ArrayList<>())
                .registrations(new ArrayList<>())
                .build();
    }

    public static User createTestAdmin() {
        return User.builder()
                .id(2L)
                .username("admin")
                .password("admin123")
                .email("admin@example.com")
                .fullName("Admin User")
                .roles(new HashSet<>(Set.of("ADMIN")))
                .organizedEvents(new ArrayList<>())
                .registrations(new ArrayList<>())
                .build();
    }

    public static User createTestOrganizer() {
        return User.builder()
                .id(3L)
                .username("organizer")
                .password("organizer123")
                .email("organizer@example.com")
                .fullName("Event Organizer")
                .roles(new HashSet<>(Set.of("ORGANIZER")))
                .organizedEvents(new ArrayList<>())
                .registrations(new ArrayList<>())
                .build();
    }

    public static UserDTO createTestUserDTO() {
        return UserDTO.builder()
                .id(1L)
                .username("testuser")
                .password("password123")
                .email("test@example.com")
                .fullName("Test User")
                .roles(new HashSet<>(Set.of("USER")))
                .build();
    }

    // Event test data
    public static Event createTestEvent() {
        return Event.builder()
                .id(1L)
                .name("Test Event")
                .description("This is a test event")
                .startTime(LocalDateTime.now().plusDays(7))
                .endTime(LocalDateTime.now().plusDays(7).plusHours(2))
                .location("Test Location")
                .category("Test Category")
                .capacity(100)
                .published(true)
                .organizer(createTestOrganizer())
                .registrations(new ArrayList<>()) // Initialize empty list
                .build();
    }

    public static EventDTO createTestEventDTO() {
        return EventDTO.builder()
                .id(1L)
                .name("Test Event")
                .description("This is a test event")
                .startTime(LocalDateTime.now().plusDays(7))
                .endTime(LocalDateTime.now().plusDays(7).plusHours(2))
                .location("Test Location")
                .category("Test Category")
                .capacity(100)
                .published(true)
                .organizerId(3L)
                .organizerName("Event Organizer")
                .build();
    }

    // Registration test data
    public static Registration createTestRegistration() {
        Registration registration = Registration.builder()
                .id(1L)
                .user(createTestUser())
                .event(createTestEvent())
                .registrationTime(LocalDateTime.now())
                .registrationStatus("CONFIRMED")
                .build();

        return registration;
    }

    public static RegistrationDTO createTestRegistrationDTO() {
        return RegistrationDTO.builder()
                .id(1L)
                .userId(1L)
                .username("testuser")
                .eventId(1L)
                .eventName("Test Event")
                .registrationTime(LocalDateTime.now())
                .registrationStatus("CONFIRMED")
                .build();
    }
}