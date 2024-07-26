package com.service.mobile.repository;

import com.service.mobile.model.DoctorSpecialization;
import com.service.mobile.model.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface DoctorSpecializationRepository extends JpaRepository<DoctorSpecialization, Integer> {
    List<DoctorSpecialization> findByUserId(Users val);

    @Query("Select u from DoctorSpecialization u where u.specializationId.id =?1")
    List<Integer> getDoctorIdFromSpecializationId(Integer specializationId);
}