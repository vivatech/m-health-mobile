package com.service.mobile.repository;

import com.service.mobile.model.AuthKey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AuthKeyRepository extends JpaRepository<AuthKey,Integer> {
}
