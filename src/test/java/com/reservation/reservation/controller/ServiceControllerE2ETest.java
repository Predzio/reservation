package com.reservation.reservation.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.reservation.reservation.dto.request.CreateServiceRequest;
import com.reservation.reservation.model.Role;
import com.reservation.reservation.model.User;
import com.reservation.reservation.repository.ServiceRepository;
import com.reservation.reservation.repository.UserRepository;
import com.reservation.reservation.security.JwtTokenService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Set;
import java.util.stream.Collectors;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ServiceControllerE2ETest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ServiceRepository serviceRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private JwtTokenService jwtTokenService;
    @Autowired private PasswordEncoder passwordEncoder;

    private final ObjectMapper objectMapper = new ObjectMapper();

    // Stores tokens
    private String adminToken;
    private String patientToken;

    @BeforeEach
    void setUp() {
        // Create and save ADMIN
        User admin = new User(
                "admin@test.com",
                passwordEncoder.encode("password"),
                "Admin", "User",
                Set.of(Role.ROLE_ADMIN)
        );
        userRepository.save(admin);
        adminToken = generateTokenForUser(admin);

        // Create and save patient
        User patient = new User(
                "patient@test.com",
                passwordEncoder.encode("password"),
                "Patient", "User",
                Set.of(Role.ROLE_PATIENT)
        );
        userRepository.save(patient);
        patientToken = generateTokenForUser(patient);
    }

    @AfterEach
    void tearDown() {
        serviceRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void securityScenario_AdminCanCreate_PatientCannot_EveryoneCanRead() throws Exception {
        CreateServiceRequest request = new CreateServiceRequest();
        request.setName("Masaż leczniczy");
        request.setPrice(BigDecimal.valueOf(120));
        request.setDurationMinutes(45);
        String requestJson = objectMapper.writeValueAsString(request);

        // Scenario 1: ADMIN creating service (Should be 200 OK)
        mockMvc.perform(post("/api/services")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Masaż leczniczy"));


        // Scenario 2:PATIENT try creating service (Should be 403 Forbidden)
        mockMvc.perform(post("/api/services")
                        .header("Authorization", "Bearer " + patientToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isForbidden());


        // Scenario 3: ANONIM try creating service (Should be 403 Forbidden)
        mockMvc.perform(post("/api/services")
                        // Brak nagłówka Authorization
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isUnauthorized()); 


        // Scenario 4: All should be see a service list (200 OK)
        mockMvc.perform(get("/api/services"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name").value("Masaż leczniczy"));
    }

    private String generateTokenForUser(User user) {
        var authorities = user.getRoles().stream()
                .map(role -> new SimpleGrantedAuthority(role.name()))
                .collect(Collectors.toList());

        UserDetails userDetails = new org.springframework.security.core.userdetails.User(
                user.getEmail(),
                user.getPassword(),
                authorities
        );

        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities());

        return jwtTokenService.generateToken(auth);
    }
}