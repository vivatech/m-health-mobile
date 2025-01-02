package com.service.mobile.dto.request;

import com.service.mobile.dto.enums.CouponCategory;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ApplyCouponCodeRequest {
    private String user_id;
    private String coupon_code;
    private String category;
    private String price;
}
