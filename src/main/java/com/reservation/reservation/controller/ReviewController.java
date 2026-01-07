package com.reservation.reservation.controller;

import com.reservation.reservation.dto.request.CreateReviewRequest;
import com.reservation.reservation.dto.response.ReviewDTO;
import com.reservation.reservation.service.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private ReviewService reviewService;

    @PostMapping("/booking/{bookingId}")
    @PreAuthorize("hasAuthority('ROLE_PATIENT')")
    public ResponseEntity<ReviewDTO> addReview(
            @PathVariable Long bookingId,
            @RequestBody @Valid CreateReviewRequest request) {

        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return ResponseEntity.ok(reviewService.addReview(bookingId, request, email));
    }

    @GetMapping("/doctor/{doctorId}")
    public ResponseEntity<List<ReviewDTO>> getDoctorReviews(@PathVariable Long doctorId) {
        return ResponseEntity.ok(reviewService.getDoctorReviews(doctorId));
    }
}
