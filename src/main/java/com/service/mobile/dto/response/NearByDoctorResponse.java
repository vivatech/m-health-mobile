package com.service.mobile.dto.response;

import com.service.mobile.dto.enums.FeeType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Map;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class NearByDoctorResponse {
    private Integer id;
    private String first_name;
    private String last_name;
    private Integer hospital_id;
    private String hospital_name;
    private String photo;
    private Map<FeeType, String> consultation_fees;
}
