package com.reservation.reservation.dto.request;

import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Getter
@Setter
public class CreateAvailabilityRequest {
    // Format in JSON "2025-11-25T09:00:00"
    private LocalDateTime startTime;
    private LocalDateTime endTime;
}
