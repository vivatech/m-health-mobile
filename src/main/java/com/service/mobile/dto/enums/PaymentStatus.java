package com.service.mobile.dto.enums;

public enum PaymentStatus {
    Pending("Pending"),
    Inprogress("InProgress"),
    Completed("Completed"),
    Cancelled("Cancelled"),
    Failed("Failed"),
    Refunded("Refunded"),
    PARTIAL_REFUNDED("Partial Refunded");

    private final String displayValue;

    PaymentStatus(String displayValue) {
        this.displayValue = displayValue;
    }

    public String getDisplayValue() {
        return displayValue;
    }

    @Override
    public String toString() {
        return displayValue;
    }
}
