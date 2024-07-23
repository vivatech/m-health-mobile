package com.service.mobile.dto.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ConsultationDTO {
    public Integer user_id;
    public Integer doctor_id;
    public String date;
    public Integer package_id;
    public String consult_type;
    public Integer slot_id;
    public String message;
    public String consultation_type;
    public String payment_method;
    public String added_type;
    public Integer added_by;
    public String created_at;
    public String currency_option;
    public String coupon_code;
    public String payment_number;
    public Integer allocated_nurse;
}
