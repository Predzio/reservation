package com.reservation.reservation.repository;

import com.reservation.reservation.model.Service;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ServiceRepository extends JpaRepository<Service, Long> {
    List<Service> findAllByIsActiveTrue();  //Get only activated services for patient
}
