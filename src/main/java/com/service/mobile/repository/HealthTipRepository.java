package com.service.mobile.repository;

import com.service.mobile.model.HealthTip;
import com.service.mobile.model.HealthTipPackage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface HealthTipRepository extends JpaRepository<HealthTip, Integer> {
    @Query("Select u from HealthTip u where u.healthTipCategory.categoryId in ?1")
    List<HealthTip> findByCategorisIds(List<Integer> categoriesIds);

    @Query("Select u from HealthTip u where u.topic like %?1% and u.healthTipCategory.categoryId in ?2 and u.healthTipCategory.categoryId = ?3")
    Page<HealthTip> findByTitlePackageCategories(String title, List<Integer> packageId, Integer categoryId, Pageable pageable);

    @Query("Select u from HealthTip u where u.topic like %?1% and u.healthTipCategory.categoryId in ?2")
    Page<HealthTip> findByTitleCategories(String title, List<Integer> categoriesIds, Pageable pageable);

    @Query("Select u from HealthTip u where u.topic like %?1% and u.healthTipCategory.categoryId = ?2")
    Page<HealthTip> findByTitleCategorieId(String title, Integer categoryId, Pageable pageable);
}