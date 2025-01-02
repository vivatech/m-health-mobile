package com.service.mobile.customException;

public class MobileServiceExceptionHandler extends RuntimeException {
    public MobileServiceExceptionHandler() {
        super("Not found"); // Provide a default error message
    }
    public MobileServiceExceptionHandler(String message) {
        super(message);
    }
}