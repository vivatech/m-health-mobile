package com.service.mobile.repository;

import com.service.mobile.dto.enums.YesNo;
import com.service.mobile.model.HealthTipOrders;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface HealthTipOrdersRepository extends JpaRepository<HealthTipOrders, Integer> {
    @Query("Select u from HealthTipOrders u where u.patientId.userId = ?1 and u.healthTipPackage.packageId = ?2")
    List<HealthTipOrders> findByPatientIdAndHathTipPackageId(Integer userId, Integer packageId);
}