package com.reservation.reservation.repository;

import com.reservation.reservation.model.Availability;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface AvailabilityRepository extends JpaRepository<Availability, Long> {
    List<Availability> findAllByDoctorIdAndStartTimeAfterOrderByStartTimeAsc(Long doctorId, LocalDateTime now);

    @Query("""
            SELECT CASE WHEN COUNT(a) > 0 THEN true ELSE false END
            FROM Availability a
            WHERE a.doctor.id = :doctorId
                AND (a.startTime < :endTime AND a.endTime > :startTime)
    """)
    boolean existsOverlappingSlot(
            @Param("doctorId") Long doctorId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime
    );




}
