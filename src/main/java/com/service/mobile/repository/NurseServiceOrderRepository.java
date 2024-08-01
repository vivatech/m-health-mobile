package com.service.mobile.repository;

import com.service.mobile.model.NurseServiceOrder;
import com.service.mobile.model.NurseServiceOrderKey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface NurseServiceOrderRepository extends JpaRepository<NurseServiceOrder, NurseServiceOrderKey> {
    @Query("Select u from NurseServiceOrder u where u.id.orderId = ?1")
    List<NurseServiceOrder> findByOrderId(Integer id);
}