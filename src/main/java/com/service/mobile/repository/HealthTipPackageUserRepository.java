package com.service.mobile.repository;

import com.service.mobile.dto.enums.YesNo;
import com.service.mobile.model.HealthTipPackageUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface HealthTipPackageUserRepository extends JpaRepository<HealthTipPackageUser,Integer> {
    @Query("Select u from HealthTipPackageUser u where u.user.userId = ?1 and u.isExpire = ?2")
    List<HealthTipPackageUser> findByUserIdAndExpirey(Integer userId, YesNo yesNo);

    @Query("Select u.id from HealthTipPackageUser u where u.user.userId = ?1 and u.isExpire = ?2")
    List<Integer> getIdByUserIdAndExpiery(Integer userId, YesNo yesNo);

    @Query("SELECT p FROM HealthTipPackageUser p WHERE p.user.userId = ?1 AND p.healthTipPackage.packageId = ?2 AND p.isExpire = 'No'")
    HealthTipPackageUser findActivePackageForUser(Integer userId, Integer categoryId);

    @Query("Select u from HealthTipPackageUser u where u.id = ?1 and u.isExpire = ?2")
    Optional<HealthTipPackageUser> getByIdAndExpiry(Integer userId, YesNo yesNo);

    @Query("Select u.healthTipPackage.packageId from HealthTipPackageUser u where u.id = ?1 and u.isExpire = ?2")
    List<Integer> findPackageIdsByUserIdAndExpire(int userId, YesNo yesNo);

    @Query("Select u from HealthTipPackageUser u where u.user.userId = ?1 and u.healthTipPackage.packageId = ?2")
    Optional<HealthTipPackageUser> findByUserIdAndPackageId(Integer userId, Integer categoryId);
}
