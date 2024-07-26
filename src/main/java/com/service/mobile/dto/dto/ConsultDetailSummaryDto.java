package com.service.mobile.dto.dto;

import com.service.mobile.dto.enums.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class ConsultDetailSummaryDto {
    private Integer case_id;
    private String time;
    private LocalDate date;
    private String doctor_name;
    private OrderStatus status;
}
