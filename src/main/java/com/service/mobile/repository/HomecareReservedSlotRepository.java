package com.service.mobile.repository;

import com.service.mobile.model.HomecareReservedSlot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;

public interface HomecareReservedSlotRepository extends JpaRepository<HomecareReservedSlot, Integer> {
    @Query("Select count(u.id) from HomecareReservedSlot u where u.slotId = ?1 and u.consultDate = ?2")
    Long countBySlotIdAndConsultDate(Integer slotId, LocalDate consultationDate);
}