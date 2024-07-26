package com.service.mobile.repository;

import com.service.mobile.dto.enums.CategoryStatus;
import com.service.mobile.dto.enums.YesNo;
import com.service.mobile.model.LabSubCategoryMaster;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface LabSubCategoryMasterRepository extends JpaRepository<LabSubCategoryMaster, Integer> {
    @Query("Select DISTINCT u.subCatId from LabPrice u where u.catId.catId = ?1 and u.subCatId.subCatStatus = ?2")
    List<LabSubCategoryMaster> findByCategoryId(Integer categoryId, CategoryStatus status);

    @Query("Select u from LabSubCategoryMaster u where u.subCatId in ?1")
    List<LabSubCategoryMaster> findByIdinList(List<Integer> reportId);


    @Query("Select u from LabSubCategoryMaster u where u.subCatId in ?1 and u.isHomeConsultantAvailable = ?2")
    List<LabSubCategoryMaster> findByIdinListAndHomeConsultant(List<Integer> reportId, YesNo homeAvailability);
}