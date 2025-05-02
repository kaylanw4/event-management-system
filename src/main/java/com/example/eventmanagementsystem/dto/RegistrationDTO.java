package com.example.eventmanagementsystem.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegistrationDTO {

    private Long id;

    private Long userId;

    private String username;

    private Long eventId;

    private String eventName;

    private LocalDateTime registrationTime;

    private String registrationStatus;
}