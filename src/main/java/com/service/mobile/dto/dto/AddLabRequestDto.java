package com.service.mobile.dto.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class AddLabRequestDto {
    private Integer user_id;
    private Integer case_id;
    private Integer lab_id;
    private List<Integer> sub_cat_id;
    private LocalDate report_date;
    private String report_time_slot;
    private String payment_method;
    private String address;
    private String payer_mobile;
    private String sample_collection_mode;
    private String currency_option;
    private String payment_number;
}
