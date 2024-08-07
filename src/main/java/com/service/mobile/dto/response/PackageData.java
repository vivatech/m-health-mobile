package com.service.mobile.dto.response;

import com.service.mobile.dto.enums.DurationType;
import com.service.mobile.dto.enums.PackageType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class PackageData {
    private Integer package_id;
    private PackageType type;
    private String topic;
    private Integer duration_value;
    private DurationType duration_type;
    private String package_price;
    private String package_price_without_currency;
    private String package_price_with_video;
    private String package_price_with_video_without_currency;
    private String package_price_slsh;
    private String package_price_slsh_without_currency;
    private String package_price_with_video_slsh;
    private String package_price_with_video_slsh_without_currency;
    private Float total_money;
    private Date expiry_date;
    private String is_purchased;
    private String purchased_package_user_id;
    private Double maxPackagefee;
    private Long total_count;
    private String image;
    private String category_name;
    private Integer category_id;
}
