package com.service.mobile.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.service.mobile.config.Constants;
import com.service.mobile.dto.dto.AddLabRequestDto;
import com.service.mobile.dto.dto.LabRequestDto;
import com.service.mobile.dto.request.*;
import com.service.mobile.dto.response.Response;
import com.service.mobile.service.PatientLabService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Locale;

@RestController
@RequestMapping("/mobile/patient-lab")
public class PatientLabControllerJson {

    @Autowired
    private PatientLabService patientLabService;
    @Autowired
    private MessageSource messageSource;


    @PostMapping(path="/lab-requests", consumes = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<?> labRequest(@RequestBody LabRequestDto request, @RequestHeader(name = "X-localization", required = false,defaultValue = "so") Locale locale) {
        return patientLabService.labRequest(request,locale);
    }

    @PostMapping(path="/added-reports", consumes = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<?> addedReports(@RequestHeader(name = "X-localization", required = false,defaultValue = "so") Locale locale,
                                          @RequestBody LabRequestDto request) {
        return patientLabService.addedReports(request.getUser_id(),locale);
    }

    @PostMapping(path="/add-report", consumes = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<?> addReports(@RequestHeader(name = "X-localization", required = false,defaultValue = "so") Locale locale,
                            @RequestBody AddReportRequest request) {
        return patientLabService.addReports(request, locale);
    }

    @PostMapping(path = "/delete-added-report", consumes = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<?> deleteAddedReport(@RequestHeader(name = "X-localization", required = false,defaultValue = "so") Locale locale,
                            @RequestBody DeleteAddedReportRequest request) {
        return patientLabService.deleteAddedReport(request,locale);
    }

    @PostMapping(path = "/select-lab", consumes = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<?> selectLab(@RequestHeader(name = "X-localization", required = false,defaultValue = "so") Locale locale,
                                       @RequestBody SelectLabRequest request) {
        return patientLabService.selectLab(request,locale);
    }

    @PostMapping(path = "/get-labs", consumes = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<?> getLabs(@RequestHeader(name = "X-localization", required = false,defaultValue = "so") Locale locale,
                                     @RequestBody GetLabsRequest request) {
        return patientLabService.getLabs(request,locale);
    }

    @PostMapping(path = "/get-bill-info", consumes = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<?> getBillInfo(@RequestHeader(name = "X-localization", required = false,defaultValue = "so") Locale locale,
                                         @RequestBody BillInfoRequest request) {
        return patientLabService.getBillInfo(request,locale);
    }

    @PostMapping(path = "/select-time-slot", consumes = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<?> selectTimeSlot(@RequestHeader(name = "X-localization", required = false,defaultValue = "so") Locale locale,
                                         @RequestBody BillInfoRequest request) {
        return patientLabService.selectTimeSlot(request,locale);
    }

    @PostMapping(path = "/add-lab-request", consumes = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<?> addLabRequest(@RequestHeader(name = "X-localization", required = false,defaultValue = "so") Locale locale,
                                         @RequestBody AddLabRequestDto request) throws JsonProcessingException {
        return patientLabService.addLabRequest(request,locale);
    }

    @PostMapping(path = "/get-lab-orders", consumes = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<?> getLabOrder(@RequestHeader(name = "X-localization", required = false,defaultValue = "so") Locale locale,
                                         @RequestBody GetLabOrderRequest request) {
        return patientLabService.getLabOrder(request,locale);
    }

    @PostMapping(path = "/get-lab-reports-by-caseid", consumes = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<?> getLabReportsByCaseId(@RequestHeader(name = "X-localization", required = false,defaultValue = "so")
                                                Locale locale,
                                                @RequestBody GetSingleRelativeProfileRequest request) throws IOException {
        return patientLabService.getLabReportsByCaseId(locale,request);
    }
}
