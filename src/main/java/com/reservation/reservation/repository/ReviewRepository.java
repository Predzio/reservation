package com.reservation.reservation.repository;

import com.reservation.reservation.model.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    @Query("SELECT r FROM Review r WHERE r.booking.doctor.id = :doctorId ORDER BY r.createdAt DESC")
    List<Review> findAllByDoctorId(@Param("doctorId") Long doctortId);

    boolean existsByBookingId(Long bookingId);
}
