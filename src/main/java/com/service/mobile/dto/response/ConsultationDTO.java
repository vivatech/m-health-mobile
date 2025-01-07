package com.service.mobile.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.sql.Date;
import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ConsultationDTO {
    private Integer case_id;
    private String name;
    private String consult_type;
    private String consultation_type;
    private Date date;
    private String time;
    private String charges;
    private String cancel_reason;
    private String status;
    private String profile_pic;
    private Object nurse;
    private Object clinic;
}
