package com.service.mobile.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class MyTransactionsResponse {
    private String title;
    private Integer case_id;
    private String transaction_id;
    private String contact_number;
    private String transaction_type;
    private String package_name;
    private String healthtips_package_name;
    private String doctor_name ;
    private LocalDateTime created_at;
    private String status;
    private String consultation_type;
    private String added_type;
    private Long total_count;
}
