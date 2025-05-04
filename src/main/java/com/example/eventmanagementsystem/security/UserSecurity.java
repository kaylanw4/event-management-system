package com.example.eventmanagementsystem.security;

import com.example.eventmanagementsystem.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

@Component("userSecurity")
public class UserSecurity {

    private final UserRepository userRepository;

    public UserSecurity(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public boolean isSameUser(Long userId, UserDetails userDetails) {
        try {
            // Load the user entity by userId
            return userRepository.findById(userId)
                    .map(user -> user.getUsername().equals(userDetails.getUsername()))
                    .orElse(false);
        } catch (Exception e) {
            // Log the error but don't throw it
            // This prevents Spring Security from converting authorization failures to 500 errors
            return false;
        }
    }
}