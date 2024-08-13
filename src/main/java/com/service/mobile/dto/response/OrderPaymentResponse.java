package com.service.mobile.dto.response;

import com.service.mobile.dto.dto.OrderPaymentResponseData;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Map;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class OrderPaymentResponse {
    private Integer status;
    private String message;
    private Map<String, Object> data;

    public  OrderPaymentResponse (Integer status,String message){
        this.status = status;
        this.message = message;
    }
}
