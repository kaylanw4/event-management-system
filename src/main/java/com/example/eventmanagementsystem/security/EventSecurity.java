package com.example.eventmanagementsystem.security;

import com.example.eventmanagementsystem.model.Event;
import com.example.eventmanagementsystem.repository.EventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

@Component("eventSecurity")
@RequiredArgsConstructor
public class EventSecurity {

    private final EventRepository eventRepository;

    public boolean isOrganizerOrAdmin(Long eventId, UserDetails userDetails) {
        Event event = eventRepository.findById(eventId).orElse(null);
        if (event == null) {
            return false;
        }

        // Check if user is admin
        if (userDetails.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
            return true;
        }

        // Check if user is the organizer
        return event.getOrganizer().getUsername().equals(userDetails.getUsername());
    }
}