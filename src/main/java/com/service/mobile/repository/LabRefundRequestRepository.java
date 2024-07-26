package com.service.mobile.repository;

import com.service.mobile.model.LabRefundRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface LabRefundRequestRepository extends JpaRepository<LabRefundRequest, Integer> {
    @Query("Select u from LabRefundRequest u where u.labOrderId = ?1")
    List<LabRefundRequest> findByLabOrderId(Integer id);
}