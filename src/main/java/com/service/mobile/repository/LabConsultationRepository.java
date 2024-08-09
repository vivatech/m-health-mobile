package com.service.mobile.repository;

import com.service.mobile.model.LabConsultation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface LabConsultationRepository extends JpaRepository<LabConsultation, Integer> {
    @Query("Select u from LabConsultation u where u.patient.userId = ?1 and u.labOrdersId is null and u.caseId is null")
    List<LabConsultation> findByPatientIdANdLabOrderIsNullAndCaseIsNull(Integer userId);

    @Query("Select u from LabConsultation u where u.patient.userId = ?1 and u.categoryId.catId= ?2 and u.subCatId.subCatId = ?3 and u.labOrdersId is null and u.caseId is null")
    List<LabConsultation> findByPatientIdCategoryIdSubCategoryIdLabOrderAndCaseNull(Integer userId, Integer categoryId, Integer subCatId);

    @Query("Select u from LabConsultation u where u.caseId.caseId = ?1 order by u.labOrdersId.id")
    List<LabConsultation> findByCaseId(Integer caseId);
    @Query("Select u from LabConsultation u where u.patient.userId = ?1 order by u.labOrdersId.id")
    List<LabConsultation> findByPatientId(Integer caseId);

    @Query("Select u from LabConsultation u where  u.labOrdersId.id = ?1")
    List<LabConsultation> findByLabOrderId(Integer id);

    @Query("Select u from LabConsultation u where u.patient.userId = ?1 and u.caseId.caseId = ?2 and u.categoryId.catId = ?3 and u.subCatId.subCatId = ?4 order by u.labConsultId DESC")
    List<LabConsultation> findByPatientIdCaseIdCategoryIdSubCategoryId(Integer userId, Integer caseId, Integer categoryId, Integer subcategoryId);

    @Query("Select u from LabConsultation u where u.patient.userId = ?1 and u.caseId.caseId = ?2 and u.categoryId.catId = ?3 order by u.labConsultId DESC")
    List<LabConsultation> findByPatientIdCaseIdCategoryId(Integer userId, Integer caseId, Integer categoryId);

    @Query("Select u from LabConsultation u where u.patient.userId = ?1 and u.caseId.caseId = ?2 and u.subCatId.subCatId = ?3 order by u.labConsultId DESC")
    List<LabConsultation> findByPatientIdCaseIdSubCategoryId(Integer userId, Integer caseId, Integer subcategoryId);

    @Query("Select u from LabConsultation u where u.patient.userId = ?1 and u.caseId.caseId = ?2 order by u.labConsultId DESC")
    List<LabConsultation> findByPatientIdCaseId(Integer userId, Integer caseId);

    @Query("Select u from LabConsultation u where u.subCatId.subCatId in ?1 and u.caseId.caseId = ?2 and labOrdersId IS NOT NULL")
    List<LabConsultation> findBySubCategoryIdCaseIdLadIdNotNull(List<Integer> subCatId,Integer caseId);
}