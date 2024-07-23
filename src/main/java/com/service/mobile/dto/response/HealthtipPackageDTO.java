package com.service.mobile.dto.response;

import com.service.mobile.model.HealthTipCategoryMaster;
import com.service.mobile.model.HealthTipPackageUser;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class HealthtipPackageDTO {
    private Integer packageId;
    private String type;
    private String topic;
    private Integer durationValue;
    private String durationType;
    private String packagePrice;
    private String packagePriceWithoutCurrency;
    private String packagePriceWithVideo;
    private String packagePriceWithVideoWithoutCurrency;
    private String packagePriceSlsh;
    private String packagePriceSlshWithoutCurrency;
    private String packagePriceWithVideoSlsh;
    private String packagePriceWithVideoSlshWithoutCurrency;
    private Float totalMoney;
    private String expiryDate;
    private String isPurchased;
    private Long purchasedPackageUserId;
    private Double maxPackageFee;
    private String image;
    private String categoryName;
    private Long categoryId;

    public HealthtipPackageDTO(HealthTipCategoryMaster category, float totalMoney, double maxFee, double paymentRate, String image, boolean isPurchased, HealthTipPackageUser userPackage) {
        // Initialize fields based on category and other parameters
//        this.packageId = category.getCategoryId();
//        this.type = category.getCategoryId();
//        this.topic = category.getT();
//        this.durationValue = category.getDurationValue();
//        this.durationType = category.getDurationType();
//        this.packagePrice = formatCurrency(category.getPackagePrice());
//        this.packagePriceWithoutCurrency = String.valueOf(category.getPackagePrice());
//        this.packagePriceWithVideo = formatCurrency(category.getPackagePriceWithVideo());
//        this.packagePriceWithVideoWithoutCurrency = String.valueOf(category.getPackagePriceWithVideo());
//        this.packagePriceSlsh = "SLSH " + formatCurrency(category.getPackagePrice() * paymentRate);
//        this.packagePriceSlshWithoutCurrency = String.valueOf(category.getPackagePrice() * paymentRate);
//        this.packagePriceWithVideoSlsh = "SLSH " + formatCurrency(category.getPackagePriceWithVideo() * paymentRate);
//        this.packagePriceWithVideoSlshWithoutCurrency = String.valueOf(category.getPackagePriceWithVideo() * paymentRate);
//        this.totalMoney = totalMoney;
//        this.expiryDate = calculateExpiryDate(category);
//        this.isPurchased = isPurchased ? "Yes" : "No";
//        this.purchasedPackageUserId = (userPackage != null) ? userPackage.getId() : null;
//        this.maxPackageFee = maxFee;
//        this.image = image;
//        this.categoryName = category.getName();
//        this.categoryId = category.getId();
    }

    private String formatCurrency(double amount) {
        return String.format("%.2f", amount);
    }

//    private String calculateExpiryDate(Category category) {
//        // Calculate the expiry date based on the category's duration
//        // ...
//        return expiryDate;
//    }
}
