package com.reservation.reservation.service;

import com.reservation.reservation.dto.request.CreateReviewRequest;
import com.reservation.reservation.dto.response.ReviewDTO;
import com.reservation.reservation.exception.BusinessException;
import com.reservation.reservation.model.Booking;
import com.reservation.reservation.model.BookingStatus;
import com.reservation.reservation.model.Review;
import com.reservation.reservation.model.User;
import com.reservation.reservation.repository.BookingRepository;
import com.reservation.reservation.repository.ReviewRepository;
import com.reservation.reservation.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReviewServiceTest {

    @Mock private ReviewRepository reviewRepository;
    @Mock private BookingRepository bookingRepository;
    @Mock private UserRepository userRepository;

    @InjectMocks private ReviewService reviewService;

    @Test
    void shouldAddReviewSuccessfully() {
        Long bookingId = 1L;
        String patientEmail = "jan@test.com";

        User patient = new User();
        patient.setEmail(patientEmail);
        patient.setFirstName("Jan");
        patient.setLastName("Kowalski");

        Booking booking = Booking.builder()
                .id(bookingId)
                .patient(patient)
                .status(BookingStatus.COMPLETED)
                .build();

        CreateReviewRequest request = new CreateReviewRequest();
        request.setRating(5);
        request.setComment("Extra");

        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));
        when(reviewRepository.existsByBookingId(bookingId)).thenReturn(false);

        when(reviewRepository.save(any(Review.class))).thenAnswer(i -> {
            Review r = i.getArgument(0);
            r.setId(10L);
            return r;
        });

        ReviewDTO result = reviewService.addReview(bookingId, request, patientEmail);


        assertNotNull(result);
        assertEquals(5, result.getRating());
        assertEquals("Jan K.", result.getPatientName()); // Check anonymize the name
    }

    @Test
    void shouldThrowWhenUserIsNotThePatient() {
        User patient = new User(); patient.setEmail("jan@test.com");
        Booking booking = Booking.builder().patient(patient).build();

        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));

        BusinessException ex = assertThrows(BusinessException.class, () ->
                reviewService.addReview(1L, new CreateReviewRequest(), "hacker@test.com")
        );
        assertEquals(HttpStatus.FORBIDDEN, ex.getHttpStatus());
    }

    @Test
    void shouldThrowWhenBookingIsNotCompleted() {
        User patient = new User();
        patient.setEmail("jan@test.com");

        Booking booking = Booking.builder()
                .patient(patient)
                .status(BookingStatus.CONFIRMED) // Not completed
                .build();

        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));

        BusinessException ex = assertThrows(BusinessException.class, () ->
                reviewService.addReview(1L, new CreateReviewRequest(), "jan@test.com")
        );
        assertEquals("You can only rate a completed visit", ex.getMessage());
    }

    @Test
    void shouldThrowWhenReviewAlreadyExists() {
        User patient = new User();
        patient.setEmail("jan@test.com");

        Booking booking = Booking.builder()
                .status(BookingStatus.COMPLETED)
                .patient(patient).build();

        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));
        when(reviewRepository.existsByBookingId(1L)).thenReturn(true);

        BusinessException ex = assertThrows(BusinessException.class, () ->
                reviewService.addReview(1L, new CreateReviewRequest(), "jan@test.com")
        );
        assertEquals(HttpStatus.CONFLICT, ex.getHttpStatus());
    }
}