package com.service.mobile.repository;

import com.service.mobile.model.HealthTipCategoryMaster;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface HealthTipCategoryMasterRepository extends JpaRepository<HealthTipCategoryMaster, Integer> {
    @Query("SELECT u FROM Users u JOIN FETCH u.doctorCharges dc WHERE u.type = 'Doctor' AND u.status = 'A' AND u.hospitalId IN :hospitalIds AND u.hasDoctorVideo IN ('visit', 'both') AND u.hospitalId > 0 GROUP BY u.userId")
    List<HealthTipCategoryMaster> findHealthtipPackages(String name, Integer userId);
}