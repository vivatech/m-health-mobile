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
public class NursePartnerControllerJson {

    @Autowired
    private NursePartnerService nursePartnerService;

    @PostMapping(path = "/pt-online-nurses", consumes = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<?> ptOnlineNurses(@RequestHeader(name = "X-localization", required = false,defaultValue = "so") Locale locale,
                                            @RequestBody LogsNurseNotFoundRequest request) {
        return nursePartnerService.ptOnlineNurses(locale, request.getUser_id());
    }

    @PostMapping(path = "/logs-nurse-not-found", consumes = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<?> logsNurseNotFound(@RequestHeader(name = "X-localization", required = false,defaultValue = "so") Locale locale,
                                               @RequestBody LogsNurseNotFoundRequest request) {
        return nursePartnerService.logsNurseNotFound(locale,request);
    }

    @PostMapping(path = "/get-nurse-location-info", consumes = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<?> getNurseLocationInfo(@RequestHeader(name = "X-localization", required = false,defaultValue = "so") Locale locale,
                                               @RequestBody GetNurseLocationInfoRequest request) {
        return nursePartnerService.getNurseLocationInfo(locale,request);
    }

    @PostMapping(path = "/process-payment", consumes = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<?> processPayment(@RequestHeader(name = "X-localization", required = false,defaultValue = "so") Locale locale,
                                               @RequestBody ProcessPaymentRequest request) {
        return nursePartnerService.processPayment(locale,request);
    }

    @PostMapping(path = "/nodack", consumes = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<?> nodack(@RequestHeader(name = "X-localization", required = false,defaultValue = "so") Locale locale,
                                               @RequestBody NodAckRequest request) {
        return nursePartnerService.nodack(locale,request);
    }

    @PostMapping(path = "/nd-patient-order-detail", consumes = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<?> ndPatientOrderDetail(@RequestHeader(name = "X-localization", required = false,defaultValue = "so") Locale locale,
                                               @RequestParam(name = "user_id") Integer user_id) {
        return nursePartnerService.ndPatientOrderDetail(locale,user_id);
    }
}
