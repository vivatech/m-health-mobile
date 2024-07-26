package com.service.mobile.repository;

import com.service.mobile.model.LabReportDoc;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface LabReportDocRepository extends JpaRepository<LabReportDoc, Integer> {
    @Query("Select u from LabReportDoc u where u.labOrdersId = ?1")
    List<LabReportDoc> findByLabOrderId(Integer id);
}