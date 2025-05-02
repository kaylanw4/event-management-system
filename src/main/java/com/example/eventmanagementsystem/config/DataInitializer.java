package com.example.eventmanagementsystem.config;

import com.example.eventmanagementsystem.model.Event;
import com.example.eventmanagementsystem.model.User;
import com.example.eventmanagementsystem.repository.EventRepository;
import com.example.eventmanagementsystem.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashSet;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final EventRepository eventRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        // Only initialize if no users exist
        if (userRepository.count() == 0) {
            initUsers();
            initEvents();
        }
    }

    private void initUsers() {
        // Create admin user
        User admin = User.builder()
                .username("admin")
                .password(passwordEncoder.encode("admin123"))
                .email("admin@example.com")
                .fullName("Admin User")
                .roles(new HashSet<>(Arrays.asList("ADMIN")))
                .build();

        userRepository.save(admin);

        // Create organizer user
        User organizer = User.builder()
                .username("organizer")
                .password(passwordEncoder.encode("organizer123"))
                .email("organizer@example.com")
                .fullName("Event Organizer")
                .roles(new HashSet<>(Arrays.asList("ORGANIZER")))
                .build();

        userRepository.save(organizer);

        // Create regular user
        User user = User.builder()
                .username("user")
                .password(passwordEncoder.encode("user123"))
                .email("user@example.com")
                .fullName("Regular User")
                .roles(new HashSet<>(Arrays.asList("USER")))
                .build();

        userRepository.save(user);
    }

    private void initEvents() {
        User organizer = userRepository.findByUsername("organizer").orElseThrow();

        // Create sample events
        Event concert = Event.builder()
                .name("Summer Music Festival")
                .description("Join us for a day of live music performances from top artists.")
                .startTime(LocalDateTime.now().plusDays(30))
                .endTime(LocalDateTime.now().plusDays(30).plusHours(8))
                .location("Central Park")
                .category("Music")
                .capacity(1000)
                .published(true)
                .organizer(organizer)
                .build();

        eventRepository.save(concert);

        Event techConference = Event.builder()
                .name("Tech Innovation Summit")
                .description("A conference showcasing the latest advancements in technology.")
                .startTime(LocalDateTime.now().plusDays(60))
                .endTime(LocalDateTime.now().plusDays(62))
                .location("Convention Center")
                .category("Technology")
                .capacity(500)
                .published(true)
                .organizer(organizer)
                .build();

        eventRepository.save(techConference);

        Event workshop = Event.builder()
                .name("Creative Writing Workshop")
                .description("Learn writing techniques from published authors.")
                .startTime(LocalDateTime.now().plusDays(15))
                .endTime(LocalDateTime.now().plusDays(15).plusHours(4))
                .location("Community Library")
                .category("Education")
                .capacity(30)
                .published(true)
                .organizer(organizer)
                .build();

        eventRepository.save(workshop);

        Event unpublishedEvent = Event.builder()
                .name("Upcoming Product Launch")
                .description("Exclusive preview of our newest product line.")
                .startTime(LocalDateTime.now().plusDays(45))
                .endTime(LocalDateTime.now().plusDays(45).plusHours(3))
                .location("Company Headquarters")
                .category("Business")
                .capacity(100)
                .published(false)
                .organizer(organizer)
                .build();

        eventRepository.save(unpublishedEvent);
    }
}