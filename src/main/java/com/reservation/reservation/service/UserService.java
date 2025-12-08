package com.reservation.reservation.service;

import com.reservation.reservation.dto.request.CreateDoctorRequest;
import com.reservation.reservation.dto.request.RegisterRequest;
import com.reservation.reservation.dto.response.DoctorDTO;
import com.reservation.reservation.exception.BusinessException;
import com.reservation.reservation.model.Role;
import com.reservation.reservation.model.User;
import com.reservation.reservation.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public List<DoctorDTO> getAllDoctors() {
        List<User> doctors = userRepository.findAllByRolesContaining(Role.ROLE_DOCTOR);
        doctors.removeIf(user -> "admin@company.com".equals(user.getEmail()));

        return doctors.stream()
                .map(doc -> new DoctorDTO(
                        doc.getId(),
                        doc.getFirstName(),
                        doc.getLastName(),
                        doc.getSpecialization()
                ))
                .collect(Collectors.toList());
    }

    public User registerNewPatient(RegisterRequest request) {
        if(userRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException("Error: Email is already in use!", HttpStatus.CONFLICT);
        }

        User user = new User(
                request.getEmail(),
                passwordEncoder.encode(request.getPassword()),
                request.getFirstName(),
                request.getLastName(),
                Set.of(Role.ROLE_PATIENT)
        );

        return userRepository.save(user);
    }

    public User createNewDoctor(CreateDoctorRequest request) {
        if(userRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException("Error: Email is already in use!", HttpStatus.CONFLICT);
        }
        
        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .specialization(request.getSpecialization())
                .roles(Set.of(Role.ROLE_DOCTOR))
                .build();

        return userRepository.save(user);
    }


}
