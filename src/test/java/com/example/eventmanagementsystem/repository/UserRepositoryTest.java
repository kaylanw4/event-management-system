package com.example.eventmanagementsystem.repository;

import com.example.eventmanagementsystem.model.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
public class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    public void whenFindByUsername_thenReturnUser() {
        // Given
        User user = User.builder()
                .username("testuser")
                .password("password")
                .email("test@example.com")
                .fullName("Test User")
                .roles(new HashSet<>(Set.of("USER")))
                .build();

        userRepository.save(user);

        // When
        Optional<User> found = userRepository.findByUsername("testuser");

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getUsername()).isEqualTo("testuser");
        assertThat(found.get().getEmail()).isEqualTo("test@example.com");
    }

    @Test
    public void whenFindByEmail_thenReturnUser() {
        // Given
        User user = User.builder()
                .username("testuser")
                .password("password")
                .email("test@example.com")
                .fullName("Test User")
                .roles(new HashSet<>(Set.of("USER")))
                .build();

        userRepository.save(user);

        // When
        Optional<User> found = userRepository.findByEmail("test@example.com");

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getUsername()).isEqualTo("testuser");
    }

    @Test
    public void whenExistsByUsername_andUsernameExists_thenReturnTrue() {
        // Given
        User user = User.builder()
                .username("testuser")
                .password("password")
                .email("test@example.com")
                .fullName("Test User")
                .roles(new HashSet<>(Set.of("USER")))
                .build();

        userRepository.save(user);

        // When
        boolean exists = userRepository.existsByUsername("testuser");

        // Then
        assertThat(exists).isTrue();
    }

    @Test
    public void whenExistsByUsername_andUsernameDoesNotExist_thenReturnFalse() {
        // When
        boolean exists = userRepository.existsByUsername("nonexistentuser");

        // Then
        assertThat(exists).isFalse();
    }

    @Test
    public void whenExistsByEmail_andEmailExists_thenReturnTrue() {
        // Given
        User user = User.builder()
                .username("testuser")
                .password("password")
                .email("test@example.com")
                .fullName("Test User")
                .roles(new HashSet<>(Set.of("USER")))
                .build();

        userRepository.save(user);

        // When
        boolean exists = userRepository.existsByEmail("test@example.com");

        // Then
        assertThat(exists).isTrue();
    }

    @Test
    public void whenExistsByEmail_andEmailDoesNotExist_thenReturnFalse() {
        // When
        boolean exists = userRepository.existsByEmail("nonexistent@example.com");

        // Then
        assertThat(exists).isFalse();
    }
}