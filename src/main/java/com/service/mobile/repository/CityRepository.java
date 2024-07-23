package com.service.mobile.repository;

import com.service.mobile.model.City;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface CityRepository extends JpaRepository<City, Integer> {
    @Query("Select u from City u order by u.name asc")
    List<City> findAllByNameAsc();
}