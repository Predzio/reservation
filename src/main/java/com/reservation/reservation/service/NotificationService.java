package com.reservation.reservation.service;

import com.reservation.reservation.model.Booking;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final JavaMailSender javaMailSender;

    @Async
    public void sendBookingConfirmation(Booking booking) {
        String patientEmail = booking.getPatient().getEmail();
        String subject = "Potwierdzenie rezerwacji - " + booking.getService().getName();
        String text = String.format("""
                Witaj %s!
                
                Twoja wizyta została potwierdzona.
                Lekarz: %s %s
                Usługa: %s
                Data: %s
                
                Pozdrawiamy
                Medical-reservation
                """,
                booking.getPatient().getFirstName(),
                booking.getDoctor().getFirstName(),
                booking.getDoctor().getLastName(),
                booking.getService().getName(),
                booking.getStartTime().toString()
        );

        sendEmail(patientEmail, subject, text);
    }

    @Async
    public void sendBookingCancellation(Booking booking) {
        String patientEmail = booking.getPatient().getEmail();
        String subject = "Anulowanie wizyty";
        String text = String.format("""
                Witaj %s.
                
                Twoja wizyta z dnia %s została anulowana.
                Przepraszamy za niedogodności.
                """,
                booking.getPatient().getFirstName(),
                booking.getStartTime().toString()
        );

        sendEmail(patientEmail, subject, text);
    }

    private void sendEmail(String to, String subject, String text) {
        try {
            log.info("Sending mail to: {}", to);
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject(subject);
            message.setText(text);
            message.setFrom("noreply@medical-reservation.com");

            javaMailSender.send(message);

            log.info("Mail sent successfully to: {}", to);
        } catch (Exception e) {
            log.error("Error during sending mail to: " +to, e);
        }
    }

}
