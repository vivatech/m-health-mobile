package com.service.mobile.dto.dto;

import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class UserRelativeDto {
    private Byte id;
    private Integer user_id;
    private String name;
    private String profile_picture;
    private LocalDate dob;
    private String relation_with_patient;
    private String status;
    private Integer created_by;
    private LocalDateTime created_at;
    private LocalDateTime updated_at;
}
