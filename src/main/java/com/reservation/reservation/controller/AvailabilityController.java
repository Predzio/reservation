package com.reservation.reservation.controller;

import com.reservation.reservation.dto.request.CreateAvailabilityRequest;
import com.reservation.reservation.model.Availability;
import com.reservation.reservation.service.AvailabilityService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/availabilities")
@RequiredArgsConstructor
public class AvailabilityController {

    private AvailabilityService availabilityService;

    @PostMapping
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<?> createAvailability(@RequestBody CreateAvailabilityRequest request) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        try {
            Availability created = availabilityService.createAvailability(request, email);
            return ResponseEntity.ok(created);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/doctor/{doctorId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<Availability>> getDoctorSchedule(@PathVariable Long doctorId) {
        return ResponseEntity.ok(availabilityService.getDoctorAvailability(doctorId));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<?> deleteAvailability(@PathVariable Long id) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        availabilityService.deleteAvailability(id, email);
        return ResponseEntity.ok("Slot deleted");
    }

}
