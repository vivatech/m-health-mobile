package com.service.mobile.controllers;

import com.service.mobile.config.Constants;
import com.service.mobile.dto.dto.DoctorRattingDTO;
import com.service.mobile.dto.request.ListSupportTicketsRequest;
import com.service.mobile.dto.request.MobileReleaseRequest;
import com.service.mobile.dto.request.NearByDoctorRequest;
import com.service.mobile.dto.request.UpdatePictureRequest;
import com.service.mobile.dto.response.NearByDoctorResponse;
import com.service.mobile.dto.response.Response;
import com.service.mobile.model.Country;
import com.service.mobile.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RequestHeader;

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

    @PostMapping("/mobile-release")
    public ResponseEntity<?> actionMobileRelease(@RequestHeader(name = "X-localization", required = false,defaultValue = "so") Locale locale,@RequestBody MobileReleaseRequest request) {
        return siteService.getMobileRelease(request,locale);
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
            response.setData(countries);
            response.setMessage(messageSource.getMessage(Constants.COUNTRY_LIST_RECIVED,null,locale));
            responseEntity = ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
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

    @PostMapping("/activities")
    public ResponseEntity<?> activities(@RequestHeader(name = "X-localization", required = false,defaultValue = "so")
                                        Locale locale,
                                        @RequestParam(name = "user_id",required = false) Integer user_id
    ) {
        return publicService.activities(locale,user_id);
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

    @PostMapping("/recent-orders")
    public ResponseEntity<?> recentOrders(@RequestHeader(name = "X-localization", required = false,defaultValue = "so")
                                          Locale locale,
                                          @RequestHeader(name = "X-type", required = false)
                                          String type,
                                          @RequestParam(name = "user_id",required = false) Integer user_id
    ) {
        return orderService.recentOrders(locale,user_id,type);
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

    //TODO Discuss with shamshad sir
    @PostMapping("/update-profile-picture") //in post man post method is written
    public ResponseEntity<?> updateProfilePicture(@ModelAttribute UpdatePictureRequest request,
        @RequestHeader(name = "X-localization", required = false,defaultValue = "so") Locale locale) {
        return usersService.updateProfilePicture(request);
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

    @GetMapping("/nearby-doctor")
    public ResponseEntity<?> nearByDoctor(@RequestHeader(name = "X-localization", required = false,defaultValue = "so")
                                               Locale locale,
                                          @RequestBody NearByDoctorRequest request) {
        return publicService.nearByDoctor(locale,request);
    }

    @GetMapping("/get-all-category-list")
    public ResponseEntity<?> getAllCategoriesList(@RequestHeader(name = "X-localization", required = false,defaultValue = "so")
                                               Locale locale) {
        return publicService.getAllCategoriesList(locale);
    }

    @GetMapping("/list-support-tickets")
    public ResponseEntity<?> listSupportTickets(@RequestHeader(name = "X-localization", required = false,defaultValue = "so")
                                               Locale locale,
                                                @RequestBody ListSupportTicketsRequest request) {
        return ticketService.listSupportTickets(request,locale);
    }
}
