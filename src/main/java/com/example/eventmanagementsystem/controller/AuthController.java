package com.example.eventmanagementsystem.controller;

import com.example.eventmanagementsystem.dto.JwtAuthResponse;
import com.example.eventmanagementsystem.dto.LoginRequest;
import com.example.eventmanagementsystem.dto.UserDTO;
import com.example.eventmanagementsystem.model.User;
import com.example.eventmanagementsystem.repository.UserRepository;
import com.example.eventmanagementsystem.security.JwtTokenProvider;
import com.example.eventmanagementsystem.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final JwtTokenProvider tokenProvider;
    private final UserService userService;

    @PostMapping("/login")
    public ResponseEntity<JwtAuthResponse> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getUsername(),
                        loginRequest.getPassword()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        String jwt = tokenProvider.generateToken(authentication);

        User user = userRepository.findByUsername(loginRequest.getUsername()).orElseThrow();

        JwtAuthResponse response = JwtAuthResponse.builder()
                .accessToken(jwt)
                .tokenType("Bearer")  // Explicitly set tokenType
                .userId(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .build();

        return ResponseEntity.ok(response);
    }

    @PostMapping("/register")
    public ResponseEntity<UserDTO> registerUser(@Valid @RequestBody UserDTO userDTO) {
        return ResponseEntity.ok(userService.createUser(userDTO));
    }
}