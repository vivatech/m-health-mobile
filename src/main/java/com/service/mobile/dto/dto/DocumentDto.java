package com.service.mobile.dto.dto;

import com.service.mobile.dto.enums.AddedType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class DocumentDto {
    private Integer report_doc_id;
    private String doc_name;
    private String doc_display_name;
    private String report_doc_type;
    private AddedType added_type;
    private Integer added_by;
    private LocalDate created_date;
    private LocalTime created_time;
}
