package com.service.mobile.repository;

import com.service.mobile.model.SmsLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SmsLogRepository extends JpaRepository<SmsLog, Integer> {
}