package com.reservation.reservation.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.reservation.reservation.dto.request.LoginRequest;
import com.reservation.reservation.model.Role;
import com.reservation.reservation.model.User;
import com.reservation.reservation.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import java.util.Set;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthControllerE2ETest {

    @Autowired
    private MockMvc mockMvc;

    private ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @AfterEach
    void tearDown() {
        userRepository.deleteAll();
    }

    @Test
    void shouldLoginSuccessfullyAndReturnJwtToken() throws Exception {
        // Given (Przygotowanie)
        // Ręcznie dodajemy użytkownika do bazy, którego będziemy próbować zalogować
        User testAdmin = new User(
                "admin-test@company.com",
                passwordEncoder.encode("password123"), // Szyfrujemy hasło
                "Test",
                "Admin",
                Set.of(Role.ROLE_ADMIN)
        );
        userRepository.save(testAdmin);

        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("admin-test@company.com");
        loginRequest.setPassword("password123"); // Wysyłamy surowe hasło

        // When & Then
        mockMvc.perform(
                        post("/api/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(loginRequest))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value(notNullValue()))
                .andExpect(jsonPath("$.email").value("admin-test@company.com"))
                .andExpect(jsonPath("$.roles[0]").value("ROLE_ADMIN"));
    }
}
