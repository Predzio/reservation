package com.reservation.reservation.dto.response;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class JwtResponse {
    private String token;
    private final String type = "Bearer";
    private String email;
    private List<String> roles;

    public JwtResponse(String accessToken, String email, java.util.List<String> roles) {
        this.token = accessToken;
        this.email = email;
        this.roles = roles;
    }
}
