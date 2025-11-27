package com.reservation.reservation.controller;

import com.reservation.reservation.dto.request.CreateDoctorRequest;
import com.reservation.reservation.dto.response.DoctorDTO;
import com.reservation.reservation.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @GetMapping("/doctors")
    public ResponseEntity<List<DoctorDTO>> getAllDoctors() {
        return ResponseEntity.ok(userService.getAllDoctors());
    }

    @PostMapping("/doctors")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> createDoctor(@RequestBody CreateDoctorRequest createDoctorRequest) {
        userService.createNewDoctor(createDoctorRequest);
        return ResponseEntity.ok("User registered successfully as DOCTOR");
    }

    @GetMapping("/me")
    @PreAuthorize("hasRole('PATIENT') or hasRole('DOCTOR') or hasRole('ADMIN')") // Dostęp dla każdego zalogowanego
    public ResponseEntity<?> getMyProfile() {
        // Filtr JWT już ustawił autentykację, więc możemy ją stąd pobrać
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();

        // Możesz tu zwrócić DTO z danymi użytkownika (bez hasła!)
        // Na razie zwrócimy sam email dla testu
        return ResponseEntity.ok("Jesteś zalogowany jako: " + userDetails.getUsername());
    }
}

