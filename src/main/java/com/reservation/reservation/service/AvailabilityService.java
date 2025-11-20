package com.reservation.reservation.service;

import com.reservation.reservation.dto.request.CreateAvailabilityRequest;
import com.reservation.reservation.model.Availability;
import com.reservation.reservation.model.User;
import com.reservation.reservation.repository.AvailabilityRepository;
import com.reservation.reservation.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AvailabilityService {

    private final AvailabilityRepository availabilityRepository;
    private final UserRepository userRepository;

    @Transactional
    public Availability createAvailability(CreateAvailabilityRequest request, String doctorEmail) {
        // Validate date
        if (request.getStartTime().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Couldn't add availability in the past");
        }
        if (request.getEndTime().isBefore(request.getStartTime())) {
            throw new IllegalArgumentException("End date must be after begin date");
        }

        // Get doctor
        User doctor = userRepository.findByEmail(doctorEmail)
                .orElseThrow(() -> new RuntimeException("Doctor not found"));

        // Validate conflicts
        boolean overlap = availabilityRepository.existsOverlappingSlot(
                doctor.getId(), request.getStartTime(), request.getEndTime());

        if (overlap) {
            throw new IllegalArgumentException("This date overlaps with another existing slot!");
        }

        // Save
        Availability availability = new Availability(
                request.getStartTime(),
                request.getEndTime(),
                doctor
        );
        return availabilityRepository.save(availability);
    }

    public List<Availability> getDoctorAvailability(Long doctorId) {
        return availabilityRepository
                .findAllByDoctorIdAndStartTimeAfterOrderByStartTimeAsc(doctorId, LocalDateTime.now());
    }

    @Transactional
    public void deleteAvailability(Long id, String doctorEmail) {
        Availability availability = availabilityRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Slot not found"));

        if (!availability.getDoctor().getEmail().equals(doctorEmail)) {
            throw new RuntimeException("You cannot delete a slot that does not belong to you!");
        }

        // Physcial delete availability from database
        availabilityRepository.delete(availability);
    }
}
