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
    private String sort_by;
    private String availability;
    private Float fees;
    private String fee_type;
    private Integer hospital_id;

    private String doctor_name;
    private Integer city_id;
    private List<Integer> specialization_id;
    private Integer language_fluency;

    private String consult_type;
    private Integer clinic_id;


    private Integer page;
    private Integer pageSize;
}
