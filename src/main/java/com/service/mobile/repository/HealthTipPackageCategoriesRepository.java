package com.service.mobile.repository;

import com.service.mobile.model.HealthTipPackageCategories;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface HealthTipPackageCategoriesRepository extends JpaRepository<HealthTipPackageCategories, Integer> {
    @Query("Select u from HealthTipPackageCategories u where u.healthTipPackage.packageId in ?1")
    List<HealthTipPackageCategories> findByPackageIds(List<Integer> healthTipsId);

    @Query("Select u.healthTipCategoryMaster.categoryId from HealthTipPackageCategories u where u.healthTipPackage.packageId in ?1")
    List<Integer> findCategoriesIdsByPackageIds(List<Integer> healthTipPackageIds);

    @Query("Select u from HealthTipPackageCategories u where u.healthTipCategoryMaster.categoryId = ?1")
    Optional<HealthTipPackageCategories> findByCategoriesId(Integer categoryId);
}