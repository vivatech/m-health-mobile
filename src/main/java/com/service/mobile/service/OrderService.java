package com.service.mobile.service;

import com.service.mobile.config.Constants;
import com.service.mobile.dto.dto.ClinicInformation;
import com.service.mobile.dto.dto.HomeConsultationInformation;
import com.service.mobile.dto.enums.YesNo;
import com.service.mobile.dto.response.ActivitiesResponse;
import com.service.mobile.dto.response.ConsultationDTO;
import com.service.mobile.dto.response.RecentOrdersResponseDTO;
import com.service.mobile.dto.response.Response;
import com.service.mobile.model.Consultation;
import com.service.mobile.model.PackageUser;
import com.service.mobile.model.Users;
import com.service.mobile.repository.ConsultationRepository;
import com.service.mobile.repository.UsersRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Service
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

    public ResponseEntity<?> recentOrders(Locale locale, Integer userId,String type) {
//        if (userId!=null && userId!=0) {
//            Users userInfo = usersRepository.findById(userId).orElse(null);
//            String photoPath = userInfo.getProfilePicture() != null ? baseUrl+"uploaded_file/UserProfile/" + userInfo.getUserId() + "/" + userInfo.getProfilePicture() : "";
//
//            if ("Patient".equals(type)) {
//                List<Consultation> consultations = consultationRepository.findUpcomingConsultationsForPatient(userId);
//                List<ConsultationDTO> consultationDTOList = new ArrayList<>();
//                for (Consultation consultation : consultations) {
//                    HomeConsultationInformation nurse = null;
//                    if(consultation.getConsultType()!=null &&
//                            consultation.getConsultType().equalsIgnoreCase("visit_home")){
//                        nurse = publicService.getHomeConsultationInformation(userId);
//                    }
//                    ClinicInformation clinic=null;
//                    if(consultation.getConsultType()!=null &&
//                            (consultation.getConsultType().equalsIgnoreCase("visit")||
//                            (consultation.getConsultType().equalsIgnoreCase("clinic visit")))){
//                        clinic = publicService.getClinicInformation(userId);
//                    }
//                    String currency = "";
//                    if(consultation.getO)
//                    ConsultationDTO consultationDTO = new ConsultationDTO();
//
//                    // Map consultation to consultationDTO
//                    // Use helperService if necessary
//                    consultationDTOList.add(consultationDTO);
//                }
//
//                return ResponseEntity.ok(new RecentOrdersResponseDTO("200", "recent_order_fetched_success", userInfo.getFirstName(), userInfo.getLastName(), photoPath, consultationDTOList));
//            } else {
//                List<Consultation> consultations = consultationRepository.findUpcomingConsultationsForDoctor(userId);
//                List<ConsultationDTO> consultationDTOList = new ArrayList<>();
//                for (Consultation consultation : consultations) {
//                    publicService.getHomeConsultationInformation(userId);
//                    ConsultationDTO consultationDTO = new ConsultationDTO();
//                    // Map consultation to consultationDTO
//                    consultationDTOList.add(consultationDTO);
//                }
//
//                return ResponseEntity.ok(new RecentOrdersResponseDTO("200", "recent_order_fetched_success", userInfo.getFirstName(), userInfo.getLastName(), photoPath, consultationDTOList));
//            }
//        }else{
//            return ResponseEntity.status(HttpStatus.NO_CONTENT).body(new Response(
//                    Constants.NO_RECORD_FOUND_CODE,
//                    Constants.BLANK_DATA_GIVEN_CODE,
//                    messageSource.getMessage(Constants.BLANK_DATA_GIVEN,null,locale)
//            ));
//        }
        return null;
    }
}
