package com.reservation.reservation.service;

import com.reservation.reservation.dto.request.CreateBookingRequest;
import com.reservation.reservation.dto.response.BookingDTO;
import com.reservation.reservation.model.Booking;
import com.reservation.reservation.model.Service;
import com.reservation.reservation.model.User;
import com.reservation.reservation.repository.AvailabilityRepository;
import com.reservation.reservation.repository.BookingRepository;
import com.reservation.reservation.repository.ServiceRepository;
import com.reservation.reservation.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class BookingServiceTest {

    @Mock
    private BookingRepository bookingRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private ServiceRepository serviceRepository;
    @Mock
    private AvailabilityRepository availabilityRepository;
    @InjectMocks
    private BookingService bookingService;

    @Test
    void shouldCreateBookingSuccessfully() {
        CreateBookingRequest request = new CreateBookingRequest();
        request.setDoctorId(1L);
        request.setServiceId(1L);
        request.setStartTime(LocalDateTime.now().plusDays(1));

        User patient = new User();
        patient.setId(10L);
        patient.setEmail("pat@t.com");
        User doctor = new User();
        doctor.setId(1L);
        doctor.setFirstName("Doc");
        Service service = new Service("Test", 30, BigDecimal.TEN);

        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(patient));
        when(userRepository.findById(1L)).thenReturn(Optional.of(doctor));
        when(serviceRepository.findById(1L)).thenReturn(Optional.of(service));

        // Doctor available, appointment available
        when(availabilityRepository.existsOverlappingSlot(any(), any(), any())).thenReturn(true);
        when(bookingRepository.existsOverlappingBooking(any(), any(), any())).thenReturn(false);

        when(bookingRepository.save(any(Booking.class))).thenAnswer(i -> {
            Booking b = i.getArgument(0);
            b.setId(123L); // Simulate ID for base
            return b;
        });

        BookingDTO result = bookingService.createBooking(request, "pat@t.com");

        assertNotNull(result);
        assertEquals(123L, result.getId());
        assertEquals("Doc", result.getDoctor().getFirstName());
    }

    @Test
    void shouldThrowWhenDoctorNotAvailable() {
        User patient = new User(); User doctor = new User(); Service service = new Service("Test", 30, BigDecimal.TEN);
        CreateBookingRequest request = new CreateBookingRequest();
        request.setDoctorId(1L); request.setServiceId(1L); request.setStartTime(LocalDateTime.now());

        when(userRepository.findByEmail(any())).thenReturn(Optional.of(patient));
        when(userRepository.findById(any())).thenReturn(Optional.of(doctor));
        when(serviceRepository.findById(any())).thenReturn(Optional.of(service));

        // DOCTOR UNAVAILABLE (false)
        when(availabilityRepository.existsOverlappingSlot(any(), any(), any())).thenReturn(false);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                bookingService.createBooking(request, "pat@t.com"));

        assertTrue(ex.getMessage().contains("The doctor is not available during these hours"));
    }

}
