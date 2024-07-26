package com.service.mobile.repository;

import com.service.mobile.dto.enums.UserType;
import com.service.mobile.model.DoctorAvailability;
import com.service.mobile.model.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface DoctorAvailabilityRepository extends JpaRepository<DoctorAvailability, Integer> {
    List<DoctorAvailability> findByDoctorId(Users doctor);

    @Query("Select u from DoctorAvailability u where u.slotId.slotId = ?1 and u.doctorId.type = ?2")
    List<DoctorAvailability> findBySlotIdAndUserType(Integer slotId, UserType userType);
}