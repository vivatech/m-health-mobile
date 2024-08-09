package com.service.mobile.service;

import com.service.mobile.config.Constants;
import com.service.mobile.controllers.FileController;
import com.service.mobile.dto.dto.CommentsDto;
import com.service.mobile.dto.dto.ConsultationFees;
import com.service.mobile.dto.dto.SearchDocResponse;
import com.service.mobile.dto.dto.TransformDto;
import com.service.mobile.dto.enums.ConsultationType;
import com.service.mobile.dto.enums.FeeType;
import com.service.mobile.dto.enums.RequestType;
import com.service.mobile.dto.enums.UserType;
import com.service.mobile.dto.request.DoctorAvailabilityListLatestRequest;
import com.service.mobile.dto.request.GetReviewRequest;
import com.service.mobile.dto.request.SearchDoctorRequest;
import com.service.mobile.dto.response.*;
import com.service.mobile.model.*;
import com.service.mobile.repository.*;
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
import org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

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


    public ResponseEntity<?> getDoctorCityList(Locale locale) {
        List<City> cities = usersRepository.getCitiesByUsertype(UserType.DOCTOR);
        if(cities.size()>0){
            List<CityResponse> responses = new ArrayList<>();
            for(City c:cities){
                responses.add(new CityResponse(c.getId(),c.getName(),null));
            }
            return ResponseEntity.status(HttpStatus.NO_CONTENT).body(new Response(
                    Constants.NO_RECORD_FOUND_CODE,
                    Constants.BLANK_DATA_GIVEN_CODE,
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
        Response responseDto = new Response();
        String message = null;
        String statusCode = Constants.FAIL;
        Object data = new ArrayList<>();
        String status = Constants.FAIL;

        if (request.getPage() != null && request.getPage() >= 0) {
            request.setPageSize(10);
            List<Users> listOfDoctor = usersRepository.findByType(UserType.Doctor);

            if (request.getClinic_id() != null && request.getClinic_id() > 0) {
                listOfDoctor = usersRepository.findByHospitalId(request.getCity_id());
            }

            if (request.getCity_id() > 0 ) {
                listOfDoctor = listOfDoctor.stream().filter(item -> item.getCity().getId() != null && item.getCity().getId().equals(request.getCity_id())).toList();
            }

            if (request.getConsult_type().equalsIgnoreCase(Constants.VIDEO)) {
                listOfDoctor = listOfDoctor.stream().filter(item -> item.getHasDoctorVideo() != null && item.getHasDoctorVideo().equalsIgnoreCase(Constants.CONSULT_BOTH)).toList();
            } else if (request.getConsult_type().equalsIgnoreCase(Constants.CONSULT_VISIT)) {
                listOfDoctor = listOfDoctor.stream().filter(item -> item.getHasDoctorVideo() != null && item.getHasDoctorVideo().equalsIgnoreCase(Constants.CONSULT_VISIT)).toList();
            }
            List<SearchDocResponse> responses = new ArrayList<>();

            for (Users val : listOfDoctor) {
                SearchDocResponse docResponse = new SearchDocResponse();

                docResponse.setId(val.getUserId());
                docResponse.setName(val.getFirstName() + " " + val.getLastName());

                int totalCases = consultationRepository.findTotalCases(val.getUserId());
                docResponse.setCases(totalCases);

                ConsultationFees fees = new ConsultationFees();
                List<Charges> chargesList = chargesRepository.findByUserId(val.getUserId());
                Float visitCharge = 0.0f;
                Float callCharge = 0.0f;
                if (chargesList.isEmpty()) docResponse.setConsultation_fees(new ConsultationFees());
                else {
                    for (Charges ch : chargesList) {
                        if (ch.getFeeType().equals(FeeType.VISIT)) visitCharge = ch.getConsultationFees();
                        else callCharge = ch.getConsultationFees();
                    }
                }
                fees.setVisit(visitCharge);
                fees.setCall(callCharge);
                docResponse.setConsultation_fees(fees);

                docResponse.setAbout_me(val.getAboutMe());
                docResponse.setExperience(val.getExperience() + Constants.EXPERIENCE);
                String imagePath = MvcUriComponentsBuilder
                        .fromMethodName(FileController.class, "serveFiles", val.getProfilePicture()).build().toUri().toString();
                docResponse.setProfile_picture(imagePath);

                List<ConsultationRating> consultationRating = consultationRatingRepository.findByDoctorId(val);
                if (!consultationRating.isEmpty()) {
                    float rating = consultationRatingRepository.findDoctorRating(val.getUserId());
                    docResponse.setRating(rating);
                    int reviews = consultationRatingRepository.findReviews(val.getUserId());
                    docResponse.setReview(reviews);
                } else {
                    docResponse.setRating(0);
                    docResponse.setReview(0);
                }
                int maxCharges = chargesRepository.getMaxConsultationFees();
                docResponse.setMax_fees(maxCharges);

                if (val.getLanguageFluency() != null) {
                    String[] lang = val.getLanguageFluency().split(",");
                    List<String> knownLang = new ArrayList<>();
                    int langCount = lang.length;
                    if (langCount == 0) docResponse.setLanguage(new ArrayList<>());
                    else {
                        while (langCount-- > 0) {
                            Optional<Language> langName = languageRepository.findById(Integer.parseInt(lang[langCount]));
                            knownLang.add(langName.get().getName());
                        }
                        docResponse.setLanguage(knownLang);
                    }
                }

                Users user = usersRepository.findById(val.getHospitalId()).orElse(null);
                if (user != null) {
                    docResponse.setHospital_id(val.getHospitalId());
                    docResponse.setHospital_name(user.getClinicName());
                }

                List<DoctorSpecialization> doctorSpecializationList = doctorSpecializationRepository.findByUserId(val.getUserId());
                if (!doctorSpecializationList.isEmpty()) {
                    List<String> specialities = new ArrayList<>();
                    for(DoctorSpecialization item : doctorSpecializationList) {
                        specialities.add(item.getSpecializationId().getName());
                    }
                    docResponse.setSpeciality(specialities);
                }

                List<DoctorAvailability> set = getAvailabilityByDay(LocalDate.now(), val);
                boolean available = false;
                if(set != null) {
                    available = (set != null) || (!set.isEmpty());
                    docResponse.setAvailableToday(available);
                }

                responses.add(docResponse);
            }

            List<SearchDocResponse> specializationDoctorList = getSpecializationList(responses, request);
            List<SearchDocResponse> availabilityDoctorList = getAvailabilityList(responses, request);
            List<SearchDocResponse> languageDoctorList = getLanguageList(responses, request);

            //only specialization
            if (!request.getSpecialization_id().isEmpty() && request.getLanguage_fluency() <= 0 && request.getAvailability().isEmpty()) {
                responses = specializationDoctorList;
            }
            //only language
            else if (request.getSpecialization_id().isEmpty() && request.getLanguage_fluency() > 0 && request.getAvailability().isEmpty()) {
                responses = languageDoctorList;
            }
            //only availability
            else if (!request.getAvailability().isEmpty() && request.getSpecialization_id().isEmpty() && request.getLanguage_fluency() <= 0) {
                responses = availabilityDoctorList;
            }
            //all condition up
            else if (!request.getAvailability().isEmpty() && !request.getSpecialization_id().isEmpty() && request.getLanguage_fluency() > 0) {
                // Use sets to find common user IDs
                Set<SearchDocResponse> set1 = new HashSet<>(availabilityDoctorList);
                Set<SearchDocResponse> set2 = new HashSet<>(specializationDoctorList);
                Set<SearchDocResponse> set3 = new HashSet<>(languageDoctorList);
                // Find common user IDs across all three sets
                set1.retainAll(set2);
                set1.retainAll(set3);

                responses = new ArrayList<>(set1);
            }
            //only specialization and Language is up
            else if (request.getAvailability().isEmpty() && !request.getSpecialization_id().isEmpty() && request.getLanguage_fluency() > 0){
                Set<SearchDocResponse> set2 = new HashSet<>(specializationDoctorList);
                Set<SearchDocResponse> set3 = new HashSet<>(languageDoctorList);

                set2.retainAll(set3);

                responses = new ArrayList<>(set2);
            }
            //only availability and specialization
            else if (!request.getAvailability().isEmpty() && !request.getSpecialization_id().isEmpty() && request.getLanguage_fluency() <= 0){
                Set<SearchDocResponse> set1 = new HashSet<>(availabilityDoctorList);
                Set<SearchDocResponse> set2 = new HashSet<>(specializationDoctorList);

                set1.retainAll(set2);

                responses = new ArrayList<>(set1);
            }
            //only availability and language
            else if (!request.getAvailability().isEmpty() && request.getSpecialization_id().isEmpty() && request.getLanguage_fluency() > 0){
                Set<SearchDocResponse> set1 = new HashSet<>(availabilityDoctorList);
                Set<SearchDocResponse> set3 = new HashSet<>(languageDoctorList);

                set1.retainAll(set3);

                responses = new ArrayList<>(set1);
            }
            //doctor search by name
            if (!request.getDoctor_name().isEmpty()) {
                List<SearchDocResponse> list = new ArrayList<>();
                for (SearchDocResponse t : responses) {
                    if(t.getName().toLowerCase().contains(request.getDoctor_name().toLowerCase())) list.add(t);
                }
                responses = list;
            }

            Map<String, Object> responseMap = new HashMap<>();
            //experience
            if (!request.getSort_by().isEmpty()) {
                if (request.getSort_by().equalsIgnoreCase(Constants.EXPERIENCE)) {
                    responses.sort(Comparator.comparing(response -> extractExperienceAsDouble(response.getExperience())));
                } else {
                    responses = getRecommendation(responses).stream().distinct().collect(Collectors.toList());
                }
            }

            Page<SearchDocResponse> paginationResponse = TransformDto.paginate(responses, request.getPage(), request.getPageSize());
            responseMap.put("doctorList", paginationResponse.getContent());
            responseMap.put("totalCount", responses.size());

            if(responseMap.size()>0){
                String msg = messageSource.getMessage(Constants.FOUND_COUNT_DOCTOR,null,locale);
                msg = msg.replace("{{count}}",String.valueOf(responses.size()));
                return ResponseEntity.status(HttpStatus.OK).body(new Response(
                        Constants.SUCCESS_CODE,
                        Constants.SUCCESS_CODE,
                        msg,
                        responseMap
                ));
            }else{
                return ResponseEntity.status(HttpStatus.OK).body(new Response(
                        Constants.NO_RECORD_FOUND_CODE,
                        Constants.NO_RECORD_FOUND_CODE,
                        messageSource.getMessage(Constants.NO_RECORD_FOUND,null,locale),
                        responseMap
                ));
            }
        } else {
            return ResponseEntity.status(HttpStatus.OK).body(new Response(
                    Constants.BLANK_DATA_GIVEN_CODE,
                    Constants.BLANK_DATA_GIVEN_CODE,
                    messageSource.getMessage(Constants.BLANK_DATA_GIVEN,null,locale)
            ));
        }
    }

    private static double extractExperienceAsDouble(String experience) {
        // Extract the first number from the string
        String[] parts = experience.split(" ");
        return Double.parseDouble(parts[0]);
    }

    public List<DoctorAvailability> getAvailabilityByDay(LocalDate requiredDate, Users doctor) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat dayFormat = new SimpleDateFormat("EEEE");
        List<DoctorAvailability> doctorSlotsList = doctorAvailabilityRepository.findByDoctorId(doctor);
        if (!doctorSlotsList.isEmpty()) {
            Date date;
            try {
                date = sdf.parse(String.valueOf(requiredDate));
            } catch (ParseException e) {
                throw new RuntimeException(e);
            }
            String day = dayFormat.format(date);

            doctorSlotsList = doctorSlotsList.stream().filter(doc -> doc.getSlotId().getSlotDay().equalsIgnoreCase(day)).toList();
            List<Consultation> consultations = consultationRepository.findByDoctorIdAndConsultationDate(doctor, requiredDate);

            doctorSlotsList = filterNonSimilarities(doctorSlotsList, consultations);

            LocalDateTime dateTime = LocalDateTime.now();

            if(requiredDate.isEqual(dateTime.toLocalDate())){
                doctorSlotsList = doctorSlotsList.stream().filter(doc -> doc.getSlotId().getSlotStartTime().isAfter(dateTime.toLocalTime())).toList();
            }

            return doctorSlotsList;
        }
        return null;
    }

    private List<DoctorAvailability> filterNonSimilarities(List<DoctorAvailability> listA, List<Consultation> listB){
        Set<Integer> idsB = new HashSet<>();
        for(Consultation item : listB){
            idsB.add(item.getSlotId().getSlotId());
        }

        List<DoctorAvailability> filteredListA = new ArrayList<>();
        for (DoctorAvailability a : listA) {
            if (!idsB.contains(a.getSlotId().getSlotId())) {
                filteredListA.add(a);
            }
        }
        return filteredListA;
    }

    private List<SearchDocResponse> getSpecializationList(List<SearchDocResponse> responses, SearchDoctorRequest request){
        HashSet<Integer> set = new HashSet<>();
        List<SearchDocResponse> specializationDoctorList = new ArrayList<>();
        for (Integer specializationId : request.getSpecialization_id()) {
            List<Integer> docId = doctorSpecializationRepository.getDoctorIdFromSpecializationId(specializationId);
            set.addAll(docId);
        }
        for(SearchDocResponse item : responses){
            if(set.contains(item.getId())) specializationDoctorList.add(item);
        }
        return specializationDoctorList;
    }

    private List<SearchDocResponse> getAvailabilityList(List<SearchDocResponse> responses, SearchDoctorRequest request){
        HashSet<SearchDocResponse> set = new HashSet<>();
        for (SearchDocResponse obj : responses) {
            List<DoctorAvailability> uniqueObj;
            Users item = usersRepository.findById(obj.getId()).orElse(null);
            if (request.getAvailability().equalsIgnoreCase(Constants.CONS_TODAY)) {
                uniqueObj = getAvailabilityByDay(LocalDate.now(), item);
                if (uniqueObj != null && !uniqueObj.isEmpty()) {
                    set.add(obj);
                }
            } else if (request.getAvailability().equalsIgnoreCase(Constants.CONS_TOMORROW)) {
                uniqueObj = getAvailabilityByDay(LocalDate.now().plusDays(1), item);
                if (uniqueObj != null && !uniqueObj.isEmpty()) {
                    set.add(obj);
                }
            } else {
                for (int i = 0; i < 7; i++) {
                    uniqueObj = getAvailabilityByDay(LocalDate.now().plusDays(i), item);
                    if (uniqueObj != null && !uniqueObj.isEmpty()) set.add(obj);
                }
            }
        }
        return new ArrayList<>(set);
    }

    private List<SearchDocResponse> getLanguageList(List<SearchDocResponse> responses, SearchDoctorRequest request){
        List<SearchDocResponse> languageList = new ArrayList<>();
        Language language = languageRepository.findById(request.getLanguage_fluency()).orElse(null);
        if(language != null) {
            for(SearchDocResponse response : responses){
                for(String value : response.getLanguage()){
                    if(value.equalsIgnoreCase(language.getName())) languageList.add(response);
                }
            }
        }
        return languageList;
    }

    private List<SearchDocResponse> getRecommendation(List<SearchDocResponse> list) {
        List<ConsultationRating> pUserList = new ArrayList<>();
        for (SearchDocResponse user : list) {
            Users u = usersRepository.findById(user.getId()).orElse(null);
            List<ConsultationRating> listByDoctor = consultationRatingRepository.findByDoctorId(u);
            pUserList.addAll(listByDoctor);
        }
        List<ConsultationRating> uniqueDoctors = pUserList.stream().distinct().toList();
        List<SearchDocResponse> setA = new ArrayList<>();
        HashSet<ConsultationRating> setB = new HashSet<>(uniqueDoctors);
        for(SearchDocResponse item : list){
            if(setB.contains(item.getId())) setA.add(item);
        }
        return setA;
    }

    //TODO : make this api based on doctor-availability-list-latest
    public ResponseEntity<?> doctorAvailabilityListLatest(Locale locale, DoctorAvailabilityListLatestRequest request) {
        Users slot_type_id = usersRepository.findById(request.getDoctor_id()).orElse(null);
        List<SlotMaster> slotListing = slotMasterRepository.findBySlotTypeIdAndSlotDay(slot_type_id.getSlotTypeId(),request.getDate());


        List<Consultation> constantsList = consultationRepository.findByRequestTypeAndCreatedAtAndPatientIdAndDoctorIdAndConstaitionTypeAndConstationDate(
                RequestType.Book,request.getNew_order_date(), request.getUser_id(),request.getDoctor_id(), ConsultationType.Paid,request.getDate()
        );
        Consultation last_consult_data = (constantsList.isEmpty())?null:constantsList.get(0);

        ConsultationType consultation_type = ConsultationType.Paid;
        if(request.getConsult_type() == FeeType.VIDEO){
            request.setConsult_type(FeeType.CALL);
        }

        List<Charges> doctorchargesList = chargesRepository.findByUserIdAndConsultantType(request.getDoctor_id(),request.getConsult_type());
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
                    request.getUser_id(),request.getDoctor_id(),request.getNew_order_date(),
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
            Long available_count = doctorAvailabilityRepository.countBySlotIdAndDoctorId(slot.getSlotId(),request.getDoctor_id());

            Long check_consultant_count = consultationRepository.countBySlotIdAndDoctorIdConsultationDate(
                    slot.getSlotId(),request.getDoctor_id(),  request.getDate());

            List<Consultation> consultantInfoList = consultationRepository.findByDoctorIdAndSlotIdAndRequestTypeAndDate(
                    request.getDoctor_id(), slot.getSlotId(),request.getDate(), RequestType.Cancel);

            Consultation consultantInfo = (consultantInfoList.isEmpty())?null:constantsList.get(0);

            String userIdInSlot = consultantInfo != null ? consultantInfo.getPatientId().toString() : "";
            Integer caseId = consultantInfo != null ? consultantInfo.getCaseId() : null;

            LocalDateTime consultantDateTime = LocalDateTime.of(request.getDate(), slot.getSlotStartTime());

            LocalDateTime currentDateTime = LocalDateTime.now();

            Long diff = Math.abs(java.time.Duration.between(consultantDateTime, currentDateTime).toMinutes());

            Integer timeLimit = Integer.parseInt(globalConfigurationRepository.findByKey("CANCEL_CONSULT_PATIENT").getValue());

            Boolean isCancel = (timeLimit <= diff) && (consultantInfo != null && consultantInfo.getRequestType() == RequestType.Book && consultantDateTime.isAfter(currentDateTime));

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


        return ResponseEntity.status(HttpStatus.OK).body(new Response(
                Constants.SUCCESS_CODE,
                Constants.SUCCESS_CODE,
                messageSource.getMessage(Constants.SUCCESS_MESSAGE,null,locale),
                response
        ));
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
                    specsFinalString = specsFinalString+ docSpec.getSpecializationId().getName()+",";
                }
            }
            String[] languageIds  = doctor.getLanguageFluency().split(",");
            String languageName = "";
            if(languageIds.length>0){
                List<Integer> langIds = new ArrayList<>();
                for(String s:languageIds){langIds.add(Integer.parseInt(s));}
                List<Language> languages = languageRepository.findByIds(langIds);
                for(Language l:languages){languageName = languageName + l.getName()+",";}
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
                    CommentsDto dto = new CommentsDto();
                    dto.setComment(r.getComment());
                    dto.setName(r.getDoctorId().getFirstName()+" "+r.getDoctorId().getLastName());
                    dto.setRating(r.getRating());
                    dto.setCreated_at(r.getCreatedAt());
                    dto.setFile_url(fileUrl);
                    dto.setTotal_count(totalConsultCount);

                    commentsDtos.add(dto);
                }
            }

            Map<String, Float> chargesMap = new HashMap<>();

            if (charges != null && !charges.isEmpty()) {
                for (Charges charge : charges) {
                    String commissionKey = charge.getFeeType() + "_commission";
                    String finalConsFeeKey = charge.getFeeType() + "_final_consultation_fees";
                    String feesKey = charge.getFeeType() + "_consultation_fees";

                    chargesMap.put(commissionKey, charge.getCommission());
                    chargesMap.put(finalConsFeeKey, charge.getFinalConsultationFees());
                    chargesMap.put(feesKey, charge.getConsultationFees());
                }
            }

            List<Object> response = new ArrayList<>();
            ViewProfileResponse dto = new ViewProfileResponse();
            dto.setFirst_name(doctor.getFirstName());
            dto.setLast_name(doctor.getLastName());
            dto.setEmail(doctor.getEmail());
            dto.setContact_number(doctor.getContactNumber());
            dto.setPhoto(photo);
            dto.setCountry(
                    (doctor.getCountry()!=null)
                            ?doctor.getCountry().getName():"");
            dto.setState(
                    (doctor.getState()!=null)
                            ?doctor.getState().getName():"");
            dto.setCity(
                    (doctor.getCity()!=null)
                            ?doctor.getCity().getName():"");
            dto.setHospital_address(doctor.getHospitalAddress());
            dto.setResidence_address(doctor.getResidenceAddress());
            dto.setProfessional_identification_number(doctor.getProfessionalIdentificationNumber());
            dto.setRating(finalCount);
            dto.setExtra_activities(doctor.getExtraActivities());
            dto.setAbout_me(doctor.getAboutMe());
            dto.setLanguage(languageName);
            dto.setReview(commentsDtos);
            dto.setGender(doctor.getGender());

            response.add(dto);
            response.add(chargesMap);
            return ResponseEntity.status(HttpStatus.OK).body(new Response(
                    Constants.SUCCESS_CODE,
                    Constants.SUCCESS_CODE,
                    messageSource.getMessage(Constants.PROFILE_FETCH_SUCCESSFULLY,null,locale)
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
                dto.setName(rating.getPatientId().getFirstName() + " "+rating.getPatientId().getLastName());
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
