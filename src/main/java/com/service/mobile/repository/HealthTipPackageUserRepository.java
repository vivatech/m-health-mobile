package com.service.mobile.repository;

import com.service.mobile.dto.enums.YesNo;
import com.service.mobile.model.HealthTipPackageUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HealthTipPackageUserRepository extends JpaRepository<HealthTipPackageUser,Integer> {
    @Query("Select u from HealthTipPackageUser u where u.user.userId = ?1 and u.isExpire = ?2")
    List<HealthTipPackageUser> findByUserIdAndExpirey(Integer userId, YesNo yesNo);

    @Query("Select u.id from HealthTipPackageUser u where u.user.userId = ?1 and u.isExpire = ?2")
    List<Integer> getIdByUserIdAndExpiery(Integer userId, YesNo yesNo);
}
