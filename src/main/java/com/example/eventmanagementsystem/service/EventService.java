package com.example.eventmanagementsystem.service;

import com.example.eventmanagementsystem.dto.EventDTO;
import com.example.eventmanagementsystem.exception.ApiException;
import com.example.eventmanagementsystem.exception.ResourceNotFoundException;
import com.example.eventmanagementsystem.model.Event;
import com.example.eventmanagementsystem.model.User;
import com.example.eventmanagementsystem.repository.EventRepository;
import com.example.eventmanagementsystem.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EventService {

    private final EventRepository eventRepository;
    private final UserRepository userRepository;

    public List<EventDTO> findAllEvents() {
        return eventRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<EventDTO> findAllPublishedEvents() {
        return eventRepository.findByPublishedTrue().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public EventDTO findEventById(Long id) {
        Event event = getEventOrThrow(id);
        return convertToDTO(event);
    }

    @Transactional
    public EventDTO createEvent(EventDTO eventDTO) {
        // Validate event dates
        validateEventDates(eventDTO);

        User organizer = getUserOrThrow(eventDTO.getOrganizerId());

        Event event = Event.builder()
                .name(eventDTO.getName())
                .description(eventDTO.getDescription())
                .startTime(eventDTO.getStartTime())
                .endTime(eventDTO.getEndTime())
                .location(eventDTO.getLocation())
                .category(eventDTO.getCategory())
                .capacity(eventDTO.getCapacity())
                .published(false) // New events are unpublished by default
                .organizer(organizer)
                .build();

        Event savedEvent = eventRepository.save(event);
        return convertToDTO(savedEvent);
    }

    @Transactional
    public EventDTO updateEvent(Long id, EventDTO eventDTO) {
        // Validate event dates
        validateEventDates(eventDTO);

        Event event = getEventOrThrow(id);

        // Update fields
        event.setName(eventDTO.getName());
        event.setDescription(eventDTO.getDescription());
        event.setStartTime(eventDTO.getStartTime());
        event.setEndTime(eventDTO.getEndTime());
        event.setLocation(eventDTO.getLocation());
        event.setCategory(eventDTO.getCategory());
        event.setCapacity(eventDTO.getCapacity());

        Event updatedEvent = eventRepository.save(event);
        return convertToDTO(updatedEvent);
    }

    @Transactional
    public void deleteEvent(Long id) {
        Event event = getEventOrThrow(id);
        eventRepository.delete(event);
    }

    @Transactional
    public EventDTO publishEvent(Long id) {
        Event event = getEventOrThrow(id);

        if (event.isPublished()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Event is already published");
        }

        event.setPublished(true);
        Event updatedEvent = eventRepository.save(event);
        return convertToDTO(updatedEvent);
    }

    @Transactional
    public EventDTO unpublishEvent(Long id) {
        Event event = getEventOrThrow(id);

        if (!event.isPublished()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Event is already unpublished");
        }

        event.setPublished(false);
        Event updatedEvent = eventRepository.save(event);
        return convertToDTO(updatedEvent);
    }

    public List<EventDTO> searchEvents(String keyword, String category, LocalDate date) {
        return eventRepository.searchEvents(keyword, category, date).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<EventDTO> findEventsByOrganizer(Long organizerId) {
        // Check if organizer exists
        getUserOrThrow(organizerId);

        return eventRepository.findByOrganizerId(organizerId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // Helper methods
    private Event getEventOrThrow(Long id) {
        return eventRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Event", "id", id));
    }

    private User getUserOrThrow(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
    }

    private void validateEventDates(EventDTO eventDTO) {
        LocalDateTime now = LocalDateTime.now();

        if (eventDTO.getStartTime().isBefore(now)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Event start time must be in the future");
        }

        if (eventDTO.getEndTime().isBefore(eventDTO.getStartTime())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Event end time must be after start time");
        }
    }

    private EventDTO convertToDTO(Event event) {
        return EventDTO.builder()
                .id(event.getId())
                .name(event.getName())
                .description(event.getDescription())
                .startTime(event.getStartTime())
                .endTime(event.getEndTime())
                .location(event.getLocation())
                .category(event.getCategory())
                .capacity(event.getCapacity())
                .published(event.isPublished())
                .organizerId(event.getOrganizer().getId())
                .organizerName(event.getOrganizer().getFullName())
                .registrationCount(event.getRegistrations().size())
                .availableSpots(event.getAvailableSpots())
                .build();
    }
}