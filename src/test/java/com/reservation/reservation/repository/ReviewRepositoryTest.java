package com.reservation.reservation.repository;

import com.reservation.reservation.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
public class ReviewRepositoryTest {

    @Autowired private ReviewRepository reviewRepository;
    @Autowired private BookingRepository bookingRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private ServiceRepository serviceRepository;

    private Long doctorId;
    private Long bookingId;

    @BeforeEach
    void setUp() {
        User doctor = userRepository.save(new User("doc@t.com", "p", "D", "R", Set.of(Role.ROLE_DOCTOR)));
        doctorId = doctor.getId();
        User patient = userRepository.save(new User("pat@t.com", "p", "P", "T", Set.of(Role.ROLE_PATIENT)));
        Service service = serviceRepository.save(new Service("Test", 30, BigDecimal.TEN));

        Booking booking = Booking.builder()
                .doctor(doctor).patient(patient).service(service)
                .startTime(LocalDateTime.now().minusDays(1))
                .endTime(LocalDateTime.now().minusDays(1).plusMinutes(30))
                .status(BookingStatus.COMPLETED)
                .build();
        booking = bookingRepository.save(booking);
        bookingId = booking.getId();

        Review review = Review.builder()
                .rating(5)
                .comment("Super doktor")
                .createdAt(LocalDateTime.now())
                .booking(booking)
                .build();
        reviewRepository.save(review);
    }

    @Test
    void shouldFindReviewsByDoctorId() {
        List<Review> reviews = reviewRepository.findAllByDoctorId(doctorId);
        assertEquals(1, reviews.size());
        assertEquals(5, reviews.get(0).getRating());
    }

    @Test
    void shouldCheckIfReviewExistsForBooking() {
        assertTrue(reviewRepository.existsByBookingId(bookingId));
        assertFalse(reviewRepository.existsByBookingId(999L));
    }
}
