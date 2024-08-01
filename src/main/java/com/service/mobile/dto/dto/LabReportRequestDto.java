package com.service.mobile.dto.dto;

import com.service.mobile.dto.enums.LabReportPaymentStatus;
import com.service.mobile.dto.enums.LabReportRequestStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class LabReportRequestDto {
    private Integer req_id;
    private Integer lab_id;
    private String lab_name;
    private LabReportRequestStatus request_status;
    private LabReportPaymentStatus payment_status;
    private Float lab_price;
}
