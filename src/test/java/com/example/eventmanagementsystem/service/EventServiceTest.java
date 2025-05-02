package com.example.eventmanagementsystem.service;

import com.example.eventmanagementsystem.dto.EventDTO;
import com.example.eventmanagementsystem.exception.ApiException;
import com.example.eventmanagementsystem.exception.ResourceNotFoundException;
import com.example.eventmanagementsystem.model.Event;
import com.example.eventmanagementsystem.model.User;
import com.example.eventmanagementsystem.repository.EventRepository;
import com.example.eventmanagementsystem.repository.UserRepository;
import com.example.eventmanagementsystem.util.TestUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class EventServiceTest {

    @Mock
    private EventRepository eventRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private EventService eventService;

    private User organizer;
    private Event testEvent;
    private EventDTO testEventDTO;

    @BeforeEach
    public void setUp() {
        organizer = TestUtils.createTestOrganizer();
        testEvent = TestUtils.createTestEvent();
        testEventDTO = TestUtils.createTestEventDTO();
    }

    @Test
    public void whenFindAllEvents_thenReturnEventList() {
        // Given
        Event anotherEvent = Event.builder()
                .id(2L)
                .name("Another Event")
                .description("Another test event description")
                .startTime(LocalDateTime.now().plusDays(14))
                .endTime(LocalDateTime.now().plusDays(14).plusHours(3))
                .location("Another Location")
                .category("Another Category")
                .capacity(50)
                .published(true)
                .organizer(organizer)
                .build();

        when(eventRepository.findAll()).thenReturn(Arrays.asList(testEvent, anotherEvent));

        // When
        List<EventDTO> events = eventService.findAllEvents();

        // Then
        assertThat(events).hasSize(2);
        assertThat(events).extracting(EventDTO::getName)
                .containsExactlyInAnyOrder("Test Event", "Another Event");
        verify(eventRepository, times(1)).findAll();
    }

    @Test
    public void whenFindAllPublishedEvents_thenReturnPublishedEventList() {
        // Given
        when(eventRepository.findByPublishedTrue()).thenReturn(Collections.singletonList(testEvent));

        // When
        List<EventDTO> events = eventService.findAllPublishedEvents();

        // Then
        assertThat(events).hasSize(1);
        assertThat(events.get(0).getName()).isEqualTo("Test Event");
        verify(eventRepository, times(1)).findByPublishedTrue();
    }

    @Test
    public void whenFindEventById_withValidId_thenReturnEvent() {
        // Given
        when(eventRepository.findById(anyLong())).thenReturn(Optional.of(testEvent));

        // When
        EventDTO foundEvent = eventService.findEventById(1L);

        // Then
        assertThat(foundEvent).isNotNull();
        assertThat(foundEvent.getName()).isEqualTo("Test Event");
        verify(eventRepository, times(1)).findById(1L);
    }

    @Test
    public void whenFindEventById_withInvalidId_thenThrowException() {
        // Given
        when(eventRepository.findById(anyLong())).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> {
            eventService.findEventById(999L);
        });
        verify(eventRepository, times(1)).findById(999L);
    }

    @Test
    public void whenCreateEvent_withValidData_thenReturnCreatedEvent() {
        // Given
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(organizer));
        when(eventRepository.save(any(Event.class))).thenReturn(testEvent);

        // When
        EventDTO createdEvent = eventService.createEvent(testEventDTO);

        // Then
        assertThat(createdEvent).isNotNull();
        assertThat(createdEvent.getName()).isEqualTo("Test Event");
        assertThat(createdEvent.isPublished()).isFalse();  // New events should be unpublished
        verify(userRepository, times(1)).findById(testEventDTO.getOrganizerId());
        verify(eventRepository, times(1)).save(any(Event.class));
    }

    @Test
    public void whenCreateEvent_withInvalidOrganizerId_thenThrowException() {
        // Given
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> {
            eventService.createEvent(testEventDTO);
        });
        verify(userRepository, times(1)).findById(testEventDTO.getOrganizerId());
        verify(eventRepository, never()).save(any(Event.class));
    }

    @Test
    public void whenCreateEvent_withPastStartTime_thenThrowException() {
        // Given
        EventDTO invalidEventDTO = EventDTO.builder()
                .name("Invalid Event")
                .description("Event with past start time")
                .startTime(LocalDateTime.now().minusDays(1))  // Past start time
                .endTime(LocalDateTime.now().plusHours(2))
                .location("Test Location")
                .category("Test Category")
                .capacity(100)
                .organizerId(3L)
                .build();

        // When & Then
        assertThrows(ApiException.class, () -> {
            eventService.createEvent(invalidEventDTO);
        });
        verify(eventRepository, never()).save(any(Event.class));
    }

    @Test
    public void whenCreateEvent_withEndTimeBeforeStartTime_thenThrowException() {
        // Given
        EventDTO invalidEventDTO = EventDTO.builder()
                .name("Invalid Event")
                .description("Event with end time before start time")
                .startTime(LocalDateTime.now().plusDays(2))
                .endTime(LocalDateTime.now().plusDays(1))  // End time before start time
                .location("Test Location")
                .category("Test Category")
                .capacity(100)
                .organizerId(3L)
                .build();

        // When & Then
        assertThrows(ApiException.class, () -> {
            eventService.createEvent(invalidEventDTO);
        });
        verify(eventRepository, never()).save(any(Event.class));
    }

    @Test
    public void whenUpdateEvent_withValidData_thenReturnUpdatedEvent() {
        // Given
        when(eventRepository.findById(anyLong())).thenReturn(Optional.of(testEvent));
        when(eventRepository.save(any(Event.class))).thenReturn(testEvent);

        EventDTO updateDTO = EventDTO.builder()
                .id(1L)
                .name("Updated Event")
                .description("Updated description")
                .startTime(LocalDateTime.now().plusDays(10))
                .endTime(LocalDateTime.now().plusDays(10).plusHours(3))
                .location("Updated Location")
                .category("Updated Category")
                .capacity(200)
                .organizerId(3L)
                .build();

        // When
        EventDTO updatedEvent = eventService.updateEvent(1L, updateDTO);

        // Then
        assertThat(updatedEvent).isNotNull();
        assertThat(updatedEvent.getName()).isEqualTo("Updated Event");
        verify(eventRepository, times(1)).findById(1L);
        verify(eventRepository, times(1)).save(any(Event.class));
    }

    @Test
    public void whenDeleteEvent_withValidId_thenDeleteEvent() {
        // Given
        when(eventRepository.findById(anyLong())).thenReturn(Optional.of(testEvent));
        doNothing().when(eventRepository).delete(any(Event.class));

        // When
        eventService.deleteEvent(1L);

        // Then
        verify(eventRepository, times(1)).findById(1L);
        verify(eventRepository, times(1)).delete(testEvent);
    }

    @Test
    public void whenPublishEvent_withUnpublishedEvent_thenReturnPublishedEvent() {
        // Given
        testEvent.setPublished(false);

        when(eventRepository.findById(anyLong())).thenReturn(Optional.of(testEvent));
        when(eventRepository.save(any(Event.class))).thenReturn(testEvent);

        // When
        EventDTO publishedEvent = eventService.publishEvent(1L);

        // Then
        assertThat(publishedEvent).isNotNull();
        assertThat(publishedEvent.isPublished()).isTrue();
        verify(eventRepository, times(1)).findById(1L);
        verify(eventRepository, times(1)).save(any(Event.class));
    }

    @Test
    public void whenPublishEvent_withAlreadyPublishedEvent_thenThrowException() {
        // Given
        testEvent.setPublished(true);

        when(eventRepository.findById(anyLong())).thenReturn(Optional.of(testEvent));

        // When & Then
        assertThrows(ApiException.class, () -> {
            eventService.publishEvent(1L);
        });
        verify(eventRepository, times(1)).findById(1L);
        verify(eventRepository, never()).save(any(Event.class));
    }

    @Test
    public void whenUnpublishEvent_withPublishedEvent_thenReturnUnpublishedEvent() {
        // Given
        testEvent.setPublished(true);

        when(eventRepository.findById(anyLong())).thenReturn(Optional.of(testEvent));
        when(eventRepository.save(any(Event.class))).thenReturn(testEvent);

        // When
        EventDTO unpublishedEvent = eventService.unpublishEvent(1L);

        // Then
        assertThat(unpublishedEvent).isNotNull();
        assertThat(unpublishedEvent.isPublished()).isFalse();
        verify(eventRepository, times(1)).findById(1L);
        verify(eventRepository, times(1)).save(any(Event.class));
    }

    @Test
    public void whenUnpublishEvent_withAlreadyUnpublishedEvent_thenThrowException() {
        // Given
        testEvent.setPublished(false);

        when(eventRepository.findById(anyLong())).thenReturn(Optional.of(testEvent));

        // When & Then
        assertThrows(ApiException.class, () -> {
            eventService.unpublishEvent(1L);
        });
        verify(eventRepository, times(1)).findById(1L);
        verify(eventRepository, never()).save(any(Event.class));
    }

    @Test
    public void whenSearchEvents_thenReturnMatchingEvents() {
        // Given
        String keyword = "test";
        String category = "Category";
        LocalDate date = LocalDate.now().plusDays(7);

        when(eventRepository.searchEvents(anyString(), anyString(), any(LocalDate.class)))
                .thenReturn(Collections.singletonList(testEvent));

        // When
        List<EventDTO> events = eventService.searchEvents(keyword, category, date);

        // Then
        assertThat(events).hasSize(1);
        assertThat(events.get(0).getName()).isEqualTo("Test Event");
        verify(eventRepository, times(1)).searchEvents(keyword, category, date);
    }

    @Test
    public void whenFindEventsByOrganizer_withValidOrganizerId_thenReturnOrganizerEvents() {
        // Given
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(organizer));
        when(eventRepository.findByOrganizerId(anyLong())).thenReturn(Collections.singletonList(testEvent));

        // When
        List<EventDTO> events = eventService.findEventsByOrganizer(3L);

        // Then
        assertThat(events).hasSize(1);
        assertThat(events.get(0).getName()).isEqualTo("Test Event");
        verify(userRepository, times(1)).findById(3L);
        verify(eventRepository, times(1)).findByOrganizerId(3L);
    }

    @Test
    public void whenFindEventsByOrganizer_withInvalidOrganizerId_thenThrowException() {
        // Given
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> {
            eventService.findEventsByOrganizer(999L);
        });
        verify(userRepository, times(1)).findById(999L);
        verify(eventRepository, never()).findByOrganizerId(anyLong());
    }
}