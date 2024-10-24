package com.service.mobile.repository;

import com.service.mobile.model.State;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface StateRepository extends JpaRepository<State, Integer> {

    @Query("SELECT s FROM State s WHERE s.country IS NOT NULL")
    List<State> findAllWithCountry();
}