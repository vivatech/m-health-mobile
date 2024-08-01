package com.service.mobile.controllers;

import com.service.mobile.dto.dto.ProcessPaymentRequest;
import com.service.mobile.dto.request.GetNurseLocationInfoRequest;
import com.service.mobile.dto.request.GetSloatsRequest;
import com.service.mobile.dto.request.LogsNurseNotFoundRequest;
import com.service.mobile.dto.request.NodAckRequest;
import com.service.mobile.service.NursePartnerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Locale;

@RestController
@RequestMapping("/mobile/nurse-partner")
public class NursePartnerController {

    @Autowired
    private NursePartnerService nursePartnerService;

    @PostMapping("/pt-online-nurses")
    public ResponseEntity<?> ptOnlineNurses(@RequestHeader(name = "X-localization", required = false,defaultValue = "so") Locale locale,
                                                @RequestParam(name = "user_id")Integer user_id) {
        return nursePartnerService.ptOnlineNurses(locale,user_id);
    }

    @GetMapping("/nurse-service")
    public ResponseEntity<?> nurseService(@RequestHeader(name = "X-localization", required = false,defaultValue = "so") Locale locale) {
        return nursePartnerService.nurseService(locale);
    }

    @PostMapping("/logs-nurse-not-found")
    public ResponseEntity<?> logsNurseNotFound(@RequestHeader(name = "X-localization", required = false,defaultValue = "so") Locale locale,
                                               @RequestBody LogsNurseNotFoundRequest request) {
        return nursePartnerService.logsNurseNotFound(locale,request);
    }

    @PostMapping("/get-nurse-location-info")
    public ResponseEntity<?> getNurseLocationInfo(@RequestHeader(name = "X-localization", required = false,defaultValue = "so") Locale locale,
                                               @RequestBody GetNurseLocationInfoRequest request) {
        return nursePartnerService.getNurseLocationInfo(locale,request);
    }

    @PostMapping("/process-payment")
    public ResponseEntity<?> processPayment(@RequestHeader(name = "X-localization", required = false,defaultValue = "so") Locale locale,
                                               @RequestBody ProcessPaymentRequest request) {
        return nursePartnerService.processPayment(locale,request);
    }

    @PostMapping("/nodack")
    public ResponseEntity<?> nodack(@RequestHeader(name = "X-localization", required = false,defaultValue = "so") Locale locale,
                                               @RequestBody NodAckRequest request) {
        return nursePartnerService.nodack(locale,request);
    }

    @PostMapping("/nd-patient-order-detail")
    public ResponseEntity<?> ndPatientOrderDetail(@RequestHeader(name = "X-localization", required = false,defaultValue = "so") Locale locale,
                                               @RequestParam(name = "user_id") Integer user_id) {
        return nursePartnerService.ndPatientOrderDetail(locale,user_id);
    }
}
