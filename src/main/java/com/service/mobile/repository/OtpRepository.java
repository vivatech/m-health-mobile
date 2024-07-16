package com.service.mobile.repository;

import com.service.mobile.model.UserOTP;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OtpRepository extends JpaRepository<UserOTP,Integer> {
    UserOTP findFirstByUserIdOrderByIdDesc(Integer userId);
}
