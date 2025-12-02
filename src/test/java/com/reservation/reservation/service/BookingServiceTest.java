package com.reservation.reservation.service;

import com.reservation.reservation.dto.request.CreateBookingRequest;
import com.reservation.reservation.dto.response.BookingDTO;
import com.reservation.reservation.model.*;
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
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

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

    @Test
    void shouldReturnDoctorBookings() {
        String doctorEmail = "doc@test.com";
        User doctor = new User();
        doctor.setId(1L);
        doctor.setEmail(doctorEmail);

        when(userRepository.findByEmail(doctorEmail)).thenReturn(Optional.of(doctor));

        Booking booking = Booking.builder()
                .id(100L)
                .startTime(LocalDateTime.now().plusDays(1))
                .endTime(LocalDateTime.now().plusDays(1).plusHours(1))
                .status(BookingStatus.CONFIRMED)
                .doctor(doctor)
                .patient(new User("pat@t.com", "p", "P", "K", Set.of(Role.ROLE_PATIENT)))
                .service(new Service("Test", 60, null))
                .build();

        when(bookingRepository.findAllByDoctorIdAndStartTimeAfterOrderByStartTimeAsc(eq(1L), any()))
                .thenReturn(List.of(booking));

        List<BookingDTO> result = bookingService.getDoctorBookings(doctorEmail);

        assertEquals(1, result.size());
        assertEquals(100L, result.get(0).getId());
    }

    @Test
    void shouldCancelBookingAsPatient() {
        Long bookingId = 10L;
        String patientEmail = "patient@test.com";

        User patient = new User();
        patient.setId(5L);
        patient.setEmail(patientEmail);
        patient.setRoles(Set.of(Role.ROLE_PATIENT));
        User doctor = new User();
        doctor.setId(1L);

        Booking booking = Booking.builder()
                .id(bookingId)
                .patient(patient)
                .doctor(doctor)
                .startTime(LocalDateTime.now().plusDays(2))
                .status(BookingStatus.CONFIRMED)
                .build();

        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));
        when(userRepository.findByEmail(patientEmail)).thenReturn(Optional.of(patient));

        bookingService.cancelBooking(bookingId, patientEmail);

        assertEquals(BookingStatus.CANCELLED, booking.getStatus());
        verify(bookingRepository).save(booking);
    }

    @Test
    void shouldThrowWhenUnauthorizedUserTriesToCancel() {
        Long bookingId = 10L;
        String hackerEmail = "hacker@test.com";

        User patient = new User(); patient.setId(5L); // Owner
        patient.setRoles(Set.of(Role.ROLE_PATIENT));
        User doctor = new User(); doctor.setId(1L);

        User hacker = new User(); hacker.setId(999L); // Other
        hacker.setEmail(hackerEmail);
        hacker.setRoles(Set.of(Role.ROLE_PATIENT));

        Booking booking = Booking.builder()
                .id(bookingId)
                .patient(patient)
                .doctor(doctor)
                .status(BookingStatus.CONFIRMED)
                .build();

        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));
        when(userRepository.findByEmail(hackerEmail)).thenReturn(Optional.of(hacker));

        RuntimeException ex = assertThrows(RuntimeException.class, () ->
                bookingService.cancelBooking(bookingId, hackerEmail)
        );
        assertEquals("You don't have permission on this appointment", ex.getMessage());

        assertNotEquals(BookingStatus.CANCELLED, booking.getStatus());
        verify(bookingRepository, never()).save(any());
    }

    @Test
    void shouldThrowWhenBookingIsAlreadyCancelled() {
        Long bookingId = 10L;
        String email = "pat@test.com";
        User patient = new User();
        patient.setId(5L);
        patient.setRoles(Set.of(Role.ROLE_PATIENT));

        Booking booking = Booking.builder()
                .id(bookingId)
                .patient(patient)
                .doctor(new User())
                .status(BookingStatus.CANCELLED)
                .build();
        booking.getDoctor().setId(2L);

        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(patient));

        RuntimeException ex = assertThrows(RuntimeException.class, () ->
                bookingService.cancelBooking(bookingId, email)
        );
        assertEquals("This appointment has already been cancelled", ex.getMessage());
    }

    @Test
    void shouldThrowWhenCancellingPastBooking() {
        User patient = new User();
        patient.setId(5L);
        patient.setRoles(Set.of(Role.ROLE_PATIENT));

        Booking booking = Booking.builder()
                .id(1L)
                .patient(patient)
                .doctor(new User())
                .startTime(LocalDateTime.now().minusDays(1))
                .status(BookingStatus.CONFIRMED)
                .build();
        booking.getDoctor().setId(2L);

        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));
        when(userRepository.findByEmail("pat@t.com")).thenReturn(Optional.of(patient));

        assertThrows(RuntimeException.class, () ->
                bookingService.cancelBooking(1L, "pat@t.com")
        );
    }

}
