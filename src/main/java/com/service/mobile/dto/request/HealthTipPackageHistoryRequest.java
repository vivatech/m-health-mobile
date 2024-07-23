package com.service.mobile.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class HealthTipPackageHistoryRequest {
    private Integer user_id;
    private Integer page;
    private String type;
    private LocalDate created_date;
    private Integer category_id;
}
