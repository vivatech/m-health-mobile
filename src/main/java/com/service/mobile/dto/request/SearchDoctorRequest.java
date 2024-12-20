package com.service.mobile.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SearchDoctorRequest {
    private Integer user_id;
    private String doctor_name;
    private String degree_id;
    private Integer city_id;
    private List<Integer> specialization_id;
    private String fees;
    private String fee_type;
    private String language_fluency;
    private List<Integer> hospital_id;
    private Integer availability;
    private Integer sort_by;
    private String clinic_id;
    private String is_international;
    private String consult_type;
    private String is_enterprise;
    private Integer page;
}
