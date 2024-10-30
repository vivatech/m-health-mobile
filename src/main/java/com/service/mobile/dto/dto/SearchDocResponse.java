package com.service.mobile.dto.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SearchDocResponse {
    private int id;
    private String name;
    private int cases;
    private String about_me;
    private String experience;
    private String profile_picture;
    private float rating;
    private int max_fees;
    private int review;
    private int hospital_id;
    private String hospital_name;
    private String speciality;
    private List<String> language;
    private ConsultationFees consultation_fees;
    private boolean isAvailableToday = false;
    private Integer total_count;
}
