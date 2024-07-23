package com.service.mobile.repository;

import com.service.mobile.model.DoctorSpecialization;
import com.service.mobile.model.Users;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DoctorSpecializationRepository extends JpaRepository<DoctorSpecialization, Integer> {
    List<DoctorSpecialization> findByUserId(Users val);

    List<Integer> getDoctorIdFromSpecializationId(Integer specializationId);
}