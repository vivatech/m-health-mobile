package com.service.mobile.repository;

import com.service.mobile.model.WalletTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface WalletTransactionRepository extends JpaRepository<WalletTransaction, Long> {
    @Query("Select count(u.id) from WalletTransaction u where u.patientId.userId = ?1")
    Long countByPatientId(Integer userId);
}