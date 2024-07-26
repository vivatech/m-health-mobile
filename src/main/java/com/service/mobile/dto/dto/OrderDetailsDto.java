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
public class OrderDetailsDto {
    private LocalDate report_date;
    private String report_time_slot;
    private String address;
    private List<String> reportList;
    private String report_charge;
    private String extra_charges;
    private String total;
    private List<String> labReportDoc;
}
