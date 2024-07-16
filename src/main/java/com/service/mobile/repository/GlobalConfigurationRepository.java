package com.service.mobile.repository;

import com.service.mobile.model.GlobalConfiguration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public interface GlobalConfigurationRepository extends JpaRepository<GlobalConfiguration,Integer> {

    @Query("SELECT m FROM GlobalConfiguration m WHERE m.key LIKE :value")
    GlobalConfiguration findByKey(@Param("value") String value);

    List<GlobalConfiguration> findByKeyIn(List<String> turnPassword);
}
