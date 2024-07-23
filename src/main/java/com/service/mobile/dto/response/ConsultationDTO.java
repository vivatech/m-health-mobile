package com.service.mobile.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ConsultationDTO {
    private String caseId;
    private String name;
    private String consultType;
    private String consultationType;
    private String date;
    private String time;
    private String charges;
    private String cancelReason;
    private String status;
    private String profilePic;
    private Object nurse;
    private Object clinic;
}
