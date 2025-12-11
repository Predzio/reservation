package com.reservation.reservation.controller;

import com.reservation.reservation.dto.request.CreateBookingRequest;
import com.reservation.reservation.dto.response.BookingDTO;
import com.reservation.reservation.model.Booking;
import com.reservation.reservation.service.BookingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
public class BookingController {
    private final BookingService bookingService;

    @GetMapping("/doctor-schedule")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<List<BookingDTO>> getDoctorBookings() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return ResponseEntity.ok(bookingService.getDoctorBookings(email));
    }

    @PatchMapping("/{id}/cancel")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> cancelBooking(@PathVariable Long id) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        bookingService.cancelBooking(id, email);

        return ResponseEntity.ok("Reservation has been cancelled");
    }

    @PostMapping
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<?> createBooking(@RequestBody CreateBookingRequest request) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        BookingDTO bookingDTO = bookingService.createBooking(request, email);

        return ResponseEntity.ok(bookingDTO);
    }

    @GetMapping("/my")
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<List<BookingDTO>> getMyBookings() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        return  ResponseEntity.ok(bookingService.getMyBookings(email));
    }

































}
