package com.reservation.reservation.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateReviewRequest {

    @NotNull(message = "Assessment is required")
    @Min(value = 1, message =  "Minimum grade is 1")
    @Max(value = 5, message = "Maximum grade is 5")
    private Integer rating;

    private String comment;
}
