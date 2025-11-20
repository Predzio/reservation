package com.reservation.reservation.model;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "availabilities")
@Getter
@Setter
@NoArgsConstructor
public class Availability {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDateTime startTime;

    @Column(nullable = false)
    private LocalDateTime endTime;

    // Relation to User doctor
    // FetchType.LAZY is more efficient (it doesn't get User, when you only ask for dates)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "doctor_id", nullable = false)
    private User doctor;

    public Availability(LocalDateTime startTime, LocalDateTime endTime, User doctor) {
        this.startTime = startTime;
        this.endTime = endTime;
        this.doctor = doctor;
    }
}


























































