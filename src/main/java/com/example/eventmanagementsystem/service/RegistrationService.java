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
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RegistrationService {

    private final RegistrationRepository registrationRepository;
    private final UserRepository userRepository;
    private final EventRepository eventRepository;

    public List<RegistrationDTO> findAllRegistrations() {
        return registrationRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public RegistrationDTO findRegistrationById(Long id) {
        Registration registration = getRegistrationOrThrow(id);
        return convertToDTO(registration);
    }

    public List<RegistrationDTO> findRegistrationsByUser(Long userId) {
        // Check if user exists
        getUserOrThrow(userId);

        return registrationRepository.findByUserId(userId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<RegistrationDTO> findRegistrationsByEvent(Long eventId) {
        // Check if event exists
        getEventOrThrow(eventId);

        return registrationRepository.findByEventId(eventId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public RegistrationDTO registerForEvent(Long userId, Long eventId) {
        User user = getUserOrThrow(userId);
        Event event = getEventOrThrow(eventId);

        // Check if the event is published
        if (!event.isPublished()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Cannot register for an unpublished event");
        }

        // Check if the user is already registered
        if (registrationRepository.existsByUserIdAndEventId(userId, eventId)) {
            throw new ApiException(HttpStatus.CONFLICT, "User is already registered for this event");
        }

        // Check if the event is at capacity
        if (!event.hasAvailableSpots()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Event is at full capacity");
        }

        // Check if the event is in the past
        if (event.getStartTime().isBefore(LocalDateTime.now())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Cannot register for past events");
        }

        Registration registration = Registration.builder()
                .user(user)
                .event(event)
                .registrationTime(LocalDateTime.now())
                .registrationStatus("CONFIRMED")
                .build();

        Registration savedRegistration = registrationRepository.save(registration);
        return convertToDTO(savedRegistration);
    }

    @Transactional
    public RegistrationDTO cancelRegistration(Long userId, Long eventId) {
        Registration registration = registrationRepository.findByUserIdAndEventId(userId, eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Registration", "userId and eventId", userId + ", " + eventId));

        // Check if the event has already started
        if (registration.getEvent().getStartTime().isBefore(LocalDateTime.now())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Cannot cancel registration for events that have already started");
        }

        registration.setRegistrationStatus("CANCELLED");
        Registration updatedRegistration = registrationRepository.save(registration);
        return convertToDTO(updatedRegistration);
    }

    @Transactional
    public void deleteRegistration(Long id) {
        Registration registration = getRegistrationOrThrow(id);
        registrationRepository.delete(registration);
    }

    // Helper methods
    private Registration getRegistrationOrThrow(Long id) {
        return registrationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Registration", "id", id));
    }

    private User getUserOrThrow(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
    }

    private Event getEventOrThrow(Long id) {
        return eventRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Event", "id", id));
    }

    private RegistrationDTO convertToDTO(Registration registration) {
        return RegistrationDTO.builder()
                .id(registration.getId())
                .userId(registration.getUser().getId())
                .username(registration.getUser().getUsername())
                .eventId(registration.getEvent().getId())
                .eventName(registration.getEvent().getName())
                .registrationTime(registration.getRegistrationTime())
                .registrationStatus(registration.getRegistrationStatus())
                .build();
    }
}