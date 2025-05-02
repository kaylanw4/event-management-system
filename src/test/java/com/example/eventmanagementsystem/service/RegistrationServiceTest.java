package com.example.eventmanagementsystem.service;

import com.example.eventmanagementsystem.dto.RegistrationDTO;
import com.example.eventmanagementsystem.exception.ApiException;
import com.example.eventmanagementsystem.exception.ResourceNotFoundException;
import com.example.eventmanagementsystem.model.Event;
import com.example.eventmanagementsystem.model.Registration;
import com.example.eventmanagementsystem.model.User;
import com.example.eventmanagementsystem.repository.EventRepository;
import com.example.eventmanagementsystem.repository.RegistrationRepository;
import com.example.eventmanagementsystem.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Registration Service Tests")
class RegistrationServiceTest {

    @Mock
    private RegistrationRepository registrationRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private EventRepository eventRepository;

    @InjectMocks
    private RegistrationService registrationService;

    @Captor
    private ArgumentCaptor<Registration> registrationCaptor;

    private User testUser;
    private Event testEvent;
    private Registration testRegistration;
    private RegistrationDTO expectedDto;

    @BeforeEach
    void setUp() {
        // Create test user
        testUser = User.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .fullName("Test User")
                .roles(new HashSet<>(Set.of("USER")))
                .build();

        // Create test event
        testEvent = Event.builder()
                .id(1L)
                .name("Test Event")
                .description("Test event description")
                .startTime(LocalDateTime.now().plusDays(7))
                .endTime(LocalDateTime.now().plusDays(7).plusHours(2))
                .location("Test Location")
                .category("Test Category")
                .capacity(100)
                .published(true)
                .organizer(User.builder().id(2L).username("organizer").build())
                .registrations(new ArrayList<>())
                .build();

        // Create test registration
        testRegistration = Registration.builder()
                .id(1L)
                .user(testUser)
                .event(testEvent)
                .registrationTime(LocalDateTime.now())
                .registrationStatus("CONFIRMED")
                .build();

        // Expected DTO
        expectedDto = RegistrationDTO.builder()
                .id(1L)
                .userId(1L)
                .username("testuser")
                .eventId(1L)
                .eventName("Test Event")
                .registrationTime(testRegistration.getRegistrationTime())
                .registrationStatus("CONFIRMED")
                .build();
    }

    @Nested
    @DisplayName("Find Registration Tests")
    class FindRegistrationTests {

        @Test
        @DisplayName("Should find all registrations")
        void shouldFindAllRegistrations() {
            // Given
            given(registrationRepository.findAll()).willReturn(List.of(testRegistration));

            // When
            List<RegistrationDTO> result = registrationService.findAllRegistrations();

            // Then
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getId()).isEqualTo(expectedDto.getId());
            assertThat(result.get(0).getUsername()).isEqualTo(expectedDto.getUsername());
            assertThat(result.get(0).getEventName()).isEqualTo(expectedDto.getEventName());

            verify(registrationRepository).findAll();
        }

        @Test
        @DisplayName("Should find registration by ID")
        void shouldFindRegistrationById() {
            // Given
            given(registrationRepository.findById(1L)).willReturn(Optional.of(testRegistration));

            // When
            RegistrationDTO result = registrationService.findRegistrationById(1L);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(expectedDto.getId());

            verify(registrationRepository).findById(1L);
        }

        @Test
        @DisplayName("Should throw exception when registration not found by ID")
        void shouldThrowExceptionWhenRegistrationNotFoundById() {
            // Given
            given(registrationRepository.findById(999L)).willReturn(Optional.empty());

            // When/Then
            ResourceNotFoundException exception = assertThrows(
                    ResourceNotFoundException.class,
                    () -> registrationService.findRegistrationById(999L)
            );

            // Then
            assertThat(exception.getMessage()).contains("Registration not found");

            verify(registrationRepository).findById(999L);
        }

        @Test
        @DisplayName("Should find registrations by user ID")
        void shouldFindRegistrationsByUserId() {
            // Given
            given(userRepository.findById(1L)).willReturn(Optional.of(testUser));
            given(registrationRepository.findByUserId(1L)).willReturn(List.of(testRegistration));

            // When
            List<RegistrationDTO> result = registrationService.findRegistrationsByUser(1L);

            // Then
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getUserId()).isEqualTo(expectedDto.getUserId());

            verify(userRepository).findById(1L);
            verify(registrationRepository).findByUserId(1L);
        }

        @Test
        @DisplayName("Should throw exception when user not found")
        void shouldThrowExceptionWhenUserNotFound() {
            // Given
            given(userRepository.findById(999L)).willReturn(Optional.empty());

            // When/Then
            ResourceNotFoundException exception = assertThrows(
                    ResourceNotFoundException.class,
                    () -> registrationService.findRegistrationsByUser(999L)
            );

            // Then
            assertThat(exception.getMessage()).contains("User not found");

            verify(userRepository).findById(999L);
            verify(registrationRepository, never()).findByUserId(anyLong());
        }

        @Test
        @DisplayName("Should find registrations by event ID")
        void shouldFindRegistrationsByEventId() {
            // Given
            given(eventRepository.findById(1L)).willReturn(Optional.of(testEvent));
            given(registrationRepository.findByEventId(1L)).willReturn(List.of(testRegistration));

            // When
            List<RegistrationDTO> result = registrationService.findRegistrationsByEvent(1L);

            // Then
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getEventId()).isEqualTo(expectedDto.getEventId());

            verify(eventRepository).findById(1L);
            verify(registrationRepository).findByEventId(1L);
        }
    }

    @Nested
    @DisplayName("Register For Event Tests")
    class RegisterForEventTests {

        @Test
        @DisplayName("Should successfully register for event")
        void shouldSuccessfullyRegisterForEvent() {
            // Given
            given(userRepository.findById(1L)).willReturn(Optional.of(testUser));
            given(eventRepository.findById(1L)).willReturn(Optional.of(testEvent));
            given(registrationRepository.existsByUserIdAndEventId(1L, 1L)).willReturn(false);
            given(registrationRepository.save(any(Registration.class))).willReturn(testRegistration);

            // When
            RegistrationDTO result = registrationService.registerForEvent(1L, 1L);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getUserId()).isEqualTo(expectedDto.getUserId());
            assertThat(result.getEventId()).isEqualTo(expectedDto.getEventId());
            assertThat(result.getRegistrationStatus()).isEqualTo("CONFIRMED");

            verify(userRepository).findById(1L);
            verify(eventRepository).findById(1L);
            verify(registrationRepository).existsByUserIdAndEventId(1L, 1L);
            verify(registrationRepository).save(registrationCaptor.capture());

            Registration capturedRegistration = registrationCaptor.getValue();
            assertThat(capturedRegistration.getUser().getId()).isEqualTo(1L);
            assertThat(capturedRegistration.getEvent().getId()).isEqualTo(1L);
        }

        @Test
        @DisplayName("Should throw exception when registering for unpublished event")
        void shouldThrowExceptionWhenRegisteringForUnpublishedEvent() {
            // Given
            Event unpublishedEvent = Event.builder()
                    .id(2L)
                    .name("Unpublished Event")
                    .published(false)
                    .startTime(LocalDateTime.now().plusDays(10))
                    .endTime(LocalDateTime.now().plusDays(10).plusHours(2))
                    .capacity(100)
                    .build();

            given(userRepository.findById(1L)).willReturn(Optional.of(testUser));
            given(eventRepository.findById(2L)).willReturn(Optional.of(unpublishedEvent));

            // When/Then
            ApiException exception = assertThrows(
                    ApiException.class,
                    () -> registrationService.registerForEvent(1L, 2L)
            );

            // Then
            assertThat(exception.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(exception.getMessage()).contains("unpublished event");

            verify(userRepository).findById(1L);
            verify(eventRepository).findById(2L);
            verify(registrationRepository, never()).save(any(Registration.class));
        }

        @Test
        @DisplayName("Should throw exception when user already registered")
        void shouldThrowExceptionWhenUserAlreadyRegistered() {
            // Given
            given(userRepository.findById(1L)).willReturn(Optional.of(testUser));
            given(eventRepository.findById(1L)).willReturn(Optional.of(testEvent));
            given(registrationRepository.existsByUserIdAndEventId(1L, 1L)).willReturn(true);

            // When/Then
            ApiException exception = assertThrows(
                    ApiException.class,
                    () -> registrationService.registerForEvent(1L, 1L)
            );

            // Then
            assertThat(exception.getStatus()).isEqualTo(HttpStatus.CONFLICT);
            assertThat(exception.getMessage()).contains("already registered");

            verify(userRepository).findById(1L);
            verify(eventRepository).findById(1L);
            verify(registrationRepository).existsByUserIdAndEventId(1L, 1L);
            verify(registrationRepository, never()).save(any(Registration.class));
        }

        @Test
        @DisplayName("Should throw exception when event is at capacity")
        void shouldThrowExceptionWhenEventIsAtCapacity() {
            // Given
            Event fullEvent = Event.builder()
                    .id(3L)
                    .name("Full Event")
                    .published(true)
                    .startTime(LocalDateTime.now().plusDays(5))
                    .endTime(LocalDateTime.now().plusDays(5).plusHours(2))
                    .capacity(0) // No available spots
                    .build();

            given(userRepository.findById(1L)).willReturn(Optional.of(testUser));
            given(eventRepository.findById(3L)).willReturn(Optional.of(fullEvent));
            given(registrationRepository.existsByUserIdAndEventId(1L, 3L)).willReturn(false);

            // When/Then
            ApiException exception = assertThrows(
                    ApiException.class,
                    () -> registrationService.registerForEvent(1L, 3L)
            );

            // Then
            assertThat(exception.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(exception.getMessage()).contains("full capacity");

            verify(userRepository).findById(1L);
            verify(eventRepository).findById(3L);
            verify(registrationRepository).existsByUserIdAndEventId(1L, 3L);
            verify(registrationRepository, never()).save(any(Registration.class));
        }
    }

    @Nested
    @DisplayName("Cancel Registration Tests")
    class CancelRegistrationTests {

        @Test
        @DisplayName("Should successfully cancel registration")
        void shouldSuccessfullyCancelRegistration() {
            // Given
            given(registrationRepository.findByUserIdAndEventId(1L, 1L))
                    .willReturn(Optional.of(testRegistration));

            testRegistration.setRegistrationStatus("CONFIRMED");

            Registration cancelledRegistration = Registration.builder()
                    .id(1L)
                    .user(testUser)
                    .event(testEvent)
                    .registrationTime(testRegistration.getRegistrationTime())
                    .registrationStatus("CANCELLED")
                    .build();

            given(registrationRepository.save(any(Registration.class)))
                    .willReturn(cancelledRegistration);

            // When
            RegistrationDTO result = registrationService.cancelRegistration(1L, 1L);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getRegistrationStatus()).isEqualTo("CANCELLED");

            verify(registrationRepository).findByUserIdAndEventId(1L, 1L);
            verify(registrationRepository).save(registrationCaptor.capture());

            Registration capturedRegistration = registrationCaptor.getValue();
            assertThat(capturedRegistration.getRegistrationStatus()).isEqualTo("CANCELLED");
        }

        @Test
        @DisplayName("Should throw exception when registration not found")
        void shouldThrowExceptionWhenRegistrationNotFound() {
            // Given
            given(registrationRepository.findByUserIdAndEventId(1L, 999L))
                    .willReturn(Optional.empty());

            // When/Then
            ResourceNotFoundException exception = assertThrows(
                    ResourceNotFoundException.class,
                    () -> registrationService.cancelRegistration(1L, 999L)
            );

            // Then
            assertThat(exception.getMessage()).contains("Registration not found");

            verify(registrationRepository).findByUserIdAndEventId(1L, 999L);
            verify(registrationRepository, never()).save(any(Registration.class));
        }

        @Test
        @DisplayName("Should throw exception when cancelling registration for already started event")
        void shouldThrowExceptionWhenCancellingRegistrationForAlreadyStartedEvent() {
            // Given
            Event startedEvent = Event.builder()
                    .id(4L)
                    .name("Started Event")
                    .startTime(LocalDateTime.now().minusDays(1)) // Event already started
                    .endTime(LocalDateTime.now().plusDays(1))
                    .build();

            Registration startedEventRegistration = Registration.builder()
                    .id(2L)
                    .user(testUser)
                    .event(startedEvent)
                    .registrationTime(LocalDateTime.now().minusDays(7))
                    .registrationStatus("CONFIRMED")
                    .build();

            given(registrationRepository.findByUserIdAndEventId(1L, 4L))
                    .willReturn(Optional.of(startedEventRegistration));

            // When/Then
            ApiException exception = assertThrows(
                    ApiException.class,
                    () -> registrationService.cancelRegistration(1L, 4L)
            );

            // Then
            assertThat(exception.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(exception.getMessage()).contains("already started");

            verify(registrationRepository).findByUserIdAndEventId(1L, 4L);
            verify(registrationRepository, never()).save(any(Registration.class));
        }
    }

    @Nested
    @DisplayName("Delete Registration Tests")
    class DeleteRegistrationTests {

        @Test
        @DisplayName("Should successfully delete registration")
        void shouldSuccessfullyDeleteRegistration() {
            // Given
            given(registrationRepository.findById(1L)).willReturn(Optional.of(testRegistration));
            doNothing().when(registrationRepository).delete(any(Registration.class));

            // When
            registrationService.deleteRegistration(1L);

            // Then
            verify(registrationRepository).findById(1L);
            verify(registrationRepository).delete(testRegistration);
        }

        @Test
        @DisplayName("Should throw exception when registration not found")
        void shouldThrowExceptionWhenRegistrationNotFound() {
            // Given
            given(registrationRepository.findById(999L)).willReturn(Optional.empty());

            // When/Then
            ResourceNotFoundException exception = assertThrows(
                    ResourceNotFoundException.class,
                    () -> registrationService.deleteRegistration(999L)
            );

            // Then
            assertThat(exception.getMessage()).contains("Registration not found");

            verify(registrationRepository).findById(999L);
            verify(registrationRepository, never()).delete(any(Registration.class));
        }
    }
}