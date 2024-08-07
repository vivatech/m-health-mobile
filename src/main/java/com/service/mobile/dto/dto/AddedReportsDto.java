package com.service.mobile.dto.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class AddedReportsDto {
    private Integer lab_consult_id;
    private Integer case_id;
    private Integer lab_orders_id;
    private Integer category_id;
    private Integer sub_cat_id;
    private Integer relative_id;
    private Integer lab_consult_patient_id;
    private Integer lab_consult_doctor_id;
    private String lab_consult_doc_prescription;
    private LocalDateTime lab_consult_created_at;
    private AddedReportsCategoryDTO category;
    private AddedReportsSubCategoryDTO subcategory;
}
