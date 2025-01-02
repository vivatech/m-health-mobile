package com.service.mobile.customException;

import com.service.mobile.config.Constants;
import com.service.mobile.dto.response.Response;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

@RestControllerAdvice
@Slf4j
public class GlobalException {
    @ExceptionHandler(UsernameNotFoundException.class)
    public ResponseEntity<?> handleUserNotFoundException(UsernameNotFoundException ex, WebRequest request) {
        Response response = new Response(Constants.FAIL, Constants.USER_NOT_FOUND_CODE, Constants.USER_NOT_FOUND, null);
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }
    @ExceptionHandler(MobileServiceExceptionHandler.class)
    public ResponseEntity<?> handleMobileServiceException(MobileServiceExceptionHandler ex, WebRequest request) {
        Response response = new Response(Constants.FAIL, Constants.USER_NOT_FOUND_CODE, ex.getMessage(), null);
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

}
