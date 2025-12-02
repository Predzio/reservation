package com.reservation.reservation.controller;

import com.reservation.reservation.model.*;
import com.reservation.reservation.repository.*;
import com.reservation.reservation.security.JwtTokenService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.web.servlet.MockMvc;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class BookingCancelE2ETest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private BookingRepository bookingRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ServiceRepository serviceRepository;
    @Autowired
    private AvailabilityRepository availabilityRepository;
    @Autowired
    private JwtTokenService jwtTokenService;

    private Long bookingId;
    private String patientToken;
    private String doctorToken;
    private String hackerToken;

    @BeforeEach
    void setUp() {
        bookingRepository.deleteAll();
        availabilityRepository.deleteAll();
        userRepository.deleteAll();
        serviceRepository.deleteAll();

        User doc = userRepository.save(new User("doc@t.com", "p", "D", "R", Set.of(Role.ROLE_DOCTOR)));
        User pat = userRepository.save(new User("pat@t.com", "p", "P", "T", Set.of(Role.ROLE_PATIENT)));
        User hacker = userRepository.save(new User("hacker@t.com", "p", "H", "K", Set.of(Role.ROLE_PATIENT)));

        doctorToken = generateToken(doc);
        patientToken = generateToken(pat);
        hackerToken = generateToken(hacker);

        Service s = serviceRepository.save(new Service("Test", 30, BigDecimal.TEN));

        Booking booking = Booking.builder()
                .doctor(doc).patient(pat).service(s)
                .startTime(LocalDateTime.now().plusDays(5)) // Przyszłość
                .endTime(LocalDateTime.now().plusDays(5).plusMinutes(30))
                .status(BookingStatus.CONFIRMED)
                .build();

        booking = bookingRepository.save(booking);
        bookingId = booking.getId();
    }

    @Test
    void patientCanCancelOwnBooking() throws Exception {
        mockMvc.perform(patch("/api/bookings/" + bookingId + "/cancel")
                        .header("Authorization", "Bearer " + patientToken))
                .andExpect(status().isOk());

        Booking b = bookingRepository.findById(bookingId).get();
        assertEquals(BookingStatus.CANCELLED, b.getStatus());
    }

    @Test
    void doctorCanCancelPatientBooking() throws Exception {
        mockMvc.perform(patch("/api/bookings/" + bookingId + "/cancel")
                        .header("Authorization", "Bearer " + doctorToken))
                .andExpect(status().isOk());

        Booking b = bookingRepository.findById(bookingId).get();
        assertEquals(BookingStatus.CANCELLED, b.getStatus());
    }

    @Test
    void hackerCannotCancelSomeoneElseBooking() throws Exception {
        mockMvc.perform(patch("/api/bookings/" + bookingId + "/cancel")
                        .header("Authorization", "Bearer " + hackerToken))
                .andExpect(status().isBadRequest());

        Booking b = bookingRepository.findById(bookingId).get();
        assertEquals(BookingStatus.CONFIRMED, b.getStatus());
    }

    private String generateToken(User user) {
        var authorities = user.getRoles().stream().map(r -> new SimpleGrantedAuthority(r.name())).toList();
        UserDetails u = new org.springframework.security.core.userdetails.User(user.getEmail(), "pass", authorities);
        return jwtTokenService.generateToken(new UsernamePasswordAuthenticationToken(u, null, authorities));
    }
}
