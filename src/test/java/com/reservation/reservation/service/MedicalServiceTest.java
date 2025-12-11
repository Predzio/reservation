package com.reservation.reservation.service;

import com.reservation.reservation.dto.request.CreateMedicalRecordRequest;
import com.reservation.reservation.dto.response.MedicalRecordDTO;
import com.reservation.reservation.exception.BusinessException;
import com.reservation.reservation.model.*;
import com.reservation.reservation.repository.BookingRepository;
import com.reservation.reservation.repository.MedicalRecordRepository;
import com.reservation.reservation.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class MedicalServiceTest {

    @Mock
    private MedicalRecordRepository medicalRecordRepository;

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private MedicalRecordService medicalRecordService;

    @Test
    void shouldCreateMedicalRecordSuccessfully() {
        Long bookingId = 100L;
        String doctorEmail = "doc@test.com";

        User doctor = new User();
        doctor.setId(1L);
        doctor.setEmail(doctorEmail);
        doctor.setFirstName("Jan");
        doctor.setLastName("Kowalski");
        User patient = new User();
        patient.setId(2L);
        Service service = new Service("Wizyta", 30, BigDecimal.valueOf(200));

        Booking booking = Booking.builder()
                .id(bookingId)
                .doctor(doctor)
                .patient(patient)
                .service(service)
                .startTime(LocalDateTime.now())
                .status(BookingStatus.CONFIRMED)
                .build();

        CreateMedicalRecordRequest request = new CreateMedicalRecordRequest();
        request.setDiagnosis("Zdrowy");

        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));
        when(userRepository.findByEmail(doctorEmail)).thenReturn(Optional.of(doctor));
        when(medicalRecordRepository.findByBookingId(bookingId)).thenReturn(Optional.empty());

        when(medicalRecordRepository.save(any(MedicalRecord.class))).thenAnswer(i -> {
            MedicalRecord r = i.getArgument(0);
            r.setId(1L);
            return r;
        });

        MedicalRecordDTO result = medicalRecordService.createMedicalRecord(bookingId, request, doctorEmail);

        assertNotNull(result);
        assertEquals("Zdrowy", result.getDiagnosis());
        assertEquals(BookingStatus.COMPLETED, booking.getStatus());
        verify(bookingRepository).save(booking);
    }

    @Test
    void shouldThrowWhenDoctorIsNotOwnerOfBooking() {
        User doctorOwner = new User();
        doctorOwner.setId(1L);
        User doctorHacker = new User();
        doctorHacker.setId(99L);

        Booking booking = Booking.builder()
                .doctor(doctorOwner)
                .build();

        when(bookingRepository.findById(any())).thenReturn(Optional.of(booking));
        when(userRepository.findByEmail(any())).thenReturn(Optional.of(doctorHacker));

        BusinessException ex = assertThrows(BusinessException.class, () ->
                medicalRecordService.createMedicalRecord(1L, new CreateMedicalRecordRequest(), "hacker@test.com")
        );
        assertEquals(HttpStatus.FORBIDDEN, ex.getHttpStatus());
    }

    @Test
    void shouldThrowWhenBookingIsCancelled() {
        User doctor = new User(); doctor.setId(1L);
        Booking booking = Booking.builder()
                .doctor(doctor)
                .status(BookingStatus.CANCELLED)
                .build();

        when(bookingRepository.findById(any())).thenReturn(Optional.of(booking));
        when(userRepository.findByEmail(any())).thenReturn(Optional.of(doctor));

        assertThrows(BusinessException.class, () ->
                medicalRecordService.createMedicalRecord(1L, new CreateMedicalRecordRequest(), "doc@test.com")
        );
    }
}
