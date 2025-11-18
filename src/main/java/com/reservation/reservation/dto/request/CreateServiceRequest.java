package com.reservation.reservation.dto.request;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class CreateServiceRequest {
    private String name;
    private Integer durationMinutes;
    private BigDecimal price;
}
