package com.service.mobile.repository;

import com.service.mobile.model.UserInitiated;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserInitiatedRepository extends JpaRepository<UserInitiated, Integer> {
    UserInitiated findByMobileNumber(Integer contactNumber);
}