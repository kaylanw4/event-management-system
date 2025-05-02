package com.example.eventmanagementsystem.config;

import com.example.eventmanagementsystem.security.EventSecurity;
import com.example.eventmanagementsystem.security.JwtAuthenticationEntryPoint;
import com.example.eventmanagementsystem.security.JwtAuthenticationFilter;
import com.example.eventmanagementsystem.security.UserSecurity;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@TestConfiguration
public class TestWebConfig {

    @MockBean
    private JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockBean
    private EventSecurity eventSecurity;

    @MockBean
    private UserSecurity userSecurity;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }
}