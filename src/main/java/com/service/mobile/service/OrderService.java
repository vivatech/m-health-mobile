package com.service.mobile.service;

import com.service.mobile.config.Constants;
import com.service.mobile.customException.MobileServiceExceptionHandler;
import com.service.mobile.dto.dto.ClinicInformation;
import com.service.mobile.dto.dto.HomeConsultationInformation;
import com.service.mobile.dto.enums.YesNo;
import com.service.mobile.dto.response.ActivitiesResponse;
import com.service.mobile.dto.response.ConsultationDTO;
import com.service.mobile.dto.response.RecentOrdersResponseDTO;
import com.service.mobile.dto.response.Response;
import com.service.mobile.model.Consultation;
import com.service.mobile.model.Orders;
import com.service.mobile.model.PackageUser;
import com.service.mobile.model.Users;
import com.service.mobile.repository.ConsultationRepository;
import com.service.mobile.repository.OrdersRepository;
import com.service.mobile.repository.UsersRepository;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@Slf4j
public class OrderService {

    @Autowired
    private MessageSource messageSource;

    @Autowired
    private UsersRepository usersRepository;

    @Autowired
    private PublicService publicService;

    @Autowired
    private ConsultationRepository consultationRepository;

    @Value("${app.base.url}")
    private String baseUrl;

    @Value("${app.currency.symbol.fdj}")
    private String currencySymbolFdj;
    @Value("${app.ZoneId}")
    private String zoneId;

    @Autowired
    private OrdersRepository ordersRepository;

    public ResponseEntity<?> recentOrders(Locale locale, String userId) {
        log.info("Entering into recent order api : {}", userId);
        if (StringUtils.isEmpty(userId)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new Response(
                    Constants.NO_CONTENT_FOUNT_CODE,
                    Constants.NO_CONTENT_FOUNT_CODE,
                    messageSource.getMessage(Constants.BLANK_DATA_GIVEN, null, locale)
            ));
        }
        Users userInfo = usersRepository.findById(Integer.parseInt(userId)).orElseThrow(()-> new MobileServiceExceptionHandler(messageSource.getMessage(Constants.USER_NOT_FOUND, null, locale)));
        String photoPath = userInfo.getProfilePicture() != null ? baseUrl + "uploaded_file/UserProfile/" + userInfo.getUserId() + "/" + userInfo.getProfilePicture() : "";
        LocalDate currentDate = LocalDate.now(ZoneId.of(zoneId));
        List<Consultation> consultations = consultationRepository.findUpcomingConsultationsForPatient(Integer.parseInt(userId), currentDate);
        List<ConsultationDTO> consultationDTOList = new ArrayList<>();

        if(!consultations.isEmpty()){
            for (Consultation consultation : consultations) {
                if (consultation.getConsultationDate().isEqual(LocalDate.now(ZoneId.of(zoneId)))
                            && consultation.getSlotId().getSlotStartTime()
                            .isBefore(LocalTime.now(ZoneId.of(zoneId)))) {

                } else {
                    HomeConsultationInformation nurse = null;
                    if (consultation.getConsultType() != null &&
                                consultation.getConsultType().equalsIgnoreCase("visit_home")) {
                            nurse = publicService.getHomeConsultationInformation(Integer.parseInt(userId));
                    }

                    ClinicInformation clinic = null;
                    if (consultation.getConsultType() != null &&
                                (consultation.getConsultType().equalsIgnoreCase("visit") ||
                                        (consultation.getConsultType().equalsIgnoreCase("clinic visit")))) {
                        clinic = publicService.getClinicInformation(consultation.getDoctorId().getHospitalId());
                    }
                    String currency = "";
                    Orders orderDetails = ordersRepository.findByCaseId(consultation.getCaseId());
                    if (orderDetails.getCurrency() != null && !orderDetails.getCurrency().isEmpty()) {
                        currency = orderDetails.getCurrency();
                    } else {
                        currency = currencySymbolFdj;
                    }
                    Float amount = (orderDetails.getCurrencyAmount() != null) ? orderDetails.getCurrencyAmount() : orderDetails.getAmount();
                    String cancelMessage = (consultation.getCancelMessage() != null && !consultation.getCancelMessage().isEmpty()) ?
                            consultation.getCancelMessage() : "";
                    ConsultationDTO consultationDTO = new ConsultationDTO();

                    consultationDTO.setCase_id(consultation.getCaseId());
                    consultationDTO.setName(consultation.getDoctorId().getFirstName() + " " + consultation.getDoctorId().getLastName());
                    consultationDTO.setConsult_type(consultation.getConsultType().equalsIgnoreCase("call") ? "video" : consultation.getConsultType());
                    consultationDTO.setConsultation_type(consultation.getConsultationType().toString());
                    consultationDTO.setDate(consultation.getConsultationDate());
                    consultationDTO.setTime(consultation.getSlotId().getSlotTime());
                    consultationDTO.setCharges((amount != null) ? currency + " " + amount : "Free");
                    consultationDTO.setCancel_reason(cancelMessage);
                    consultationDTO.setStatus(consultation.getRequestType().toString());
                    consultationDTO.setProfile_pic((consultation.getDoctorId().getProfilePicture() != null && !consultation.getDoctorId().getProfilePicture().isEmpty()) ?
                            baseUrl + "uploaded_file/UserProfile/" + consultation.getDoctorId().getUserId() + "/" + consultation.getDoctorId().getProfilePicture() :
                            "");
                    consultationDTO.setNurse(nurse);
                    consultationDTO.setClinic(clinic);

                    consultationDTOList.add(consultationDTO);
                }
            }
            return ResponseEntity.ok(new RecentOrdersResponseDTO(Constants.SUCCESS_CODE,
                        messageSource.getMessage(Constants.RECENT_ORDERS_FOUND_SUCCESSFULLY, null, locale),
                        userInfo.getFirstName(), userInfo.getLastName(), photoPath, consultationDTOList));
        }

        Map<String, Object> response = new HashMap<>();
        response.put("status", Constants.SUCCESS_CODE);
        response.put("message", messageSource.getMessage(Constants.RECENT_ORDERS_FOUND_SUCCESSFULLY, null, locale));
        response.put("first_name", userInfo.getFirstName());
        response.put("last_name", userInfo.getLastName());
        response.put("profile_picture", photoPath);
        response.put("data", new ArrayList<>());
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}
