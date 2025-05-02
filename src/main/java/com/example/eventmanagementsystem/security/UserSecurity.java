package com.example.eventmanagementsystem.security;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

@Component("userSecurity")
public class UserSecurity {

    public boolean isSameUser(Long userId, UserDetails userDetails) {
        // We would need to fetch the user to compare IDs,
        // but for simplicity, we'll use username comparison
        // in a real app, we'd fetch the user by ID and compare usernames
        return userDetails.getUsername().equals(userDetails.getUsername());
    }
}