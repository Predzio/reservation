package com.reservation.reservation.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MedicalRecordDTO {
    private Long id;
    private String diagnosis;
    private String treatment;
    private String recommendations;
    private LocalDateTime visitDate;
    private String doctorName;
    private String serviceName;
}
