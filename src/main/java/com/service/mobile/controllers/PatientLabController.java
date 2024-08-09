package com.service.mobile.controllers;

import com.service.mobile.dto.dto.AddLabRequestDto;
import com.service.mobile.dto.dto.LabRequestDto;
import com.service.mobile.dto.request.*;
import com.service.mobile.service.PatientLabService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
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

    @PostMapping("/added-reports")
    public ResponseEntity<?> addedReports(@RequestHeader(name = "X-localization", required = false,defaultValue = "so") Locale locale,
                            @RequestParam(name ="user_id")Integer user_id) {
        return patientLabService.addedReports(user_id,locale);
    }

    @PostMapping("/add-report")
    public ResponseEntity<?> addReports(@RequestHeader(name = "X-localization", required = false,defaultValue = "so") Locale locale,
                            @RequestBody AddReportRequest request) {
        return patientLabService.addReports(request,locale);
    }

    @PostMapping("/delete-added-report")
    public ResponseEntity<?> deleteAddedReport(@RequestHeader(name = "X-localization", required = false,defaultValue = "so") Locale locale,
                            @RequestBody DeleteAddedReportRequest request) {
        return patientLabService.deleteAddedReport(request,locale);
    }

    @PostMapping("/select-lab")
    public ResponseEntity<?> selectLab(@RequestHeader(name = "X-localization", required = false,defaultValue = "so") Locale locale,
                                       @RequestBody SelectLabRequest request) {
        return patientLabService.selectLab(request,locale);
    }

    @PostMapping("/get-labs")
    public ResponseEntity<?> getLabs(@RequestHeader(name = "X-localization", required = false,defaultValue = "so") Locale locale,
                                     @RequestBody GetLabsRequest request) {
        return patientLabService.getLabs(request,locale);
    }

    @PostMapping("/get-bill-info")
    public ResponseEntity<?> getBillInfo(@RequestHeader(name = "X-localization", required = false,defaultValue = "so") Locale locale,
                                         @RequestBody BillInfoRequest request) {
        return patientLabService.getBillInfo(request,locale);
    }

    @PostMapping("/select-time-slot")
    public ResponseEntity<?> selectTimeSlot(@RequestHeader(name = "X-localization", required = false,defaultValue = "so") Locale locale,
                                         @RequestBody BillInfoRequest request) {
        return patientLabService.selectTimeSlot(request,locale);
    }

    @PostMapping("/add-lab-request")
    public ResponseEntity<?> addLabRequest(@RequestHeader(name = "X-localization", required = false,defaultValue = "so") Locale locale,
                                         @RequestBody AddLabRequestDto request) {
        return patientLabService.addLabRequest(request,locale);
    }

    @PostMapping("/get-lab-orders")
    public ResponseEntity<?> getLabOrder(@RequestHeader(name = "X-localization", required = false,defaultValue = "so") Locale locale,
                                         @RequestBody GetLabOrderRequest request) {
        return patientLabService.getLabOrder(request,locale);
    }

    @PostMapping("/get-lab-reports-by-caseid")
    public ResponseEntity<?> getLabReportsByCaseId(@RequestHeader(name = "X-localization", required = false,defaultValue = "so")
                                                Locale locale,
                                                @RequestBody GetSingleRelativeProfileRequest request) throws IOException {
        return patientLabService.getLabReportsByCaseId(locale,request);
    }
}
