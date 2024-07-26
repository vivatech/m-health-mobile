package com.service.mobile.repository;

import com.service.mobile.dto.enums.Status;
import com.service.mobile.dto.enums.UserType;
import com.service.mobile.model.LabPrice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface LabPriceRepository extends JpaRepository<LabPrice, Integer> {

    @Query("Select u from LabPrice u where u.subCatId.subCatId = ?1 and u.labUser.type = ?2 and u.labUser.status = ?3")
    List<LabPrice> findBySubCatIdAndUserTypeAndStatus(List<Integer> labcatIds, UserType userType, Status status);

    @Query("Select u from LabPrice u where u.labUser.userId = ?1 and u.subCatId.subCatId = ?2")
    List<LabPrice> findByLabIdAndSubCatId(Integer labId, Integer subCatId);
}