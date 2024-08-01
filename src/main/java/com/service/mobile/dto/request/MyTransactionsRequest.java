package com.service.mobile.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class MyTransactionsRequest {
    private Integer user_id;
    private Integer created_date;
    private Integer page;
    private Integer type;
}
