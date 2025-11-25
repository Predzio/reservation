package com.reservation.reservation.dto.request;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class CreateBookingRequest {
    private Long doctorId;
    private Long serviceId;
    private LocalDateTime startTime;
}
