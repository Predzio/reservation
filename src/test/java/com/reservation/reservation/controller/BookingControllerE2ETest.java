package com.reservation.reservation.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.reservation.reservation.dto.request.CreateBookingRequest;
import com.reservation.reservation.model.Availability;
import com.reservation.reservation.model.Role;
import com.reservation.reservation.model.Service;
import com.reservation.reservation.model.User;
import com.reservation.reservation.repository.AvailabilityRepository;
import com.reservation.reservation.repository.BookingRepository;
import com.reservation.reservation.repository.ServiceRepository;
import com.reservation.reservation.repository.UserRepository;
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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class BookingControllerE2ETest {

    @Autowired
    private MockMvc mockMvc;
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
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private NotificationService notificationService;

    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
    private String patientToken;
    private Long doctorId;
    private Long serviceId;

    @BeforeEach
    void setUp() {
        bookingRepository.deleteAll(); availabilityRepository.deleteAll();
        serviceRepository.deleteAll(); userRepository.deleteAll();

        User doc = userRepository.save(new User("doc@t.com", passwordEncoder.encode("p"), "D", "R", Set.of(Role.ROLE_DOCTOR)));
        doctorId = doc.getId();
        User pat = userRepository.save(new User("pat@t.com", passwordEncoder.encode("p"), "P", "T", Set.of(Role.ROLE_PATIENT)));
        patientToken = generateToken(pat);
        Service s = serviceRepository.save(new Service("Wizyta kardiologiczna", 30, BigDecimal.TEN));
        serviceId = s.getId();

        // Doctor available at 10:00-14:00
        LocalDateTime start = LocalDateTime.now().plusDays(1).withHour(10).withMinute(0).withSecond(0).withNano(0);
        Availability avail = new Availability(start, start.plusHours(4), doc);
        availabilityRepository.save(avail);
    }

    @Test
    void fullBookingProcess() throws Exception {
        // Patient want apointment tommorow at 10:00
        LocalDateTime bookingTime = LocalDateTime.now().plusDays(1).withHour(10).withMinute(0).withSecond(0).withNano(0);

        CreateBookingRequest request = new CreateBookingRequest();
        request.setDoctorId(doctorId);
        request.setServiceId(serviceId);
        request.setStartTime(bookingTime);

        mockMvc.perform(post("/api/bookings")
                        .header("Authorization", "Bearer " + patientToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.serviceName").value("Wizyta kardiologiczna"))
                .andExpect(jsonPath("$.doctor.id").value(doctorId));

        // Check in base
        assertEquals(1, bookingRepository.count());

        verify(notificationService).sendBookingConfirmation(any());
    }

    private String generateToken(User user) {
        var authorities = user.getRoles().stream().map(r -> new SimpleGrantedAuthority(r.name())).toList();
        UserDetails u = new org.springframework.security.core.userdetails.User(user.getEmail(), user.getPassword(), authorities);
        return jwtTokenService.generateToken(new UsernamePasswordAuthenticationToken(u, null, authorities));
    }
}
