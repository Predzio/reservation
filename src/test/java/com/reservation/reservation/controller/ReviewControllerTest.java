package com.reservation.reservation.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.reservation.reservation.dto.request.CreateReviewRequest;
import com.reservation.reservation.dto.response.ReviewDTO;
import com.reservation.reservation.security.AuthEntryPointJwt;
import com.reservation.reservation.security.JwtRequestFilter;
import com.reservation.reservation.security.JwtTokenService;
import com.reservation.reservation.security.UserDetailsServiceImpl;
import com.reservation.reservation.service.ReviewService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ReviewController.class)
@AutoConfigureMockMvc(addFilters = false)
class ReviewControllerTest {

    @Autowired
    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean private ReviewService reviewService;
    @MockitoBean private JwtTokenService jwtTokenService;
    @MockitoBean private UserDetailsServiceImpl userDetailsService;
    @MockitoBean private JwtRequestFilter jwtRequestFilter;
    @MockitoBean private AuthEntryPointJwt authEntryPointJwt;

    @Test
    @WithMockUser(username = "pat@test.com", authorities = "ROLE_PATIENT")
    void shouldAddReview() throws Exception {
        CreateReviewRequest request = new CreateReviewRequest();
        request.setRating(5);
        request.setComment("Super");

        when(reviewService.addReview(eq(1L), any(), anyString()))
                .thenReturn(ReviewDTO.builder().rating(5).build());

        mockMvc.perform(post("/api/reviews/booking/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    void shouldGetDoctorReviews() throws Exception {
        when(reviewService.getDoctorReviews(1L))
                .thenReturn(List.of(ReviewDTO.builder().rating(5).comment("Wow").build()));

        mockMvc.perform(get("/api/reviews/doctor/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].comment").value("Wow"));
    }
}
