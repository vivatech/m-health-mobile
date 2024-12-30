package com.service.mobile.dto.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class SmsData {
    private Integer fromId;
    private Integer toId;
    private String smsFor;
    private Integer caseId;
    private String userType;
    private Integer labOrderId;
    private LocalDateTime consultantTime;
}
