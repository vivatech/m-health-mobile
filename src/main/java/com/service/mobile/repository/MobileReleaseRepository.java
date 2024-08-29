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

    @Query("SELECT m FROM MobileRelease m WHERE m.appVersion = :appVersion AND m.deviceType = :deviceType AND m.userType = :type" )
    MobileRelease findByAppVersionAndDeviceTypeAndType(@Param("appVersion") String appVersion, @Param("deviceType") DeviceType deviceType, @Param("type") UserType type);
}
