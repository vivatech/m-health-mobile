package com.service.mobile.dto.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class CancelOrderRequest {
    private Integer user_id;
    private Integer order_id;
    private String search_id;
    private String message;
}
