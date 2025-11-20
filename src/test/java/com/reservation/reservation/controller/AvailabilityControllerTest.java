package com.reservation.reservation.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.reservation.reservation.dto.request.CreateAvailabilityRequest;
import com.reservation.reservation.model.Availability;
import com.reservation.reservation.security.JwtRequestFilter;
import com.reservation.reservation.security.JwtTokenService;
import com.reservation.reservation.security.UserDetailsServiceImpl;
import com.reservation.reservation.service.AvailabilityService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AvailabilityController.class)
@AutoConfigureMockMvc(addFilters = false) // Off security
class AvailabilityControllerTest {

    @Autowired private MockMvc mockMvc;

    // ObjectMapper with (LocalDateTime)
    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @MockitoBean
    private AvailabilityService availabilityService;
    @MockitoBean
    private JwtTokenService jwtTokenService;
    @MockitoBean
    private UserDetailsServiceImpl userDetailsService;
    @MockitoBean
    private JwtRequestFilter jwtRequestFilter;

    @Test
    @WithMockUser(username = "doctor@test.com", authorities = "ROLE_DOCTOR")
    void shouldCreateAvailability() throws Exception {
        CreateAvailabilityRequest request = new CreateAvailabilityRequest();
        request.setStartTime(LocalDateTime.now().plusDays(1));
        request.setEndTime(LocalDateTime.now().plusDays(1).plusHours(4));

        when(availabilityService.createAvailability(any(), anyString())).thenReturn(new Availability());
        
        mockMvc.perform(post("/api/availabilities")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }
}
