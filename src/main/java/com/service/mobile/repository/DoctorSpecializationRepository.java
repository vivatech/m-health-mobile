package com.service.mobile.repository;

import com.service.mobile.dto.enums.Status;
import com.service.mobile.model.DoctorSpecialization;
import com.service.mobile.model.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface DoctorSpecializationRepository extends JpaRepository<DoctorSpecialization, Integer> {
    @Query("Select u from DoctorSpecialization u where u.userId.userId = ?1")
    List<DoctorSpecialization> findByUserId(Integer val);

    @Query("SELECT CASE WHEN ?2 = 'so' THEN ds.specializationId.nameSl ELSE ds.specializationId.name END " +
            "FROM DoctorSpecialization ds " +
            "WHERE ds.userId.userId = ?1 AND ds.specializationId.status IN ?3")
    List<String> findSpecializationsByUserId(Integer userId, String lang, Status status);
}