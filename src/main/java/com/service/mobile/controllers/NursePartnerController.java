package com.service.mobile.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.service.mobile.dto.dto.CancelOrderRequest;
import com.service.mobile.dto.dto.ProcessPaymentRequest;
import com.service.mobile.dto.request.GetNurseLocationInfoRequest;
import com.service.mobile.dto.request.LogsNurseNotFoundRequest;
import com.service.mobile.dto.request.NodAckRequest;
import com.service.mobile.dto.request.NotificationFlagRequest;
import com.service.mobile.dto.response.NurseReviewRatingRequest;
import com.service.mobile.service.NursePartnerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Locale;

@RestController
@RequestMapping("/mobile/nurse-partner")
@Slf4j
public class NursePartnerController {

    @Autowired
    private NursePartnerService nursePartnerService;

    /*
    Fetching online nurses from another database whose state is "active", "to-activate"
     */
    @PostMapping(path = "/pt-online-nurses", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity<?> ptOnlineNurses(@RequestHeader(name = "X-localization", required = false,defaultValue = "so") Locale locale,
                                            @ModelAttribute LogsNurseNotFoundRequest request) {
        log.info("Entering into nurseService api whose request body is : {}", request);
        return nursePartnerService.ptOnlineNurses(locale, request.getUser_id());
    }

    /*
    List of services provided by the nurse
     */
    @GetMapping("/nurse-service")
    public ResponseEntity<?> nurseService(@RequestHeader(name = "X-localization", required = false,defaultValue = "so") Locale locale) {
        log.info("Entering into nurseService api");
        return nursePartnerService.nurseService(locale);
    }

    /*
    Logs generated
     */
    @PostMapping(path = "/logs-nurse-not-found", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity<?> logsNurseNotFound(@RequestHeader(name = "X-localization", required = false,defaultValue = "so") Locale locale,
                                               @ModelAttribute LogsNurseNotFoundRequest request) {
        log.info("Entering into logsNurseNotFound api request :{}", request);
        return nursePartnerService.logsNurseNotFound(locale,request);
    }

    /*
    Getting nurse information ->distance fees etc.
     */
    @PostMapping(path = "/get-nurse-location-info", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity<?> getNurseLocationInfo(@RequestHeader(name = "X-localization", required = false,defaultValue = "so") Locale locale,
                                               @ModelAttribute GetNurseLocationInfoRequest request) {
        log.info("Entering into getNurseLocationInfo api request :{}", request);
        return nursePartnerService.getNurseLocationInfo(locale,request);
    }

    /*
    Payment processing
     */
    @PostMapping(path = "/process-payment", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity<?> processPayment(@RequestHeader(name = "X-localization", required = false,defaultValue = "so") Locale locale,
                                               @ModelAttribute ProcessPaymentRequest request) throws JsonProcessingException {
        log.info("Entering into processPayment api request :{}", request);
        return nursePartnerService.processPayment(locale,request);
    }

    /*
        Acknowledgment of NOD and writing logs and sending notification
     */
    @PostMapping(path = "/nodack", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity<?> nodack(@RequestHeader(name = "X-localization", required = false,defaultValue = "so") Locale locale,
                                               @ModelAttribute NodAckRequest request) {
        log.info("Entering into nodack api request :{}", request);
        return nursePartnerService.nodack(locale,request);
    }

    /*
     Getting order details of patient whose payment is done but service is not yet done
     */
    @PostMapping(path = "/nd-patient-order-detail", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity<?> ndPatientOrderDetail(@RequestHeader(name = "X-localization", required = false,defaultValue = "so") Locale locale,
                                               @ModelAttribute NodAckRequest request){
        log.info("Entering into ndPatientOrderDetail api request :{}", request);
        return nursePartnerService.ndPatientOrderDetail(locale, request.getUser_id());
    }
    /*
    Cancel order by patient
     */
    @PostMapping(path = "/nd-patient-cancel-order", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity<?> ndPatientCancelOrder(@RequestHeader(name = "X-localization", required = false,defaultValue = "so") Locale locale,
                                                  @ModelAttribute CancelOrderRequest request){
        log.info("Entering into ndPatientCancelOrder api request :{}", request);
        return nursePartnerService.ndPatientCancelOrder(locale, request);
    }
    /*
    Nurse tracker
     */
    @PostMapping(path = "/pt-nurse-tracker", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity<?> ptNurseTracker(@RequestHeader(name = "X-localization", required = false,defaultValue = "so") Locale locale,
                                                  @ModelAttribute CancelOrderRequest request){
        log.info("Entering into ptNurseTracker api request :{}", request);
        return nursePartnerService.ptNurseTracker(locale, request);
    }
    /*
    Nurse review and rating
     */
    @PostMapping(path = "/nd-review-rating", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity<?> ndReviewRating(@RequestHeader(name = "X-localization", required = false,defaultValue = "so") Locale locale,
                                            @ModelAttribute NurseReviewRatingRequest request){
        log.info("Entering into ndReviewRating api request :{}", request);
        return nursePartnerService.ndReviewRating(locale, request);
    }
    /*
    Check Notification
     */
    @PostMapping(path = "/check-notification", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity<?> checkNotification(@RequestHeader(name = "X-localization", required = false,defaultValue = "so") Locale locale,
                                            @ModelAttribute NurseReviewRatingRequest request){
        log.info("Entering into checkNotification api request :{}", request);
        return nursePartnerService.checkNotification(locale, request.getUser_id());
    }
    /*
    Update Notified flag
   */
    @PostMapping(path = "/update-notified-flag", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity<?> updateNotifiedFlag(@RequestHeader(name = "X-localization", required = false,defaultValue = "so") Locale locale,
                                                @ModelAttribute NotificationFlagRequest request){
        log.info("Entering into updateNotifiedFlag api request :{}", request);
        return nursePartnerService.updateNotifiedFlag(locale, request);
    }
}
