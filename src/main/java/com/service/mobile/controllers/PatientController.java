package com.service.mobile.controllers;

import com.service.mobile.dto.request.UpdateFullNameRequest;
import com.service.mobile.service.PatientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Locale;

@RestController
@RequestMapping("/mobile/patient")
public class PatientController {

    @Autowired
    private PatientService patientService;

    @PostMapping("/update-fullname")
    public ResponseEntity<?> actionUpdateFullName(@RequestBody UpdateFullNameRequest request,@RequestHeader(name = "X-localization", required = false,defaultValue = "so") Locale locale) {
        return patientService.actionUpdateFullname(request,locale);
    }

    @PostMapping("/get-active-healthtips-package")
    public ResponseEntity<?> getActiveHealthTipsPackage(@RequestBody UpdateFullNameRequest request,@RequestHeader(name = "X-localization", required = false,defaultValue = "so") Locale locale) {
        return patientService.getActiveHealthTipsPackage(request,locale);
    }

    @PostMapping("/check-user-healthtip-package")
    public ResponseEntity<?> checkUserHealthTipPackage(@RequestHeader(name = "X-localization", required = false,defaultValue = "so")
                                                       Locale locale,
                                                       @RequestParam(name = "user_id",required = false) Integer user_id
    ) {
        return patientService.checkUserHealthTipPackage(locale,user_id);
    }
}
