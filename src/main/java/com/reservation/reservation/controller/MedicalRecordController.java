package com.reservation.reservation.controller;

import com.reservation.reservation.dto.request.CreateMedicalRecordRequest;
import com.reservation.reservation.dto.response.MedicalRecordDTO;
import com.reservation.reservation.service.MedicalRecordService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/medical-records")
@RequiredArgsConstructor
public class MedicalRecordController {

    private final MedicalRecordService medicalRecordService;

    @PostMapping("/booking/{bookingId}")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<MedicalRecordDTO> createRecord(
            @PathVariable Long bookingId,
            @RequestBody @Valid CreateMedicalRecordRequest request) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        return ResponseEntity.ok(medicalRecordService.createMedicalRecord(bookingId, request, email));
    }

    @GetMapping("/my-history")
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<List<MedicalRecordDTO>> getMyHistory() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        return ResponseEntity.ok(medicalRecordService.getMyMedicalHistory(email));
    }
}
