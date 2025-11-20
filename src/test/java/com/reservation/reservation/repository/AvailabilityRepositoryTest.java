package com.reservation.reservation.repository;


import com.reservation.reservation.model.Availability;
import com.reservation.reservation.model.Role;
import com.reservation.reservation.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.time.LocalDateTime;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
class AvailabilityRepositoryTest {

    @Autowired
    private AvailabilityRepository availabilityRepository;

    @Autowired
    private UserRepository userRepository;

    private Long doctorId;

    @BeforeEach
    void setUp() {
        User doctor = new User("doc@test.com", "password", "John", "Tor", Set.of(Role.ROLE_DOCTOR));
        doctor = userRepository.save(doctor);
        doctorId = doctor.getId();

        // 10:00 - 12:00
        Availability slot = new Availability(
                LocalDateTime.of(2025, 12, 1, 10, 0),
                LocalDateTime.of(2025, 12, 1, 12, 0),
                doctor
        );
        availabilityRepository.save(slot);
    }

    @Test
    void shouldDetectOverlappingSlots() {
        // (10:30 - 11:30) -> should be conflict (True)
        boolean resultA = availabilityRepository.existsOverlappingSlot(doctorId,
                LocalDateTime.of(2025, 12, 1, 10, 30),
                LocalDateTime.of(2025, 12, 1, 11, 30));
        assertTrue(resultA, "Should be conflict");

        // (09:00 - 13:00) -> should be conflict (True)
        boolean resultB = availabilityRepository.existsOverlappingSlot(doctorId,
                LocalDateTime.of(2025, 12, 1, 9, 0),
                LocalDateTime.of(2025, 12, 1, 13, 0));
        assertTrue(resultB, "Should be conflict");

        // (12:00 - 14:00) -> no conflict (False)
        boolean resultC = availabilityRepository.existsOverlappingSlot(doctorId,
                LocalDateTime.of(2025, 12, 1, 12, 0),
                LocalDateTime.of(2025, 12, 1, 14, 0));
        assertFalse(resultC, "Should be no conflict at the interface.");

        // (14:00 - 16:00) -> no conflict (False)
        boolean resultD = availabilityRepository.existsOverlappingSlot(doctorId,
                LocalDateTime.of(2025, 12, 1, 14, 0),
                LocalDateTime.of(2025, 12, 1, 16, 0));
        assertFalse(resultD, "A completely different term should not interfere.");
    }
}
