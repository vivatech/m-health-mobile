package com.service.mobile.repository;

import com.service.mobile.dto.enums.Status;
import com.service.mobile.model.HealthTipPackageCategories;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface HealthTipPackageCategoriesRepository extends JpaRepository<HealthTipPackageCategories, Integer> {
    @Query("Select u from HealthTipPackageCategories u where u.healthTipPackage.packageId in ?1 order by u.healthTipPackageCategoryId DESC")
    List<HealthTipPackageCategories> findByPackageIds(List<Integer> healthTipsId);

    @Query("Select u.healthTipCategoryMaster.categoryId from HealthTipPackageCategories u where u.healthTipPackage.packageId in ?1 ORDER BY u.healthTipPackageCategoryId DESC")
    List<Integer> findCategoriesIdsByPackageIds(List<Integer> healthTipPackageIds);

    @Query("Select u from HealthTipPackageCategories u where u.healthTipCategoryMaster.categoryId = ?1")
    Optional<HealthTipPackageCategories> findByCategoriesId(Integer categoryId);

    @Query("Select u from HealthTipPackageCategories u where u.healthTipCategoryMaster.status = ?1 and " +
            "u.healthTipPackage.packagePrice >= ?2 and u.healthTipPackage.packagePrice <= ?3 and " +
            "u.healthTipCategoryMaster.categoryId in ?4 order by u.healthTipPackage.packagePrice ASC")
    Page<HealthTipPackageCategories> findByStatusPriceFromToCategoryIdsAndPriceAndSort(Status status, Float fromPrice,
                                                                                       Float toPrice, String[] catIds,
                                                                                       Pageable pageable);

    @Query("Select u from HealthTipPackageCategories u where u.healthTipCategoryMaster.status = ?1 and " +
            "u.healthTipPackage.packagePrice >= ?2 and u.healthTipPackage.packagePrice <= ?3 and " +
            "u.healthTipCategoryMaster.categoryId in ?4 order by u.healthTipPackage.packagePrice DESC")
    Page<HealthTipPackageCategories> findByStatusPriceFromToCategoryIdsAndPriceAndSortDesc(Status status, Float fromPrice,
                                                                                       Float toPrice, String[] catIds,
                                                                                       Pageable pageable);

    @Query("Select u from HealthTipPackageCategories u where u.healthTipCategoryMaster.status = ?1 and " +
            "u.healthTipPackage.packagePrice >= ?2 and u.healthTipPackage.packagePrice <= ?3 and " +
            "u.healthTipCategoryMaster.categoryId in ?4 order by u.healthTipCategoryMaster.priority")
    Page<HealthTipPackageCategories> findByStatusPriceFromToCategoryIdsAndPriceAndSortPriority(Status status,
                                                                                               Float fromPrice, Float toPrice,
                                                                                               String[] catIds, Pageable pageable);

    @Query("Select u from HealthTipPackageCategories u where u.healthTipCategoryMaster.status = ?1 and " +
            "u.healthTipPackage.packagePrice >= ?2 and u.healthTipPackage.packagePrice <= ?3 " +
            " order by u.healthTipPackage.packagePrice ASC")
    Page<HealthTipPackageCategories> findByStatusPriceFromToAndPriceAndSort(Status status,
                                                                            Float fromPrice, Float toPrice,
                                                                            String sortByPrice, Pageable pageable);

    @Query("Select u from HealthTipPackageCategories u where u.healthTipCategoryMaster.status = ?1 and " +
            "u.healthTipPackage.packagePrice >= ?2 and u.healthTipPackage.packagePrice <= ?3 " +
            " order by u.healthTipPackage.packagePrice DESC")
    Page<HealthTipPackageCategories> findByStatusPriceFromToAndPriceAndSortDesc(Status status,
                                                                            Float fromPrice, Float toPrice,
                                                                            String sortByPrice, Pageable pageable);

    @Query("Select u from HealthTipPackageCategories u where u.healthTipCategoryMaster.status = ?1 and " +
            "u.healthTipPackage.packagePrice >= ?2 and u.healthTipPackage.packagePrice <= ?3 " +
            " order by u.healthTipCategoryMaster.priority")
    Page<HealthTipPackageCategories> findByStatusPriceFromToAndPriceAndSortPriority(Status status,
                                                                                    Float fromPrice, Float toPrice, Pageable pageable);


    @Query("Select u from HealthTipPackageCategories u where u.healthTipCategoryMaster.status = ?1 and " +
            "u.healthTipCategoryMaster.categoryId in ?2 order by u.healthTipPackage.packagePrice ASC")
    Page<HealthTipPackageCategories> findByStatusCategoryIdsAndPriceAndSort(Status status, List<Integer> catIds,
                                                                             Pageable pageable);

    @Query("Select u from HealthTipPackageCategories u where u.healthTipCategoryMaster.status = ?1 and " +
            "u.healthTipCategoryMaster.categoryId in ?2 order by u.healthTipPackage.packagePrice DESC")
    Page<HealthTipPackageCategories> findByStatusCategoryIdsAndPriceAndSortDesc(Status status, List<Integer> catIds,
                                                                                 Pageable pageable);

    @Query("Select u from HealthTipPackageCategories u where u.healthTipCategoryMaster.status = ?1 and " +
            "u.healthTipCategoryMaster.categoryId in ?2 order by u.healthTipCategoryMaster.priority")
    Page<HealthTipPackageCategories> findByStatusCategoryIdsAndPriceAndSortPriority(Status status, List<Integer> catIds, Pageable pageable);

    @Query("Select u from HealthTipPackageCategories u where u.healthTipCategoryMaster.status = ?1 " +
            " order by u.healthTipPackage.packagePrice ASC")
    Page<HealthTipPackageCategories> findByStatusAndPriceAndSort(Status status, Pageable pageable);


    @Query("Select u from HealthTipPackageCategories u where u.healthTipCategoryMaster.status = ?1 " +
            " order by u.healthTipPackage.packagePrice DESC")
    Page<HealthTipPackageCategories> findByStatusAndPriceAndSortDesc(Status status, Pageable pageable);

    @Query("Select u from HealthTipPackageCategories u where u.healthTipCategoryMaster.status = ?1 " +
            " order by u.healthTipCategoryMaster.priority")
    Page<HealthTipPackageCategories> findByStatusSortPriority(Status status, Pageable pageable);

    @Query("Select u.healthTipCategoryMaster.categoryId from HealthTipPackageCategories u where u.healthTipPackage.packageId in ?1")
    List<Integer> findCategoryIdsByPackageIds(List<Integer> packageIds);

    @Query("Select u from HealthTipPackageCategories u where u.healthTipPackage.packageId = ?1")
    List<HealthTipPackageCategories> findByPackageIds(Integer packageIds);

    @Query("Select u from HealthTipPackageCategories u where u.healthTipPackage.packageId = ?1 ORDER BY u.healthTipPackageCategoryId DESC LIMIT 1")
    HealthTipPackageCategories findByHealthTipPackage(Integer packageId);

    @Query("Select u.healthTipPackage.packageId from HealthTipPackageCategories u where u.healthTipCategoryMaster.categoryId IN ?1 ORDER BY u.healthTipPackageCategoryId DESC")
    List<Integer> findByCategoriesIds(List<Integer> categoryId);
}