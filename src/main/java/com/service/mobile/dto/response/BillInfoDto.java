package com.service.mobile.dto.response;

import com.service.mobile.dto.dto.LabDetailDto;
import com.service.mobile.dto.dto.ReportDto;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class BillInfoDto {
    private String labName;
    private LabDetailDto labDetail;
    private String labVisitOnly;
    private Boolean isHomeVisit;
    private String only_lab_visit_msg;
    private String only_lab_visit_msg_api;
    private String diagnosisCost;
    private String collectionCharge;
    private String totalPrice;
    private Map<Integer, String> reportName;
    private List<ReportDto> reportNameDto;
    private Float reportCharge;
    private Float extraCharges;
    private Float total;
}
