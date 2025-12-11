package com.reservation.reservation.repository;

import com.reservation.reservation.model.MedicalRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MedicalRecordRepository extends JpaRepository<MedicalRecord, Long> {
    // Get the entire treatment history for a given patient
    List<MedicalRecord> findAllByBooking_Patient_IdOrderByCreatedAtDesc(Long patientId);

    // Check whether a description already exists for the visit
    Optional<MedicalRecord> findByBookingId(Long bookingId);

}
