package com.service.mobile.dto.dto;

import com.service.mobile.dto.enums.ConsultationType;
import com.service.mobile.dto.enums.RequestType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class CheckOnGoingConsultationDto {
    private Integer case_id;
    private String name;
    private String consult_type;
    private ConsultationType consultation_type;
    private String date;
    private String time;
    private String charges;
    private RequestType status;
    private String profile_pic;
}
