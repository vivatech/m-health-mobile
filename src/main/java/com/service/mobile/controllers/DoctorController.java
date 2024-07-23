package com.service.mobile.controllers;

import com.service.mobile.service.HospitalService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Locale;

@RestController
@RequestMapping("/mobile/doctor")
public class DoctorController {

    @Autowired
    private HospitalService hospitalService;

    @GetMapping("/get-hospital-list")
    public ResponseEntity<?> getHospitalList(@RequestHeader(name = "X-localization", required = false,defaultValue = "so")
                                       Locale locale) {
        return hospitalService.getHospitalList(locale);
    }
}
