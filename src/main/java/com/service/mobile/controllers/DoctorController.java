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
public class DoctorController {

    @Autowired
    private HospitalService hospitalService;

    @Autowired
    private DoctorService doctorService;

    @GetMapping("/get-hospital-list")
    public ResponseEntity<?> getHospitalList(@RequestHeader(name = "X-localization", required = false,defaultValue = "so")
                                             Locale locale) {
        return hospitalService.getHospitalList(locale);
    }

    @PostMapping(path = "/view-profile", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity<?> viewProfile(@RequestHeader(name = "X-localization", required = false,defaultValue = "so")
                                             Locale locale,
                                         @ModelAttribute GetSingleRelativeProfileRequest request) {
        return doctorService.viewProfile(locale, Integer.parseInt(request.getDoctor_id()));
    }

    @PostMapping(path = "/get-review", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity<?> getReview(@RequestHeader(name = "X-localization", required = false,defaultValue = "so")
                                             Locale locale,
                                         @ModelAttribute GetReviewRequest request) {
        return doctorService.getReview(locale,request);
    }
}
