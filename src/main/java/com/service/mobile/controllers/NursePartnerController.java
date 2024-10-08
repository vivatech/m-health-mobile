package com.service.mobile.controllers;

import com.service.mobile.dto.dto.ProcessPaymentRequest;
import com.service.mobile.dto.request.GetNurseLocationInfoRequest;
import com.service.mobile.dto.request.LogsNurseNotFoundRequest;
import com.service.mobile.dto.request.NodAckRequest;
import com.service.mobile.service.NursePartnerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Locale;

@RestController
@RequestMapping("/mobile/nurse-partner")
public class NursePartnerController {

    @Autowired
    private NursePartnerService nursePartnerService;

    @PostMapping(path = "/pt-online-nurses", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity<?> ptOnlineNurses(@RequestHeader(name = "X-localization", required = false,defaultValue = "so") Locale locale,
                                            @ModelAttribute LogsNurseNotFoundRequest request) {
        return nursePartnerService.ptOnlineNurses(locale, request.getUser_id());
    }

    @GetMapping("/nurse-service")
    public ResponseEntity<?> nurseService(@RequestHeader(name = "X-localization", required = false,defaultValue = "so") Locale locale) {
        return nursePartnerService.nurseService(locale);
    }

    @PostMapping(path = "/logs-nurse-not-found", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity<?> logsNurseNotFound(@RequestHeader(name = "X-localization", required = false,defaultValue = "so") Locale locale,
                                               @ModelAttribute LogsNurseNotFoundRequest request) {
        return nursePartnerService.logsNurseNotFound(locale,request);
    }

    @PostMapping(path = "/get-nurse-location-info", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity<?> getNurseLocationInfo(@RequestHeader(name = "X-localization", required = false,defaultValue = "so") Locale locale,
                                               @ModelAttribute GetNurseLocationInfoRequest request) {
        return nursePartnerService.getNurseLocationInfo(locale,request);
    }

    @PostMapping(path = "/process-payment", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity<?> processPayment(@RequestHeader(name = "X-localization", required = false,defaultValue = "so") Locale locale,
                                               @ModelAttribute ProcessPaymentRequest request) {
        return nursePartnerService.processPayment(locale,request);
    }

    @PostMapping(path = "/nodack", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity<?> nodack(@RequestHeader(name = "X-localization", required = false,defaultValue = "so") Locale locale,
                                               @ModelAttribute NodAckRequest request) {
        return nursePartnerService.nodack(locale,request);
    }

    @PostMapping(path = "/nd-patient-order-detail", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity<?> ndPatientOrderDetail(@RequestHeader(name = "X-localization", required = false,defaultValue = "so") Locale locale,
                                               @RequestParam(name = "user_id") Integer user_id) {
        return nursePartnerService.ndPatientOrderDetail(locale,user_id);
    }
}
