package com.service.mobile.repository;

import com.service.mobile.dto.enums.CategoryStatus;
import com.service.mobile.model.LabCategoryMaster;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface LabCategoryMasterRepository extends JpaRepository<LabCategoryMaster, Integer> {

    @Query("Select DISTINCT u.catId from LabPrice u where u.catId.catStatus = ?1")
    List<LabCategoryMaster> findActiveLabCategoryByLabPrice(CategoryStatus status);
}