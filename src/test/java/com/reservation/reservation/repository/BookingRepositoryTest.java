package com.reservation.reservation.repository;

import com.reservation.reservation.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
class BookingRepositoryTest {

    @Autowired
    private BookingRepository bookingRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ServiceRepository serviceRepository;

    private Long doctorId;

    @BeforeEach
    void setUp() {
        // Setup Doctor, Patient, Service
        User doctor = userRepository.save(new User("doctor3@test.com", "p", "Daniel", "Rot", Set.of(Role.ROLE_DOCTOR)));
        doctorId = doctor.getId();
        User patient = userRepository.save(new User("patient3@test.com", "p", "Patrick", "Track", Set.of(Role.ROLE_PATIENT)));
        Service service = serviceRepository.save(new Service("Test", 30, BigDecimal.TEN));

        // Add one reservation: 10:00 - 10:30
        Booking booking = new Booking(null,
                LocalDateTime.of(2025, 12, 1, 10, 0),
                LocalDateTime.of(2025, 12, 1, 10, 30),
                patient, doctor, service, BookingStatus.CONFIRMED);
        bookingRepository.save(booking);
    }

    @Test
    void shouldDetectOverlappingBooking() {
        // Scenario 1 - reservation 10:15 - 10:45 (overlaps)
        boolean overlap = bookingRepository.existsOverlappingBooking(doctorId,
                LocalDateTime.of(2025, 12, 1, 10, 15),
                LocalDateTime.of(2025, 12, 1, 10, 45));

        assertTrue(overlap);
    }

    @Test
    void shouldNotDetectOverlapWhenBookingIsCancelled() {
        // Cancell exist reservation
        Booking booking = bookingRepository.findAll().get(0);
        booking.setStatus(BookingStatus.CANCELLED);
        bookingRepository.save(booking);

        // Check the same date
        boolean overlap = bookingRepository.existsOverlappingBooking(doctorId,
                LocalDateTime.of(2025, 12, 1, 10, 0),
                LocalDateTime.of(2025, 12, 1, 10, 30));

        assertFalse(overlap, "Cancelled reservation shouldn't blocked the date");
    }
}
