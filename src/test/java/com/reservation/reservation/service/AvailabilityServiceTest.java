package com.reservation.reservation.service;

import com.reservation.reservation.dto.request.CreateAvailabilityRequest;
import com.reservation.reservation.model.Availability;
import com.reservation.reservation.model.User;
import com.reservation.reservation.repository.AvailabilityRepository;
import com.reservation.reservation.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.AssertionsKt.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AvailabilityServiceTest {

    @Mock
    private AvailabilityRepository availabilityRepository;
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private AvailabilityService availabilityService;

    @Test
    void shouldCreateAvailabilitySuccessfully() {
        String email = "doc@test.com";
        LocalDateTime start = LocalDateTime.now().plusDays(1).withHour(10); // Tomorrow 10:00
        LocalDateTime end = start.plusHours(2);

        CreateAvailabilityRequest request = new CreateAvailabilityRequest();
        request.setStartTime(start);
        request.setEndTime(end);

        User doctor = new User();
        doctor.setId(1L);

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(doctor));

        when(availabilityRepository.existsOverlappingSlot(any(), any(), any())).thenReturn(false);
        when(availabilityRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        Availability result = availabilityService.createAvailability(request, email);

        assertNotNull(result);
        assertEquals(start, result.getStartTime());
        verify(availabilityRepository).save(any(Availability.class));
    }

    @Test
    void shouldThrowExceptionWhenDatesAreInvalid() {
        CreateAvailabilityRequest request = new CreateAvailabilityRequest();
        request.setStartTime(LocalDateTime.now().minusDays(1));
        request.setEndTime(LocalDateTime.now().plusHours(1));

        assertThrows(IllegalArgumentException.class, () ->
                availabilityService.createAvailability(request, "doc@test.com")
        );
    }

    @Test
    void shouldThrowExceptionWhenSlotOverlaps() {
        LocalDateTime start = LocalDateTime.now().plusDays(1);
        CreateAvailabilityRequest request = new CreateAvailabilityRequest();
        request.setStartTime(start);
        request.setEndTime(start.plusHours(2));

        User doctor = new User();
        doctor.setId(1L);

        when(userRepository.findByEmail("doc@test.com")).thenReturn(Optional.of(doctor));
        when(availabilityRepository.existsOverlappingSlot(any(), any(), any())).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () ->
                availabilityService.createAvailability(request, "doc@test.com")
        );

        verify(availabilityRepository, never()).save(any());
    }
}
