package com.reservation.reservation.service;

import com.reservation.reservation.dto.request.CreateBookingRequest;
import com.reservation.reservation.dto.response.BookingDTO;
import com.reservation.reservation.model.Booking;
import com.reservation.reservation.model.BookingStatus;
import com.reservation.reservation.model.User;
import com.reservation.reservation.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BookingService {

    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final ServiceRepository serviceRepository;
    private final AvailabilityRepository availabilityRepository;

    @Transactional
    public BookingDTO createBooking(CreateBookingRequest request, String patientEmail) {
        User patient = userRepository.findByEmail(patientEmail)
                .orElseThrow(() -> new RuntimeException("Patient not found"));
        User doctor = userRepository.findById(request.getDoctorId())
                .orElseThrow(() -> new RuntimeException("Doctor not found"));
        com.reservation.reservation.model.Service service = serviceRepository.findById(request.getServiceId())
                .orElseThrow(() -> new RuntimeException("Service not found"));

        if(!service.isActive()) {
            throw new IllegalArgumentException("This service is currently unavailable");
        }

        LocalDateTime startTime = request.getStartTime();
        LocalDateTime endTime = startTime.plusMinutes(service.getDurationMinutes());

        // Check whether the doctor is working during these hours
        boolean isDoctorAvailable = availabilityRepository.existsOverlappingSlot(
                doctor.getId(), startTime, endTime
        );

        if(!isDoctorAvailable) {
            throw new IllegalArgumentException("The doctor is not available during these hours");
        }

        boolean isSlotTaken = bookingRepository.existsOverlappingBooking(
                doctor.getId(), startTime, endTime
        );

        if(isSlotTaken) {
            throw new IllegalArgumentException("This date is already booked by someone else!");
        }

        Booking booking = Booking.builder()
                .startTime(startTime)
                .endTime(endTime)
                .patient(patient)
                .doctor(doctor)
                .service(service)
                .status(BookingStatus.CONFIRMED)
                .build();

        Booking savedBooking = bookingRepository.save(booking);

        return  mapToDTO(savedBooking);
    }

    public List<BookingDTO> getMyBookings(String patientEmail) {
        User patient = userRepository.findByEmail(patientEmail).orElseThrow();
        List<Booking> bookings = bookingRepository.findAllByPatientIdOrderByStartTimeDesc(patient.getId());
        return bookings.stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    private BookingDTO mapToDTO(Booking booking) {
        return BookingDTO.builder()
                .id(booking.getId())
                .startTime(booking.getStartTime())
                .endTime(booking.getEndTime())
                .status(booking.getStatus())
                .serviceName(booking.getService().getName())
                .doctor(new BookingDTO.DoctorSummary(
                        booking.getDoctor().getId(),
                        booking.getDoctor().getFirstName(),
                        booking.getDoctor().getLastName(),
                        booking.getDoctor().getSpecialization()
                ))
                .patient(new BookingDTO.PatientSummary(
                        booking.getPatient().getId(),
                        booking.getPatient().getFirstName(),
                        booking.getPatient().getLastName()
                ))
                .build();
    }
}
