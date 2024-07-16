package com.service.mobile.repository;

import com.service.mobile.model.HealthTipPackageCategories;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HealthTipPackageCategoriesRepository extends JpaRepository<HealthTipPackageCategories, Integer> {
    @Query("Select u from HealthTipPackageCategories u where u.healthTipPackage.packageId = ?1")
    List<HealthTipPackageCategories> findByPackageIds(List<Integer> healthTipsId);
}