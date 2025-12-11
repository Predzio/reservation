package com.reservation.reservation.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.reservation.reservation.dto.request.CreateMedicalRecordRequest;
import com.reservation.reservation.dto.response.MedicalRecordDTO;
import com.reservation.reservation.security.AuthEntryPointJwt;
import com.reservation.reservation.security.JwtRequestFilter;
import com.reservation.reservation.security.JwtTokenService;
import com.reservation.reservation.security.UserDetailsServiceImpl;
import com.reservation.reservation.service.MedicalRecordService;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(MedicalRecordController.class)
@AutoConfigureMockMvc(addFilters = false)
public class MedicalControllerTest {
    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private MedicalRecordService medicalRecordService;

    @MockitoBean
    private JwtTokenService jwtTokenService;

    @MockitoBean
    private UserDetailsServiceImpl userDetailsService;

    @MockitoBean
    private JwtRequestFilter jwtRequestFilter;

    @MockitoBean
    private AuthEntryPointJwt authEntryPointJwt;

    @Test
    @WithMockUser(username = "doc@test.com", authorities = "ROLE_DOCTOR")
    void shouldCreateMedicalRecord() throws Exception {
        CreateMedicalRecordRequest request = new CreateMedicalRecordRequest();
        request.setDiagnosis("OK");

        when(medicalRecordService.createMedicalRecord(eq(1L), any(), anyString()))
                .thenReturn(MedicalRecordDTO.builder().id(1L).diagnosis("OK").build());

        mockMvc.perform(post("/api/medical-records/booking/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.diagnosis").value("OK"));
    }

    @Test
    @WithMockUser(username = "pat@test.com", authorities = "ROLE_PATIENT")
    void shouldGetMyHistory() throws Exception {
        when(medicalRecordService.getMyMedicalHistory("pat@test.com"))
                .thenReturn(List.of(MedicalRecordDTO.builder().diagnosis("Grypa").build()));

        mockMvc.perform(get("/api/medical-records/my-history"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].diagnosis").value("Grypa"));
    }
}
