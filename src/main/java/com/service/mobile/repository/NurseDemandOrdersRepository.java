package com.service.mobile.repository;

import com.service.mobile.dto.enums.PaymentStatus;
import com.service.mobile.dto.enums.State;
import com.service.mobile.dto.enums.StatusFullName;
import com.service.mobile.model.NurseDemandOrders;
import com.service.mobile.model.PartnerNurse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface NurseDemandOrdersRepository extends JpaRepository<NurseDemandOrders, Integer> {
    @Query("Select DISTINCT(u) from NurseDemandOrders u where u.patientId.userId = ?1 and u.nurseId IS NOT NULL and u.paymentStatus = ?2 and u.status = ?3 " +
            "and u.id IN (SELECT d.orderId FROM NurseServiceState d WHERE d.state NOT IN ?4) ")
    List<NurseDemandOrders> findByPatientNurseNotNullPaymentStatusStatus(Integer userId, PaymentStatus paymentStatus, StatusFullName status, List<State> state);

    @Query("SELECT u.nurseId FROM NurseDemandOrders u WHERE u.tripId = ?1")
    PartnerNurse findByTripId(String searchId);
}