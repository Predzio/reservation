package com.reservation.reservation.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.reservation.reservation.dto.request.CreateAvailabilityRequest;
import com.reservation.reservation.model.Role;
import com.reservation.reservation.model.User;
import com.reservation.reservation.repository.AvailabilityRepository;
import com.reservation.reservation.repository.UserRepository;
import com.reservation.reservation.security.JwtTokenService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AvailabilityControllerE2ETest {

    @Autowired private MockMvc mockMvc;
    @Autowired private AvailabilityRepository availabilityRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private JwtTokenService jwtTokenService;

    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
    private String doctorToken;
    private String patientToken;
    private User doctor;

    @BeforeEach
    void setUp() {
        availabilityRepository.deleteAll();
        userRepository.deleteAll();

        // Create doctor
        doctor = new User("doc@test.com", "pass", "Dr", "House", Set.of(Role.ROLE_DOCTOR));
        doctor = userRepository.save(doctor);
        doctorToken = generateToken(doctor);

        // Create patient
        User patient = new User("pat@test.com", "pass", "Jan", "Kowalski", Set.of(Role.ROLE_PATIENT));
        userRepository.save(patient);
        patientToken = generateToken(patient);
    }

    @Test
    void scenario_DoctorAddsSlot_ChecksConflict_AndPatientCannotDelete() throws Exception {
        // doctor added correct availability
        LocalDateTime start = LocalDateTime.now().plusDays(2).withHour(10).withMinute(0);
        LocalDateTime end = start.plusHours(4);

        CreateAvailabilityRequest request = new CreateAvailabilityRequest();
        request.setStartTime(start);
        request.setEndTime(end);

        mockMvc.perform(post("/api/availabilities")
                        .header("Authorization", "Bearer " + doctorToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        assertEquals(1, availabilityRepository.count()); // Check in base

        // doctor try added slot with conflict (11:00 - 12:00)
        CreateAvailabilityRequest conflictRequest = new CreateAvailabilityRequest();
        conflictRequest.setStartTime(start.plusHours(1));
        conflictRequest.setEndTime(start.plusHours(2));

        mockMvc.perform(post("/api/availabilities")
                        .header("Authorization", "Bearer " + doctorToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(conflictRequest)))
                .andExpect(status().isBadRequest()); // 400 Bad Request

        assertEquals(1, availabilityRepository.count()); // Only 1 slot

        // patient try to delete slot
        Long slotId = availabilityRepository.findAll().get(0).getId();

        mockMvc.perform(delete("/api/availabilities/" + slotId)
                        .header("Authorization", "Bearer " + patientToken))
                .andExpect(status().isForbidden()); // 403 Forbidden

        assertEquals(1, availabilityRepository.count()); // Slot is exists
    }

    private String generateToken(User user) {
        List<SimpleGrantedAuthority> authorities = user.getRoles().stream()
                .map(role -> new SimpleGrantedAuthority(role.name()))
                .toList();
        UserDetails userDetails = new org.springframework.security.core.userdetails.User(user.getEmail(), user.getPassword(), authorities);
        return jwtTokenService.generateToken(new UsernamePasswordAuthenticationToken(userDetails, null, authorities));
    }
}
