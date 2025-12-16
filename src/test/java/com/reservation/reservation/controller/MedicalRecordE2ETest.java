package com.reservation.reservation.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.reservation.reservation.dto.request.CreateMedicalRecordRequest;
import com.reservation.reservation.model.*;
import com.reservation.reservation.repository.*;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;



@SpringBootTest
@AutoConfigureMockMvc
class MedicalRecordE2ETest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private MedicalRecordRepository medicalRecordRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private AvailabilityRepository availabilityRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ServiceRepository serviceRepository;

    @Autowired
    private JwtTokenService jwtTokenService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private Long bookingId;
    private String doctorToken;
    private String patientToken;

    @BeforeEach
    void setUp() {
        medicalRecordRepository.deleteAll();
        bookingRepository.deleteAll();
        availabilityRepository.deleteAll();
        userRepository.deleteAll();
        serviceRepository.deleteAll();

        User doc = userRepository.save(new User("doc@t.com", "p", "D", "R", Set.of(Role.ROLE_DOCTOR)));
        User pat = userRepository.save(new User("pat@t.com", "p", "P", "T", Set.of(Role.ROLE_PATIENT)));

        doctorToken = generateToken(doc);
        patientToken = generateToken(pat);

        Service s = serviceRepository.save(new Service("Test", 30, BigDecimal.TEN));

        Booking booking = Booking.builder()
                .doctor(doc).patient(pat).service(s)
                .startTime(LocalDateTime.now().minusHours(1))
                .endTime(LocalDateTime.now().minusMinutes(30))
                .status(BookingStatus.CONFIRMED)
                .build();

        booking = bookingRepository.save(booking);
        bookingId = booking.getId();
    }

    @Test
    void fullScenario_DoctorCompletesVisit_PatientSeesHistory() throws Exception {
        CreateMedicalRecordRequest request = new CreateMedicalRecordRequest();
        request.setDiagnosis("Zapalenie płuc");
        request.setTreatment("Antybiotyk");
        request.setRecommendations("Leżeć");

        mockMvc.perform(post("/api/medical-records/booking/" + bookingId)
                        .header("Authorization", "Bearer " + doctorToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.diagnosis").value("Zapalenie płuc"));

        assertEquals(1, medicalRecordRepository.count());
        Booking updatedBooking = bookingRepository.findById(bookingId).get();
        assertEquals(BookingStatus.COMPLETED, updatedBooking.getStatus());

        mockMvc.perform(get("/api/medical-records/my-history")
                        .header("Authorization", "Bearer " + patientToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].diagnosis").value("Zapalenie płuc"))
                .andExpect(jsonPath("$[0].doctorName").exists());
    }

    private String generateToken(User user) {
        var authorities = user.getRoles().stream().map(r -> new SimpleGrantedAuthority(r.name())).toList();
        UserDetails u = new org.springframework.security.core.userdetails.User(user.getEmail(), "pass", authorities);
        return jwtTokenService.generateToken(new UsernamePasswordAuthenticationToken(u, null, authorities));
    }
}
