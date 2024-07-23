package com.service.mobile.repository;

import com.service.mobile.model.ConsultationRating;
import com.service.mobile.model.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ConsultationRatingRepository extends JpaRepository<ConsultationRating, Integer> {
    List<ConsultationRating> findByDoctorId(Users val);

    @Query(value = "SELECT AVG(cr.rating) FROM mh_consultation_rating cr WHERE cr.doctor_id = :id", nativeQuery = true)
    float findDoctorRating(Integer userId);

    @Query(value = "SELECT COUNT(DISTINCT(cr.patient_id)) FROM mh_consultation_rating cr WHERE cr.doctor_id = :userId", nativeQuery = true)
    int findReviews(Integer userId);

    @Query("SELECT SUM(cr.rating) FROM ConsultationRating cr WHERE cr.doctorId.userId = ?1 AND cr.status = 'Approve'")
    Double sumRatingsByDoctorId(Integer doctorId);

    @Query("SELECT COUNT(cr.id) FROM ConsultationRating cr WHERE cr.doctorId.userId = ?1 AND cr.status = 'Approve'")
    Long countApprovedRatingsByDoctorId(Integer doctorId);

    @Query("SELECT cr FROM ConsultationRating cr WHERE cr.caseId = ?1")
    List<ConsultationRating> getByCaseId(Integer caseId);
}