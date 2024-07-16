package com.service.mobile.repository;

import com.service.mobile.model.UsersCreatedWithPromoCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UsersCreatedWithPromoCodeRepository extends JpaRepository<UsersCreatedWithPromoCode,Integer> {
}
