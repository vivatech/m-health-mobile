package com.service.mobile.repository;

import com.service.mobile.dto.enums.RequestType;
import com.service.mobile.model.SlotMaster;
import com.service.mobile.model.SlotType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.sql.Time;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Collection;
import java.util.List;

public interface SlotMasterRepository extends JpaRepository<SlotMaster, Integer> {

    @Query("Select u from SlotMaster u where u.slotType.id= ?1 and u.slotDay = ?2 and u.slotStartTime in ?3")
    Collection<SlotMaster> findBySlotTypeIdAndSlotDayAndSlotStartTimeIn(Integer id, String slotDay, List<LocalTime> slotStartTime);

    @Query("Select u from SlotMaster u where u.slotType.id= ?1 and u.slotDay LIKE ?2 ORDER BY u.slotId ASC")
    List<SlotMaster> findBySlotTypeIdAndSlotDay(Integer slotTypeId, String date);

    @Query("Select u.slotId from SlotMaster u where u.slotDay IN ?1 AND u.slotType.id = 4 and u.slotStartTime > ?2 AND u.slotId NOT IN (SELECT c.slotId.slotId FROM Consultation c WHERE c.requestType IN ?4 AND c.consultationDate >= ?3) ORDER BY u.slotStartTime ASC")
    List<Integer> findBySlotDayAndSlotStartTime(String[] dayName, LocalTime time, LocalDate date, List<RequestType> type);

    @Query("Select u.slotId from SlotMaster u where u.slotDay IN ?1 AND u.slotType.id = 4 AND u.slotId NOT IN (SELECT c.slotId.slotId FROM Consultation c WHERE c.requestType IN ?4 AND c.consultationDate >= ?2 AND c.consultationDate <= ?3) ORDER BY u.slotStartTime ASC")
    List<Integer> findBySlotDay(String[] dayName, LocalDate startDate, LocalDate endDate, List<RequestType> type);
}