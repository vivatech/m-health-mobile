package com.service.mobile.repository;

import com.service.mobile.model.State;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface StateRepository extends JpaRepository<State, Integer> {
    @Query("Select u from State u order by u.name asc")
    List<State> findAllByNameAsc();
}