package com.service.mobile.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.sql.Date;
import java.time.LocalDate;
import java.util.List;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class LabReportsByCaseIdResponse {
    private String doctor_name;
    private Integer case_id;
    private Date consultation_date;
    private List<LabReportsByCaseIdReportResponse> reports;
}
