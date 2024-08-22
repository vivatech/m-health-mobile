package com.service.mobile.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.service.mobile.dto.dto.AddLabRequestDto;
import com.service.mobile.dto.dto.LabRequestDto;
import com.service.mobile.dto.request.*;
import com.service.mobile.service.PatientLabService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Locale;

@RestController
@RequestMapping("/mobile/patient-lab")
public class PatientLabController {

    @Autowired
    private PatientLabService patientLabService;


    @PostMapping(path="/lab-requests", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> labRequest(@ModelAttribute LabRequestDto request, @RequestHeader(name = "X-localization", required = false,defaultValue = "so") Locale locale) {
        return patientLabService.labRequest(request,locale);
    }

    @PostMapping(path="/added-reports", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> addedReports(@RequestHeader(name = "X-localization", required = false,defaultValue = "so") Locale locale,
                                          @ModelAttribute LabRequestDto request) {
        return patientLabService.addedReports(request.getUser_id(),locale);
    }

    @PostMapping(path="/add-report", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> addReports(@RequestHeader(name = "X-localization", required = false,defaultValue = "so") Locale locale,
                            @ModelAttribute AddReportRequest request) {
        return patientLabService.addReports(request,locale);
    }

    @PostMapping(path = "/delete-added-report", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> deleteAddedReport(@RequestHeader(name = "X-localization", required = false,defaultValue = "so") Locale locale,
                            @ModelAttribute DeleteAddedReportRequest request) {
        return patientLabService.deleteAddedReport(request,locale);
    }

    @PostMapping(path = "/select-lab", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> selectLab(@RequestHeader(name = "X-localization", required = false,defaultValue = "so") Locale locale,
                                       @ModelAttribute SelectLabRequest request) {
        return patientLabService.selectLab(request,locale);
    }

    @PostMapping(path = "/get-labs", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> getLabs(@RequestHeader(name = "X-localization", required = false,defaultValue = "so") Locale locale,
                                     @ModelAttribute GetLabsRequest request) {
        return patientLabService.getLabs(request,locale);
    }

    @PostMapping(path = "/get-bill-info", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> getBillInfo(@RequestHeader(name = "X-localization", required = false,defaultValue = "so") Locale locale,
                                         @ModelAttribute BillInfoRequest request) {
        return patientLabService.getBillInfo(request,locale);
    }

    @PostMapping(path = "/select-time-slot", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> selectTimeSlot(@RequestHeader(name = "X-localization", required = false,defaultValue = "so") Locale locale,
                                         @ModelAttribute BillInfoRequest request) {
        return patientLabService.selectTimeSlot(request,locale);
    }

    @PostMapping(path = "/add-lab-request", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> addLabRequest(@RequestHeader(name = "X-localization", required = false,defaultValue = "so") Locale locale,
                                         @ModelAttribute AddLabRequestDto request) throws JsonProcessingException {
        return patientLabService.addLabRequest(request,locale);
    }

    @PostMapping(path = "/get-lab-orders", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> getLabOrder(@RequestHeader(name = "X-localization", required = false,defaultValue = "so") Locale locale,
                                         @ModelAttribute GetLabOrderRequest request) {
        return patientLabService.getLabOrder(request,locale);
    }

    @PostMapping(path = "/get-lab-reports-by-caseid", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> getLabReportsByCaseId(@RequestHeader(name = "X-localization", required = false,defaultValue = "so")
                                                Locale locale,
                                                @ModelAttribute GetSingleRelativeProfileRequest request) throws IOException {
        return patientLabService.getLabReportsByCaseId(locale,request);
    }
}
