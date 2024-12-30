package com.service.mobile.repository;

import com.service.mobile.dto.enums.State;
import com.service.mobile.model.NurseServiceState;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;

public interface NurseServiceStateRepository extends JpaRepository<NurseServiceState, Integer> {
    @Query("Select u from NurseServiceState u where u.searchId = ?1 ORDER BY u.id DESC LIMIT 1")
    NurseServiceState findBySearchId(String searchId);

    @Query("Select u from NurseServiceState u where u.orderId = ?1")
    NurseServiceState findByOrderId(Integer id);

    @Query("SELECT SUM(n.nRating) FROM NurseServiceState n WHERE n.nurseId = ?1")
    Integer findRatingSumByNurseId(Integer id);

    @Query("SELECT COUNT(n.nRating) FROM NurseServiceState n WHERE n.nurseId = ?1 AND n.nRating IS NOT NULL")
    Integer findRatingCountByNurseId(Integer id);

    @Query("SELECT u FROM NurseServiceState u WHERE u.state = ?2 AND u.patientId = ?1 " +
            " AND u.nRating IS NULL AND u.ratingNotifiedToPatient = 0 " +
            "AND u.completedAt BETWEEN ?3 AND ?4 ORDER BY u.id DESC LIMIT 1")
    NurseServiceState findDataByDate(Integer userId, State state, LocalDateTime sd, LocalDateTime ed);
}