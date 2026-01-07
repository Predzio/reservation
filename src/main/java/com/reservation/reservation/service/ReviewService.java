package com.reservation.reservation.service;

import com.reservation.reservation.dto.request.CreateReviewRequest;
import com.reservation.reservation.dto.response.ReviewDTO;
import com.reservation.reservation.exception.BusinessException;
import com.reservation.reservation.exception.ResourceNotFoundException;
import com.reservation.reservation.model.Booking;
import com.reservation.reservation.model.BookingStatus;
import com.reservation.reservation.model.Review;
import com.reservation.reservation.repository.BookingRepository;
import com.reservation.reservation.repository.ReviewRepository;
import com.reservation.reservation.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;

    @Transactional
    public ReviewDTO addReview(Long bookingId, CreateReviewRequest request, String patientEmail) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment not found"));

        if (!booking.getPatient().getEmail().equals(patientEmail)) {
            throw new BusinessException("You cannot evaluate someone else's visit", HttpStatus.FORBIDDEN);
        }

        if (booking.getStatus() != BookingStatus.COMPLETED) {
            throw new BusinessException("You can only rate a completed visit", HttpStatus.BAD_REQUEST);
        }

        if (reviewRepository.existsByBookingId(bookingId)) {
            throw new BusinessException("This visit has already been rated", HttpStatus.CONFLICT);
        }

        Review review = Review.builder()
                .rating(request.getRating())
                .comment((request.getComment()))
                .createdAt(LocalDateTime.now())
                .booking(booking)
                .build();

        Review saved = reviewRepository.save(review);
        return mapToDTO(saved);
    }

    public List<ReviewDTO> getDoctorReviews(Long doctorId) {
        if(!userRepository.existsById(doctorId)) {
            throw new ResourceNotFoundException("Doctor not exists");
        }

        return reviewRepository.findAllByDoctorId(doctorId).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    private ReviewDTO mapToDTO(Review review) {
        String firstName = review.getBooking().getPatient().getFirstName();
        String lastNameInitial = review.getBooking().getPatient().getLastName().charAt(0) + ".";

        return ReviewDTO.builder()
                .id(review.getId())
                .rating(review.getRating())
                .comment(review.getComment())
                .patientName(firstName + " " + lastNameInitial)
                .createdAt(review.getCreatedAt())
                .build();
    }
}
