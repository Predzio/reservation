package com.reservation.reservation.controller;

import com.reservation.reservation.dto.request.CreateServiceRequest;
import com.reservation.reservation.model.Service;
import com.reservation.reservation.service.ServiceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/services")
@RequiredArgsConstructor
public class ServiceController {

    private final ServiceService serviceService;

    @GetMapping
    public ResponseEntity<List<Service>> getActiveServices() {
        return ResponseEntity.ok(serviceService.getAllActiveServices());
    }

    @PostMapping
    @PreAuthorize(("hasRole('ADMIN')"))
    public ResponseEntity<Service> createService(@RequestBody CreateServiceRequest request) {
        return ResponseEntity.ok(serviceService.createService(request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN'")
    public ResponseEntity<?> deleteService(@PathVariable Long id) {
        serviceService.deleteService(id);
        return ResponseEntity.ok("Service deactivated successfully");
    }
}
















