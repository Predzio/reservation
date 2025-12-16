package com.reservation.reservation.service;

import com.reservation.reservation.model.Booking;
import com.reservation.reservation.model.Service;
import com.reservation.reservation.model.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private JavaMailSender javaMailSender;

    @InjectMocks
    private NotificationService notificationService;

    @Captor
    private ArgumentCaptor<SimpleMailMessage> messageCaptor;

    @Test
    void shouldSendConfirmationEmailWithCorrectData() {
        User doctor = new User();
        doctor.setFirstName("Andrew");
        doctor.setLastName("Mod");
        User patient = new User();
        patient.setFirstName("Jan");
        patient.setEmail("jan@test.com");
        Service service = new Service("Diagnoza", 60, BigDecimal.ZERO);

        Booking booking = Booking.builder()
                .doctor(doctor)
                .patient(patient)
                .service(service)
                .startTime(LocalDateTime.of(2025, 12, 1, 10, 0))
                .build();

        notificationService.sendBookingConfirmation(booking);

        verify(javaMailSender).send(messageCaptor.capture());

        SimpleMailMessage sentMessage = messageCaptor.getValue();

        assertEquals("jan@test.com", sentMessage.getTo()[0]);
        assertEquals("Potwierdzenie rezerwacji - Diagnoza", sentMessage.getSubject());
        assertEquals("noreply@medical-reservation.com", sentMessage.getFrom());

        assertTrue(sentMessage.getText().contains("Witaj Jan"));
        assertTrue(sentMessage.getText().contains("Lekarz: Andrew Mod"));
        assertTrue(sentMessage.getText().contains("2025-12-01T10:00"));
    }
}
