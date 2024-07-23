package com.service.mobile.repository;

import com.service.mobile.model.Language;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface LanguageRepository extends JpaRepository<Language, Integer> {
    @Query("Select u from Language u where u.status LIKE ?1")
    List<Language> findAllByStatus(String a);
}