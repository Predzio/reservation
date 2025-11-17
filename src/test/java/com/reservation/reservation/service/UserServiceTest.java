package com.reservation.reservation.service;

import com.reservation.reservation.dto.request.RegisterRequest;
import com.reservation.reservation.model.Role;
import com.reservation.reservation.model.User;
import com.reservation.reservation.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private RegisterRequest registerRequest;

    @BeforeEach
    void setUp() {
        registerRequest = new RegisterRequest();
        registerRequest.setEmail("test@xyz.com");
        registerRequest.setPassword("zaq1@WSXxyz");
        registerRequest.setFirstName("Andrew");
        registerRequest.setLastName("Kowalski");
    }

    @Test
    void shouldRegisterNewPatientSuccessfully() {
        when(userRepository.existsByEmail("test@xyz.com")).thenReturn(false);
        when(passwordEncoder.encode("zaq1@WSXxyz")).thenReturn("zaszyfrowane_haslo");
        when(userRepository.save(any(User.class))).thenAnswer(invocationOnMock -> invocationOnMock.getArgument(0));

        User result = userService.registerNewPatient(registerRequest);

        assertNotNull(result);
        assertEquals("test@xyz.com", result.getEmail());
        assertEquals("zaszyfrowane_haslo", result.getPassword());
        assertTrue(result.getRoles().contains(Role.ROLE_PATIENT));
        assertFalse(result.getRoles().contains(Role.ROLE_DOCTOR));
        assertFalse(result.getRoles().contains(Role.ROLE_ADMIN));
    }

    @Test
    void shouldThrowExceptionWhenEmailAlreadyExists() {
        when(userRepository.existsByEmail("test@xyz.com")).thenReturn(true);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            userService.registerNewPatient(registerRequest);
        });

        assertEquals("Error: Email is already in use!", exception.getMessage());

        verify(passwordEncoder, never()).encode(anyString());
        verify(userRepository, never()).save(any(User.class));
    }
}




















