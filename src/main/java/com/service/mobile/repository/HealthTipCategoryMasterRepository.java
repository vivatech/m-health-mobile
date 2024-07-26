package com.service.mobile.repository;

import com.service.mobile.model.HealthTipCategoryMaster;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface HealthTipCategoryMasterRepository extends JpaRepository<HealthTipCategoryMaster, Integer> {
}