package com.service.mobile.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class BillInfoRequest {
    private Integer user_id;
    private Integer lab_id;
    private Integer case_id;
    private List<Integer> report_id;
    private String collection_mode;
    private String currency_option;
}
