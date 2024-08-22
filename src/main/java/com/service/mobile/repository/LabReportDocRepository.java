package com.service.mobile.repository;

import com.service.mobile.dto.enums.AddedType;
import com.service.mobile.dto.enums.Status;
import com.service.mobile.model.LabReportDoc;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface LabReportDocRepository extends JpaRepository<LabReportDoc, Integer> {
    @Query("Select u from LabReportDoc u where u.labOrdersId = ?1")
    List<LabReportDoc> findByLabOrderId(Integer id);

    @Query("Select u from LabReportDoc u where u.caseId = ?1 and u.status = ?2")
    List<LabReportDoc> findByCaseIdAndStatus(Integer id, Status status);

    @Query("Select u from LabReportDoc u where u.status = ?1")
    List<LabReportDoc> findByStatus(Status status);

    @Query("Select u from LabReportDoc u where u.caseId = ?1 and u.status = ?2 and u.labOrdersId = ?3")
    List<LabReportDoc> findByCaseIdAndStatusAndLabOrdersId(Integer caseId, Status status,Integer labOrdersId);

    @Query("Select u from LabReportDoc u where u.status = ?1 and u.labOrdersId = ?2")
    List<LabReportDoc> findByStatusAndLabOrdersId(Status status,Integer labOrdersId);

    @Query("Select u from LabReportDoc u where u.caseId = ?1 and u.addedBy = ?2 and u.addedType = ?3 and u.status = ?4")
    List<LabReportDoc> findByCaseIdAndAddedByAddedTypeAndStatus(Integer caseId, Integer userId, AddedType addedType, Status status);
}