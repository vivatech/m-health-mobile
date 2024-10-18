package com.service.mobile.controllers;

import com.service.mobile.config.Constants;
import com.service.mobile.dto.LogoutRequest;
import com.service.mobile.dto.dto.DoctorRattingDTO;
import com.service.mobile.dto.dto.ResendOtpRequest;
import com.service.mobile.dto.request.*;
import com.service.mobile.dto.response.NearByDoctorResponse;
import com.service.mobile.dto.response.Response;
import com.service.mobile.dto.response.ViewReplyMessageRequest;
import com.service.mobile.model.Country;
import com.service.mobile.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RequestHeader;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

@RestController
@RequestMapping("/mobile/site")
public class SiteController {

    @Autowired
    private SiteService siteService;

    @Autowired
    private PublicService publicService;

    @Autowired
    private MessageSource messageSource;

    @Autowired
    private OrderService orderService;

    @Autowired
    private UsersService usersService;

    @Autowired
    private ConsultationService consultationService;

    @Autowired
    private DoctorService doctorService;

    @Autowired
    private TicketService ticketService;
    @Autowired
    private AuthService authService;

    @PostMapping(path="/mobile-release", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity<?> actionMobileRelease(@RequestHeader(name = "X-localization", required = false,defaultValue = "so") Locale locale, @RequestHeader(name = "X-type", required = false) String type, @ModelAttribute MobileReleaseRequest request) {
        return siteService.getMobileRelease(request,locale, type);
    }

    @GetMapping("/get-country-list")
    public ResponseEntity<?> getCountriesList(@RequestHeader(name = "X-localization", required = false,defaultValue = "so") Locale locale) {
        Response response = new Response();
        ResponseEntity<?> responseEntity = ResponseEntity.status(HttpStatus.NOT_FOUND).body(new Response(
                Constants.NO_RECORD_FOUND_CODE,
                Constants.NO_RECORD_FOUND_CODE,
                messageSource.getMessage(Constants.NO_COUNTRY_FOUND,null,locale)
        ));
        List<Country> countries = publicService.findAllCountry();
        if (countries.size()>0) {
            response = new Response(Constants.SUCCESS_CODE,
                    Constants.SUCCESS_CODE,
                    messageSource.getMessage(Constants.COUNTRY_LIST_RECIVED,null,locale),
                    countries
                    );
            responseEntity = ResponseEntity.status(HttpStatus.OK).body(response);
        }
        return responseEntity;
    }


    @GetMapping("/global-params")
    public ResponseEntity<?> getGlobalParams(@RequestHeader(name = "X-localization", required = false,defaultValue = "so") Locale locale) {
        return publicService.getGlobalParams(locale);
    }

    @GetMapping("/get-static-page-content")
    public ResponseEntity<?> getStaticPageContent(@RequestHeader(name = "X-localization", required = false,defaultValue = "so")
          Locale locale,
        @RequestParam String type
    ) {
        return publicService.getStaticPageContent(locale,type);
    }

    @PostMapping(path = "/activities", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity<?> activities(@RequestHeader(name = "X-localization", required = false,defaultValue = "so")
                                        Locale locale,
                                        @ModelAttribute UpdatePictureRequest request
    ) {
        return publicService.activities(locale, request.getUser_id());
    }

    @GetMapping("/get-consult-type")
    public ResponseEntity<?> getConsultType(@RequestHeader(name = "X-localization", required = false,defaultValue = "so")
                                            Locale locale
    ) {
        return publicService.getConsultType(locale);
    }

    @GetMapping("/get-offers")
    public ResponseEntity<?> getOffers(@RequestHeader(name = "X-localization", required = false,defaultValue = "so")
                                            Locale locale) {
        return publicService.getOffers(locale);
    }

    @PostMapping(path = "/recent-orders", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity<?> recentOrders(@RequestHeader(name = "X-localization", required = false,defaultValue = "so")
                                          Locale locale,
                                          @RequestHeader(name = "X-type", required = false)
                                          String type,
                                          @ModelAttribute UpdatePictureRequest request
    ) {
        return orderService.recentOrders(locale,request.getUser_id(),type);
    }

    @GetMapping("/get-state-list")
    public ResponseEntity<?> getStateList(@RequestHeader(name = "X-localization", required = false,defaultValue = "so")
                                          Locale locale) {
        return publicService.getStateList(locale);
    }

    @GetMapping("/get-city-list")
    public ResponseEntity<?> getCityList(@RequestHeader(name = "X-localization", required = false,defaultValue = "so")
                                          Locale locale) {
        return publicService.getCityList(locale);
    }

    @PostMapping(path = "/update-profile-picture", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE}) //in post man post method is written
    public ResponseEntity<?> updateProfilePicture(@ModelAttribute UpdatePictureRequest request,
        @RequestHeader(name = "X-localization", required = false,defaultValue = "so") Locale locale) {
        return usersService.updateProfilePicture(request,locale);
    }

    @GetMapping("/get-doctor-city-list")
    public ResponseEntity<?> getDoctorCityList(@RequestHeader(name = "X-localization", required = false,defaultValue = "so")
                                               Locale locale) {
        return doctorService.getDoctorCityList(locale);
    }

    @GetMapping("/get-language")
    public ResponseEntity<?> getLanguage(@RequestHeader(name = "X-localization", required = false,defaultValue = "so")
                                               Locale locale) {
        return publicService.getLanguage(locale);
    }

    @GetMapping("/get-specialization")
    public ResponseEntity<?> getSpecialization(@RequestHeader(name = "X-localization", required = false,defaultValue = "so")
                                               Locale locale) {
        return publicService.getSpecialization(locale);
    }

    @GetMapping("/get-payment-method")
    public ResponseEntity<?> getPaymentMethod(@RequestHeader(name = "X-localization", required = false,defaultValue = "so")
                                               Locale locale) {
        return publicService.getPaymentMethod(locale);
    }

    @GetMapping("/get-doctor-by-rating")
    public ResponseEntity<?> getDoctorByRatting(@RequestHeader(name = "X-localization", required = false,defaultValue = "so")
                                               Locale locale) {
        List<DoctorRattingDTO> doctors = publicService.getDoctorByRatting(locale);
        if(doctors.size()>0){
            return ResponseEntity.status(HttpStatus.OK).body(new Response(
                    Constants.SUCCESS_CODE,
                    Constants.SUCCESS_CODE,
                    messageSource.getMessage(Constants.RATED_DOCTOR_LIST_RETRIEVED,null,locale),
                    doctors
            ));
        }else{
            return ResponseEntity.status(HttpStatus.OK).body(new Response(
                    Constants.SUCCESS_CODE,
                    Constants.SUCCESS_CODE,
                    messageSource.getMessage(Constants.NO_RATED_DOCTOR_FOUND,null,locale)
            ));
        }
    }

    @GetMapping(path = "/nearby-doctor", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity<?> nearByDoctor(@RequestHeader(name = "X-localization", required = false,defaultValue = "so")
                                               Locale locale,
                                          @ModelAttribute NearByDoctorRequest request) {
        return publicService.nearByDoctor(locale,request);
    }

    @GetMapping("/get-all-category-list")
    public ResponseEntity<?> getAllCategoriesList(@RequestHeader(name = "X-localization", required = false,defaultValue = "so")
                                               Locale locale) {
        return publicService.getAllCategoriesList(locale);
    }

    @PostMapping(path = "/list-support-tickets", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity<?> listSupportTickets(@RequestHeader(name = "X-localization", required = false,defaultValue = "so")
                                               Locale locale,
                                                @ModelAttribute ListSupportTicketsRequest request) {
        return ticketService.listSupportTickets(request,locale);
    }

    @PostMapping(path = "/create-support-ticket", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity<?> createSupportTicket(@RequestHeader(name = "X-localization", required = false,defaultValue = "so")
                                               Locale locale,
                                                @ModelAttribute CreateSupportTicketsRequest request) throws IOException {
        return ticketService.createSupportTicket(request,locale);
    }

    @PostMapping(path = "/view-reply-message", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity<?> viewReplyMessage(@RequestHeader(name = "X-localization", required = false,defaultValue = "so")
                                               Locale locale,
                                                @ModelAttribute ViewReplyMessageRequest request) throws IOException {
        return ticketService.viewReplyMessage(request,locale);
    }

    @PostMapping(path = "/reply-support-ticket", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity<?> replySupportTicket(@RequestHeader(name = "X-localization", required = false,defaultValue = "so")
                                               Locale locale,
                                                @ModelAttribute ReplySupportTicketRequest request) throws IOException {
        return ticketService.replySupportTicket(request,locale);
    }

    @PostMapping(path = "/change-support-ticket-status", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity<?> changeSupportTicketStatus(@RequestHeader(name = "X-localization", required = false,defaultValue = "so")
                                               Locale locale,
                                                @ModelAttribute ChangeSupportTicketStatusRequest request) throws IOException {
        return ticketService.changeSupportTicketStatus(request,locale);
    }

    @PostMapping(path = "/check-on-going-consultation", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity<?> checkOnGoingConsultation(@RequestHeader(name = "X-localization", required = false,defaultValue = "so")
                                               Locale locale,
                                                      @ModelAttribute ConsultationsRequest request) throws IOException {
        return consultationService.checkOnGoingConsultation(request.getUser_id(), locale);
    }

    @PostMapping(path = "/consultations", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity<?> consultations(@RequestHeader(name = "X-localization", required = false,defaultValue = "so") Locale locale,
                                           @RequestHeader(name = "X-type") String type,
                                           @ModelAttribute ConsultationsRequest request) {
        return consultationService.consultations(request,type,locale);
    }

    @PostMapping(path = "/search-consultations", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity<?> searchConsultations(@RequestHeader(name = "X-localization", required = false,defaultValue = "so") Locale locale,
                                           @RequestHeader(name = "X-type") String type,
                                           @ModelAttribute ConsultationsRequest request) {
        return consultationService.searchConsultations(request,type,locale);
    }

    @GetMapping("/app-banner")
    public ResponseEntity<?> appBanner(@RequestHeader(name = "X-localization", required = false,defaultValue = "so") Locale locale) {
        return siteService.appBanner(locale);
    }

    @PostMapping(path = "/get-video-attachment", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity<?> getVideoAttachment(@RequestHeader(name = "X-localization", required = false,defaultValue = "so") Locale locale,
                                                @ModelAttribute GetSloatsRequest request) {
        return siteService.getVideoAttachment(locale,request);
    }
    @PostMapping(path="/login", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public Object actionLogin(@RequestHeader(name = "X-localization", required = false,defaultValue = "so") Locale locale,@ModelAttribute MobileReleaseRequest request) {
        return authService.actionLogin(request,locale);
    }
    /*
     logout api
     */
    @PostMapping(path= "/logout", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity<?> resendOTP(@RequestHeader(name = "X-localization", required = false,defaultValue = "so")
                                       Locale locale,
                                       @ModelAttribute LogoutRequest request) {
        if(request.getUser_id()!= null){
            return authService.logout(locale,request);
        }else{
            return ResponseEntity.status(HttpStatus.NO_CONTENT).body(new Response(
                    Constants.NO_RECORD_FOUND_CODE,
                    Constants.BLANK_DATA_GIVEN_CODE,
                    messageSource.getMessage(Constants.BLANK_DATA_GIVEN,null,locale)
            ));
        }
    }
}
