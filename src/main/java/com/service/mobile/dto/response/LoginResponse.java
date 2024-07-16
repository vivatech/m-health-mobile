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
    private String contactNumber;
    private String isRegistered;
    private String doctorName;
    private String termsConditions;
    private String confirm_password;
    private List<String> specialization;
    private String degreeList;
    private List<String> degree;
    private String fees;
    private String chatConsultationFees;
    private String callConsultationFees;
    private String telephoneConsultationFees;
    private String visitConsultationFees;
    private String visitHomeConsultationFees;
    private String chatCommission;
    private String standardCommission;
    private String callCommission;
    private String telephoneCommission;
    private String visitCommission;
    private String standardCommissionType;
    private String visitHomeCommission;
    private String chatCommissionType;
    private String callCommissionType;
    private String telephoneCommissionType;
    private String visitCommissionType;
    private String visitHomeCommissionType;
    private String standardFinalConsFee;
    private String chatFinalConsFee;
    private String callFinalConsFee;
    private String telephoneFinalConsFee;
    private String visitFinalConsFee;
    private String visitHomeFinalConsFee;
    private String languageList;
    private List<String> language;
    private String documents;
    private String documentsName;
    private String adminCommission;
    private String finalConsultationCharge;
    private String id;
    private String name;
    private String profilePictureRemove;
    private String oldPassword;
    private String newPassword;
    private String otp;
    private String otpTime;
    private String paymentOption;
    private String labPrice;
    private String specialisationList;
    private String fullName;
    private String promoCodeOf;
}
