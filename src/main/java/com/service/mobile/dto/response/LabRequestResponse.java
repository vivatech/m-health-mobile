package com.service.mobile.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class LabRequestResponse {
    private String caseId;
    private String doctorName;
    private String date;
    private String time;
    private String status;
    private List<String> labReportDoc;
    private Integer totalCount;
    private String requestResultText;
}
