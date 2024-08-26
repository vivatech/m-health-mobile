package com.service.mobile.dto.request;

import com.service.mobile.dto.enums.PackageType;
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
    private Integer case_id;
    private Integer page;
    private String package_name;
    private String doctor_name;
    private PackageType type;
    private LocalDate created_date;
    private LocalDate consultation_date;
    private Integer category_id;
    private String status;
}
