package com.reservation.reservation.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.reservation.reservation.dto.request.CreateBookingRequest;
import com.reservation.reservation.dto.response.BookingDTO;
import com.reservation.reservation.model.BookingStatus;
import com.reservation.reservation.security.JwtRequestFilter;
import com.reservation.reservation.security.JwtTokenService;
import com.reservation.reservation.security.UserDetailsServiceImpl;
import com.reservation.reservation.service.BookingService;
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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BookingController.class)
@AutoConfigureMockMvc(addFilters = false)
class BookingControllerTest {

    @Autowired
    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @MockitoBean
    private BookingService bookingService;
    @MockitoBean
    private JwtTokenService jwtTokenService;
    @MockitoBean
    private UserDetailsServiceImpl userDetailsService;
    @MockitoBean
    private JwtRequestFilter jwtRequestFilter;

    @Test
    @WithMockUser(username = "patient@test.com", authorities = "ROLE_PATIENT")
    void shouldReturnBookingDTO() throws Exception {
        CreateBookingRequest request = new CreateBookingRequest();
        request.setStartTime(LocalDateTime.now().plusDays(1));

        BookingDTO responseDTO = BookingDTO.builder()
                .id(99L)
                .status(BookingStatus.CONFIRMED)
                .build();

        when(bookingService.createBooking(any(), anyString())).thenReturn(responseDTO);

        mockMvc.perform(post("/api/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(99))
                .andExpect(jsonPath("$.status").value("CONFIRMED"));
    }
}
