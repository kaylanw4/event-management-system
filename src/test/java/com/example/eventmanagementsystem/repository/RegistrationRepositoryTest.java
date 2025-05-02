package com.example.eventmanagementsystem.repository;

import com.example.eventmanagementsystem.model.Event;
import com.example.eventmanagementsystem.model.Registration;
import com.example.eventmanagementsystem.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@DisplayName("Registration Repository Tests")
public class RegistrationRepositoryTest {

    @Autowired
    private RegistrationRepository registrationRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EventRepository eventRepository;

    private User user1;
    private User user2;
    private User organizer;
    private Event event1;
    private Event event2;
    private Registration registration1;
    private Registration registration2;
    private Registration registration3;

    @BeforeEach
    void setUp() {
        // Create test users
        user1 = User.builder()
                .username("user1")
                .password("password")
                .email("user1@example.com")
                .fullName("User One")
                .roles(new HashSet<>(Set.of("USER")))
                .build();

        user2 = User.builder()
                .username("user2")
                .password("password")
                .email("user2@example.com")
                .fullName("User Two")
                .roles(new HashSet<>(Set.of("USER")))
                .build();

        organizer = User.builder()
                .username("organizer")
                .password("password")
                .email("organizer@example.com")
                .fullName("Event Organizer")
                .roles(new HashSet<>(Set.of("ORGANIZER")))
                .build();

        userRepository.saveAll(List.of(user1, user2, organizer));

        // Create test events
        event1 = Event.builder()
                .name("Conference")
                .description("Annual tech conference")
                .startTime(LocalDateTime.now().plusDays(10))
                .endTime(LocalDateTime.now().plusDays(10).plusHours(8))
                .location("Convention Center")
                .category("Technology")
                .capacity(200)
                .published(true)
                .organizer(organizer)
                .build();

        event2 = Event.builder()
                .name("Workshop")
                .description("Coding workshop")
                .startTime(LocalDateTime.now().plusDays(15))
                .endTime(LocalDateTime.now().plusDays(15).plusHours(4))
                .location("Tech Hub")
                .category("Education")
                .capacity(50)
                .published(true)
                .organizer(organizer)
                .build();

        eventRepository.saveAll(List.of(event1, event2));

        // Create registrations
        registration1 = Registration.builder()
                .user(user1)
                .event(event1)
                .registrationTime(LocalDateTime.now().minusDays(1))
                .registrationStatus("CONFIRMED")
                .build();

        registration2 = Registration.builder()
                .user(user2)
                .event(event1)
                .registrationTime(LocalDateTime.now().minusDays(2))
                .registrationStatus("CONFIRMED")
                .build();

        registration3 = Registration.builder()
                .user(user1)
                .event(event2)
                .registrationTime(LocalDateTime.now().minusDays(3))
                .registrationStatus("CONFIRMED")
                .build();

        registrationRepository.saveAll(List.of(registration1, registration2, registration3));
    }

    @Nested
    @DisplayName("Find Registration Tests")
    class FindRegistrationTests {

        @Test
        @DisplayName("Should find registrations by user ID")
        void shouldFindRegistrationsByUserId() {
            // When
            List<Registration> user1Registrations = registrationRepository.findByUserId(user1.getId());
            List<Registration> user2Registrations = registrationRepository.findByUserId(user2.getId());

            // Then
            assertThat(user1Registrations).hasSize(2);
            assertThat(user1Registrations)
                    .extracting(r -> r.getEvent().getName())
                    .containsExactlyInAnyOrder("Conference", "Workshop");

            assertThat(user2Registrations).hasSize(1);
            assertThat(user2Registrations)
                    .extracting(r -> r.getEvent().getName())
                    .containsExactly("Conference");
        }

        @Test
        @DisplayName("Should find registrations by event ID")
        void shouldFindRegistrationsByEventId() {
            // When
            List<Registration> event1Registrations = registrationRepository.findByEventId(event1.getId());
            List<Registration> event2Registrations = registrationRepository.findByEventId(event2.getId());

            // Then
            assertThat(event1Registrations).hasSize(2);
            assertThat(event1Registrations)
                    .extracting(r -> r.getUser().getUsername())
                    .containsExactlyInAnyOrder("user1", "user2");

            assertThat(event2Registrations).hasSize(1);
            assertThat(event2Registrations)
                    .extracting(r -> r.getUser().getUsername())
                    .containsExactly("user1");
        }

        @Test
        @DisplayName("Should find registration by user ID and event ID")
        void shouldFindRegistrationByUserIdAndEventId() {
            // When
            Optional<Registration> found = registrationRepository.findByUserIdAndEventId(
                    user1.getId(), event1.getId());
            Optional<Registration> notFound = registrationRepository.findByUserIdAndEventId(
                    user2.getId(), event2.getId());

            // Then
            assertThat(found).isPresent();
            assertThat(found.get().getUser().getId()).isEqualTo(user1.getId());
            assertThat(found.get().getEvent().getId()).isEqualTo(event1.getId());

            assertThat(notFound).isEmpty();
        }
    }

    @Nested
    @DisplayName("Exists and Count Tests")
    class ExistsAndCountTests {

        @Test
        @DisplayName("Should check if registration exists by user ID and event ID")
        void shouldCheckIfRegistrationExistsByUserIdAndEventId() {
            // When
            boolean exists1 = registrationRepository.existsByUserIdAndEventId(
                    user1.getId(), event1.getId());
            boolean exists2 = registrationRepository.existsByUserIdAndEventId(
                    user2.getId(), event2.getId());

            // Then
            assertThat(exists1).isTrue();
            assertThat(exists2).isFalse();
        }

        @Test
        @DisplayName("Should count registrations by event ID")
        void shouldCountRegistrationsByEventId() {
            // When
            int count1 = registrationRepository.countByEventId(event1.getId());
            int count2 = registrationRepository.countByEventId(event2.getId());

            // Then
            assertThat(count1).isEqualTo(2);
            assertThat(count2).isEqualTo(1);
        }
    }

    @Nested
    @DisplayName("Modification Tests")
    class ModificationTests {

        @Test
        @DisplayName("Should save new registration")
        void shouldSaveNewRegistration() {
            // Given
            Registration newRegistration = Registration.builder()
                    .user(user2)
                    .event(event2)
                    .registrationTime(LocalDateTime.now())
                    .registrationStatus("CONFIRMED")
                    .build();

            // When
            Registration saved = registrationRepository.save(newRegistration);

            // Then
            assertThat(saved.getId()).isNotNull();
            assertThat(saved.getUser().getId()).isEqualTo(user2.getId());
            assertThat(saved.getEvent().getId()).isEqualTo(event2.getId());

            // Verify it's in the database
            assertThat(registrationRepository.findById(saved.getId())).isPresent();
        }

        @Test
        @DisplayName("Should update registration status")
        void shouldUpdateRegistrationStatus() {
            // Given
            Registration registration = registrationRepository.findByUserIdAndEventId(
                    user1.getId(), event1.getId()).orElseThrow();

            // When
            registration.setRegistrationStatus("CANCELLED");
            Registration updated = registrationRepository.save(registration);

            // Then
            assertThat(updated.getRegistrationStatus()).isEqualTo("CANCELLED");

            // Verify it's updated in the database
            Registration fromDb = registrationRepository.findById(updated.getId()).orElseThrow();
            assertThat(fromDb.getRegistrationStatus()).isEqualTo("CANCELLED");
        }

        @Test
        @DisplayName("Should delete registration")
        void shouldDeleteRegistration() {
            // Given
            Long registrationId = registration1.getId();

            // When
            registrationRepository.delete(registration1);

            // Then
            assertThat(registrationRepository.findById(registrationId)).isEmpty();
        }
    }
}