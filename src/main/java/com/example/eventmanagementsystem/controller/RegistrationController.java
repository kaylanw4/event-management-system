package com.example.eventmanagementsystem.controller;

import com.example.eventmanagementsystem.dto.RegistrationDTO;
import com.example.eventmanagementsystem.service.RegistrationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/registrations")
@RequiredArgsConstructor
public class RegistrationController {

    private final RegistrationService registrationService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<RegistrationDTO>> getAllRegistrations() {
        return ResponseEntity.ok(registrationService.findAllRegistrations());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @registrationSecurity.isUserOrEventOrganizer(#id, principal)")
    public ResponseEntity<RegistrationDTO> getRegistrationById(@PathVariable Long id) {
        return ResponseEntity.ok(registrationService.findRegistrationById(id));
    }

    @GetMapping("/user/{userId}")
    @PreAuthorize("hasRole('ADMIN') or @userSecurity.isSameUser(#userId, principal)")
    public ResponseEntity<List<RegistrationDTO>> getRegistrationsByUser(@PathVariable Long userId) {
        return ResponseEntity.ok(registrationService.findRegistrationsByUser(userId));
    }

    @GetMapping("/event/{eventId}")
    @PreAuthorize("hasRole('ADMIN') or @eventSecurity.isOrganizerOrAdmin(#eventId, principal)")
    public ResponseEntity<List<RegistrationDTO>> getRegistrationsByEvent(@PathVariable Long eventId) {
        return ResponseEntity.ok(registrationService.findRegistrationsByEvent(eventId));
    }

    @PostMapping("/user/{userId}/event/{eventId}")
    @PreAuthorize("hasRole('ADMIN') or @userSecurity.isSameUser(#userId, principal)")
    public ResponseEntity<RegistrationDTO> registerForEvent(
            @PathVariable Long userId,
            @PathVariable Long eventId) {
        return new ResponseEntity<>(registrationService.registerForEvent(userId, eventId), HttpStatus.CREATED);
    }

    @PatchMapping("/user/{userId}/event/{eventId}/cancel")
    @PreAuthorize("hasRole('ADMIN') or @userSecurity.isSameUser(#userId, principal)")
    public ResponseEntity<RegistrationDTO> cancelRegistration(
            @PathVariable Long userId,
            @PathVariable Long eventId) {
        return ResponseEntity.ok(registrationService.cancelRegistration(userId, eventId));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteRegistration(@PathVariable Long id) {
        registrationService.deleteRegistration(id);
        return ResponseEntity.noContent().build();
    }
}