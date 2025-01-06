package com.service.mobile.repository;

import com.service.mobile.dto.enums.ConsultationType;
import com.service.mobile.dto.enums.RequestType;
import com.service.mobile.model.Consultation;
import com.service.mobile.model.Users;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

public interface ConsultationRepository extends JpaRepository<Consultation, Integer> {
    //TODO fix this query (this is based on date we need time)
    @Query("SELECT c FROM Consultation c WHERE c.patientId.userId = ?1 AND c.requestType IN ('Book', 'Cancel', 'Pending', 'Failed') AND c.consultationDate >= ?2 ORDER BY c.caseId DESC LIMIT 5")
    List<Consultation> findUpcomingConsultationsForPatient(Integer userId, LocalDate date);

    @Query("SELECT c FROM Consultation c WHERE c.doctorId.userId = ?1 AND c.requestType IN ('Book', 'Cancel') AND TO_TIMESTAMP(CONCAT(c.consultationDate, ' ', c.slotId.slotTime)) > CURRENT_TIMESTAMP ORDER BY c.caseId DESC")
    List<Consultation> findUpcomingConsultationsForDoctor(Integer doctorId);

    @Query("SELECT COUNT(c) FROM Consultation c WHERE c.doctorId.userId = ?1 AND c.requestType IN ?2 AND c.consultationDate <= CURRENT_DATE()")
    int findTotalCases(Integer userId, List<RequestType> types);

    List<Consultation> findByDoctorIdAndConsultationDate(Users doctor, LocalDate requiredDate);

    @Query("Select u from Consultation u where u.slotId.slotId = ?1 and u.consultationDate=?2 and u.doctorId.userId = ?3 and u.requestType in ?4")
    List<Consultation> findBySlotDateAndDoctorAndRequestType(Integer slotId, LocalDate date, Integer doctorId, List<RequestType> requestTypes);

    @Query("Select u from Consultation u where u.slotId.slotId = ?1 and u.consultationDate=?2 and u.patientId.userId = ?3 and u.requestType in ?4")
    List<Consultation> findBySlotDateAndPatientAndRequestType(Integer slotId, LocalDate date, Integer patientId, List<RequestType> requestTypes);

    @Query("Select u from Consultation u where u.slotId.slotId = ?2 and u.consultationDate=?4 and u.doctorId.userId = ?1 and u.requestType = ?3")
    List<Consultation> findByDoctorIdAndSlotIdAndRequestTypeAndDate(Integer doctorId, Integer slotId, RequestType requestType, LocalDate date);

    @Query("Select u from Consultation u where u.doctorId.userId = ?1 and  u.slotId.slotId = ?2 and u.consultationDate=?3 and u.requestType = ?4")
    List<Consultation> findByDoctorIdAndSlotIdAndRequestTypeAndDate(Integer doctorId, Integer slotId, LocalDate date, RequestType requestType);

    @Query("Select u from Consultation u where u.patientId.userId = ?1 and u.reportSuggested like ?2 and u.requestType =?3 and CONCAT(u.doctorId.firstName,' ', u.doctorId.lastName) like %?4% order by u.caseId DESC")
    Page<Consultation> findByPatientReportSuggestedAndRequestTypeAndName(Integer userId, String number, RequestType requestType,String name,Pageable pageable);

    @Query("Select u from Consultation u where u.patientId.userId = ?1 and u.reportSuggested like ?2 and u.requestType =?3 and CONCAT(u.doctorId.firstName,' ', u.doctorId.lastName) like %?4% and u.consultationDate = ?5 order by u.caseId DESC")
    Page<Consultation> findByPatientReportSuggestedAndRequestTypeAndNameAndDate(Integer userId, String number, RequestType requestType, String name, LocalDate date, Pageable pageable);

    @Query("Select u from Consultation u where u.patientId.userId = ?1 AND u.consultationDate = CURRENT_DATE and u.requestType IN ('Book','Pending')")
    List<Consultation> findUpcomingConsultationForPatient(Integer userId, LocalDate localDate);

    @Query("Select u from Consultation u where u.patientId.userId = ?1 order by u.caseId DESC")
    Page<Consultation> findByPatientIdOrderByCaseId(Integer userId,Pageable pageable);

    @Query("Select u from Consultation u where u.patientId.userId = ?1 and u.consultationDate = ?2 order by u.caseId DESC")
    List<Consultation> findByPatientIdAndDateOrderByCaseId(Integer userId, LocalDate date);

    @Query("Select u from Consultation u where u.doctorId.userId = ?1 and u.consultationDate = ?2 order by u.caseId DESC")
    List<Consultation> findByDoctorIdAndDateOrderByCaseId(Integer userId, LocalDate date);

    @Query("Select count(u.caseId) from Consultation u where u.requestType in ?1 and u.slotId.slotId in ?2 and u.doctorId.userId = ?3 and u.consultationDate = ?4")
    Long countByRequestTypeAndSlotIdAndDoctorIdAndConsultationDate(List<RequestType> list, List<Integer> allocatedSlots, Integer doctorId, LocalDate consultationDate);

    @Query("Select u from Consultation u where u.requestType = ?1 and u.createdAt >= DATE(?2) and u.patientId.userId = ?3 and" +
            " u.doctorId.userId = ?4 and u.consultationType = ?5 and u.consultationDate <= ?6 order by u.consultationDate, u.createdAt DESC")
    List<Consultation> findByRequestTypeAndCreatedAtAndPatientIdAndDoctorIdAndConstaitionTypeAndConstationDate(
            RequestType requestType,LocalDate newOrderDate, Integer patient, Integer doctor
            , ConsultationType consultationType,
            LocalDate consultationDate);

    @Query("Select count(u.caseId) from Consultation u where u.patientId.userId = ?1 and u.doctorId.userId = ?2 and " +
            "u.requestType != ?4 and u.consultationDate >= ?3 and u.consultationType = ?5")
    Long countByPatientIdAndDoctorIdAndConsultationDateAndConsultationTypeAndRequestType(
            Users patientId, Integer userId,
            LocalDate consultationDate, RequestType requestType, ConsultationType type);

    @Query("SELECT COUNT(u.caseId) FROM Consultation u WHERE u.patientId.userId = ?1 AND u.doctorId.userId = ?2 AND " +
            "DATE(u.createdAt) >= ?3 AND u.consultationType = ?4 AND u.consultType = ?5 AND " +
            "u.consultationDate >= ?6")
    Long countByPatientIdAndDoctorIdCreatedAtAndConstaitionTypeConsultTypeAndConstationDate(
            Integer patient, Integer doctor,LocalDate createdAt
            , ConsultationType consultationType, String consultType,
            LocalDate consultationDate);

    @Query("Select count(u.caseId) from Consultation u where u.slotId.slotId = ?1 and u.doctorId.userId = ?2 and " +
            " u.consultationDate = ?3")
    Long countBySlotIdAndDoctorIdConsultationDate(Integer slotId, Integer doctorId, LocalDate date);

    @Query("SELECT c FROM Consultation c WHERE c.patientId.userId = ?1 AND c.consultationDate >= ?2 ORDER BY c.caseId DESC")
    Page<Consultation> findByPatientIdAndConsultationDate(Integer userId, LocalDate localDate, Pageable pageable);
}