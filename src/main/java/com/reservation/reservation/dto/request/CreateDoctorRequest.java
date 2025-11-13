package com.reservation.reservation.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateDoctorRequest {
    private String email;
    private String password;
    private String firstName;
    private String lastName;
    private String specialization;
}
