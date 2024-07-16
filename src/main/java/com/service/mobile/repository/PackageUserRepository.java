package com.service.mobile.repository;

import com.service.mobile.dto.enums.YesNo;
import com.service.mobile.model.PackageUser;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface PackageUserRepository extends JpaRepository<PackageUser, Integer> {

    @Query("Select u from PackageUser u where u.user.userId = ?1 and u.isExpire = ?2 order by u.id desc")
    List<PackageUser> getActivePackageDetail(Integer userId, YesNo expired, Pageable pageable);
}