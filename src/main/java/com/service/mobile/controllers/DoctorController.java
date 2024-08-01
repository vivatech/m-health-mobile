package com.service.mobile.controllers;

import com.service.mobile.dto.request.GetReviewRequest;
import com.service.mobile.service.DoctorService;
import com.service.mobile.service.HospitalService;
import org.springframework.beans.factory.annotation.Autowired;
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

    @GetMapping("/view-profile")
    public ResponseEntity<?> viewProfile(@RequestHeader(name = "X-localization", required = false,defaultValue = "so")
                                             Locale locale,
                                         @RequestParam(name = "doctor_id") Integer doctor_id) {
        return doctorService.viewProfile(locale,doctor_id);
    }

    @GetMapping("/get-review")
    public ResponseEntity<?> getReview(@RequestHeader(name = "X-localization", required = false,defaultValue = "so")
                                             Locale locale,
                                         @RequestBody GetReviewRequest request) {
        return doctorService.getReview(locale,request);
    }
}
