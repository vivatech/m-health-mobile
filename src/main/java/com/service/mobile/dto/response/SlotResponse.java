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
    private Integer slot_id;
    private String slot_day;
    private String slot_time;
    private String user_id;
    private Object case_id;
    private String slot_status;
    private Integer is_cancel;
    private String actual_status;
    private String consultation_type;
    private String consult_type;
    private String displayTime;
}
