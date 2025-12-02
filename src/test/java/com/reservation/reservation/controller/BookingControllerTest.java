package com.reservation.reservation.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.reservation.reservation.dto.request.CreateBookingRequest;
import com.reservation.reservation.dto.response.BookingDTO;
import com.reservation.reservation.model.BookingStatus;
import com.reservation.reservation.security.AuthEntryPointJwt;
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
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

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
    @MockitoBean
    private AuthEntryPointJwt authEntryPointJwt;

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

    @Test
    @WithMockUser(username = "doc@test.com", authorities = "ROLE_DOCTOR")
    void shouldReturnDoctorSchedule() throws Exception {
        BookingDTO dto = BookingDTO.builder().id(1L).status(BookingStatus.CONFIRMED).build();

        when(bookingService.getDoctorBookings("doc@test.com")).thenReturn(List.of(dto));

        mockMvc.perform(get("/api/bookings/doctor-schedule"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].status").value("CONFIRMED"));
    }

    @Test
    @WithMockUser(username = "pat@test.com")
    void shouldCancelBookingSuccessfully() throws Exception {
        Long bookingId = 123L;

        doNothing().when(bookingService).cancelBooking(eq(bookingId), eq("pat@test.com"));

        mockMvc.perform(patch("/api/bookings/" + bookingId + "/cancel"))
                .andExpect(status().isOk())
                .andExpect(content().string("Reservation has been cancelled"));
    }

    @Test
    @WithMockUser(username = "hacker@test.com")
    void shouldReturnBadRequestWhenServiceThrowsException() throws Exception {
        Long bookingId = 123L;

        doThrow(new RuntimeException("You don't have permission!"))
                .when(bookingService).cancelBooking(eq(bookingId), anyString());

        mockMvc.perform(patch("/api/bookings/" + bookingId + "/cancel"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("You don't have permission!"));
    }
}
