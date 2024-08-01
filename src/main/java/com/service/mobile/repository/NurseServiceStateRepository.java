package com.service.mobile.repository;

import com.service.mobile.model.NurseServiceState;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface NurseServiceStateRepository extends JpaRepository<NurseServiceState, Integer> {
    @Query("Select u from NurseServiceState u where u.searchId = ?1")
    List<NurseServiceState> findBySearchId(String searchId);

    @Query("Select u from NurseServiceState u where u.orderId = ?1")
    List<NurseServiceState> findByOrderId(Integer id);
}