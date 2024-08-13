package com.service.mobile.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.Date;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class PaymentRequest {
    private Integer patientId; //mandatory

    private Integer doctorId;
    private Integer slotId; //for checking whether consultation already present or not
    private LocalDate consultationDate;
    private String consultType; //either video or clinic visit
    private String consultationType; //either Paid or Free
    private String paymentMethod;

    private Integer healthTipPackageId;
    private Boolean isVideo;


    private Float amount;
    private Float currencyAmount;
    private String currency;

    private Integer couponId;

    private String fullName;
    private Date dob;
    private String residenceAddress;
}
