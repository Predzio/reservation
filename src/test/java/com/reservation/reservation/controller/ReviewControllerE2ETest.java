package com.reservation.reservation.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.reservation.reservation.dto.request.CreateReviewRequest;
import com.reservation.reservation.model.*;
import com.reservation.reservation.repository.*;
import com.reservation.reservation.security.JwtTokenService;
import com.reservation.reservation.service.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ReviewControllerE2ETest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ReviewRepository reviewRepository;
    @Autowired private MedicalRecordRepository medicalRecordRepository;
    @Autowired private BookingRepository bookingRepository;
    @Autowired private AvailabilityRepository availabilityRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private ServiceRepository serviceRepository;
    @Autowired private JwtTokenService jwtTokenService;

    @MockitoBean private NotificationService notificationService; // Mock maila

    private final ObjectMapper objectMapper = new ObjectMapper();

    private Long bookingId;
    private Long doctorId;
    private String patientToken;

    @BeforeEach
    void setUp() {
        reviewRepository.deleteAll();
        medicalRecordRepository.deleteAll();
        bookingRepository.deleteAll();
        availabilityRepository.deleteAll();
        userRepository.deleteAll();
        serviceRepository.deleteAll();

        // Aktorzy
        User doc = userRepository.save(new User("doc@t.com", "p", "D", "R", Set.of(Role.ROLE_DOCTOR)));
        doctorId = doc.getId();
        User pat = userRepository.save(new User("pat@t.com", "p", "P", "T", Set.of(Role.ROLE_PATIENT)));
        patientToken = generateToken(pat);

        Service s = serviceRepository.save(new Service("Test", 30, BigDecimal.TEN));

        Booking booking = Booking.builder()
                .doctor(doc).patient(pat).service(s)
                .startTime(LocalDateTime.now().minusDays(1))
                .endTime(LocalDateTime.now().minusDays(1).plusMinutes(30))
                .status(BookingStatus.COMPLETED)
                .build();
        booking = bookingRepository.save(booking);
        bookingId = booking.getId();
    }

    @Test
    void fullReviewScenario() throws Exception {
        CreateReviewRequest request = new CreateReviewRequest();
        request.setRating(5);
        request.setComment("The best doctor!");

        mockMvc.perform(post("/api/reviews/booking/" + bookingId)
                        .header("Authorization", "Bearer " + patientToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.patientName").value("P T."));

        mockMvc.perform(get("/api/reviews/doctor/" + doctorId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(1))
                .andExpect(jsonPath("$[0].comment").value("The best doctor!"));
    }

    private String generateToken(User user) {
        var authorities = user.getRoles().stream().map(r -> new SimpleGrantedAuthority(r.name())).toList();
        UserDetails u = new org.springframework.security.core.userdetails.User(user.getEmail(), "pass", authorities);
        return jwtTokenService.generateToken(new UsernamePasswordAuthenticationToken(u, null, authorities));
    }
}
