package com.service.mobile.dto.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

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
    private int rating;
    private int max_fees;
    private long review;
    private int hospital_id;
    private String hospital_name;
    private String speciality;
    private List<String> languages;
    private Map<String, String> consultation_fees;
    private Integer total_count;
}
