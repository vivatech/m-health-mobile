package com.service.mobile.controllers;

import com.service.mobile.config.Constants;
import com.service.mobile.dto.request.MobileReleaseRequest;
import com.service.mobile.dto.response.Response;
import com.service.mobile.model.Country;
import com.service.mobile.service.PublicService;
import com.service.mobile.service.SiteService;
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

    @GetMapping("/activities")
    public ResponseEntity<?> activities(@RequestHeader(name = "X-localization", required = false,defaultValue = "so")
                                        Locale locale,
                                        @RequestParam(name = "user_id",required = false) Integer user_id
    ) {
        return publicService.activities(locale,user_id);
    }

    @GetMapping("/get-consult-type")
    public ResponseEntity<?> getConsultType(@RequestHeader(name = "X-localization", required = false,defaultValue = "so")
                                            Locale locale,
                                            @RequestParam(name = "user_id",required = false) Integer user_id
    ) {
        return publicService.getConsultType(locale,user_id);
    }

}
