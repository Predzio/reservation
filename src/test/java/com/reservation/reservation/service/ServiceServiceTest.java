package com.reservation.reservation.service;

import com.reservation.reservation.dto.request.CreateServiceRequest;
import com.reservation.reservation.model.Service;
import com.reservation.reservation.repository.ServiceRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ServiceServiceTest {

    @Mock
    private ServiceRepository serviceRepository;

    @InjectMocks
    private ServiceService serviceService;

    @Test
    void shouldReturnOnlyActiveServices() {
        Service activeService = new Service("Badanie krwi", 15, BigDecimal.TEN);

        when(serviceRepository.findAllByIsActiveTrue()).thenReturn(List.of(activeService));

        List<Service> result = serviceService.getAllActiveServices();

        assertEquals(1, result.size());
        assertEquals("Badanie krwi", result.get(0).getName());
        verify(serviceRepository, times(1)).findAllByIsActiveTrue();
    }

    @Test
    void shouldCreateService() {
        CreateServiceRequest request = new CreateServiceRequest();
        request.setName("Konsultacja kardiologiczna");
        request.setDurationMinutes(30);
        request.setPrice(BigDecimal.valueOf(200));

        when(serviceRepository.save((any(Service.class)))).thenAnswer(i -> i.getArgument(0));
        Service result = serviceService.createService(request);

        assertNotNull(result);
        assertEquals("Konsultacja kardiologiczna", result.getName());
        assertTrue(result.isActive());
    }

    @Test
    void shouldSoftDeleteService() {
        Long serviceId = 1l;
        Service service = new Service("Badanie krwi", 10, BigDecimal.TEN);
        service.setId(serviceId);
        service.setActive(true);

        when(serviceRepository.findById(serviceId)).thenReturn(Optional.of(service));

        serviceService.deleteService(serviceId);

        assertFalse(service.isActive());
        verify(serviceRepository).save(service);
    }
}