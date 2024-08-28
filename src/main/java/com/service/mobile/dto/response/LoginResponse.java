package com.service.mobile.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class LoginResponse {
    private String contact_number;
    private String is_registered;
    private String doctor_name;
    private String terms_conditions;
    private String confirm_password;
    private List<String> specialization;
    private String degreeList;
    private List<String> degree;
    private String fees;
    private String chat_consultation_fees;
    private String call_consultation_fees;
    private String telephone_consultation_fees;
    private String visit_consultation_fees;
    private String visit_home_consultation_fees;
    private String chat_commission;
    private String standard_commission;
    private String call_commission;
    private String telephone_commission;
    private String visit_commission;
    private String standard_commission_type;
    private String visit_home_commission;
    private String chat_commission_type;
    private String call_commission_type;
    private String telephone_commission_type;
    private String visit_commission_type;
    private String visit_home_commission_type;
    private String standard_final_cons_fee;
    private String chat_final_cons_fee;
    private String call_final_cons_fee;
    private String telephone_final_cons_fee;
    private String visit_final_cons_fee;
    private String visit_home_final_cons_fee;
    private String language_list;
    private List<String> language;
    private String documents;
    private String documents_name;
    private String admin_commission;
    private String final_consultation_charge;
    private String id;
    private String name;
    private String profile_picture_remove;
    private String old_password;
    private String new_password;
    private String otp;
    private String otp_time;
    private String payment_option;
    private String lab_price;
    private String specialisation_list;
    private String full_name;
    private String promo_code_of;
}
