package com.service.mobile.repository;

import com.service.mobile.model.LabReportRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface LabReportRequestRepository extends JpaRepository<LabReportRequest, Integer> {
    @Query("Select u from LabReportRequest u where u.labConsultId.labConsultId = ?1")
    List<LabReportRequest> findByLabConsultationId(Integer labConsultId);
}