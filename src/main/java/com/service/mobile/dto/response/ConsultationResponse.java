package com.service.mobile.dto.response;

import com.service.mobile.dto.dto.ClinicInformation;
import com.service.mobile.dto.dto.HomeConsultationInformation;
import com.service.mobile.dto.enums.AddedType;
import com.service.mobile.dto.enums.ConsultationType;
import com.service.mobile.dto.enums.RequestType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class ConsultationResponse {
    private Integer case_id;
//    private Integer patient_id;
    private String name;
    private String consult_type;
    private ConsultationType consultation_type;
    private AddedType added_type;
    private LocalDate date;
    private String time;
    private String charges;
    private RequestType status;
    private String cancel_reason;
    private String profile_pic;
    private HomeConsultationInformation nurse;
    private ClinicInformation clinic;
    private Long total_count;
}
