package com.service.mobile.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class LogsNurseNotFoundRequest {
    private Integer user_id;
    private String state;
    private String search_id;
    private String reason;
    private String lat_patient;
    private String long_patient;
}
