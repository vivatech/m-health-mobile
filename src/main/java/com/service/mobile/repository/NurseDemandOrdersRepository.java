package com.service.mobile.repository;

import com.service.mobile.dto.enums.PaymentStatus;
import com.service.mobile.dto.enums.StatusFullName;
import com.service.mobile.model.NurseDemandOrders;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface NurseDemandOrdersRepository extends JpaRepository<NurseDemandOrders, Integer> {
    @Query("Select u from NurseDemandOrders u where u.patientId.userId = ?1 and u.nurseId IS NOT NULL and u.paymentStatus = ?2 and u.status = ?3")
    List<NurseDemandOrders> findByPatientNurseNotNullPaymentStatusStatus(Integer userId, PaymentStatus paymentStatus, StatusFullName status);
}