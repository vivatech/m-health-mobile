package com.service.mobile.repository;

import com.service.mobile.dto.enums.SlotStatus;
import com.service.mobile.model.SlotType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface SlotTypeRepository extends JpaRepository<SlotType, Integer> {
    @Query("Select u from SlotType u where u.status = ?1")
    List<SlotType> findByStatus(SlotStatus active);

    @Query("Select u.value from SlotType u where u.status = 'active' AND u.id = ?1")
    String findMinutesBySlotType(int slotTypeId);
}