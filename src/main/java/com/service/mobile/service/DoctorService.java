package com.service.mobile.service;

import com.service.mobile.config.Constants;
import com.service.mobile.controllers.FileController;
import com.service.mobile.dto.dto.ConsultationFees;
import com.service.mobile.dto.dto.SearchDocResponse;
import com.service.mobile.dto.dto.TransformDto;
import com.service.mobile.dto.enums.FeeType;
import com.service.mobile.dto.enums.UserType;
import com.service.mobile.dto.request.SearchDoctorRequest;
import com.service.mobile.dto.response.CityResponse;
import com.service.mobile.dto.response.Response;
import com.service.mobile.model.*;
import com.service.mobile.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
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

                List<DoctorSpecialization> doctorSpecializationList = doctorSpecializationRepository.findByUserId(val);
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
    public ResponseEntity<?> doctorAvailabilityListLatest(Locale locale, SearchDoctorRequest request) {
        return null;
    }
}
