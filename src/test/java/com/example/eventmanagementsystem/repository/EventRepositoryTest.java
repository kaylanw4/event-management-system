package com.example.eventmanagementsystem.repository;

import com.example.eventmanagementsystem.model.Event;
import com.example.eventmanagementsystem.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
public class EventRepositoryTest {

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private UserRepository userRepository;

    private User organizer;

    @BeforeEach
    public void setUp() {
        // Create a test organizer
        organizer = User.builder()
                .username("organizer")
                .password("password")
                .email("organizer@example.com")
                .fullName("Event Organizer")
                .roles(new HashSet<>(Set.of("ORGANIZER")))
                .build();

        userRepository.save(organizer);
    }

    @Test
    public void whenFindByPublishedTrue_thenReturnPublishedEvents() {
        // Given
        Event publishedEvent = Event.builder()
                .name("Published Event")
                .description("This is a published event")
                .startTime(LocalDateTime.now().plusDays(1))
                .endTime(LocalDateTime.now().plusDays(1).plusHours(2))
                .location("Test Location")
                .category("Test Category")
                .capacity(100)
                .published(true)
                .organizer(organizer)
                .build();

        Event unpublishedEvent = Event.builder()
                .name("Unpublished Event")
                .description("This is an unpublished event")
                .startTime(LocalDateTime.now().plusDays(2))
                .endTime(LocalDateTime.now().plusDays(2).plusHours(2))
                .location("Test Location")
                .category("Test Category")
                .capacity(100)
                .published(false)
                .organizer(organizer)
                .build();

        eventRepository.save(publishedEvent);
        eventRepository.save(unpublishedEvent);

        // When
        List<Event> publishedEvents = eventRepository.findByPublishedTrue();

        // Then
        assertThat(publishedEvents).hasSize(1);
        assertThat(publishedEvents.get(0).getName()).isEqualTo("Published Event");
    }

    @Test
    public void whenFindByOrganizerId_thenReturnOrganizerEvents() {
        // Given
        Event event1 = Event.builder()
                .name("Organizer Event 1")
                .description("This is an event by the organizer")
                .startTime(LocalDateTime.now().plusDays(1))
                .endTime(LocalDateTime.now().plusDays(1).plusHours(2))
                .location("Test Location 1")
                .category("Test Category")
                .capacity(100)
                .published(true)
                .organizer(organizer)
                .build();

        Event event2 = Event.builder()
                .name("Organizer Event 2")
                .description("This is another event by the organizer")
                .startTime(LocalDateTime.now().plusDays(2))
                .endTime(LocalDateTime.now().plusDays(2).plusHours(2))
                .location("Test Location 2")
                .category("Test Category")
                .capacity(200)
                .published(false)
                .organizer(organizer)
                .build();

        eventRepository.save(event1);
        eventRepository.save(event2);

        // When
        List<Event> organizerEvents = eventRepository.findByOrganizerId(organizer.getId());

        // Then
        assertThat(organizerEvents).hasSize(2);
        assertThat(organizerEvents).extracting(Event::getName)
                .containsExactlyInAnyOrder("Organizer Event 1", "Organizer Event 2");
    }

    @Test
    public void whenSearchEvents_withKeyword_thenReturnMatchingEvents() {
        // Given
        Event event1 = Event.builder()
                .name("Music Festival")
                .description("A great music festival")
                .startTime(LocalDateTime.now().plusDays(10))
                .endTime(LocalDateTime.now().plusDays(10).plusHours(6))
                .location("Park")
                .category("Music")
                .capacity(1000)
                .published(true)
                .organizer(organizer)
                .build();

        Event event2 = Event.builder()
                .name("Tech Conference")
                .description("A conference about technology")
                .startTime(LocalDateTime.now().plusDays(20))
                .endTime(LocalDateTime.now().plusDays(20).plusHours(8))
                .location("Convention Center")
                .category("Technology")
                .capacity(500)
                .published(true)
                .organizer(organizer)
                .build();

        eventRepository.save(event1);
        eventRepository.save(event2);

        // When
        List<Event> results = eventRepository.searchEvents("music", null, null);

        // Then
        assertThat(results).hasSize(1);
        assertThat(results.get(0).getName()).isEqualTo("Music Festival");
    }

    @Test
    public void whenSearchEvents_withCategory_thenReturnMatchingEvents() {
        // Given
        Event event1 = Event.builder()
                .name("Music Festival")
                .description("A great music festival")
                .startTime(LocalDateTime.now().plusDays(10))
                .endTime(LocalDateTime.now().plusDays(10).plusHours(6))
                .location("Park")
                .category("Music")
                .capacity(1000)
                .published(true)
                .organizer(organizer)
                .build();

        Event event2 = Event.builder()
                .name("Tech Conference")
                .description("A conference about technology")
                .startTime(LocalDateTime.now().plusDays(20))
                .endTime(LocalDateTime.now().plusDays(20).plusHours(8))
                .location("Convention Center")
                .category("Technology")
                .capacity(500)
                .published(true)
                .organizer(organizer)
                .build();

        eventRepository.save(event1);
        eventRepository.save(event2);

        // When
        List<Event> results = eventRepository.searchEvents(null, "Technology", null);

        // Then
        assertThat(results).hasSize(1);
        assertThat(results.get(0).getName()).isEqualTo("Tech Conference");
    }

    @Test
    public void whenSearchEvents_withDate_thenReturnMatchingEvents() {
        // Given
        LocalDateTime eventDay = LocalDateTime.now().plusDays(15);
        LocalDate searchDate = eventDay.toLocalDate();

        Event event1 = Event.builder()
                .name("Music Festival")
                .description("A great music festival")
                .startTime(eventDay)
                .endTime(eventDay.plusHours(6))
                .location("Park")
                .category("Music")
                .capacity(1000)
                .published(true)
                .organizer(organizer)
                .build();

        Event event2 = Event.builder()
                .name("Tech Conference")
                .description("A conference about technology")
                .startTime(LocalDateTime.now().plusDays(20))
                .endTime(LocalDateTime.now().plusDays(20).plusHours(8))
                .location("Convention Center")
                .category("Technology")
                .capacity(500)
                .published(true)
                .organizer(organizer)
                .build();

        eventRepository.save(event1);
        eventRepository.save(event2);

        // When
        List<Event> results = eventRepository.searchEvents(null, null, searchDate);

        // Then
        assertThat(results).hasSize(1);
        assertThat(results.get(0).getName()).isEqualTo("Music Festival");
    }

    @Test
    public void whenFindByStartTimeBetween_thenReturnEventsInRange() {
        // Given
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime start = now.plusDays(5);
        LocalDateTime end = now.plusDays(15);

        Event event1 = Event.builder()
                .name("Event Within Range")
                .description("This event is within the time range")
                .startTime(now.plusDays(10))
                .endTime(now.plusDays(10).plusHours(2))
                .location("Test Location")
                .category("Test Category")
                .capacity(100)
                .published(true)
                .organizer(organizer)
                .build();

        Event event2 = Event.builder()
                .name("Event Outside Range")
                .description("This event is outside the time range")
                .startTime(now.plusDays(20))
                .endTime(now.plusDays(20).plusHours(2))
                .location("Test Location")
                .category("Test Category")
                .capacity(100)
                .published(true)
                .organizer(organizer)
                .build();

        eventRepository.save(event1);
        eventRepository.save(event2);

        // When
        List<Event> eventsInRange = eventRepository.findByStartTimeBetween(start, end);

        // Then
        assertThat(eventsInRange).hasSize(1);
        assertThat(eventsInRange.get(0).getName()).isEqualTo("Event Within Range");
    }

    @Test
    public void whenFindByCapacityGreaterThan_thenReturnEventsWithHigherCapacity() {
        // Given
        Event event1 = Event.builder()
                .name("Large Event")
                .description("This is a large event")
                .startTime(LocalDateTime.now().plusDays(1))
                .endTime(LocalDateTime.now().plusDays(1).plusHours(2))
                .location("Large Venue")
                .category("Conference")
                .capacity(500)
                .published(true)
                .organizer(organizer)
                .build();

        Event event2 = Event.builder()
                .name("Small Event")
                .description("This is a small event")
                .startTime(LocalDateTime.now().plusDays(2))
                .endTime(LocalDateTime.now().plusDays(2).plusHours(2))
                .location("Small Venue")
                .category("Workshop")
                .capacity(50)
                .published(true)
                .organizer(organizer)
                .build();

        eventRepository.save(event1);
        eventRepository.save(event2);

        // When
        List<Event> largeEvents = eventRepository.findByCapacityGreaterThan(100);

        // Then
        assertThat(largeEvents).hasSize(1);
        assertThat(largeEvents.get(0).getName()).isEqualTo("Large Event");
    }
}