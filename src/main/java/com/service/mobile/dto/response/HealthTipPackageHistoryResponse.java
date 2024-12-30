package com.service.mobile.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.service.mobile.dto.enums.DurationType;
import com.service.mobile.dto.enums.PackageType;
import com.service.mobile.dto.enums.YesNo;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class HealthTipPackageHistoryResponse {
    private String category_name;
    private String  package_type;
    @JsonProperty("Frequency")
    private String Frequency;
    private String package_price;
    private YesNo is_expire;
    private String cancel_flg;
    private String created_at;
    private String expired_at;
    private Integer total_count;
}
