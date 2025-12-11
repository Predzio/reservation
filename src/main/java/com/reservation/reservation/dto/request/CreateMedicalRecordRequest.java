package com.reservation.reservation.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateMedicalRecordRequest {
    @NotBlank(message = "The diagnosis cannot be empty")
    private String diagnosis;

    private String treatment;
    private String recommendations;
}
