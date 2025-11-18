package com.reservation.reservation.service;

import com.reservation.reservation.dto.request.CreateServiceRequest;
import com.reservation.reservation.model.Service;
import com.reservation.reservation.repository.ServiceRepository;
import lombok.RequiredArgsConstructor;

import java.util.List;

@org.springframework.stereotype.Service
@RequiredArgsConstructor
public class ServiceService {

    private final ServiceRepository serviceRepository;

    // Get all services (for ADMIN)
    public List<Service> getAllServices() {
        return serviceRepository.findAll();
    }

    // Get only active services
    public List<Service> getAllActiveServices() {
        return serviceRepository.findAllByIsActiveTrue();
    }

    // Adding new service (for ADMIN)
    public Service createService(CreateServiceRequest request) {
        Service service = new Service(
                request.getName(),
                request.getDurationMinutes(),
                request.getPrice()
        );

        return serviceRepository.save(service);
    }

    // Deleting service (Soft Delete)
    public void deleteService(Long id) {
        Service service = serviceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Service not found"));

        service.setActive(false);
        serviceRepository.save(service);
    }
}








