package com.service.mobile.controllers;

import com.service.mobile.config.Constants;
import com.service.mobile.dto.dto.ResendOtpRequest;
import com.service.mobile.dto.request.*;
import com.service.mobile.dto.response.Response;
import com.service.mobile.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Locale;

@RestController
@RequestMapping("/mobile/patient")
public class PatientControllerJson {

    @Autowired
    private PatientService patientService;

    @Autowired
    private PublicService publicService;

    @Autowired
    private DoctorService doctorService;

    @Autowired
    private MessageSource messageSource;

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private RelativeService relativeService;
    @Autowired
    private AuthService authService;

    @PostMapping(path="/update-fullname", consumes = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<?> actionUpdateFullName(@RequestBody UpdateFullNameRequest request,@RequestHeader(name = "X-localization", required = false,defaultValue = "so") Locale locale) {
        return patientService.actionUpdateFullname(request,locale);
    }

    @PostMapping(path="/get-active-healthtips-package", consumes = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<?> getActiveHealthTipsPackage(@RequestBody UpdateFullNameRequest request,@RequestHeader(name = "X-localization", required = false,defaultValue = "so") Locale locale) {
        return patientService.getActiveHealthTipsPackage(request,locale);
    }

    @PostMapping(path ="/check-user-healthtip-package", consumes = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<?> checkUserHealthTipPackage(@RequestHeader(name = "X-localization", required = false,defaultValue = "so")
                                                       Locale locale,
                                                       @RequestBody UpdateFullNameRequest request
    ) {
        return patientService.checkUserHealthTipPackage(locale,request.getUser_id());
    }

    @PostMapping(path = "/get-profile", consumes = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<?> getProfile(@RequestHeader(name = "X-localization", required = false,defaultValue = "so")
                                        Locale locale,
                                        @RequestBody UpdateFullNameRequest request
    ) {
        return publicService.getProfile(locale,request.getUser_id());
    }

    @PostMapping(path = "/edit-profile", consumes = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<?> editProfile(@RequestHeader(name = "X-localization", required = false,defaultValue = "so")
                                        Locale locale,
                                         @RequestBody EditProfileRequest request
                                         ) {
        return publicService.editProfile(locale,request);
    }

    @PostMapping(path = "/search-doctor", consumes = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<?> searchDoctor(@RequestHeader(name = "X-localization", required = false,defaultValue = "so")
                                       Locale locale, @RequestBody SearchDoctorRequest request) {
        return doctorService.searchDoctor(locale,request);
    }

    @PostMapping(path = "/doctor-availability-list-latest", consumes = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<?> doctorAvailabilityListLatest(@RequestHeader(name = "X-localization", required = false,defaultValue = "so")
                                       Locale locale, @RequestBody DoctorAvailabilityListLatestRequest request) {
        return doctorService.doctorAvailabilityListLatest(locale,request);
    }

    @PostMapping(path = "/book-doctor", consumes = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<?> bookDoctor(@RequestHeader(name = "X-localization", required = false,defaultValue = "so")
                                         Locale locale,
                                         @RequestBody BookDoctorRequest request
    ) {
        return patientService.bookDoctor(locale,request);
    }

    @PostMapping(path = "/check-home-visit-availability", consumes = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<?> checkHomeVisitAvailability(@RequestHeader(name = "X-localization", required = false,defaultValue = "so")
                                         Locale locale,
                                         @RequestBody HomeVisitAvailabilityRequest request
    ) {
        return patientService.checkHomeVisitAvailability(locale,request);
    }

    @PostMapping(path = "/apply-coupon-code", consumes = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<?> applyCouponCode(@RequestHeader(name = "X-localization", required = false,defaultValue = "so")
                                        Locale locale,
                                        @RequestBody ApplyCouponCodeRequest request
    ) {
        return patientService.applyCouponCode(locale,request);
    }

    @PostMapping(path = "/thank-you", consumes = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<?> thankYou(@RequestHeader(name = "X-localization", required = false,defaultValue = "so")
                                        Locale locale,
                                        @RequestBody ThankYouRequest request
    ) {
        return publicService.thankYou(locale,request);
    }

    @PostMapping(path = "/clinic-list", consumes = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<?> getClinicList(@RequestHeader(name = "X-localization", required = false,defaultValue = "so")
                                              Locale locale,
                                              @RequestBody ThankYouRequest request) {
        return patientService.getClinicList(locale);
    }

    @PostMapping(path= "/healthtip-package-list", consumes = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<?> healthTipPackageList(@RequestHeader(name = "X-localization", required = false,defaultValue = "so")
                                              Locale locale,
                                              @RequestBody HealthTipPackageListRequest request) {
        return patientService.healthTipPackageList(locale,request);
    }

    @PostMapping(path= "/get-balance", consumes = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<?> getBalance(@RequestHeader(name = "X-localization", required = false,defaultValue = "so")
                                              Locale locale,
                                        @RequestBody HealthTipPackageListRequest request) {
        if(request.getUser_id()!=null && request.getUser_id()!=0){
            return patientService.getBalance(locale,request.getUser_id());
        }else{
            return ResponseEntity.status(HttpStatus.NO_CONTENT).body(new Response(
                    Constants.NO_RECORD_FOUND_CODE,
                    Constants.BLANK_DATA_GIVEN_CODE,
                    messageSource.getMessage(Constants.BLANK_DATA_GIVEN,null,locale)
            ));
        }
    }

    @PostMapping(path = "/healthtip-package-booking", consumes = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<?> healthTipPackageBooking(@RequestHeader(name = "X-localization", required = false,defaultValue = "so")
                                              Locale locale,
                                              @RequestBody HealthTipPackageBookingRequest request) {
        return patientService.healthTipPackageBooking(locale,request);
    }

    @PostMapping(path = "/cancel-healthtip-package", consumes = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<?> cancelHealthTipPackage(@RequestHeader(name = "X-localization", required = false,defaultValue = "so")
                                              Locale locale,
                                              @RequestBody CancelHealthTipPackageRequest request) {
        return patientService.cancelHealthTipPackage(locale,request);
    }

    @PostMapping(path= "/get-healthtips-list", consumes = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<?> getHealthTipsList(@RequestHeader(name = "X-localization", required = false,defaultValue = "so")
                                              Locale locale,
                                              @RequestBody HealthTipsListRequest request) {
        return patientService.getHealthTipsList(locale,request);
    }

    @PostMapping(path="/healthtips-export", consumes = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<?> healthTipsExport(@RequestHeader(name = "X-localization", required = false,defaultValue = "so")
                                              Locale locale,
                                              @RequestBody HealthTipsListRequest request) {
        return patientService.healthTipsExport(locale,request);
    }

    @PostMapping(path ="/delete-export-file", consumes = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<?> deleteExportFile(@RequestHeader(name = "X-localization", required = false,defaultValue = "so")
                                              Locale locale,
                                              @RequestBody DeleteExportFileRequest request) {
        return patientService.deleteExportFile(locale,request);
    }

    @PostMapping(path="/healthtip-package-history", consumes = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<?> healthTipPackageHistory(@RequestHeader(name = "X-localization", required = false,defaultValue = "so")
                                                     Locale locale,
                                                     @RequestBody HealthTipPackageHistoryRequest request) {
        return patientService.healthTipPackageHistory(locale,request);
    }

    @PostMapping(path="/my-orders", consumes = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<?> myOrders(@RequestHeader(name = "X-localization", required = false,defaultValue = "so")
                                                     Locale locale,
                                                     @RequestBody HealthTipPackageHistoryRequest request) {
        return patientService.myOrders(locale,request);
    }

    @PostMapping(path="/add-rating", consumes = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<?> addRating(@RequestHeader(name = "X-localization", required = false,defaultValue = "so")
                                                     Locale locale,
                                                     @RequestBody AddRatingRequest request) {
        return patientService.addRating(locale,request);
    }

    @PostMapping(path="/my-transactions", consumes = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<?> myTransactions(@RequestHeader(name = "X-localization", required = false,defaultValue = "so")
                                                     Locale locale,
                                                     @RequestBody MyTransactionsRequest request) {
        return transactionService.myTransactions(locale,request);
    }

    @PostMapping(path = "/relative-list", consumes = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<?> relativeList(@RequestHeader(name = "X-localization", required = false,defaultValue = "so")
                                       Locale locale,
                                          @RequestBody CreateRelativeProfileRequest request) {
        return relativeService.relativeList(locale,request.getUser_id());
    }

    @PostMapping(path = "/create-relative-profile", consumes = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<?> createRelativeProfile(@RequestHeader(name = "X-localization", required = false,defaultValue = "so")
                                       Locale locale,
                                       @RequestBody CreateRelativeProfileRequest request) throws IOException {
        return relativeService.createRelativeProfile(locale,request);
    }

    @PostMapping(path = "/update-relative-profile", consumes = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<?> updateRelativeProfile(@RequestHeader(name = "X-localization", required = false,defaultValue = "so")
                                       Locale locale,
                                       @RequestBody CreateRelativeProfileRequest request) throws IOException {
        return relativeService.updateRelativeProfile(locale,request);
    }

    @PostMapping(path = "/get-single-relative-profile", consumes = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<?> getSingleRelativeProfile(@RequestHeader(name = "X-localization", required = false,defaultValue = "so")
                                       Locale locale,
                                       @RequestBody GetSingleRelativeProfileRequest request) throws IOException {
        return relativeService.getSingleRelativeProfile(locale,request);
    }

    @PostMapping(path = "/relative-type", consumes = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<?> relativeType(@RequestHeader(name = "X-localization", required = false,defaultValue = "so")
                                       Locale locale,
                                       @RequestBody GetSingleRelativeProfileRequest request) throws IOException {
        return relativeService.relativeType(locale,request);
    }

    @PostMapping(path = "/cancel-consultation", consumes = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<?> cancelConsultation(@RequestHeader(name = "X-localization", required = false,defaultValue = "so")
                                                Locale locale,
                                                @RequestBody GetSingleRelativeProfileRequest request) throws IOException {
        return relativeService.cancelConsultation(locale,request);
    }

    @PostMapping(path = "/get-sloats", consumes = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<?> getSloats(@RequestHeader(name = "X-localization", required = false,defaultValue = "so")
                                                Locale locale,
                                        @RequestHeader(name = "X-type", required = false) String type,
                                        @RequestBody GetSloatsRequest request) throws IOException {
        return patientService.getSloats(locale,request,type);
    }

    @PostMapping(path="/verify-otp", consumes = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<?> actionVerifyOtp(@RequestHeader(name = "X-localization", required = false,defaultValue = "so") Locale locale,@RequestBody VerifyOtpRequest request) {
        return authService.actionVerifyOtp(request,locale);
    }

    /*
     resend -otp
     */
    @PostMapping(path= "/resend-otp", consumes = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<?> resendOTP(@RequestHeader(name = "X-localization", required = false,defaultValue = "so")
                                       Locale locale,
                                       @RequestBody ResendOtpRequest request) {
        if(request.getContact_number() != null && request.getIs_registered() != null){
            return patientService.getResendOTP(locale,request);
        }else{
            return ResponseEntity.status(HttpStatus.NO_CONTENT).body(new Response(
                    Constants.NO_RECORD_FOUND_CODE,
                    Constants.BLANK_DATA_GIVEN_CODE,
                    messageSource.getMessage(Constants.BLANK_DATA_GIVEN,null,locale)
            ));
        }
    }

}
