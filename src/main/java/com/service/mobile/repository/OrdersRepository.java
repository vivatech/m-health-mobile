package com.service.mobile.repository;

import com.service.mobile.model.Orders;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.Optional;

public interface OrdersRepository extends JpaRepository<Orders, Integer> {
    @Query("SELECT o FROM Orders o WHERE o.id = ?1")
    Optional<Orders> findById(Integer orderId);

    @Query("SELECT o FROM Orders o WHERE o.caseId.caseId = ?1")
    Orders findByCaseId(Integer caseId);

    @Query("Select u from Orders u where u.caseId.consultationDate = ?1 and u.caseId.caseId = ?2 And " +
            "CONCAT(u.doctorId.firstName,' ', u.doctorId.lastName) like %?3% ")
    Page<Orders> findByConsultationDateAndCaseIdAndDoctorName(LocalDate consultationDate, Integer caseId,
                                                              String doctorName, Pageable pageable);

    @Query("Select u from Orders u where u.caseId.consultationDate = ?1 And " +
            "CONCAT(u.doctorId.firstName,' ', u.doctorId.lastName) like %?2% ")
    Page<Orders> findByConsultationDateAndDoctorName(LocalDate consultationDate,
                                                              String doctorName, Pageable pageable);

    @Query("Select u from Orders u where u.caseId.caseId = ?1 And " +
            "CONCAT(u.doctorId.firstName,' ', u.doctorId.lastName) like %?2% ")
    Page<Orders> findByCaseIdAndDoctorName(Integer caseId, String doctorName, Pageable pageable);

    @Query("Select u from Orders u where CONCAT(u.doctorId.firstName,' ', u.doctorId.lastName) like %?1% ")
    Page<Orders> findByDoctorName(String doctorName, Pageable pageable);
}