package com.service.mobile.dto.dto;

import com.service.mobile.dto.enums.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Map;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class OrderDto {
    private Integer order_id;
    private Float order_amount;
    private String sample_collection_mode;
    private OrderStatus status;
    private Integer case_id;
    private Map<String, Object> labDetail;
    private OrderDetailsDto orderDetails;
    private String lab_name;
    private String doc_prescription;
    private LocalDateTime created_at;
    private String refund_status;
    private Long total_count;
}
