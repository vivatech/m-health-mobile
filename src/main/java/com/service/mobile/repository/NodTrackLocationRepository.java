package com.service.mobile.repository;

import com.service.mobile.model.NodTrackLocation;
import com.service.mobile.model.PartnerNurse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface NodTrackLocationRepository extends JpaRepository<NodTrackLocation, Integer> {
    @Query("SELECT u FROM NodTrackLocation u WHERE u.patientId = ?1 AND u.searchId = ?2")
    NodTrackLocation findByPatientIdAndSearchId(Integer userId, String searchId);
}