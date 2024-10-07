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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Service
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

    public ResponseEntity<?> checkOnGoingConsultation(Integer userId, Locale locale) {
        List<SlotType> slotTypeOpt = slotTypeRepository.findByStatus(SlotStatus.active);

        Integer slotValue = Integer.parseInt(slotTypeOpt.get(0).getValue());
        LocalTime consultationStartTime = LocalTime.now();
        LocalTime consultationEndTime = consultationStartTime.minus(slotValue, ChronoUnit.MINUTES);

        LocalDate localDate = LocalDate.now();
        List<RequestType> requestType = List.of(RequestType.Book,RequestType.Pending);
        Optional<Consultation> consultationOpt = consultationRepository.findUpcomingConsultationForPatient(
                userId,requestType,
                consultationStartTime,
                consultationEndTime,localDate
        );

        if (!consultationOpt.isPresent()) {
            return ResponseEntity.ok(new Response(Constants.SUCCESS_CODE, "No records found",Constants.SUCCESS_CODE, null));
        }

        Consultation consultation = consultationOpt.get();
        Orders orderDetail = ordersRepository.findByCaseId(consultation.getCaseId());
        String amount = String.valueOf((orderDetail.getCurrencyAmount() != null) ? orderDetail.getCurrencyAmount() : orderDetail.getAmount());

        CheckOnGoingConsultationDto responseDTO = new CheckOnGoingConsultationDto();
        responseDTO.setCase_id(consultation.getCaseId());
        responseDTO.setName(consultation.getDoctorId().getFirstName() + " " + consultation.getDoctorId().getLastName());
        responseDTO.setConsult_type(consultation.getConsultType().equals("call") ? "video" : consultation.getConsultType());
        responseDTO.setConsultation_type(consultation.getConsultationType());
        responseDTO.setDate(consultation.getConsultationDate().toString());
        responseDTO.setTime(consultation.getSlotId().getSlotTime());
        responseDTO.setCharges((amount != null) ? currencySymbolFdj.toLowerCase() + " " + amount : "free");
        responseDTO.setStatus(consultation.getRequestType());
        responseDTO.setProfile_pic((consultation.getDoctorId().getProfilePicture() != null && !consultation.getDoctorId().getProfilePicture().isEmpty())
                ? baseUrl + "uploaded_file/UserProfile/" + consultation.getDoctorId().getUserId() + "/" + consultation.getDoctorId().getProfilePicture() : "");

        return ResponseEntity.status(HttpStatus.OK).body(new Response(
                Constants.SUCCESS_CODE,
                Constants.SUCCESS_CODE,
                messageSource.getMessage(Constants.SUCCESS,null,locale),
                responseDTO
        ));
    }

    public ResponseEntity<?> consultations(ConsultationsRequest request,String type, Locale locale) {
        Pageable pageable = PageRequest.of(request.getPage(),5);
        if(type.equalsIgnoreCase("Patient")){
            Page<Consultation> consultations = consultationRepository.findByPatientIdOrderByCaseId(request.getUser_id(),pageable);
            if(!consultations.getContent().isEmpty()){
                List<ConsultationResponse> list = new ArrayList<>();
                for(Consultation consultation:consultations.getContent()){
                    HomeConsultationInformation nurse = null;
                    ClinicInformation clinic = null;
                    if(consultation.getConsultType().equalsIgnoreCase("visit_home")){
                        nurse = publicService.getHomeConsultationInformation(consultation.getAssignedTo());
                    }
                    if(consultation.getConsultType().equalsIgnoreCase("visit") ||
                            consultation.getConsultType().equalsIgnoreCase("clinic visit")){
                        clinic = publicService.getClinicInformation(consultation.getDoctorId().getUserId());
                    }
                    String cancelMessage = consultation.getCancelMessage();
                    ConsultationResponse dto = new ConsultationResponse();

                    dto.setCase_id(consultation.getCaseId());
                    dto.setName(consultation.getDoctorId().getFirstName()+" "+consultation.getDoctorId().getLastName());
                    dto.setConsult_type(consultation.getConsultType().equalsIgnoreCase("call")?
                            "video":consultation.getConsultType());
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
                        messageSource.getMessage(Constants.RECENT_ORDERS_FOUND_SUCCESSFULLY,null,locale),
                        list
                ));
            }else{
                return ResponseEntity.status(HttpStatus.OK).body(new Response(
                        Constants.SUCCESS_CODE,
                        Constants.SUCCESS_CODE,
                        messageSource.getMessage(Constants.NO_RECORD_FOUND,null,locale)
                ));
            }
        }else{
            Page<Consultation> consultations = consultationRepository.findByDoctorIdOrderByCaseId(request.getUser_id(),pageable);
            if(!consultations.getContent().isEmpty()){
                List<ConsultationResponse> list = new ArrayList<>();
                for(Consultation consultation:consultations.getContent()){
                    String cancelMessage = consultation.getCancelMessage();
                    ConsultationResponse dto = new ConsultationResponse();

                    dto.setCase_id(consultation.getCaseId());
                    dto.setPatient_id(consultation.getPatientId().getUserId());
                    dto.setName(consultation.getPatientId().getFirstName()+" "+consultation.getPatientId().getLastName());
                    dto.setConsult_type(consultation.getConsultType());
                    dto.setConsultation_type(consultation.getConsultationType());
                    dto.setAdded_type(consultation.getAddedType());
                    dto.setDate(consultation.getConsultationDate());
                    dto.setTime(consultation.getSlotId().getSlotTime());
                    dto.setCharges(publicService.getTotalConsultationAmount(consultation.getCaseId()));
                    dto.setStatus(consultation.getRequestType());
                    dto.setCancel_reason(cancelMessage);
                    dto.setProfile_pic((consultation.getPatientId().getProfilePicture() != null && !consultation.getPatientId().getProfilePicture().isEmpty())
                            ? baseUrl + "uploaded_file/UserProfile/" + consultation.getPatientId().getUserId() + "/" + consultation.getPatientId().getProfilePicture() : "");
                    dto.setTotal_count(consultations.getTotalElements());

                    list.add(dto);
                }
                return ResponseEntity.status(HttpStatus.OK).body(new Response(
                        Constants.SUCCESS_CODE,
                        Constants.SUCCESS_CODE,
                        messageSource.getMessage(Constants.RECENT_ORDERS_FOUND_SUCCESSFULLY,null,locale),
                        list
                ));
            }else{
                return ResponseEntity.status(HttpStatus.OK).body(new Response(
                        Constants.SUCCESS_CODE,
                        Constants.SUCCESS_CODE,
                        messageSource.getMessage(Constants.NO_RECORD_FOUND,null,locale)
                ));
            }
        }
    }

    public ResponseEntity<?> searchConsultations(ConsultationsRequest request, String type, Locale locale) {
        if(type.equalsIgnoreCase("Patient")){
            List<Consultation> consultations = consultationRepository.findByPatientIdAndDateOrderByCaseId(request.getUser_id(),request.getDate());
            if(!consultations.isEmpty()){
                List<ConsultationResponse> list = new ArrayList<>();
                for(Consultation consultation:consultations){
                    String cancelMessage = consultation.getCancelMessage();
                    ConsultationResponse dto = new ConsultationResponse();

                    dto.setCase_id(consultation.getCaseId());
                    dto.setName(consultation.getDoctorId().getFirstName()+" "+consultation.getDoctorId().getLastName());
                    dto.setConsult_type(consultation.getConsultType());
                    dto.setConsultation_type(consultation.getConsultationType());
                    dto.setAdded_type(consultation.getAddedType());
                    dto.setDate(consultation.getConsultationDate());
                    dto.setTime(consultation.getSlotId().getSlotTime());
                    dto.setCharges(publicService.getTotalConsultationAmount(consultation.getCaseId()));
                    dto.setStatus(consultation.getRequestType());
                    dto.setCancel_reason(cancelMessage);
                    dto.setProfile_pic((consultation.getDoctorId().getProfilePicture() != null && !consultation.getDoctorId().getProfilePicture().isEmpty())
                            ? baseUrl + "uploaded_file/UserProfile/" + consultation.getDoctorId().getUserId() + "/" + consultation.getDoctorId().getProfilePicture() : "");
                    dto.setTotal_count(Long.valueOf(consultations.size()));

                    list.add(dto);
                }
                return ResponseEntity.status(HttpStatus.OK).body(new Response(
                        Constants.SUCCESS_CODE,
                        Constants.SUCCESS_CODE,
                        messageSource.getMessage(Constants.RECENT_ORDERS_FOUND_SUCCESSFULLY,null,locale),
                        list
                ));
            }else{
                return ResponseEntity.status(HttpStatus.OK).body(new Response(
                        Constants.SUCCESS_CODE,
                        Constants.SUCCESS_CODE,
                        messageSource.getMessage(Constants.NO_RECORD_FOUND,null,locale)
                ));
            }
        }else{
            List<Consultation> consultations = consultationRepository.findByDoctorIdAndDateOrderByCaseId(request.getUser_id(),request.getDate());
            if(!consultations.isEmpty()){
                List<ConsultationResponse> list = new ArrayList<>();
                for(Consultation consultation:consultations){
                    String cancelMessage = consultation.getCancelMessage();
                    ConsultationResponse dto = new ConsultationResponse();

                    dto.setCase_id(consultation.getCaseId());
                    dto.setPatient_id(consultation.getPatientId().getUserId());
                    dto.setName(consultation.getPatientId().getFirstName()+" "+consultation.getPatientId().getLastName());
                    dto.setConsult_type(consultation.getConsultType());
                    dto.setConsultation_type(consultation.getConsultationType());
                    dto.setAdded_type(consultation.getAddedType());
                    dto.setDate(consultation.getConsultationDate());
                    dto.setTime(consultation.getSlotId().getSlotTime());
                    dto.setCharges(publicService.getTotalConsultationAmount(consultation.getCaseId()));
                    dto.setStatus(consultation.getRequestType());
                    dto.setCancel_reason(cancelMessage);
                    dto.setProfile_pic((consultation.getPatientId().getProfilePicture() != null && !consultation.getPatientId().getProfilePicture().isEmpty())
                            ? baseUrl + "uploaded_file/UserProfile/" + consultation.getPatientId().getUserId() + "/" + consultation.getPatientId().getProfilePicture() : "");
                    dto.setTotal_count(Long.valueOf(consultations.size()));

                    list.add(dto);
                }
                return ResponseEntity.status(HttpStatus.OK).body(new Response(
                        Constants.SUCCESS_CODE,
                        Constants.SUCCESS_CODE,
                        messageSource.getMessage(Constants.RECENT_ORDERS_FOUND_SUCCESSFULLY,null,locale),
                        list
                ));
            }else{
                return ResponseEntity.status(HttpStatus.OK).body(new Response(
                        Constants.SUCCESS_CODE,
                        Constants.SUCCESS_CODE,
                        messageSource.getMessage(Constants.NO_RECORD_FOUND,null,locale)
                ));
            }
        }
    }



//    public ResponseEntity<?> getConsultationType(Locale locale) {
//
//        List<ConsultationDTO> consultDtoList = new ArrayList<>();
//
//        for (ConsultType consultType : APIGatewayEnum.ConsultType.values()) {
//            ConsultDto consultDto = new ConsultDto();
//            consultDto.setType(consultType);
//            consultDto.setTitle(getConsultTypeTitle(consultType));
//
//            consultDtoList.add(consultDto);
//        }
//
//    }
}
