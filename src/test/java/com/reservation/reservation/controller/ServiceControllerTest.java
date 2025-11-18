package com.reservation.reservation.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.reservation.reservation.dto.request.CreateServiceRequest;
import com.reservation.reservation.model.Service;
import com.reservation.reservation.security.JwtRequestFilter;
import com.reservation.reservation.security.JwtTokenService;
import com.reservation.reservation.security.UserDetailsServiceImpl;
import com.reservation.reservation.service.ServiceService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import java.math.BigDecimal;
import java.util.List;

import static org.mockito.Mockito.when;

@WebMvcTest(ServiceController.class)
@AutoConfigureMockMvc(addFilters = false)
public class ServiceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private ServiceService serviceService;

    @MockitoBean
    private JwtTokenService jwtTokenService;

    @MockitoBean
    private UserDetailsServiceImpl userDetailsService;

    @MockitoBean
    private JwtRequestFilter jwtRequestFilter;

    @Test
    void shouldGetActiveServicesPublicly() throws Exception {
        // Given
        Service service = new Service("Konsultacja xyz", 30, BigDecimal.valueOf(160));
        // Symulujemy odpowiedź serwisu
        when(serviceService.getAllActiveServices()).thenReturn(List.of(service));

        // When & Then
        mockMvc.perform(get("/api/services")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()) // Spodziewamy się 200 OK
                .andExpect(jsonPath("$[0].name").value("Konsultacja xyz")); // Teraz to zadziała!
    }

    @Test
    void shouldCreateService() throws Exception {
        // Given
        CreateServiceRequest request = new CreateServiceRequest();
        request.setName("Nowa Usługa");
        request.setDurationMinutes(45);
        request.setPrice(BigDecimal.valueOf(200));

        Service createdService = new Service("Nowa Usługa", 45, BigDecimal.valueOf(200));

        // Symulujemy, że serwis zwraca utworzony obiekt
        when(serviceService.createService(any(CreateServiceRequest.class))).thenReturn(createdService);

        // When & Then
        mockMvc.perform(post("/api/services")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Nowa Usługa"));
    }
}
