package com.service.mobile.repository;

import com.service.mobile.dto.enums.Status;
import com.service.mobile.model.Specialization;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface SpecializationRepository extends JpaRepository<Specialization, Integer> {
    @Query("Select u from Specialization u where u.status = ?1")
    List<Specialization> findAllByStatus(Status a);
}