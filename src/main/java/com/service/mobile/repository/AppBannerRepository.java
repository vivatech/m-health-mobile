package com.service.mobile.repository;

import com.service.mobile.model.AppBanner;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface AppBannerRepository extends JpaRepository<AppBanner, Integer> {
    @Query("Select u from AppBanner u order by u.id desc")
    List<AppBanner> findAllByIdDesc();
}