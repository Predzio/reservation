package com.reservation.reservation.controller;

import com.reservation.reservation.dto.request.RegisterRequest;
import com.reservation.reservation.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    // Będziesz tu potrzebował też AuthenticationManager do logowania

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody RegisterRequest registerRequest) {
        userService.registerNewPatient(registerRequest);
        return ResponseEntity.ok("User registered successfully as PATIENT");
    }

    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser() {
        // In the future adding the logic JWT
        return ResponseEntity.ok("Login endpoint - implement me!");
    }
}
