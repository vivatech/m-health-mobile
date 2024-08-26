package com.service.mobile.controllers;

import com.service.mobile.dto.request.GetReviewRequest;
import com.service.mobile.dto.request.GetSingleRelativeProfileRequest;
import com.service.mobile.service.DoctorService;
import com.service.mobile.service.HospitalService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Locale;

@RestController
@RequestMapping("/mobile/doctor")
public class DoctorControllerJson {

    @Autowired
    private HospitalService hospitalService;

    @Autowired
    private DoctorService doctorService;

    @PostMapping(path = "/view-profile", consumes = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<?> viewProfile(@RequestHeader(name = "X-localization", required = false,defaultValue = "so")
                                             Locale locale,
                                         @RequestBody GetSingleRelativeProfileRequest request) {
        return doctorService.viewProfile(locale,request.getDoctor_id());
    }

    @PostMapping(path = "/get-review", consumes = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<?> getReview(@RequestHeader(name = "X-localization", required = false,defaultValue = "so")
                                             Locale locale,
                                         @RequestBody GetReviewRequest request) {
        return doctorService.getReview(locale,request);
    }
}
