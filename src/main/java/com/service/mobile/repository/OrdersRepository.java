package com.service.mobile.repository;

import com.service.mobile.model.Orders;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface OrdersRepository extends JpaRepository<Orders, Integer> {
    @Query("SELECT o FROM Orders o WHERE o.id = :orderId")
    Optional<Orders> findById(Integer orderId);

    @Query("SELECT o FROM Orders o JOIN FETCH o.transactionList t JOIN FETCH o.caseId c JOIN FETCH c.slotDetail JOIN FETCH o.doctorId d WHERE o.id = :orderId")
    Orders findDetailedOrder(Integer orderId);

    @Query("SELECT o FROM Orders o JOIN FETCH o.transactionList t JOIN FETCH o.packageId p WHERE o.id = :orderId")
    Orders findBasicOrder(Integer orderId);
}