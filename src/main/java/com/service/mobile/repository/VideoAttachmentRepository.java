package com.service.mobile.repository;

import com.service.mobile.model.VideoAttachment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface VideoAttachmentRepository extends JpaRepository<VideoAttachment, Integer> {
    @Query("Select u from VideoAttachment u where u.caseId = ?1")
    List<VideoAttachment> findByCaseId(Integer caseId);
}