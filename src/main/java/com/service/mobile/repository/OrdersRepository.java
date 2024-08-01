package com.service.mobile.repository;

import com.service.mobile.model.Orders;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface OrdersRepository extends JpaRepository<Orders, Integer> {
    @Query("SELECT o FROM Orders o WHERE o.id = ?1")
    Optional<Orders> findById(Integer orderId);

    @Query("SELECT o FROM Orders o WHERE o.caseId = ?1")
    Orders findByCaseId(Integer caseId);
}