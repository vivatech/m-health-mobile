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
public class NursePartnerControllerJson {

    @Autowired
    private NursePartnerService nursePartnerService;

    /*
    Fetching online nurses from another database whose state is "active", "to-activate"
     */
    @PostMapping(path = "/pt-online-nurses", consumes = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<?> ptOnlineNurses(@RequestHeader(name = "X-localization", required = false,defaultValue = "so") Locale locale,
                                            @RequestBody LogsNurseNotFoundRequest request) {
        return nursePartnerService.ptOnlineNurses(locale, request.getUser_id());
    }
    /*
       Logs generated
     */
    @PostMapping(path = "/logs-nurse-not-found", consumes = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<?> logsNurseNotFound(@RequestHeader(name = "X-localization", required = false,defaultValue = "so") Locale locale,
                                               @RequestBody LogsNurseNotFoundRequest request) {
        return nursePartnerService.logsNurseNotFound(locale,request);
    }

    /*
        Getting nurse information ->distance fees etc.
     */
    @PostMapping(path = "/get-nurse-location-info", consumes = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<?> getNurseLocationInfo(@RequestHeader(name = "X-localization", required = false,defaultValue = "so") Locale locale,
                                               @RequestBody GetNurseLocationInfoRequest request) {
        return nursePartnerService.getNurseLocationInfo(locale,request);
    }

    /*
    Payment processing
     */
    @PostMapping(path = "/process-payment", consumes = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<?> processPayment(@RequestHeader(name = "X-localization", required = false,defaultValue = "so") Locale locale,
                                               @RequestBody ProcessPaymentRequest request) throws JsonProcessingException {
        return nursePartnerService.processPayment(locale,request);
    }

    /*
        Acknowledgment of NOD and writing logs and sending notification
     */
    @PostMapping(path = "/nodack", consumes = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<?> nodack(@RequestHeader(name = "X-localization", required = false,defaultValue = "so") Locale locale,
                                               @RequestBody NodAckRequest request) {
        return nursePartnerService.nodack(locale,request);
    }

    /*
     Getting order details of patient whose payment is done but service is not yet done
     */
    @PostMapping(path = "/nd-patient-order-detail", consumes = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<?> ndPatientOrderDetail(@RequestHeader(name = "X-localization", required = false,defaultValue = "so") Locale locale,
                                               @RequestBody NodAckRequest request) {
        return nursePartnerService.ndPatientOrderDetail(locale, request.getUser_id());
    }
    /*
        Cancel order by patient
     */
    @PostMapping(path = "/nd-patient-cancel-order", consumes = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<?> ndPatientCancelOrder(@RequestHeader(name = "X-localization", required = false,defaultValue = "so") Locale locale,
                                                  @RequestBody CancelOrderRequest request){
        log.info("Entering into ndPatientCancelOrder api request :{}", request);
        return nursePartnerService.ndPatientCancelOrder(locale, request);
    }
    /*
        Nurse tracker
     */
    @PostMapping(path = "/pt-nurse-tracker", consumes = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<?> ptNurseTracker(@RequestHeader(name = "X-localization", required = false,defaultValue = "so") Locale locale,
                                            @RequestBody CancelOrderRequest request){
        log.info("Entering into ptNurseTracker api request :{}", request);
        return nursePartnerService.ptNurseTracker(locale, request);
    }
    /*
         Nurse review and rating
    */
    @PostMapping(path = "/nd-review-rating", consumes = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<?> ndReviewRating(@RequestHeader(name = "X-localization", required = false,defaultValue = "so") Locale locale,
                                            @RequestBody NurseReviewRatingRequest request){
        log.info("Entering into ndReviewRating api request :{}", request);
        return nursePartnerService.ndReviewRating(locale, request);
    }
    /*
        Patient Confirmed History
    */
    @GetMapping(path = "/pt-confirmed-history")
    public ResponseEntity<?> ptConfirmedHistory(@RequestHeader(name = "X-localization", required = false,defaultValue = "so") Locale locale,
                                                @RequestParam(value = "user_id") String user_id,
                                                @RequestParam(value = "page", required = false) Integer page,
                                                @RequestParam(value = "trip_id", required = false) String trip_id,
                                                @RequestParam(value = "date", required = false) String date){
        log.info("Entering into ptConfirmedHistory api");
        return nursePartnerService.ptConfirmedHistory(locale, user_id, trip_id, date, page);
    }
    /*
   Check Notification
    */
    @PostMapping(path = "/check-notification", consumes = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<?> checkNotification(@RequestHeader(name = "X-localization", required = false,defaultValue = "so") Locale locale,
                                               @RequestBody NurseReviewRatingRequest request){
        log.info("Entering into checkNotification api request :{}", request);
        return nursePartnerService.checkNotification(locale, request.getUser_id());
    }
    /*
    Update Notified flag
   */
    @PostMapping(path = "/update-notified-flag", consumes = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<?> updateNotifiedFlag(@RequestHeader(name = "X-localization", required = false,defaultValue = "so") Locale locale,
                                               @RequestBody NotificationFlagRequest request){
        log.info("Entering into updateNotifiedFlag api request :{}", request);
        return nursePartnerService.updateNotifiedFlag(locale, request);
    }
}
