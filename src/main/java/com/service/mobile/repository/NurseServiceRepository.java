package com.service.mobile.repository;

import com.service.mobile.model.NurseService;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface NurseServiceRepository extends JpaRepository<NurseService, Integer> {
    @Query("Select u from NurseService u where u.status LIKE ?1")
    List<NurseService> findByStatus(String a);

    @Query("Select u from NurseService u where u.status LIKE ?2 and u.id in ?1")
    List<NurseService> findByIdsAndStatus(List<Integer> serviceIntIds, String a);

    @Query("Select u from NurseService u where u.id in ?1")
    List<NurseService> findByIds(List<Integer> ids);

    @Query("SELECT s.seviceName FROM NurseService s WHERE s.id IN (SELECT o.id.serviceId FROM NurseServiceOrder o WHERE o.id.orderId = ?1) ")
    List<String> findByOrderId(Integer id);
}