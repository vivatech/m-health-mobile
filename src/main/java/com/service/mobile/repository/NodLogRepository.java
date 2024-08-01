package com.service.mobile.repository;

import com.service.mobile.model.NodLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NodLogRepository extends JpaRepository<NodLog, Integer> {
}