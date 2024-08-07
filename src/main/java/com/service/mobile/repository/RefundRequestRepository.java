package com.service.mobile.repository;

import com.service.mobile.model.RefundRequest;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RefundRequestRepository extends JpaRepository<RefundRequest, Integer> {
}