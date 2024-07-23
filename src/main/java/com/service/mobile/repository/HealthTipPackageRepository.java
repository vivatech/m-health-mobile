package com.service.mobile.repository;

import com.service.mobile.model.HealthTipPackage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface HealthTipPackageRepository extends JpaRepository<HealthTipPackage,Integer> {
    @Query("SELECT MAX(p.packagePrice) FROM HealthTipPackage p")
    Optional<Double> findMaxPackagePrice();

    @Query("SELECT p FROM HealthTipPackage p where p.packageId = ?1")
    List<HealthTipPackage> findByPackageId(Integer packageId);
}
