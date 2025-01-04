package com.service.mobile.service;

import com.service.mobile.config.Constants;
import com.service.mobile.customException.MobileServiceExceptionHandler;
import com.service.mobile.dto.availabiltyDoctorDto.DoctorAvailabilityRequest;
import com.service.mobile.dto.dto.*;
import com.service.mobile.dto.enums.*;
import com.service.mobile.dto.request.GetReviewRequest;
import com.service.mobile.dto.request.SearchDoctorRequest;
import com.service.mobile.dto.response.*;
import com.service.mobile.model.*;
import com.service.mobile.model.State;
import com.service.mobile.repository.*;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.*;

import static com.service.mobile.config.Constants.*;
import static com.service.mobile.constants.Constants.General_Practitioner;

@Service
@Slf4j
public class DoctorService {

    @Autowired
    private UsersRepository usersRepository;

    @Autowired
    private MessageSource messageSource;

    @Autowired
    private ConsultationRepository consultationRepository;

    @Autowired
    private ChargesRepository chargesRepository;

    @Autowired
    private ConsultationRatingRepository consultationRatingRepository;

    @Autowired
    private LanguageRepository languageRepository;

    @Autowired
    private DoctorSpecializationRepository doctorSpecializationRepository;

    @Autowired
    private DoctorAvailabilityRepository doctorAvailabilityRepository;

    @Value("${app.base.url}")
    private String baseUrl;
    @Autowired
    private SlotMasterRepository slotMasterRepository;
    @Autowired
    private GlobalConfigurationRepository globalConfigurationRepository;
    @Autowired
    private StateRepository stateRepository;
    @Autowired
    private CityRepository cityRepository;
    @Value("${app.ZoneId}")
    private String zone;
    @Value("${app.currency.symbol}")
    private String currencySymbol;
    @PersistenceContext
    private EntityManager entityManager;


    public ResponseEntity<?> getDoctorCityList(Locale locale) {
        Map<String, Object> res = new HashMap<>();
        List<City> cities = usersRepository.getCitiesByUsertype(UserType.Doctor);
        if(!cities.isEmpty()){
            List<Map<String, Object>> responses = new ArrayList<>();
            for(City c:cities){
                Map<String, Object> response =  new HashMap<>();
                response.put("id", c.getId());
                response.put("name", c.getName());
                responses.add(response);
            }
            return ResponseEntity.status(HttpStatus.OK).body(new Response(
                    Constants.SUCCESS_CODE,
                    Constants.SUCCESS_CODE,
                    messageSource.getMessage(Constants.CITY_FOUND_SUCCESSFULLY,null,locale),
                    responses
            ));
        }else{
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new Response(
                    Constants.NO_RECORD_FOUND_CODE,
                    Constants.BLANK_DATA_GIVEN_CODE,
                    messageSource.getMessage(Constants.NO_CITY_FOUND,null,locale)
            ));
        }
    }

    public ResponseEntity<?> searchDoctor(Locale locale, SearchDoctorRequest request) {
        if(StringUtils.isEmpty(request.getUser_id())){
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new Response(
                    Constants.BLANK_DATA_GIVEN_CODE,
                    Constants.BLANK_DATA_GIVEN_CODE,
                    messageSource.getMessage(Constants.UNAUTHORIZED_MSG, null, locale)
            ));
        }
        StringBuilder sb = new StringBuilder("SELECT u FROM Users u LEFT JOIN ConsultationRating cr ON cr.doctorId.userId = u.userId WHERE u.type = 'Doctor' AND u.status = 'A' ");
        String[] dayName = {"sunday", "monday", "tuesday", "wednesday", "thursday", "friday", "saturday"};
        LocalDateTime dateTime = LocalDateTime.now(ZoneId.of(zone));
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
        String formattedLocalTime = dateTime.format(formatter);
        LocalTime time = LocalTime.parse(formattedLocalTime);
        List<Integer> daySlots = null;

        LocalDate startDate = dateTime.toLocalDate();
        LocalDate endDate = dateTime.toLocalDate();

        //availability
        List<RequestType> type = Arrays.asList(RequestType.Book, RequestType.Inprocess, RequestType.Pending);
        if(!StringUtils.isEmpty(request.getAvailability())){
            if(request.getAvailability().equals("1") || request.getAvailability().equals("01")){
                DayOfWeek dayOfWeek = dateTime.toLocalDate().getDayOfWeek();
                dayName = new String[]{dayOfWeek.toString().toLowerCase()};
                daySlots = slotMasterRepository.findBySlotDayAndSlotStartTime(dayName, time, startDate, type);
            }
            else if(request.getAvailability().equals("2") || request.getAvailability().equals("02")){
                endDate = dateTime.toLocalDate().plusDays(1);
                startDate = dateTime.toLocalDate().plusDays(1);
                DayOfWeek dayOfWeek = dateTime.toLocalDate().plusDays(1).getDayOfWeek();
                dayName = new String[]{dayOfWeek.toString().toLowerCase()};
                daySlots = slotMasterRepository.findBySlotDay(dayName, startDate, endDate, type);
            }
            else {
                endDate = dateTime.toLocalDate().plusDays(6);
                daySlots = slotMasterRepository.findBySlotDay(dayName, startDate, endDate, type);
            }
            sb.append(" AND u.userId IN (SELECT da.doctorId.userId FROM DoctorAvailability da WHERE da.slotId.slotId IN (:daySlots) GROUP BY da.doctorId.userId ) ");
        }

        if(request.getDegree_id() != null){
            //Since no data in database regarding this filter
        }
        //clinic id
        if(!StringUtils.isEmpty(request.getClinic_id())){
            sb.append(" AND u.hospitalId = "+ Integer.parseInt(request.getClinic_id()));
        }
        //consult type
        if(!StringUtils.isEmpty(request.getConsult_type())){
            if(request.getConsult_type().equalsIgnoreCase("video")){
                sb.append(" AND u.hasDoctorVideo IN ('video','both') ");
            }
            else if(request.getConsult_type().equalsIgnoreCase("visit")){
                sb.append(" AND u.hasDoctorVideo IN ('visit','both') ");
            }
        }

        //enterprise
        List<String> enterpriseNumbers = null;
        if(request.getIs_enterprise() != null && !request.getIs_enterprise().isEmpty()){
            if(request.getIs_enterprise().equalsIgnoreCase("Yes")){
                GlobalConfiguration glb = globalConfigurationRepository.findByKey("ENTERPRISE_DOCTOR_CONTACT_NUMBER");
                enterpriseNumbers = Arrays.stream(glb.getValue().split(",")).toList();

                sb.append(" AND u.contactNumber IN (:enterpriseNumbers) ");
            }
        }

        //international or not
        String international = "No";
        if(!StringUtils.isEmpty(request.getIs_international())
                && request.getIs_international().equalsIgnoreCase("Yes"))
            international = "Yes";
        sb.append(" AND u.isInternational = '" + international + "' ");

        //specialization
        if(!StringUtils.isEmpty(request.getSpecialization_id())){
            //getting userIds from specialization
            sb.append(" AND u.userId IN (SELECT ds.userId.userId FROM DoctorSpecialization ds WHERE ds.userId.doctorClassification != 'general_practitioner' AND ds.specializationId.id IN (:sId)) ");
        }

        //doctor name
        if(!StringUtils.isEmpty(request.getDoctor_name())){
            sb.append(" AND (u.firstName like '%" + request.getDoctor_name().trim()
                    + "%' OR u.lastName like '%" + request.getDoctor_name().trim() + "%') ");
        }

        //fees
        if(!StringUtils.isEmpty(request.getFees())){
            String[] fees = request.getFees().split(",");
            sb.append(" AND u.userId IN (SELECT ch.userId FROM Charges ch WHERE ch.finalConsultationFees >= "+ Float.valueOf(fees[0]) + " AND ch.finalConsultationFees <= "+ Float.valueOf(fees[1])+") ");
        }
        //fee type -> call or visit
        if(!StringUtils.isEmpty(request.getFee_type())){
            sb.append( " AND u.userId IN (SELECT ch.userId FROM Charges ch WHERE ch.feeType IN ("+FeeType.valueOf(request.getFee_type())+")) ");
        }

        //city id
        if(!StringUtils.isEmpty(request.getCity_id())){
            sb.append(" AND u.city = "+ Integer.parseInt(request.getCity_id()));
        }

        //hospital id
        if(!StringUtils.isEmpty(request.getHospital_id())){
            sb.append(" AND u.hospitalId IN (:hId) ");
        }

        //language fluency
        if(!StringUtils.isEmpty(request.getLanguage_fluency())){
            sb.append(" AND FIND_IN_SET(" + request.getLanguage_fluency()+", u.languageFluency) > 0 ");
        }

        //sort by
        if(!StringUtils.isEmpty(request.getSort_by())){
            if(request.getSort_by().equals("1") || request.getSort_by().equals("01"))
                sb.append(" ORDER BY u.experience DESC ");
            else if(request.getSort_by().equals("2") || request.getSort_by().equals("02"))
                sb.append(" GROUP BY u.userId ORDER BY SUM(CASE WHEN cr.doctorId.userId = u.userId THEN cr.rating ELSE 0 END) DESC");
        }
        else{
            sb.append(" ORDER BY u.userId DESC");
        }

        Query query = entityManager.createQuery(sb.toString(), Users.class);

        if(!StringUtils.isEmpty(request.getSpecialization_id())){
            List<Integer> sp = Arrays.stream(request.getSpecialization_id().split(","))
                    .map(Integer::parseInt).toList();
            query.setParameter("sId", sp);
        }
        if(!StringUtils.isEmpty(request.getHospital_id())){
            query.setParameter("hId", Integer.parseInt(request.getHospital_id()));
        }
        if(!StringUtils.isEmpty(request.getAvailability())){
            query.setParameter("daySlots", daySlots);
        }
        if(!StringUtils.isEmpty(request.getIs_enterprise())){
            query.setParameter("enterpriseNumbers", enterpriseNumbers);
        }

        List<Users> users = query.getResultList();
        int total = users.size();

        users = users.stream().skip((StringUtils.isEmpty(request.getPage()) ? 0 : Integer.parseInt(request.getPage())) * 10L).limit(10).toList();

        int maxFee = (int)chargesRepository.findMaxConsultationFee().floatValue();
        List<SearchDocResponse> responseList = new ArrayList<>();
        for(Users u : users){
            responseList.add(getResponse(u, locale, startDate, endDate, type, total, maxFee));
        }
        String message = messageSource.getMessage(FOUND_COUNT_DOCTOR, null, locale);
        message = message.replaceFirst("X", String.valueOf(users.size()));
        return ResponseEntity.status(HttpStatus.OK).body(new Response(
                Constants.SUCCESS_CODE,
                Constants.SUCCESS_CODE,
                message, responseList
        ));
    }

    private SearchDocResponse getResponse(Users u, Locale locale, LocalDate startDate, LocalDate endDate, List<RequestType> types, int totalCount, int maxFee) {
        SearchDocResponse response = new SearchDocResponse();
        response.setProfile_picture(u.getProfilePicture() == null || u.getProfilePicture().isEmpty()
                ? "" : baseUrl + "uploaded_file/UserProfile/" + u.getUserId() +"/" + u.getProfilePicture());

        //language
        if(!StringUtils.isEmpty(u.getLanguageFluency())){
            List<Integer> langs = Arrays.stream(u.getLanguageFluency().split(",")).map(Integer::parseInt).toList();
            response.setLanguages(languageRepository.findLanguages(langs));
        }
        else response.setLanguages(new ArrayList<>());

        Long sum = consultationRatingRepository.findSumByDoctorId(u.getUserId());
        Long count = consultationRatingRepository.findCountByDoctorId(u.getUserId());
        Object finalCount = sum != null && count != null ? (int) (sum/count) : 0;
        Long review = consultationRatingRepository.findReview(u.getUserId());

        //charges
        Map<String, String> formattedCharges = new HashMap<>();
        List<Charges> charges = chargesRepository.findByUserId(u.getUserId());
        if(!charges.isEmpty()) {
            for (Charges charge : charges) {
                if (charge.getFinalConsultationFees() > 0) {
                    String formattedFee = currencySymbol + String.format("%.2f", charge.getFinalConsultationFees());
                    formattedCharges.put(charge.getFeeType().name(), formattedFee);
                }
            }
        }
        formattedCharges.putIfAbsent("visit", "free");
        formattedCharges.putIfAbsent("call", "free");
        response.setConsultation_fees(formattedCharges);

        //specialization
        String speciality = null;
        if(!StringUtils.isEmpty(u.getDoctorClassification())){
            if(u.getDoctorClassification().equalsIgnoreCase(General_Practitioner))
            speciality = messageSource.getMessage(General_Practitioner, null, locale);
        }
        else{
            List<String> specializationNames = doctorSpecializationRepository.findSpecializationsByUserId(u.getUserId(), locale.getLanguage().toLowerCase(), Status.A);
            speciality = specializationNames == null || specializationNames.isEmpty()
                    ? messageSource.getMessage(NOT_SET, null, locale) : String.join(",",specializationNames);
        }
        response.setSpeciality(speciality);

        //total cases based upon future consultation
        int cases = consultationRepository.findTotalCases(u.getUserId(), types);
        response.setCases(cases);

        response.setId(u.getUserId());
        response.setName(u.getFirstName() + " " + u.getLastName());
        response.setAbout_me(u.getAboutMe());
        response.setExperience(u.getExperience() == null ? "" : (int)u.getExperience().floatValue() + " " + messageSource.getMessage(YEAR_OF_EXPERIENCE, null, locale));
        response.setRating(finalCount);
        response.setMax_fees(maxFee);
        response.setReview(review);
        response.setTotal_count(totalCount);
        response.setHospital_id(u.getHospitalId());
        Users hospital = usersRepository.findById(u.getHospitalId()).orElse(null);
        response.setHospital_name(hospital != null ? hospital.getClinicName() : "");

        return response;
    }


    public ResponseEntity<?> doctorAvailabilityListLatest(Locale locale, DoctorAvailabilityRequest request) {
        log.info("Entering into doctor-availability-latest-list : {}", request);
        if(StringUtils.isEmpty(request.getUser_id()) || StringUtils.isEmpty(request.getDate())
            || StringUtils.isEmpty(request.getDoctor_id()) || StringUtils.isEmpty(request.getConsult_type())){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new Response(
                    NO_CONTENT_FOUNT_CODE,
                    NO_CONTENT_FOUNT_CODE,
                    messageSource.getMessage(BLANK_DATA_GIVEN, null, locale)
            ));
        }
        try {
            //new-order-date
            LocalDate newOrderDate = LocalDate.parse("2020-09-29");
            Users doctor = usersRepository.findById(Integer.parseInt(request.getDoctor_id())).orElseThrow(() -> new MobileServiceExceptionHandler(messageSource.getMessage(USER_NOT_FOUND, null, locale)));
            String dayName = LocalDate.parse(request.getDate()).getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.ENGLISH).toLowerCase();
            List<SlotMaster> slotListing = slotMasterRepository.findBySlotTypeIdAndSlotDay(doctor.getSlotTypeId(), dayName);


            List<Consultation> constantsList = consultationRepository.findByRequestTypeAndCreatedAtAndPatientIdAndDoctorIdAndConstaitionTypeAndConstationDate(
                    RequestType.Book, newOrderDate, Integer.parseInt(request.getUser_id()), Integer.parseInt(request.getDoctor_id()), ConsultationType.Paid, LocalDate.parse(request.getDate())
            );
            Consultation last_consult_data = (constantsList.isEmpty()) ? null : constantsList.get(0);

            ConsultationType consultation_type = ConsultationType.Paid;

            FeeType type = FeeType.visit;
            if (request.getConsult_type().equalsIgnoreCase("video")) {
                type = FeeType.call;
            }

            List<Charges> doctorchargesList = chargesRepository.findByUserIdAndConsultantType(Integer.parseInt(request.getDoctor_id()), type);
            Charges doctorcharges = (doctorchargesList.isEmpty()) ? null : doctorchargesList.get(0);

            String rem_cnt_msg = "";
            if (last_consult_data != null) {
                GlobalConfiguration free_cnt = globalConfigurationRepository.findByKey("NO_OF_FREE_BOOKING");
                GlobalConfiguration free_days = globalConfigurationRepository.findByKey("DAYS_FOR_FREE_BOOKING");

                LocalDate last_free_date = last_consult_data.getConsultationDate().plusDays(Integer.parseInt(free_cnt.getValue()));
                SlotMaster timeslot = last_consult_data.getSlotId();

                Long free_consult_cnt = consultationRepository.countByPatientIdAndDoctorIdCreatedAtAndConstaitionTypeConsultTypeAndConstationDate(
                        Integer.parseInt(request.getUser_id()), Integer.parseInt(request.getDoctor_id()), newOrderDate,
                        ConsultationType.Free, last_consult_data.getConsultType(), last_consult_data.getConsultationDate()
                );
                long rem_cnt = Long.parseLong(free_cnt.getValue()) - free_consult_cnt;
                if (rem_cnt > 0) {
                    rem_cnt_msg = "(You have " + rem_cnt + " Free booking(s) for " + last_consult_data.getConsultType() + " till " + last_free_date + " )";
                }
            }

            Map<String, List<SlotResponse>> slotArray = new LinkedHashMap<>();
            slotArray.put("Morning", new ArrayList<>());
            slotArray.put("Afternoon", new ArrayList<>());
            slotArray.put("Evening", new ArrayList<>());

            for (SlotMaster slot : slotListing) {
                String[] timeArray = slot.getSlotTime().split(":");
                LocalTime time = LocalTime.parse(timeArray[0] + ":" + timeArray[1] + ":00");
                if(LocalDate.parse(request.getDate()).equals(LocalDate.now(ZoneId.of(zone)))
                        && time.isBefore(LocalTime.now(ZoneId.of(zone)))) {
                    continue;
                }

                Long available_count = doctorAvailabilityRepository.countBySlotIdAndDoctorId(slot.getSlotId(), Integer.parseInt(request.getDoctor_id()));

                Long check_consultant_count = consultationRepository.countBySlotIdAndDoctorIdConsultationDate(
                        slot.getSlotId(), Integer.parseInt(request.getDoctor_id()), LocalDate.parse(request.getDate()));

                List<Consultation> consultantInfoList = consultationRepository.findByDoctorIdAndSlotIdAndRequestTypeAndDate(
                        Integer.parseInt(request.getDoctor_id()), slot.getSlotId(), LocalDate.parse(request.getDate()), RequestType.Cancel);

                Consultation consultantInfo = (consultantInfoList.isEmpty()) ? null : constantsList.get(0);

                String userIdInSlot = consultantInfo != null ? consultantInfo.getPatientId().toString() : "";
                Object caseId = consultantInfo != null ? consultantInfo.getCaseId() : "";

                LocalDateTime consultantDateTime = LocalDateTime.of(LocalDate.parse(request.getDate()), time);

                LocalDateTime currentDateTime = LocalDateTime.now(ZoneId.of(zone));

                long diff = Math.abs(java.time.Duration.between(consultantDateTime, currentDateTime).toMinutes());

                int timeLimit = Integer.parseInt(globalConfigurationRepository.findByKey("CANCEL_CONSULT_PATIENT").getValue());

                Integer isCancel = (timeLimit <= diff) && (consultantInfo != null && consultantInfo.getRequestType() == RequestType.Book && consultantDateTime.isAfter(currentDateTime)) ? 1 : 0;

                if (check_consultant_count > 0 || currentDateTime.isAfter(consultantDateTime)) {
                    available_count = 0L;
                }

                String status;
                if (available_count > 0) {
                    status = "Available";
                } else {
                    status = "Not Available";
                }

                SlotResponse finalArray = new SlotResponse(
                        slot.getSlotId(),
                        slot.getSlotDay(),
                        slot.getSlotTime(),
                        userIdInSlot,
                        caseId,
                        status,
                        isCancel,
                        consultantInfo != null ? consultantInfo.getRequestType().name() : "",
                        consultation_type.name(),
                        last_consult_data != null ? last_consult_data.getConsultType() : "",
                        time.format(DateTimeFormatter.ofPattern("HH:mm a")).toUpperCase()
                );

                String slotTime = getSlotTime(time);

                if (status.equals("Available") && slotTime != null) {
                    slotArray.get(slotTime).add(finalArray);
                }
            }

            // Sorting slots and preparing the response

            int morningSlotCount = slotArray.get("Morning").size();
            int afternoonSlotCount = slotArray.get("Afternoon").size();
            int eveningSlotCount = slotArray.get("Evening").size();

            double paymentRate = Double.parseDouble(globalConfigurationRepository.findByKey("WAAFI_PAYMENT_RATE").getValue());

            double finalConsultFee = doctorcharges != null ? doctorcharges.getFinalConsultationFees() : 0.0;
            String doctorFinalConsultFee = finalConsultFee > 0 ? "USD " + finalConsultFee : "Free";
            String doctorFinalConsultSlsh = finalConsultFee > 0 ? "SLSH " + (finalConsultFee * paymentRate) : "Free";

            Map<String, Object> response = new HashMap<>();
            response.put("status", SUCCESS_CODE);
            response.put("message", messageSource.getMessage(Constants.AVAILABILITY_FOUND, null, locale));
            response.put("totalSlot", morningSlotCount + afternoonSlotCount + eveningSlotCount);
            response.put("final_consultation_fees", doctorFinalConsultFee);
            response.put("amount_slsh", doctorFinalConsultSlsh);
            response.put("price_usd", finalConsultFee);
            response.put("price_slsh", finalConsultFee * paymentRate);
            response.put("morningSlotCount", morningSlotCount);
            response.put("afternoonSlotCount", afternoonSlotCount);
            response.put("eveningSlotCount", eveningSlotCount);
            response.put("rem_cnt_msg", rem_cnt_msg);
            response.put("data", slotArray);


            return ResponseEntity.status(HttpStatus.OK).body(
                    response
            );
        } catch (Exception e) {
            e.printStackTrace();
            log.error("Error found in doctor -availability latest list api : {}", e);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new Response(
                    NO_CONTENT_FOUNT_CODE,
                    NO_CONTENT_FOUNT_CODE,
                    messageSource.getMessage(SOMETHING_WENT_WRONG, null, locale),
                    null
            ));
        }
    }


    private String getSlotTime(LocalTime fromTime) {
        LocalTime morningStart = LocalTime.parse("06:00");
        LocalTime morningEnd = LocalTime.parse("11:59");
        LocalTime afternoonStart = LocalTime.parse("12:00");
        LocalTime afternoonEnd = LocalTime.parse("17:59");
        LocalTime eveningStart = LocalTime.parse("18:00");

        if (fromTime.isAfter(morningStart.minusSeconds(1)) && fromTime.isBefore(morningEnd.plusSeconds(1))) {
            return "Morning";
        } else if (fromTime.isAfter(afternoonStart.minusSeconds(1)) && fromTime.isBefore(afternoonEnd.plusSeconds(1))) {
            return "Afternoon";
        } else if (fromTime.isAfter(eveningStart.minusSeconds(1))) {
            return "Evening";
        }
        return null;
    }

    public ResponseEntity<?> viewProfile(Locale locale,Integer doctorId) {
        Users doctor = usersRepository.findById(doctorId).orElseThrow(()-> new MobileServiceExceptionHandler(messageSource.getMessage(USER_NOT_FOUND, null, locale)));
        String photo = "";
        if(!StringUtils.isEmpty(doctor.getProfilePicture())){
            photo = baseUrl + "uploaded_file/UserProfile/" + doctor.getUserId() + "/" + doctor.getProfilePicture();
        }

        List<DoctorSpecialization> specialization = doctorSpecializationRepository.findByUserId(doctor.getUserId());
        List<Charges> charges = chargesRepository.findByUserId(doctor.getUserId());
        Double raiting = consultationRatingRepository.sumRatingsByDoctorId(doctor.getUserId());
        Long raitingCount = consultationRatingRepository.countApprovedRatingsByDoctorId(doctor.getUserId());
        Double finalCount = (raitingCount!=null && raitingCount!=0)?raiting/raitingCount:0;
        String specsFinalString = "";
        if(!StringUtils.isEmpty(doctor.getDoctorClassification()) &&
                doctor.getDoctorClassification().equalsIgnoreCase("general_practitioner")){
            specsFinalString = messageSource.getMessage(Constants.GENRAL_PRACTITIONER,null,locale);
        }
        else{
            if(!specialization.isEmpty()) {
                for (DoctorSpecialization docSpec : specialization) {
                    specsFinalString = specsFinalString + docSpec.getSpecializationId().getName() + ",";
                }
            }
        }
        String[] languageIds  = StringUtils.isEmpty(doctor.getLanguageFluency()) ? null : doctor.getLanguageFluency().split(",");
        String languageName = "";
        if(languageIds != null && languageIds.length>0) {
            for (String i : languageIds) {
                Language language = languageRepository.findById(Integer.valueOf(i)).orElse(null);
                if (language != null) languageName += language.getName() + ",";
            }
            languageName = languageName.substring(0, languageName.length()-1);
        }

        List<ConsultationRating> consultationRatings = consultationRatingRepository.getByDoctorIdActive(doctorId);
        Long totalConsultCount = consultationRatingRepository.countByDoctorIdAll(doctorId);
        List<CommentsDto> commentsDtos = new ArrayList<>();

        if(!consultationRatings.isEmpty()){
            for(ConsultationRating r:consultationRatings){
                CommentsDto dto = getCommentsDto(r, totalConsultCount);

                commentsDtos.add(dto);
            }
        }

        Map<String, Float> chargesMap = new HashMap<>();
        ViewProfileResponse dto = new ViewProfileResponse();
        if (!charges.isEmpty()) {
            for (Charges charge : charges) {
                if(charge.getFeeType() != null && charge.getFeeType().name().equalsIgnoreCase("call")){
                    dto.setCall_commission(charge.getCommission() == null ? null : (int)charge.getCommission().floatValue());
                    dto.setCall_consultation_fees((int)charge.getConsultationFees().floatValue());
                    dto.setCall_final_consultation_fees(charge.getFinalConsultationFees() == null ? null : (int)charge.getFinalConsultationFees().floatValue());
                }else if(charge.getFeeType() != null && charge.getFeeType().name().equalsIgnoreCase("visit")){
                    dto.setVisit_commission(charge.getCommission() == null ? null : (int)charge.getCommission().floatValue());
                    dto.setVisit_consultation_fees((int)charge.getConsultationFees().floatValue());
                    dto.setVisit_final_consultation_fees(charge.getFinalConsultationFees() == null ? null : (int)charge.getFinalConsultationFees().floatValue());
                }
            }
        }

        dto.setFirst_name(doctor.getFirstName());
        dto.setLast_name(doctor.getLastName());
        dto.setEmail(doctor.getEmail());
        dto.setContact_number(doctor.getContactNumber());
        dto.setPhoto(photo);
        State state = null;
        if(doctor.getState()!=null && doctor.getState()!=0){
            state = stateRepository.findById(doctor.getState()).orElseThrow(() -> new MobileServiceExceptionHandler(messageSource.getMessage(NO_STATE_FOUND, null, locale)));
        }
        City city = null;
        if(doctor.getCity()!=null && doctor.getCity()!=0){
            city = cityRepository.findById(doctor.getCity()).orElseThrow(() -> new MobileServiceExceptionHandler(messageSource.getMessage(NO_CITY_FOUND, null, locale)));
        }
        dto.setCountry(
                (doctor.getCountry()!=null)
                        ?doctor.getCountry().getName():"");
        dto.setState(
                (state!=null)
                        ?state.getName():"");
        dto.setCity(
                (city!=null)
                        ?city.getName():"");
        dto.setHospital_address(doctor.getHospitalAddress());
        dto.setResidence_address(doctor.getResidenceAddress());
        dto.setProfessional_identification_number(doctor.getProfessionalIdentificationNumber());
        dto.setRating(finalCount);
        dto.setExtra_activities(doctor.getExtraActivities());
        dto.setAbout_me(doctor.getAboutMe());
        dto.setLanguage(languageName);
        dto.setReview(commentsDtos);
        dto.setGender(doctor.getGender());
        dto.setExperience(doctor.getExperience());
        dto.setSpcialization(specsFinalString);

        return ResponseEntity.status(HttpStatus.OK).body(new Response(
                Constants.SUCCESS_CODE,
                Constants.SUCCESS_CODE,
                messageSource.getMessage(Constants.PROFILE_FETCH_SUCCESSFULLY,null,locale),
                dto
        ));
    }

    private CommentsDto getCommentsDto(ConsultationRating r, Long totalConsultCount) {
        String fileUrl = baseUrl + "/uploaded_file/no-image-found.png";
        if(r.getPatientId().getProfilePicture()!=null && !r.getPatientId().getProfilePicture().isEmpty()){
            fileUrl = baseUrl + "/uploaded_file/UserProfile/"+ r.getPatientId().getUserId()+"/"+ r.getPatientId().getProfilePicture();
        }
//                DateTimeFormatter spaceFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
//                String dateTimeFromSpace = spaceFormatter.format();
        CommentsDto dto = new CommentsDto();
        dto.setComment(r.getComment());
        dto.setName(r.getPatientId() == null ? "" : r.getPatientId().getFullName());
        dto.setRating(r.getRating() == null ? 0 : (int) r.getRating().floatValue());
        dto.setCreated_at(r.getCreatedAt().toString().replace("T", ""));
        dto.setFile_url(fileUrl);
        dto.setTotal_count(totalConsultCount);
        return dto;
    }

    public ResponseEntity<?> getReview(Locale locale, GetReviewRequest request) {
        log.info("Entering into getReview api : {} ", request);
        try {
            if (StringUtils.isEmpty(request.getUser_id()) || StringUtils.isEmpty(request.getDoctor_id())) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new Response(
                        NO_CONTENT_FOUNT_CODE,
                        NO_CONTENT_FOUNT_CODE,
                        messageSource.getMessage(BLANK_DATA_GIVEN, null, locale)
                ));
            }
            int page = StringUtils.isEmpty(request.getPage()) ? 0 : Integer.parseInt(request.getPage());
            int doctorId = Integer.parseInt(request.getDoctor_id());
            Pageable pageable = PageRequest.of(page, 10);
            Page<ConsultationRating> consultationRatings = consultationRatingRepository.findByDoctorIdApproveOrderIdDesc(doctorId, pageable);
            if (!consultationRatings.getContent().isEmpty()) {
                List<GetReviewResponse> responses = new ArrayList<>();
                for (ConsultationRating rating : consultationRatings.getContent()) {
                    GetReviewResponse dto = getGetReviewResponse(rating, consultationRatings);

                    responses.add(dto);
                }

                return ResponseEntity.status(HttpStatus.OK).body(new Response(
                        Constants.SUCCESS_CODE,
                        Constants.SUCCESS_CODE,
                        messageSource.getMessage(Constants.REVIEW_FOUND_SUCCESSFULLY, null, locale),
                        responses
                ));
            } else {
                return ResponseEntity.status(HttpStatus.OK).body(new Response(
                        Constants.SUCCESS_CODE,
                        Constants.SUCCESS_CODE,
                        messageSource.getMessage(Constants.NO_RECORD_FOUND, null, locale)
                ));
            }
        } catch (Exception e) {
            e.printStackTrace();
            log.error("Error found in review api : {} ",e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new Response(
                    NO_CONTENT_FOUNT_CODE,
                    NO_CONTENT_FOUNT_CODE,
                    messageSource.getMessage(SOMETHING_WENT_WRONG, null, locale)
            ));
        }
    }

    private GetReviewResponse getGetReviewResponse(ConsultationRating rating, Page<ConsultationRating> consultationRatings) {
        String file = baseUrl + "/uploaded_file/no-image-found.png";
        if (rating.getPatientId() != null &&
                rating.getPatientId().getProfilePicture() != null &&
                !rating.getPatientId().getProfilePicture().isEmpty()) {
            file = baseUrl + "/uploaded_file/UserProfile/" + rating.getPatientId().getUserId() + "/" + rating.getPatientId().getProfilePicture();
        }
        GetReviewResponse dto = new GetReviewResponse();

        dto.setComment(rating.getComment());
        dto.setName((rating.getPatientId() != null) ? rating.getPatientId().getFirstName() + " " + rating.getPatientId().getLastName() : null);
        dto.setRating(rating.getRating());
        dto.setCreated_at(rating.getCreatedAt());
        dto.setFile_url(file);
        dto.setTotal_count(consultationRatings.getTotalElements());
        return dto;
    }
}
