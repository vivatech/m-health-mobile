package com.service.mobile.repository;

import com.service.mobile.model.StaticPage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StaticPageRepository extends JpaRepository<StaticPage, Integer> {
    StaticPage findByName(String name);
}
