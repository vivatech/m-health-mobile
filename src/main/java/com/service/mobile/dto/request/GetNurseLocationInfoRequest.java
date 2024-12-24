package com.service.mobile.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class GetNurseLocationInfoRequest {
    private Integer user_id;
    private String p_latutude;
    private String p_longitude;
    private String n_latutude;
    private String n_longitude;
    private String service_id;
    private String nurse_mobile;
    private String search_id;
    private String distance;
}
