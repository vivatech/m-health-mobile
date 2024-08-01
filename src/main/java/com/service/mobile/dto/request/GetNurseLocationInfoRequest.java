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
    private Integer p_latutude;
    private Integer p_longitude;
    private Integer n_latutude;
    private Integer n_longitude;
    private String service_id;
    private String nurse_mobile;
    private Integer search_id;
    private Integer distance;
}
