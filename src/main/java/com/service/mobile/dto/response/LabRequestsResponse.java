package com.service.mobile.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class LabRequestsResponse {
    private Integer case_id;
    private String doctor_name;
    private Date date;
    private String time;
    private String status;
    private List<String> labReportDoc;
    private Long total_count;
    private String request_result_text;
}
