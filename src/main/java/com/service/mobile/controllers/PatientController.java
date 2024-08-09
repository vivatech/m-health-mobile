package com.service.mobile.controllers;

import com.service.mobile.config.Constants;
import com.service.mobile.dto.request.*;
import com.service.mobile.dto.response.Response;
import com.service.mobile.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Locale;

@RestController
@RequestMapping("/mobile/patient")
public class PatientController {

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

    @PostMapping("/update-fullname")
    public ResponseEntity<?> actionUpdateFullName(@RequestBody UpdateFullNameRequest request,@RequestHeader(name = "X-localization", required = false,defaultValue = "so") Locale locale) {
        return patientService.actionUpdateFullname(request,locale);
    }

    @PostMapping("/get-active-healthtips-package")
    public ResponseEntity<?> getActiveHealthTipsPackage(@RequestBody UpdateFullNameRequest request,@RequestHeader(name = "X-localization", required = false,defaultValue = "so") Locale locale) {
        return patientService.getActiveHealthTipsPackage(request,locale);
    }

    @PostMapping("/check-user-healthtip-package")
    public ResponseEntity<?> checkUserHealthTipPackage(@RequestHeader(name = "X-localization", required = false,defaultValue = "so")
                                                       Locale locale,
                                                       @RequestParam(name = "user_id",required = false) Integer user_id
    ) {
        return patientService.checkUserHealthTipPackage(locale,user_id);
    }

    @PostMapping("/get-profile")
    public ResponseEntity<?> getProfile(@RequestHeader(name = "X-localization", required = false,defaultValue = "so")
                                        Locale locale,
                                        @RequestParam(name = "user_id",required = false) Integer user_id
    ) {
        return publicService.getProfile(locale,user_id);
    }

    @PostMapping("/edit-profile")
    public ResponseEntity<?> editProfile(@RequestHeader(name = "X-localization", required = false,defaultValue = "so")
                                        Locale locale,
                                         @RequestBody EditProfileRequest request
                                         ) {
        return publicService.editProfile(locale,request);
    }

    @GetMapping("/get-sort-by")
    public ResponseEntity<?> getSortBy(@RequestHeader(name = "X-localization", required = false,defaultValue = "so")
                                       Locale locale) {
        return patientService.getSortBy(locale);
    }

    @GetMapping("/get-availability")
    public ResponseEntity<?> getAvailability(@RequestHeader(name = "X-localization", required = false,defaultValue = "so")
                                       Locale locale) {
        return patientService.getAvailability(locale);
    }

    @GetMapping("/search-doctor")
    public ResponseEntity<?> searchDoctor(@RequestHeader(name = "X-localization", required = false,defaultValue = "so")
                                       Locale locale, @RequestBody SearchDoctorRequest request) {
        return doctorService.searchDoctor(locale,request);
    }

    @GetMapping("/doctor-availability-list-latest")
    public ResponseEntity<?> doctorAvailabilityListLatest(@RequestHeader(name = "X-localization", required = false,defaultValue = "so")
                                       Locale locale, @RequestBody DoctorAvailabilityListLatestRequest request) {
        return doctorService.doctorAvailabilityListLatest(locale,request);
    }

    @PostMapping("/book-doctor")
    public ResponseEntity<?> bookDoctor(@RequestHeader(name = "X-localization", required = false,defaultValue = "so")
                                         Locale locale,
                                         @RequestBody BookDoctorRequest request
    ) {
        return patientService.bookDoctor(locale,request);
    }

    @PostMapping("/check-home-visit-availability")
    public ResponseEntity<?> checkHomeVisitAvailability(@RequestHeader(name = "X-localization", required = false,defaultValue = "so")
                                         Locale locale,
                                         @RequestBody HomeVisitAvailabilityRequest request
    ) {
        return patientService.checkHomeVisitAvailability(locale,request);
    }

    @PostMapping("/apply-coupon-code")
    public ResponseEntity<?> applyCouponCode(@RequestHeader(name = "X-localization", required = false,defaultValue = "so")
                                        Locale locale,
                                        @RequestBody ApplyCouponCodeRequest request
    ) {
        return patientService.applyCouponCode(locale,request);
    }

    @PostMapping("/thank-you")
    public ResponseEntity<?> thankYou(@RequestHeader(name = "X-localization", required = false,defaultValue = "so")
                                        Locale locale,
                                        @RequestBody ThankYouRequest request
    ) {
        return publicService.thankYou(locale,request);
    }

    @PostMapping("/clinic-list")
    public ResponseEntity<?> getClinicList(@RequestHeader(name = "X-localization", required = false,defaultValue = "so")
                                              Locale locale,
                                              @RequestParam(name = "user_id",required = false) Integer user_id) {
        return patientService.getClinicList(locale);
    }

    @PostMapping("/healthtip-package-list")
    public ResponseEntity<?> healthTipPackageList(@RequestHeader(name = "X-localization", required = false,defaultValue = "so")
                                              Locale locale,
                                              @RequestBody HealthTipPackageListRequest request) {
        return patientService.healthTipPackageList(locale,request);
    }

    @PostMapping("/get-balance")
    public ResponseEntity<?> getBalance(@RequestHeader(name = "X-localization", required = false,defaultValue = "so")
                                              Locale locale,
                                              @RequestParam(name = "user_id",required = false) Integer user_id) {
        if(user_id!=null && user_id!=0){
            return ResponseEntity.status(HttpStatus.NO_CONTENT).body(new Response(
                    Constants.NO_RECORD_FOUND_CODE,
                    Constants.BLANK_DATA_GIVEN_CODE,
                    messageSource.getMessage(Constants.BLANK_DATA_GIVEN,null,locale)
            ));
        }else{
            return patientService.getBalance(locale,user_id);
        }
    }

    @PostMapping("/healthtip-package-booking")
    public ResponseEntity<?> healthTipPackageBooking(@RequestHeader(name = "X-localization", required = false,defaultValue = "so")
                                              Locale locale,
                                              @RequestBody HealthTipPackageBookingRequest request) {
        return patientService.healthTipPackageBooking(locale,request);
    }

    @PostMapping("/cancel-healthtip-package")
    public ResponseEntity<?> cancelHealthTipPackage(@RequestHeader(name = "X-localization", required = false,defaultValue = "so")
                                              Locale locale,
                                              @RequestBody CancelHealthTipPackageRequest request) {
        return patientService.cancelHealthTipPackage(locale,request);
    }

    @PostMapping("/get-healthtips-list")
    public ResponseEntity<?> getHealthTipsList(@RequestHeader(name = "X-localization", required = false,defaultValue = "so")
                                              Locale locale,
                                              @RequestBody HealthTipsListRequest request) {
        return patientService.getHealthTipsList(locale,request);
    }

    @PostMapping("/healthtips-export")
    public ResponseEntity<?> healthTipsExport(@RequestHeader(name = "X-localization", required = false,defaultValue = "so")
                                              Locale locale,
                                              @RequestBody HealthTipsListRequest request) {
        return patientService.healthTipsExport(locale,request);
    }

    @PostMapping("/delete-export-file")
    public ResponseEntity<?> deleteExportFile(@RequestHeader(name = "X-localization", required = false,defaultValue = "so")
                                              Locale locale,
                                              @RequestBody DeleteExportFileRequest request) {
        return patientService.deleteExportFile(locale,request);
    }

    @PostMapping("/healthtip-package-history")
    public ResponseEntity<?> healthTipPackageHistory(@RequestHeader(name = "X-localization", required = false,defaultValue = "so")
                                                     Locale locale,
                                                     @RequestBody HealthTipPackageHistoryRequest request) {
        return patientService.healthTipPackageHistory(locale,request);
    }

    @PostMapping("/add-rating")
    public ResponseEntity<?> addRating(@RequestHeader(name = "X-localization", required = false,defaultValue = "so")
                                                     Locale locale,
                                                     @RequestBody AddRatingRequest request) {
        return patientService.addRating(locale,request);
    }

    @PostMapping("/my-transactions")
    public ResponseEntity<?> myTransactions(@RequestHeader(name = "X-localization", required = false,defaultValue = "so")
                                                     Locale locale,
                                                     @RequestBody MyTransactionsRequest request) {
        return transactionService.myTransactions(locale,request);
    }

    @PostMapping("/relative-list")
    public ResponseEntity<?> relativeList(@RequestHeader(name = "X-localization", required = false,defaultValue = "so")
                                       Locale locale,
                                       @RequestParam(name = "user_id") Integer user_id) {
        return relativeService.relativeList(locale,user_id);
    }

    @PostMapping("/create-relative-profile")
    public ResponseEntity<?> createRelativeProfile(@RequestHeader(name = "X-localization", required = false,defaultValue = "so")
                                       Locale locale,
                                       @ModelAttribute CreateRelativeProfileRequest request) throws IOException {
        return relativeService.createRelativeProfile(locale,request);
    }

    @PostMapping("/update-relative-profile")
    public ResponseEntity<?> updateRelativeProfile(@RequestHeader(name = "X-localization", required = false,defaultValue = "so")
                                       Locale locale,
                                       @ModelAttribute CreateRelativeProfileRequest request) throws IOException {
        return relativeService.updateRelativeProfile(locale,request);
    }

    @PostMapping("/get-single-relative-profile")
    public ResponseEntity<?> getSingleRelativeProfile(@RequestHeader(name = "X-localization", required = false,defaultValue = "so")
                                       Locale locale,
                                       @RequestBody GetSingleRelativeProfileRequest request) throws IOException {
        return relativeService.getSingleRelativeProfile(locale,request);
    }

    @PostMapping("/relative-type")
    public ResponseEntity<?> relativeType(@RequestHeader(name = "X-localization", required = false,defaultValue = "so")
                                       Locale locale,
                                       @RequestBody GetSingleRelativeProfileRequest request) throws IOException {
        return relativeService.relativeType(locale,request);
    }

    @PostMapping("/cancel-consultation")
    public ResponseEntity<?> cancelConsultation(@RequestHeader(name = "X-localization", required = false,defaultValue = "so")
                                                Locale locale,
                                                @RequestBody GetSingleRelativeProfileRequest request) throws IOException {
        return relativeService.cancelConsultation(locale,request);
    }

    @PostMapping("/get-sloats")
    public ResponseEntity<?> getSloats(@RequestHeader(name = "X-localization", required = false,defaultValue = "so")
                                                Locale locale,
                                        @RequestHeader(name = "X-type", required = false) String type,
                                        @RequestBody GetSloatsRequest request) throws IOException {
        return patientService.getSloats(locale,request,type);
    }


}
