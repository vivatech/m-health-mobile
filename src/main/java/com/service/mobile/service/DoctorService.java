package com.service.mobile.service;

import com.service.mobile.config.Constants;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.*;

import static com.service.mobile.config.Constants.*;
import static com.service.mobile.constants.Constants.General_Practitioner;

@Service
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
        List<City> cities = usersRepository.getCitiesByUsertype(UserType.Doctor);
        if(!cities.isEmpty()){
            List<CityResponse> responses = new ArrayList<>();
            for(City c:cities){
                responses.add(new CityResponse(c.getId(),c.getName(),null));
            }
            return ResponseEntity.status(HttpStatus.OK).body(new Response(
                    Constants.SUCCESS_CODE,
                    Constants.SUCCESS_CODE,
                    messageSource.getMessage(Constants.CITY_FOUND_SUCCESSFULLY,null,locale),
                    responses
            ));
        }else{
            return ResponseEntity.status(HttpStatus.NO_CONTENT).body(new Response(
                    Constants.NO_RECORD_FOUND_CODE,
                    Constants.BLANK_DATA_GIVEN_CODE,
                    messageSource.getMessage(Constants.NO_CITY_FOUND,null,locale)
            ));
        }
    }

    public ResponseEntity<?> searchDoctor(Locale locale, SearchDoctorRequest request) {
        if(request.getUser_id() == null || request.getPage() == null){
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
        if(request.getAvailability() != null && request.getAvailability() != 0){
            if(request.getAvailability() == 1){
                DayOfWeek dayOfWeek = dateTime.toLocalDate().getDayOfWeek();
                dayName = new String[]{dayOfWeek.toString().toLowerCase()};
                daySlots = slotMasterRepository.findBySlotDayAndSlotStartTime(dayName, time, startDate, type);
            }
            else if(request.getAvailability() == 2){
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
        if(request.getClinic_id() != null && !request.getClinic_id().isEmpty()){
            sb.append(" AND u.hospitalId = "+request.getClinic_id());
        }
        //consult type
        if(request.getConsult_type() != null && !request.getConsult_type().isEmpty()){
            if(request.getConsult_type().equalsIgnoreCase("video")){
                sb.append(" AND u.hasDoctorVideo IN ('video','both')");
            }
            else if(request.getConsult_type().equalsIgnoreCase("visit")){
                sb.append(" AND u.hasDoctorVideo IN ('visit','both')");
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
        if(request.getIs_international() != null && !request.getIs_international().isEmpty()){
            if(request.getIs_international().equalsIgnoreCase("Yes")){
                sb.append(" AND u.isInternational = 'Yes'");
                sb.append(" AND u.hasDoctorVideo IN ('video','both')");
            }else sb.append(" AND u.isInternational = 'No' ");
        }
        else sb.append(" AND u.isInternational = 'No' ");

        //specialization
        if(request.getSpecialization_id() != null && !request.getSpecialization_id().isEmpty()){
            //getting userIds from specialization
            sb.append(" AND u.userId IN (SELECT ds.userId.userId FROM DoctorSpecialization ds WHERE ds.userId.doctorClassification != 'general_practitioner' AND ds.specializationId.id IN (:sId)) ");
        }

        //doctor name
        if(request.getDoctor_name() != null && !request.getDoctor_name().isEmpty()){
            sb.append(" AND (u.firstName like '%" + request.getDoctor_name().trim()
                    + "%' OR u.lastName like '%" + request.getDoctor_name().trim() + "%') ");
        }

        //fees
        if(request.getFees() != null && !request.getFees().isEmpty()){
            String[] fees = request.getFees().split(",");
            sb.append(" AND u.userId IN (SELECT ch.userId FROM Charges ch WHERE ch.finalConsultationFees >= "+ Float.valueOf(fees[0]) + " AND ch.finalConsultationFees <= "+ Float.valueOf(fees[1])+") ");
        }
        //fee type -> call or visit
        if(request.getFee_type() != null && !request.getFee_type().isEmpty()){
            sb.append( " AND u.userId IN (SELECT ch.userId FROM Charges ch WHERE ch.feeType IN ("+FeeType.valueOf(request.getFee_type())+")) ");
        }

        //city id
        if(request.getCity_id() != null){
            sb.append(" AND u.city = "+request.getCity_id());
        }

        //hospital id
        if(request.getHospital_id() != null && !request.getHospital_id().isEmpty()){
            sb.append(" AND u.hospitalId IN (:hId) ");
        }

        //language fluency
        if(request.getLanguage_fluency() != null){
            sb.append(" AND FIND_IN_SET(" + request.getLanguage_fluency()+", u.languageFluency) > 0 ");
        }

        //sort by
        if(request.getSort_by() != null){
            if(request.getSort_by() == 01)
                sb.append(" ORDER BY u.experience DESC ");
            else if(request.getSort_by() == 02)
                sb.append(" GROUP BY u.userId ORDER BY SUM(CASE WHEN cr.doctorId.userId = u.userId THEN cr.rating ELSE 0 END) DESC");
        }
        else{
            sb.append(" ORDER BY u.userId DESC");
        }

        Query query = entityManager.createQuery(sb.toString(), Users.class);

        if(request.getSpecialization_id() != null && !request.getSpecialization_id().isEmpty()){
            query.setParameter("sId", request.getSpecialization_id());
        }
        if(request.getHospital_id() != null && !request.getHospital_id().isEmpty()){
            query.setParameter("hId", request.getHospital_id());
        }
        if(request.getAvailability() != null){
            query.setParameter("daySlots", daySlots);
        }
        if(request.getIs_enterprise() != null && !request.getIs_enterprise().isEmpty()){
            query.setParameter("enterpriseNumbers", enterpriseNumbers);
        }

        List<Users> users = query.getResultList();
        int total = users.size();
        query.setFirstResult(request.getPage() * 10);
        query.setMaxResults(10);
        users = query.getResultList();

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
        if(u.getLanguageFluency() != null && !u.getLanguageFluency().isEmpty()){
            List<Integer> langs = Arrays.stream(u.getLanguageFluency().split(",")).map(Integer::parseInt).toList();
            response.setLanguages(languageRepository.findLanguages(langs));
        }

        Long sum = consultationRatingRepository.findSumByDoctorId(u.getUserId());
        Long count = consultationRatingRepository.findCountByDoctorId(u.getUserId());
        Object finalCount = sum != null && count != null ? (int) (sum/count) : 0;
        Long review = consultationRatingRepository.findReview(u.getUserId());

        //charges
        Map<String, String> formattedCharges = new HashMap<>();
        List<Charges> charges = chargesRepository.findByUserId(u.getUserId());
        for (Charges charge : charges) {
            if (charge.getFinalConsultationFees() > 0) {
                String formattedFee = currencySymbol + String.format("%.2f", charge.getFinalConsultationFees());
                formattedCharges.put(charge.getFeeType().name(), formattedFee);
            }
        }
        formattedCharges.putIfAbsent("visit", "free");
        formattedCharges.putIfAbsent("call", "free");
        response.setConsultation_fees(formattedCharges);

        //specialization
        String speciality = null;
        if(u.getDoctorClassification().equalsIgnoreCase(General_Practitioner)){
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
        response.setExperience((int)u.getExperience().floatValue() + " " + messageSource.getMessage(YEAR_OF_EXPERIENCE, null, locale));
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
        //new-order-date
        LocalDate newOrderDate = LocalDate.parse("2020-09-29");
        Users slot_type_id = usersRepository.findById(Integer.parseInt(request.getDoctor_id())).orElse(null);
        String dayName = request.getDate().getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.ENGLISH).toLowerCase();
        List<SlotMaster> slotListing = slotMasterRepository.findBySlotTypeIdAndSlotDay(slot_type_id.getSlotTypeId(),dayName);


        List<Consultation> constantsList = consultationRepository.findByRequestTypeAndCreatedAtAndPatientIdAndDoctorIdAndConstaitionTypeAndConstationDate(
                RequestType.Book,newOrderDate, Integer.parseInt(request.getUser_id()), Integer.parseInt(request.getDoctor_id()), ConsultationType.Paid,request.getDate()
        );
        Consultation last_consult_data = (constantsList.isEmpty())?null:constantsList.get(0);

        ConsultationType consultation_type = ConsultationType.Paid;

        FeeType type = FeeType.visit;
        if(request.getConsult_type().equalsIgnoreCase("video")){
            type = FeeType.call;
        }

        List<Charges> doctorchargesList = chargesRepository.findByUserIdAndConsultantType(Integer.parseInt(request.getDoctor_id()),type);
        Charges doctorcharges = (doctorchargesList.isEmpty())?null:doctorchargesList.get(0);

        String rem_cnt_msg = "";
        if(last_consult_data!=null){
            GlobalConfiguration free_cnt = globalConfigurationRepository.findByKey("NO_OF_FREE_BOOKING");
            GlobalConfiguration free_days = globalConfigurationRepository.findByKey("DAYS_FOR_FREE_BOOKING");

            LocalDate last_free_date = last_consult_data.getConsultationDate().plusDays(Integer.parseInt(free_cnt.getValue()));
            SlotMaster timeslot = last_consult_data.getSlotId();
            LocalTime time_array = LocalTime.parse(timeslot.getSlotTime());
            LocalDateTime sconsultant_date = last_consult_data.getConsultationDate().atTime(time_array);

            Long free_consult_cnt = consultationRepository.countByPatientIdAndDoctorIdCreatedAtAndConstaitionTypeConsultTypeAndConstationDate(
                    Integer.parseInt(request.getUser_id()), Integer.parseInt(request.getDoctor_id()),newOrderDate,
                    ConsultationType.Free,last_consult_data.getConsultType(),sconsultant_date
                    );
            Long rem_cnt = Long.valueOf(free_cnt.getValue()) - free_consult_cnt;
            if(rem_cnt>0){
                rem_cnt_msg = "(You have "+rem_cnt+" Free booking(s) for "+last_consult_data.getConsultType()+" till "+last_free_date+" )";
            }
        }

        Map<String, List<SlotResponse>> slotArray = new LinkedHashMap<>();
        slotArray.put("Morning", new ArrayList<>());
        slotArray.put("Afternoon", new ArrayList<>());
        slotArray.put("Evening", new ArrayList<>());


        for (SlotMaster slot : slotListing) {
            Long available_count = doctorAvailabilityRepository.countBySlotIdAndDoctorId(slot.getSlotId(), Integer.parseInt(request.getDoctor_id()));

            Long check_consultant_count = consultationRepository.countBySlotIdAndDoctorIdConsultationDate(
                    slot.getSlotId(),Integer.parseInt(request.getDoctor_id()),  request.getDate());

            List<Consultation> consultantInfoList = consultationRepository.findByDoctorIdAndSlotIdAndRequestTypeAndDate(
                    Integer.parseInt(request.getDoctor_id()), slot.getSlotId(),request.getDate(), RequestType.Cancel);

            Consultation consultantInfo = (consultantInfoList.isEmpty())?null:constantsList.get(0);

            String userIdInSlot = consultantInfo != null ? consultantInfo.getPatientId().toString() : "";
            Integer caseId = consultantInfo != null ? consultantInfo.getCaseId() : null;

            LocalDateTime consultantDateTime = LocalDateTime.of(request.getDate(), slot.getSlotStartTime());

            LocalDateTime currentDateTime = LocalDateTime.now();

            Long diff = Math.abs(java.time.Duration.between(consultantDateTime, currentDateTime).toMinutes());

            Integer timeLimit = Integer.parseInt(globalConfigurationRepository.findByKey("CANCEL_CONSULT_PATIENT").getValue());

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
                    slot.getSlotStartTime().format(DateTimeFormatter.ofPattern("hh:mm a"))
            );

            String slotTime = getSlotTime(slot.getSlotStartTime());

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
        response.put("status", "200");
        response.put("message", "Availability found successfully");
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
        Users doctor = usersRepository.findById(doctorId).orElse(null);
        if(doctor!=null){
            String photo = "";
            if(doctor.getProfilePicture()!=null && !doctor.getProfilePicture().isEmpty()){
                photo = baseUrl + "uploaded_file/UserProfile/" + doctor.getUserId() + "/" + doctor.getProfilePicture();
            }
            List<DoctorSpecialization> specialization = doctorSpecializationRepository.findByUserId(doctor.getUserId());
            List<Charges> charges = chargesRepository.findByUserId(doctor.getUserId());
            Double raiting = consultationRatingRepository.sumRatingsByDoctorId(doctor.getUserId());
            Long raitingCount = consultationRatingRepository.countApprovedRatingsByDoctorId(doctor.getUserId());
            Double finalCount = (raitingCount!=null && raitingCount!=0)?raiting/raitingCount:0;
            String specsFinalString = "";
            if(doctor.getDoctorClassification()!=null &&
                    doctor.getDoctorClassification().equalsIgnoreCase("general_practitioner")){
                    specsFinalString = messageSource.getMessage(Constants.GENRAL_PRACTITIONER,null,locale);
            }else{
                for(DoctorSpecialization docSpec :specialization){
                    specsFinalString = specsFinalString + docSpec.getSpecializationId().getName()+",";
                }
            }
            String[] languageIds  = doctor.getLanguageFluency().split(",");
            String languageName = "";
            if(languageIds.length>0) {
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
                    String fileUrl = baseUrl + "/uploaded_file/no-image-found.png";
                    if(r.getPatientId().getProfilePicture()!=null && !r.getPatientId().getProfilePicture().isEmpty()){
                        fileUrl = baseUrl + "/uploaded_file/UserProfile/"+r.getPatientId().getUserId()+"/"+r.getPatientId().getProfilePicture();
                    }
                    DateTimeFormatter spaceFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                    String dateTimeFromSpace = spaceFormatter.format(r.getCreatedAt());
                    CommentsDto dto = new CommentsDto();
                    dto.setComment(r.getComment());
                    dto.setName(r.getPatientId().getFirstName()+" "+r.getPatientId().getLastName());
                    dto.setRating((int)r.getRating().floatValue());
                    dto.setCreated_at(dateTimeFromSpace);
                    dto.setFile_url(fileUrl);
                    dto.setTotal_count(totalConsultCount);

                    commentsDtos.add(dto);
                }
            }

            Map<String, Float> chargesMap = new HashMap<>();
            ViewProfileResponse dto = new ViewProfileResponse();
            if (charges != null && !charges.isEmpty()) {
                for (Charges charge : charges) {
                    if(charge.getFeeType().name().equalsIgnoreCase("call")){
                        dto.setCall_commission((int)charge.getCommission().floatValue());
                        dto.setCall_consultation_fees((int)charge.getConsultationFees().floatValue());
                        dto.setCall_final_consultation_fees((int)charge.getFinalConsultationFees().floatValue());
                    }else if(charge.getFeeType().name().equalsIgnoreCase("visit")){
                        dto.setVisit_commission((int)charge.getCommission().floatValue());
                        dto.setVisit_consultation_fees((int)charge.getConsultationFees().floatValue());
                        dto.setVisit_final_consultation_fees((int)charge.getFinalConsultationFees().floatValue());
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
                state = stateRepository.findById(doctor.getState()).orElse(null);
            }
            City city = null;
            if(doctor.getCity()!=null && doctor.getCity()!=0){
                city = cityRepository.findById(doctor.getCity()).orElse(null);
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
        }else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new Response(
                    Constants.NO_RECORD_FOUND_CODE,
                    Constants.NO_RECORD_FOUND_CODE,
                    messageSource.getMessage(Constants.NO_RECORD_FOUND,null,locale)
            ));
        }
    }

    public ResponseEntity<?> getReview(Locale locale, GetReviewRequest request) {
        Pageable pageable = PageRequest.of(request.getPage(),10);
        Page<ConsultationRating> consultationRatings = consultationRatingRepository.findByDoctorIdApproveOrderIdDesc(request.getDoctor_id(),pageable);
        if(!consultationRatings.getContent().isEmpty()){
            List<GetReviewResponse> responses = new ArrayList<>();
            for(ConsultationRating rating:consultationRatings.getContent()){
                String file = baseUrl+"/uploaded_file/no-image-found.png";
                if(rating.getPatientId()!=null &&
                        rating.getPatientId().getProfilePicture()!=null &&
                            !rating.getPatientId().getProfilePicture().isEmpty()){
                    file = baseUrl + "/uploaded_file/UserProfile/"+ rating.getPatientId().getUserId() + "/" + rating.getPatientId().getProfilePicture();
                }
                GetReviewResponse dto = new GetReviewResponse();

                dto.setComment(rating.getComment());
                dto.setName((rating.getPatientId()!=null)?rating.getPatientId().getFirstName() + " "+rating.getPatientId().getLastName():null);
                dto.setRating(rating.getRating());
                dto.setCreated_at(rating.getCreatedAt());
                dto.setFile_url(file);
                dto.setTotal_count(consultationRatings.getTotalElements());

                responses.add(dto);

            }
            return ResponseEntity.status(HttpStatus.OK).body(new Response(
                    Constants.SUCCESS_CODE,
                    Constants.SUCCESS_CODE,
                    messageSource.getMessage(Constants.REVIEW_FOUND_SUCCESSFULLY,null,locale),
                    responses
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
