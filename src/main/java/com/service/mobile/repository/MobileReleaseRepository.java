package com.service.mobile.repository;

import com.service.mobile.dto.enums.DeviceType;
import com.service.mobile.dto.enums.UserType;
import com.service.mobile.model.MobileRelease;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface MobileReleaseRepository extends JpaRepository<MobileRelease,Integer> {

    @Query(value = "SELECT m.* FROM mh_mobile_release m WHERE m.app_version = ?1 AND m.device_type = ?2 AND m.user_type = ?3", nativeQuery = true)
    MobileRelease findByAppVersionAndDeviceTypeAndType(@Param("appVersion") String appVersion, @Param("deviceType") String deviceType, @Param("type") String type);
}
