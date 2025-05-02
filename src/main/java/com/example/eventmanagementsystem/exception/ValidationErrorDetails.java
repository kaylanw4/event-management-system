package com.example.eventmanagementsystem.exception;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Map;

@Getter
@Setter
public class ValidationErrorDetails extends ErrorDetails {
    private Map<String, String> validationErrors;

    public ValidationErrorDetails(LocalDateTime timestamp, String message, String errorCode, Map<String, String> validationErrors) {
        super(timestamp, message, errorCode);
        this.validationErrors = validationErrors;
    }
}