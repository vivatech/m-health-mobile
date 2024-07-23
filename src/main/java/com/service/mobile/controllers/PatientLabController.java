package com.service.mobile.controllers;

import com.service.mobile.dto.dto.LabRequestDto;
import com.service.mobile.dto.request.UpdateFullNameRequest;
import com.service.mobile.service.PatientLabService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Locale;

@RestController
@RequestMapping("/mobile/patient-lab")
public class PatientLabController {

    @Autowired
    private PatientLabService patientLabService;


    @PostMapping("/lab-requests")
    public ResponseEntity<?> labRequest(@RequestBody LabRequestDto request, @RequestHeader(name = "X-localization", required = false,defaultValue = "so") Locale locale) {
        return patientLabService.labRequest(request,locale);
    }
}
