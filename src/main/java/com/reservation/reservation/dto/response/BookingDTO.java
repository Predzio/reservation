package com.reservation.reservation.dto.response;

import com.reservation.reservation.model.Booking;
import com.reservation.reservation.model.BookingStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingDTO {
    private Long id;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private BookingStatus status;
    private String serviceName;
    private DoctorSummary doctor;
    private PatientSummary patient;

    @Data
    @AllArgsConstructor
    public static class DoctorSummary {
        private Long id;
        private String firstName;
        private String lastName;
        private String specialization;
    }

    @Data
    @AllArgsConstructor
    public static class PatientSummary {
        private Long id;
        private String firstName;
        private String lastName;
    }























}
