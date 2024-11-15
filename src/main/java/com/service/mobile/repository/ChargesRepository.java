package com.service.mobile.repository;

import com.service.mobile.dto.enums.FeeType;
import com.service.mobile.model.Charges;
import com.service.mobile.model.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChargesRepository extends JpaRepository<Charges,Integer> {

    @Query("Select MAX(finalConsultationFees) from Charges u")
    Integer getMaxConsultationFees();

    @Query("Select u from Charges u where u.userId = ?1")
    List<Charges> findByUserId(Integer val);

    @Query("Select u from Charges u where u.userId = ?1 and u.feeType = ?2")
    List<Charges> findByUserIdAndConsultantType(Integer doctorId, FeeType consultType);

    @Query(value = "SELECT * FROM mh_charges WHERE user_id = ?1 AND fee_type = ?2 ORDER BY charge_id DESC LIMIT 1", nativeQuery = true)
    Charges findByUserIdAndFeeType(Users doctor, FeeType type);
}
