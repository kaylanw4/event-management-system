package com.example.eventmanagementsystem.service;

import com.example.eventmanagementsystem.dto.UserDTO;
import com.example.eventmanagementsystem.exception.ResourceAlreadyExistsException;
import com.example.eventmanagementsystem.exception.ResourceNotFoundException;
import com.example.eventmanagementsystem.model.User;
import com.example.eventmanagementsystem.repository.UserRepository;
import com.example.eventmanagementsystem.util.TestUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private User testUser;
    private UserDTO testUserDTO;

    @BeforeEach
    public void setUp() {
        testUser = TestUtils.createTestUser();
        testUserDTO = TestUtils.createTestUserDTO();
    }

    @Test
    public void whenFindAllUsers_thenReturnUserList() {
        // Given
        User admin = TestUtils.createTestAdmin();
        when(userRepository.findAll()).thenReturn(Arrays.asList(testUser, admin));

        // When
        List<UserDTO> users = userService.findAllUsers();

        // Then
        assertThat(users).hasSize(2);
        assertThat(users).extracting(UserDTO::getUsername)
                .containsExactlyInAnyOrder("testuser", "admin");
        verify(userRepository, times(1)).findAll();
    }

    @Test
    public void whenFindUserById_withValidId_thenReturnUser() {
        // Given
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(testUser));

        // When
        UserDTO foundUser = userService.findUserById(1L);

        // Then
        assertThat(foundUser).isNotNull();
        assertThat(foundUser.getUsername()).isEqualTo("testuser");
        verify(userRepository, times(1)).findById(1L);
    }

    @Test
    public void whenFindUserById_withInvalidId_thenThrowException() {
        // Given
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> {
            userService.findUserById(999L);
        });
        verify(userRepository, times(1)).findById(999L);
    }

    @Test
    public void whenFindUserByUsername_withValidUsername_thenReturnUser() {
        // Given
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(testUser));

        // When
        UserDTO foundUser = userService.findUserByUsername("testuser");

        // Then
        assertThat(foundUser).isNotNull();
        assertThat(foundUser.getUsername()).isEqualTo("testuser");
        verify(userRepository, times(1)).findByUsername("testuser");
    }

    @Test
    public void whenFindUserByUsername_withInvalidUsername_thenThrowException() {
        // Given
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> {
            userService.findUserByUsername("nonexistentuser");
        });
        verify(userRepository, times(1)).findByUsername("nonexistentuser");
    }

    @Test
    public void whenCreateUser_withNewUser_thenReturnSavedUser() {
        // Given
        when(userRepository.existsByUsername(anyString())).thenReturn(false);
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // When
        UserDTO savedUser = userService.createUser(testUserDTO);

        // Then
        assertThat(savedUser).isNotNull();
        assertThat(savedUser.getUsername()).isEqualTo("testuser");
        verify(userRepository, times(1)).existsByUsername("testuser");
        verify(userRepository, times(1)).existsByEmail("test@example.com");
        verify(passwordEncoder, times(1)).encode("password123");
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    public void whenCreateUser_withExistingUsername_thenThrowException() {
        // Given
        when(userRepository.existsByUsername(anyString())).thenReturn(true);

        // When & Then
        assertThrows(ResourceAlreadyExistsException.class, () -> {
            userService.createUser(testUserDTO);
        });
        verify(userRepository, times(1)).existsByUsername("testuser");
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    public void whenCreateUser_withExistingEmail_thenThrowException() {
        // Given
        when(userRepository.existsByUsername(anyString())).thenReturn(false);
        when(userRepository.existsByEmail(anyString())).thenReturn(true);

        // When & Then
        assertThrows(ResourceAlreadyExistsException.class, () -> {
            userService.createUser(testUserDTO);
        });
        verify(userRepository, times(1)).existsByUsername("testuser");
        verify(userRepository, times(1)).existsByEmail("test@example.com");
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    public void whenUpdateUser_withValidData_thenReturnUpdatedUser() {
        // Given
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        UserDTO updateDTO = UserDTO.builder()
                .id(1L)
                .username("testuser") // Same username
                .email("test@example.com") // Same email
                .fullName("Updated Name")
                .password("newpassword")
                .build();

        // When
        UserDTO updatedUser = userService.updateUser(1L, updateDTO);

        // Then
        assertThat(updatedUser).isNotNull();
        assertThat(updatedUser.getFullName()).isEqualTo("Updated Name");
        verify(userRepository, times(1)).findById(1L);
        verify(passwordEncoder, times(1)).encode("newpassword");
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    public void whenUpdateUser_withNewUsername_andUsernameExists_thenThrowException() {
        // Given
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(testUser));
        when(userRepository.existsByUsername(anyString())).thenReturn(true);

        UserDTO updateDTO = UserDTO.builder()
                .id(1L)
                .username("newusername") // Different username
                .email("test@example.com")
                .fullName("Test User")
                .build();

        // When & Then
        assertThrows(ResourceAlreadyExistsException.class, () -> {
            userService.updateUser(1L, updateDTO);
        });
        verify(userRepository, times(1)).findById(1L);
        verify(userRepository, times(1)).existsByUsername("newusername");
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    public void whenDeleteUser_withValidId_thenDeleteUser() {
        // Given
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(testUser));
        doNothing().when(userRepository).delete(any(User.class));

        // When
        userService.deleteUser(1L);

        // Then
        verify(userRepository, times(1)).findById(1L);
        verify(userRepository, times(1)).delete(testUser);
    }

    @Test
    public void whenDeleteUser_withInvalidId_thenThrowException() {
        // Given
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> {
            userService.deleteUser(999L);
        });
        verify(userRepository, times(1)).findById(999L);
        verify(userRepository, never()).delete(any(User.class));
    }
}