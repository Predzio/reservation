package com.reservation.reservation.controller;

import com.reservation.reservation.dto.request.CreateDoctorRequest;
import com.reservation.reservation.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @PostMapping("/doctors")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> createDoctor(@RequestBody CreateDoctorRequest createDoctorRequest) {
        userService.createNewDoctor(createDoctorRequest);
        return ResponseEntity.ok("User registered successfully as DOCTOR");
    }
}

