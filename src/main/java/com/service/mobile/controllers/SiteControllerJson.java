package com.service.mobile.controllers;

import com.service.mobile.config.Constants;
import com.service.mobile.dto.dto.DoctorRattingDTO;
import com.service.mobile.dto.request.*;
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

import java.io.IOException;
import java.util.List;
import java.util.Locale;

@RestController
@RequestMapping("/mobile/site")
public class SiteControllerJson {

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

    @PostMapping(path="/mobile-release", consumes = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<?> actionMobileRelease(@RequestHeader(name = "X-localization", required = false,defaultValue = "so") Locale locale, @RequestHeader(name = "X-type", required = false) String type, @RequestBody MobileReleaseRequest request) {
        return siteService.getMobileRelease(request,locale, type);
    }

    @PostMapping(path = "/activities", consumes = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<?> activities(@RequestHeader(name = "X-localization", required = false,defaultValue = "so")
                                        Locale locale,
                                        @RequestBody UpdatePictureRequest request
    ) {
        return publicService.activities(locale, request.getUser_id());
    }

    @PostMapping(path = "/recent-orders", consumes = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<?> recentOrders(@RequestHeader(name = "X-localization", required = false,defaultValue = "so")
                                          Locale locale,
                                          @RequestHeader(name = "X-type", required = false)
                                          String type,
                                          @RequestBody UpdatePictureRequest request
    ) {
        return orderService.recentOrders(locale,request.getUser_id(),type);
    }

    @PostMapping(path = "/update-profile-picture", consumes = {MediaType.APPLICATION_JSON_VALUE}) //in post man post method is written
    public ResponseEntity<?> updateProfilePicture(@RequestBody UpdatePictureRequest request,
        @RequestHeader(name = "X-localization", required = false,defaultValue = "so") Locale locale) {
        return usersService.updateProfilePicture(request,locale);
    }

    @GetMapping(path = "/nearby-doctor", consumes = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<?> nearByDoctor(@RequestHeader(name = "X-localization", required = false,defaultValue = "so")
                                               Locale locale,
                                          @RequestBody NearByDoctorRequest request) {
        return publicService.nearByDoctor(locale,request);
    }

    @PostMapping(path = "/list-support-tickets", consumes = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<?> listSupportTickets(@RequestHeader(name = "X-localization", required = false,defaultValue = "so")
                                               Locale locale,
                                                @RequestBody ListSupportTicketsRequest request) {
        return ticketService.listSupportTickets(request,locale);
    }

    @PostMapping(path = "/create-support-ticket", consumes = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<?> createSupportTicket(@RequestHeader(name = "X-localization", required = false,defaultValue = "so")
                                               Locale locale,
                                                @RequestBody CreateSupportTicketsRequest request) throws IOException {
        return ticketService.createSupportTicket(request,locale);
    }

    @PostMapping(path = "/view-reply-message", consumes = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<?> viewReplyMessage(@RequestHeader(name = "X-localization", required = false,defaultValue = "so")
                                               Locale locale,
                                                @RequestBody ViewReplyMessageRequest request) throws IOException {
        return ticketService.viewReplyMessage(request,locale);
    }

    @PostMapping(path = "/reply-support-ticket", consumes = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<?> replySupportTicket(@RequestHeader(name = "X-localization", required = false,defaultValue = "so")
                                               Locale locale,
                                                @RequestBody ReplySupportTicketRequest request) throws IOException {
        return ticketService.replySupportTicket(request,locale);
    }

    @PostMapping(path = "/change-support-ticket-status", consumes = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<?> changeSupportTicketStatus(@RequestHeader(name = "X-localization", required = false,defaultValue = "so")
                                               Locale locale,
                                                @RequestBody ChangeSupportTicketStatusRequest request) throws IOException {
        return ticketService.changeSupportTicketStatus(request,locale);
    }

    @PostMapping(path = "/check-on-going-consultation", consumes = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<?> checkOnGoingConsultation(@RequestHeader(name = "X-localization", required = false,defaultValue = "so")
                                               Locale locale,
                                                      @RequestBody ConsultationsRequest request) throws IOException {
        return consultationService.checkOnGoingConsultation(request.getUser_id(), locale);
    }

    @PostMapping(path = "/consultations", consumes = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<?> consultations(@RequestHeader(name = "X-localization", required = false,defaultValue = "so") Locale locale,
                                           @RequestHeader(name = "X-type") String type,
                                           @RequestBody ConsultationsRequest request) {
        return consultationService.consultations(request,type,locale);
    }

    @PostMapping(path = "/search-consultations", consumes = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<?> searchConsultations(@RequestHeader(name = "X-localization", required = false,defaultValue = "so") Locale locale,
                                           @RequestHeader(name = "X-type") String type,
                                           @RequestBody ConsultationsRequest request) {
        return consultationService.searchConsultations(request,type,locale);
    }

    @PostMapping(path = "/get-video-attachment", consumes = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<?> getVideoAttachment(@RequestHeader(name = "X-localization", required = false,defaultValue = "so") Locale locale,
                                                @RequestBody GetSloatsRequest request) {
        return siteService.getVideoAttachment(locale,request);
    }
    @PostMapping(path="/login", consumes = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<?> actionLogin(@RequestHeader(name = "X-localization", required = false,defaultValue = "so") Locale locale,@RequestBody MobileReleaseRequest request) {
        return authService.actionLogin(request,locale);
    }
}
