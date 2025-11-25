package com.reservation.reservation.repository;

import com.reservation.reservation.model.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    // List of reservations for patient (history)
    List<Booking> findAllByPatientIdOrderByStartTimeDesc(Long patientId);

    // List of reservations for doctor
    List<Booking> findAllByDoctorIdAndStartTimeAfterOrderByStartTimeAsc(Long doctor);

    // Check conflicts
    @Query("""
            SELECT CASE WHEN COUNT(b) > 0 THEN true ELSE false END
            FROM Booking b
            WHERE b.doctor.id = :doctorId
                AND b.status != 'CANCELLED'
                AND (b.startTime < :endTime AND b.endTime > :startTime)
    """)
    boolean existsOverlappingBooking(
            @Param("doctorId") Long doctorId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime
    );


}
