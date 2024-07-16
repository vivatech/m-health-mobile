package com.service.mobile.repository;

import com.service.mobile.model.HealthTipPackage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface HealthTipPackageRepository extends JpaRepository<HealthTipPackage,Integer> {
}
