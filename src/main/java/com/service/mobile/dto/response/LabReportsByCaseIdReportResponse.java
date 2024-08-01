package com.service.mobile.dto.response;

import com.service.mobile.dto.dto.DocumentDto;
import com.service.mobile.dto.dto.LabReportRequestDto;
import com.service.mobile.dto.enums.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class LabReportsByCaseIdReportResponse {
    private Integer lab_consult_id;
    private Integer case_id;
    private String category_name;
    private Integer category_id;
    private Integer subcategory_id;
    private String sub_category_name;
    private String doc_prescription;
    private OrderStatus rep_status;
    private String usr_status;
    private List<DocumentDto> documents;
    private List<LabReportRequestDto> lab_list;
    private LocalDate created_date;
    private LocalTime created_time;
}
