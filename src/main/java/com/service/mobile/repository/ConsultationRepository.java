package com.service.mobile.repository;

import com.service.mobile.dto.enums.RequestType;
import com.service.mobile.model.Consultation;
import com.service.mobile.model.Users;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;

public interface ConsultationRepository extends JpaRepository<Consultation, Integer> {
    @Query("SELECT c FROM Consultation c WHERE c.patientId.userId = ?1 AND c.requestType IN ('Book', 'Cancel', 'Pending', 'Failed') AND TO_TIMESTAMP(CONCAT(c.consultationDate, ' ', c.slotId.slotTime)) > CURRENT_TIMESTAMP ORDER BY c.caseId DESC")
    List<Consultation> findUpcomingConsultationsForPatient(Integer userId);

    @Query("SELECT c FROM Consultation c WHERE c.doctorId.userId = ?1 AND c.requestType IN ('Book', 'Cancel') AND TO_TIMESTAMP(CONCAT(c.consultationDate, ' ', c.slotId.slotTime)) > CURRENT_TIMESTAMP ORDER BY c.caseId DESC")
    List<Consultation> findUpcomingConsultationsForDoctor(Integer doctorId);

    @Query(value = "SELECT COUNT(*) FROM mh_consultation c WHERE c.doctor_id = :userId", nativeQuery = true)
    int findTotalCases(Integer userId);

    List<Consultation> findByDoctorIdAndConsultationDate(Users doctor, LocalDate requiredDate);

    @Query("Select u from Consultation u where u.slotId.slotId = ?1 and u.consultationDate=?2 and u.doctorId.userId = ?3 and u.requestType in ?4")
    List<Consultation> findBySlotDateAndDoctorAndRequestType(Integer slotId, LocalDate date, Integer doctorId, List<RequestType> requestTypes);

    @Query("Select u from Consultation u where u.slotId.slotId = ?1 and u.consultationDate=?2 and u.patientId.userId = ?3 and u.requestType in ?4")
    List<Consultation> findBySlotDateAndPatientAndRequestType(Integer slotId, LocalDate date, Integer patientId, List<RequestType> requestTypes);

    @Query("Select u from Consultation u where u.slotId.slotId = ?2 and u.consultationDate=?4 and u.doctorId.userId = ?1 and u.requestType = ?3")
    List<Consultation> findByDoctorIdAndSlotIdAndRequestTypeAndDate(Integer doctorId, Integer slotId, RequestType requestType, LocalDate date);

    @Query("Select u from Consultation u where u.patientId.userId = ?1 and u.reportSuggested like ?2 and u.requestType =?3 order by u.caseId and CONCAT(u.doctorId.first_name,' ', u.doctorId.last_name) like %?4% DESC")
    List<Consultation> findByPatientReportSuggestedAndRequestTypeAndName(Integer userId, String number, RequestType requestType,String name,Pageable pageable);

    @Query("Select u from Consultation u where u.patientId.userId = ?1 and u.reportSuggested like ?2 and u.requestType =?3 order by u.caseId and CONCAT(u.doctorId.first_name,' ', u.doctorId.last_name) like %?4% and u.consultationDate = ?5 DESC")
    List<Consultation> findByPatientReportSuggestedAndRequestTypeAndNameAndDate(Integer userId, String number, RequestType requestType, String name, LocalDate date, Pageable pageable);
}