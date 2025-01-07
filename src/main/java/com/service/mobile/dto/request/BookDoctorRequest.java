package com.service.mobile.dto.request;

import com.service.mobile.dto.enums.ConsultationType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.sql.Date;
import java.time.LocalDate;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class BookDoctorRequest {
    private Integer user_id;
    private Integer slot_id;

    private String message;
    private String date;

    private Integer doctor_id;
    private String time_slot;
    private Integer package_id;
    private String consult_type;
    private ConsultationType consultation_type;
    private Integer allocated_nurse;
    private String payment_method;
    private String currency_option;
    private String coupon_code;
    private String payment_number;



}
