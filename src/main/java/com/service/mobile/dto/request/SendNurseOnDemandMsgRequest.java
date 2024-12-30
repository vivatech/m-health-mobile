package com.service.mobile.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class SendNurseOnDemandMsgRequest {
    private Integer patient_id;
    private Integer nurse_id;
    private Integer id;
    private String status;
    private String tripId;
    private String zaadNumber;
    private Float amount;
    private String lat;
    private String longi;
}
