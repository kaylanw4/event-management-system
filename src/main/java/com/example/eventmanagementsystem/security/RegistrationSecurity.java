package com.example.eventmanagementsystem.security;

import com.example.eventmanagementsystem.model.Registration;
import com.example.eventmanagementsystem.repository.RegistrationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

@Component("registrationSecurity")
@RequiredArgsConstructor
public class RegistrationSecurity {

    private final RegistrationRepository registrationRepository;
    private final UserSecurity userSecurity;
    private final EventSecurity eventSecurity;

    /**
     * Checks if the current user is either:
     * 1. The user who made the registration
     * 2. The organizer of the event being registered for
     * 3. An admin (checked by eventSecurity's isOrganizerOrAdmin method)
     */
    public boolean isUserOrEventOrganizer(Long registrationId, UserDetails userDetails) {
        Registration registration = registrationRepository.findById(registrationId).orElse(null);

        if (registration == null) {
            return false;
        }

        // Check if the user is the one who made the registration
        boolean isRegisteredUser = userSecurity.isSameUser(
                registration.getUser().getId(), userDetails);

        // Check if the user is the organizer of the event or an admin
        boolean isOrganizerOrAdmin = eventSecurity.isOrganizerOrAdmin(
                registration.getEvent().getId(), userDetails);

        return isRegisteredUser || isOrganizerOrAdmin;
    }
}