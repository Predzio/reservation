package com.reservation.reservation.service;

import com.reservation.reservation.dto.request.CreateMedicalRecordRequest;
import com.reservation.reservation.dto.response.MedicalRecordDTO;
import com.reservation.reservation.exception.BusinessException;
import com.reservation.reservation.exception.ResourceNotFoundException;
import com.reservation.reservation.model.Booking;
import com.reservation.reservation.model.BookingStatus;
import com.reservation.reservation.model.MedicalRecord;
import com.reservation.reservation.model.User;
import com.reservation.reservation.repository.BookingRepository;
import com.reservation.reservation.repository.MedicalRecordRepository;
import com.reservation.reservation.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MedicalRecordService {

    private final MedicalRecordRepository medicalRecordRepository;
    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;

    @Transactional
    public MedicalRecordDTO createMedicalRecord(Long bookingId, CreateMedicalRecordRequest request, String doctorEmail) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Reservation not found"));

        User doctor = userRepository.findByEmail(doctorEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor not found"));

        if(!booking.getDoctor().getId().equals(doctor.getId())) {
            throw new BusinessException("You cannot complete documentation for a visit that is not yours", HttpStatus.FORBIDDEN);
        }

        if(booking.getStatus() == BookingStatus.CANCELLED) {
            throw new BusinessException("You cannot add a description to a cancelled appointment");
        }

        if(medicalRecordRepository.findByBookingId(bookingId).isPresent()) {
            throw new BusinessException("The documentation for this visit has already been completed", HttpStatus.CONFLICT);
        }

        MedicalRecord record = MedicalRecord.builder()
                .diagnosis(request.getDiagnosis())
                .treatment(request.getTreatment())
                .recommendations(request.getRecommendations())
                .createdAt(LocalDateTime.now())
                .booking(booking)
                .build();

        booking.setStatus(BookingStatus.COMPLETED);
        bookingRepository.save(booking);

        MedicalRecord saved = medicalRecordRepository.save(record);

        return mapToDTO(saved);
    }

    public List<MedicalRecordDTO> getMyMedicalHistory(String patientEmail) {
        User patient = userRepository.findByEmail(patientEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found"));

        return medicalRecordRepository.findAllByBooking_Patient_IdOrderByCreatedAtDesc(patient.getId())
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    private MedicalRecordDTO mapToDTO(MedicalRecord record) {
        return MedicalRecordDTO.builder()
                .id(record.getId())
                .diagnosis(record.getDiagnosis())
                .treatment(record.getTreatment())
                .recommendations(record.getRecommendations())
                .visitDate(record.getBooking().getStartTime())
                .doctorName(record.getBooking().getDoctor().getFirstName() +" " +record.getBooking().getDoctor().getLastName())
                .serviceName(record.getBooking().getService().getName())
                .build();
    }
    
}
