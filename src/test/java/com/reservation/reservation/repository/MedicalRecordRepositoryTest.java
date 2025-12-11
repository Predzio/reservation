package com.reservation.reservation.repository;

import com.reservation.reservation.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
public class MedicalRecordRepositoryTest {

    @Autowired
    private MedicalRecordRepository medicalRecordRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ServiceRepository serviceRepository;

    private Long bookingId;
    private Long patientId;

    @BeforeEach
    void setUp() {
        User doctor = userRepository.save(new User("doc@t.com", "p", "D", "R", Set.of(Role.ROLE_DOCTOR)));
        User patient = userRepository.save(new User("pat@t.com", "p", "P", "T", Set.of(Role.ROLE_PATIENT)));
        patientId = patient.getId();

        Service service = serviceRepository.save(new Service("Test", 30, BigDecimal.TEN));

        Booking booking = Booking.builder()
                .doctor(doctor).patient(patient).service(service)
                .startTime(LocalDateTime.now().minusDays(1))
                .endTime(LocalDateTime.now().minusDays(1).plusMinutes(30))
                .status(BookingStatus.CONFIRMED)
                .build();
        booking = bookingRepository.save(booking);
        bookingId = booking.getId();

        MedicalRecord record = MedicalRecord.builder()
                .diagnosis("Grypa")
                .treatment("Leki")
                .booking(booking)
                .createdAt(LocalDateTime.now())
                .build();
        medicalRecordRepository.save(record);
    }

    @Test
    void shouldFindRecordByBookingId() {
        Optional<MedicalRecord> found = medicalRecordRepository.findByBookingId(bookingId);
        assertTrue(found.isPresent());
        assertEquals("Grypa", found.get().getDiagnosis());
    }

    @Test
    void shouldFindHistoryByPatientId() {
        List<MedicalRecord> history = medicalRecordRepository.findAllByBooking_Patient_IdOrderByCreatedAtDesc(patientId);

        assertEquals(1, history.size());
        assertEquals("Grypa", history.get(0).getDiagnosis());
    }
}