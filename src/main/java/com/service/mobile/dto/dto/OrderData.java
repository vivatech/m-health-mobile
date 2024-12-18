package com.service.mobile.dto.dto;

import com.service.mobile.dto.enums.RequestType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class OrderData {
    private Integer id;
    private Integer case_id;
    private Integer doctor_id;
    private String photo;
    private String transaction_id;
    private LocalDate consultation_date;
    private String report_suggested;
    private String consultation_type;
    private String rec_consultation_type;
    private String added_type;
    private String slot_time;
    private String specialization;
    private String package_name;
    private String doctor_name;
    private String created_at;
    private String rating;
    private String review;
    private Integer is_rating;
    private String pdf_url;
    private RequestType status;
    private String cancel_message;
    private Long total_count;
}
