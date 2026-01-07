package com.reservation.reservation.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ReviewDTO {
    private Long id;
    private Integer rating;
    private String comment;
    private String patientName; // Anonymise the name
    private LocalDateTime createdAt;
}
