package com.service.mobile.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class SlotResponse {
    private Integer slotId;
    private String slotDay;
    private String slotTime;
    private String userId;
    private Integer caseId;
    private String slotStatus;
    private Boolean isCancel;
    private String actualStatus;
    private String consultationType;
    private String consultType;
    private String displayTime;
}
