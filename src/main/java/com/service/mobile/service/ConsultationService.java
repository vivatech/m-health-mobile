package com.service.mobile.service;

import com.service.mobile.config.Constants;
import com.service.mobile.dto.dto.CheckOnGoingConsultationDto;
import com.service.mobile.dto.dto.ClinicInformation;
import com.service.mobile.dto.dto.HomeConsultationInformation;
import com.service.mobile.dto.enums.SlotStatus;
import com.service.mobile.dto.request.ConsultationsRequest;
import com.service.mobile.dto.enums.RequestType;
import com.service.mobile.dto.response.ConsultationResponse;
import com.service.mobile.dto.response.Response;
import com.service.mobile.model.Consultation;
import com.service.mobile.model.Orders;
import com.service.mobile.model.SlotType;
import com.service.mobile.repository.ConsultationRepository;
import com.service.mobile.repository.OrdersRepository;
import com.service.mobile.repository.SlotTypeRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Service
@Slf4j
public class ConsultationService {

    @Autowired
    private SlotTypeRepository slotTypeRepository;

    @Autowired
    private ConsultationRepository consultationRepository;

    @Autowired
    private OrdersRepository ordersRepository;

    @Autowired
    private MessageSource messageSource;

    @Autowired
    private PublicService publicService;

    @Value("${app.base.url}")
    private String baseUrl;

    @Value("${app.currency.symbol.fdj}")
    private String currencySymbolFdj;
    @Value("${app.ZoneId}")
    private String zone;

    public ResponseEntity<?> checkOnGoingConsultation(String user_id, Locale locale) {

        log.info("Entering into check on going consultation api : {}", user_id);
        try {
            int userId = Integer.parseInt(user_id);
            LocalTime consultationStartTime = LocalTime.now(ZoneId.of(zone));

            LocalDate currentDate = LocalDate.now(ZoneId.of(zone));

            List<Consultation> consultationList = consultationRepository.findUpcomingConsultationForPatient(userId, currentDate);

            if (consultationList.isEmpty()) {
                return ResponseEntity.ok(new Response(Constants.SUCCESS_CODE, Constants.SUCCESS_CODE, Constants.SUCCESS, null));
            }

            for(Consultation c : consultationList){
                String[] timeArray = c.getSlotId().getSlotTime().split(":");
                LocalTime st = LocalTime.parse(timeArray[0]+":"+timeArray[1]);
                LocalTime et = LocalTime.parse(timeArray[2]+":"+timeArray[3]);

                if((consultationStartTime.equals(st) || consultationStartTime.isAfter(st))
                        && (consultationStartTime.isBefore(et) || consultationStartTime.equals(et))){

                    Orders orderDetail = ordersRepository.findByCaseId(c.getCaseId());
                    CheckOnGoingConsultationDto responseDTO = getCheckOnGoingConsultationDto(orderDetail, c);

                    return ResponseEntity.status(HttpStatus.OK).body(new Response(
                            Constants.SUCCESS_CODE,
                            Constants.SUCCESS_CODE,
                            messageSource.getMessage(Constants.SUCCESS_MESSAGE, null, locale),
                            responseDTO
                    ));
                }
            }
            return ResponseEntity.ok(new Response(Constants.SUCCESS_CODE, Constants.SUCCESS_CODE, Constants.SUCCESS, null));
        } catch (Exception e) {
            e.printStackTrace();
            log.error("Error found in on going consultation : {}", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new Response(
                    Constants.NO_CONTENT_FOUNT_CODE,
                    Constants.NO_CONTENT_FOUNT_CODE,
                    messageSource.getMessage(Constants.SOMETHING_WENT_WRONG, null, locale)
            ));
        }
    }

    private CheckOnGoingConsultationDto getCheckOnGoingConsultationDto(Orders orderDetail, Consultation consultation) {
        String amount = String.valueOf((orderDetail.getCurrencyAmount() != null) ? orderDetail.getCurrencyAmount() : orderDetail.getAmount());

        CheckOnGoingConsultationDto responseDTO = new CheckOnGoingConsultationDto();
        responseDTO.setCase_id(consultation.getCaseId());
        responseDTO.setName(consultation.getDoctorId() == null ? "" : consultation.getDoctorId().getFullName());
        responseDTO.setConsult_type(consultation.getConsultType() == null ? ""
                : consultation.getConsultType().equals("call") ? "video" : consultation.getConsultType());
        responseDTO.setConsultation_type(consultation.getConsultationType());
        responseDTO.setDate(consultation.getConsultationDate().toString());
        responseDTO.setTime(consultation.getSlotId() == null ? ""
                :consultation.getSlotId().getSlotTime());
        responseDTO.setCharges((amount != null) ? currencySymbolFdj.toLowerCase() + " " + amount : "free");
        responseDTO.setStatus(consultation.getRequestType());
        responseDTO.setProfile_pic((consultation.getDoctorId().getProfilePicture() != null && !consultation.getDoctorId().getProfilePicture().isEmpty())
                ? baseUrl + "uploaded_file/UserProfile/" + consultation.getDoctorId().getUserId() + "/" + consultation.getDoctorId().getProfilePicture() : "");
        return responseDTO;
    }

    public ResponseEntity<?> consultations(ConsultationsRequest request, Locale locale) {
        log.info("Entering into consultation api : {}", request);
        try {
            Map<String, Object> res = new HashMap<>();
            Pageable pageable = PageRequest.of(Integer.parseInt(request.getPage()), 5);
            LocalDateTime localDateTime = LocalDateTime.now(ZoneId.of(zone));
            LocalDate date = localDateTime.toLocalDate();
            Page<Consultation> consultations = consultationRepository.findByPatientIdAndConsultationDate(Integer.parseInt(request.getUser_id()), date, pageable);
            if (!consultations.getContent().isEmpty()) {
                List<ConsultationResponse> list = new ArrayList<>();
                for (Consultation consultation : consultations.getContent()) {
                    //check if the consultation is already passed then not to include
                    if (consultation.getConsultationDate().equals(localDateTime.toLocalDate()) && consultation.getSlotId().getSlotStartTime().isBefore(localDateTime.toLocalTime())) {
                        continue;
                    }
                    HomeConsultationInformation nurse = null;
                    ClinicInformation clinic = null;
                    if (consultation.getConsultType().equalsIgnoreCase("visit_home")) {
                        nurse = publicService.getHomeConsultationInformation(consultation.getAssignedTo());
                    }
                    if (consultation.getConsultType().equalsIgnoreCase("visit") ||
                            consultation.getConsultType().equalsIgnoreCase("clinic visit")) {
                        clinic = publicService.getClinicInformation(consultation.getDoctorId().getHospitalId());
                    }
                    String cancelMessage = consultation.getCancelMessage();
                    ConsultationResponse dto = new ConsultationResponse();

                    dto.setCase_id(consultation.getCaseId());
                    dto.setName(consultation.getDoctorId().getFirstName() + " " + consultation.getDoctorId().getLastName());
                    dto.setConsult_type(consultation.getConsultType().equalsIgnoreCase("call") ?
                            "video" : consultation.getConsultType());
                    dto.setConsultation_type(consultation.getConsultationType());
                    dto.setAdded_type(consultation.getAddedType());
                    dto.setDate(consultation.getConsultationDate());
                    dto.setTime(consultation.getSlotId().getSlotTime());
                    dto.setCharges(publicService.getTotalConsultationAmount(consultation.getCaseId()));
                    dto.setStatus(consultation.getRequestType());
                    dto.setCancel_reason(cancelMessage);
                    dto.setProfile_pic((consultation.getDoctorId().getProfilePicture() != null && !consultation.getDoctorId().getProfilePicture().isEmpty())
                            ? baseUrl + "uploaded_file/UserProfile/" + consultation.getDoctorId().getUserId() + "/" + consultation.getDoctorId().getProfilePicture() : "");
                    dto.setNurse(nurse);
                    dto.setClinic(clinic);
                    dto.setTotal_count(consultations.getTotalElements());

                    list.add(dto);
                }
                return ResponseEntity.status(HttpStatus.OK).body(new Response(
                        Constants.SUCCESS_CODE,
                        Constants.SUCCESS_CODE,
                        messageSource.getMessage(Constants.RECENT_ORDERS_FOUND_SUCCESSFULLY, null, locale),
                        list
                ));
            } else {
                return ResponseEntity.status(HttpStatus.OK).body(new Response(
                        Constants.SUCCESS_CODE,
                        Constants.SUCCESS_CODE,
                        messageSource.getMessage(Constants.NO_RECORD_FOUND, null, locale),
                        new ArrayList<>()
                ));
            }
        } catch (Exception e) {
            e.printStackTrace();
            log.error("Error found in consultation api : {}", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new Response(
                    Constants.NO_CONTENT_FOUNT_CODE,
                    Constants.NO_CONTENT_FOUNT_CODE,
                    messageSource.getMessage(Constants.SOMETHING_WENT_WRONG, null, locale)
            ));
        }
    }

    public ResponseEntity<?> searchConsultations(ConsultationsRequest request, Locale locale) {
        log.info("Entering into search consultation api : {}", request);
        Map<String, Object> res = new HashMap<>();
        try {
            List<Consultation> consultations = consultationRepository.findByPatientIdAndDateOrderByCaseId(Integer.parseInt(request.getUser_id()), LocalDate.parse(request.getDate()));
            if (!consultations.isEmpty()) {
                List<ConsultationResponse> list = new ArrayList<>();
                for (Consultation consultation : consultations) {
                    String cancelMessage = consultation.getCancelMessage();
                    ConsultationResponse dto = new ConsultationResponse();

                    dto.setCase_id(consultation.getCaseId());
                    dto.setName(consultation.getDoctorId() == null ? "" : consultation.getDoctorId().getFullName());
                    dto.setConsult_type(consultation.getConsultType());
                    dto.setConsultation_type(consultation.getConsultationType());
                    dto.setAdded_type(consultation.getAddedType());
                    dto.setDate(consultation.getConsultationDate());
                    dto.setTime(consultation.getSlotId() == null ? "": consultation.getSlotId().getSlotTime());
                    dto.setCharges(publicService.getTotalConsultationAmount(consultation.getCaseId()));
                    dto.setStatus(consultation.getRequestType());
                    dto.setCancel_reason(cancelMessage);
                    dto.setProfile_pic((consultation.getDoctorId().getProfilePicture() != null && !consultation.getDoctorId().getProfilePicture().isEmpty())
                            ? baseUrl + "uploaded_file/UserProfile/" + consultation.getDoctorId().getUserId() + "/" + consultation.getDoctorId().getProfilePicture() : "");
                    dto.setTotal_count((long) consultations.size());
                    ClinicInformation clinic = null;
                    if (consultation.getConsultType().equalsIgnoreCase("visit") ||
                            consultation.getConsultType().equalsIgnoreCase("clinic visit")) {
                        clinic = publicService.getClinicInformation(consultation.getDoctorId().getHospitalId());
                    }
                    dto.setClinic(clinic);

                    list.add(dto);
                }
                return ResponseEntity.status(HttpStatus.OK).body(new Response(
                        Constants.SUCCESS_CODE,
                        Constants.SUCCESS_CODE,
                        messageSource.getMessage(Constants.RECENT_ORDERS_FOUND_SUCCESSFULLY, null, locale),
                        list
                ));
            } else {
                return ResponseEntity.status(HttpStatus.OK).body(new Response(
                        Constants.SUCCESS_CODE,
                        Constants.SUCCESS_CODE,
                        messageSource.getMessage(Constants.NO_RECORD_FOUND, null, locale),
                        res
                ));
            }
        } catch (Exception e) {
            e.printStackTrace();
            log.error("Error found in search consultation : {}", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new Response(
                    Constants.NO_CONTENT_FOUNT_CODE,
                    Constants.NO_CONTENT_FOUNT_CODE,
                    messageSource.getMessage(Constants.SOMETHING_WENT_WRONG, null, locale)
            ));
        }
    }
}
