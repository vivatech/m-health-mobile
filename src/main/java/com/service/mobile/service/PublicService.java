package com.service.mobile.service;

import com.service.mobile.config.Constants;
import com.service.mobile.config.PaymentOptionConfig;
import com.service.mobile.dto.MessageService;
import com.service.mobile.dto.OfferInformationDTO;
import com.service.mobile.dto.dto.*;
import com.service.mobile.dto.enums.*;
import com.service.mobile.dto.request.*;
import com.service.mobile.dto.response.*;
import com.service.mobile.model.*;
import com.service.mobile.model.State;
import com.service.mobile.repository.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import org.springframework.data.domain.Pageable;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.*;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class PublicService {
    @Autowired
    private SmsLogRepository smsLogRepository;
    @Autowired
    private NotificationRepository notificationRepository;

    public static final String PATIENT ="patient";
    @Autowired
    private CountryRepository countryRepository;

    @Autowired
    private GlobalConfigurationRepository globalConfigurationRepository;

    @Autowired
    private ChargesService chargesService;

    @Autowired
    private MessageSource messageSource;

    @Autowired
    private StaticPageService staticPageService;

    @Autowired
    private PackageUserRepository packageUserRepository;

    @Autowired
    private CouponRepository couponRepository;

    @Autowired
    private UsersRepository usersRepository;

    @Value("${app.base.url}")
    private String baseUrl;

    @Value("${app.default.image}")
    private String defaultImage;

    @Value("${app.currency.symbol.fdj}")
    private String currencySymbolFdj;

    @Value("${app.currency.symbol.slsh}")
    private String currencySymbolSLSH;

    @Value("${app.currency.symbol}")
    private String currencySymbol;

    @Value("${app.payment.rate}")
    private Float PaymentRate;

    @Value("${app.location.radius}")
    private Float locationRadius;

    @Value("${app.log.path}")
    private Float logPath;

    @Value("${app.system.user.id}")
    private Integer SystemUserId;
    @Value("${app.charge.home.collection}")
    private Integer homeChargeCollection;

    @Autowired
    private UserLocationRepository userLocationRepository;

    @Autowired
    private StateRepository stateRepository;

    @Autowired
    private CityRepository cityRepository;

    @Autowired
    private LanguageRepository languageRepository;

    @Autowired
    private SpecializationRepository specializationRepository;

    @Autowired
    private PaymentOptionConfig paymentOptionConfig;
    @Autowired
    private ConsultationRepository consultationRepository;
    @Autowired
    private HomecareReservedSlotRepository homecareReservedSlotRepository;
    @Autowired
    private DoctorAvailabilityRepository doctorAvailabilityRepository;
    @Autowired
    private SlotMasterRepository slotMasterRepository;
    @Autowired
    private UsersUsedCouponCodeRepository usersUsedCouponCodeRepository;
    @Autowired
    private OrdersRepository ordersRepository;
    @Autowired
    private ConsultationRatingRepository consultationRatingRepository;
    @Autowired
    private HealthTipCategoryMasterRepository healthTipCategoryMasterRepository;
    @Autowired
    private LabCategoryMasterRepository labCategoryMasterRepository;
    @Autowired
    private LabPriceRepository labPriceRepository;
    @Autowired
    private LabSubCategoryMasterRepository labSubCategoryMasterRepository;
    @Autowired
    private PartnerNurseRepository partnerNurseRepository;
    @Autowired
    private NurseServiceStateRepository nurseServiceStateRepository;
    @Autowired
    private WalletTransactionRepository walletTransactionRepository;
    @Autowired
    private LabConsultationRepository labConsultationRepository;
    @Autowired
    private WalletRepository walletRepository;
    @Autowired
    private LanguageService languageService;
    @Autowired
    private SDFSMSService sdfsmsService;
    @Autowired
    private MessageService messageService;
    @Value("${app.ZoneId}")
    private String zone;
    @Value("${app.database.url}")
    private String nurseURL;
    @Value("${app.database.username}")
    private String nurseUserName;
    @Value("${app.database.password}")
    private String nursePassword;

    public List<Country> findAllCountry(){
        return countryRepository.findAll();
    }

    public ResponseEntity<?> getGlobalParams(Locale locale) {
        Map<String, String> globalDetail = globalConfigurationRepository.findByKeyIn(
                List.of("TURN_PASSWORD", "STURN_SERVER", "TURN_SERVER", "TURN_USERNAME")).stream()
                .collect(Collectors.toMap(GlobalConfiguration::getKey, GlobalConfiguration::getValue));

        Integer maxFees = chargesService.getMaxConsultationFees();
        Long minFees = 0l;

        Map<String, String>[] slotTime = new HashMap[3];
        slotTime[0] = new HashMap<>();
        slotTime[0].put("key", "Morning");
        slotTime[0].put("value", "0-11");

        slotTime[1] = new HashMap<>();
        slotTime[1].put("key", "Afternoon");
        slotTime[1].put("value", "12-16");

        slotTime[2] = new HashMap<>();
        slotTime[2].put("key", "Evening");
        slotTime[2].put("value", "17-23");

        Map<String, Object> responseData = new HashMap<>();
        responseData.put("min_fees", minFees);
        responseData.put("max_fees", maxFees);
        responseData.put("turn_username", globalDetail.getOrDefault("TURN_USERNAME", ""));
        responseData.put("turn_password", globalDetail.getOrDefault("TURN_PASSWORD", ""));
        responseData.put("turn_server", globalDetail.getOrDefault("TURN_SERVER", ""));
        responseData.put("sturn_server", globalDetail.getOrDefault("STURN_SERVER", ""));
        responseData.put("currency_fdj", "currency_fdj_value"); // replace with appropriate value
        responseData.put("currency_symbol_fdj", "currency_fdj_symbol"); // replace with appropriate value
        responseData.put("upload_doc_msg", "upload_doc_msg_value"); // replace with appropriate value
        responseData.put("upload_id_msg", "upload_id_msg_value"); // replace with appropriate value
        responseData.put("slot_time", slotTime);

        Response response = new Response(
                Constants.SUCCESS_CODE,
                Constants.SUCCESS_CODE,
                messageSource.getMessage(Constants.GLOBAL_VARIABLES_FETCH,null,locale),
                responseData
        );
        return ResponseEntity.ok(response);
    }

    public ResponseEntity<?> getStaticPageContent(Locale locale, String type) {
        StaticPage page = staticPageService.findByName(type);
        String msg;
        Response response = new Response();
        if ("privacy_policy".equals(type)) {
            msg = messageSource.getMessage(Constants.PRIVEECY_POLICY_SUCCESS,null,locale);
        } else if ("terms_and_conditions".equals(type)) {
            msg = messageSource.getMessage(Constants.TERM_AND_CONDITION_SUCCESS,null,locale);
        } else {
            return ResponseEntity.status(403).body(
                    new Response(Constants.NO_CONTENT_FOUNT_CODE,
                            Constants.NO_CONTENT_FOUNT_CODE,
                            messageSource.getMessage(Constants.NO_CONTENT_FOUNT,null,locale)
                    )
            );
        }

        if (page == null) {
            return ResponseEntity.status(403).body(
                    new Response(Constants.NO_CONTENT_FOUNT_CODE,
                            Constants.NO_CONTENT_FOUNT_CODE,
                            messageSource.getMessage(Constants.NO_CONTENT_FOUNT,null,locale)
                    )
            );
        } else {
            response = new Response(
                    Constants.SUCCESS_CODE,
                    Constants.SUCCESS_CODE,
                    msg,
                    page.getDescription()
            );
            return ResponseEntity.ok(response);
        }
    }

    public ResponseEntity<?> activities(Locale locale, Integer userId) {
        if (userId!=null && userId!=0) {
            Pageable pageable = PageRequest.of(0,1);
            List<PackageUser> packageDetailOpt = packageUserRepository.getActivePackageDetail(userId, YesNo.No,pageable);
            if (packageDetailOpt.size()>0) {
                PackageUser packageDetail = packageDetailOpt.get(0);
                ActivitiesResponse data = new ActivitiesResponse();

                data.setPackage_expired(packageDetail.getExpiredAt().format(DateTimeFormatter.ofPattern("d, MMMM yyyy HH:mm")));
                data.setPackage_status(Constants.ACTIVE);
                data.setPackage_name(packageDetail.getPackageInfo().getPackageName());
                data.setPackage_price("$"+packageDetail.getPackageInfo().getPackagePrice());

                return ResponseEntity.status(HttpStatus.OK).body(new Response(
                        Constants.SUCCESS_CODE,
                        Constants.SUCCESS_CODE,
                        messageSource.getMessage(Constants.ACTIVITIES_FETCH_SUCCESSFULLY,null,locale),
                        data
                ));
            } else {
                return ResponseEntity.status(HttpStatus.OK).body(new Response(
                        Constants.SUCCESS_CODE,
                        Constants.SUCCESS_CODE,
                        messageSource.getMessage(Constants.ACTIVITIES_NOT_FOUND,null,locale)
                ));
            }
        }else{
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new Response(
                    Constants.NO_RECORD_FOUND_CODE,
                    Constants.BLANK_DATA_GIVEN_CODE,
                    messageSource.getMessage(Constants.BLANK_DATA_GIVEN,null,locale)
            ));
        }
    }

    public ResponseEntity<?> getConsultType(Locale locale) {
        List<ConsultTypeResponse> response = new ArrayList<>();
        ConsultTypeResponse data = new ConsultTypeResponse(
                messageSource.getMessage(Constants.VIDEO_CONSULTATION,null,locale),
                "",
                1,"video"
        );
        response.add(data);
        return ResponseEntity.status(HttpStatus.OK).body(new Response(
                Constants.SUCCESS_CODE,
                Constants.SUCCESS_CODE,
                messageSource.getMessage(Constants.SUCCESS_MESSAGE,null,locale),
                response
        ));
    }

    public ResponseEntity<?> getOffers(Locale locale) {
        List<Coupon> coupons = couponRepository.findByStatus(1);
        if (coupons.size()>0) {
            OfferResponseDTO responseDTO = new OfferResponseDTO();
            List<OfferInformationDTO> offer_information = new ArrayList<>();
            for(Coupon coupon:coupons){
                OfferInformationDTO infoDto = new OfferInformationDTO();
                String discountSymbol = (coupon.getDiscountType()== DiscountType.PERCENTAGE)?"%":currencySymbol;
                Float discountAmount = (coupon.getType()== OfferType.PAID)?coupon.getDiscountAmount():100f;
                infoDto.setDiscount_amount(discountAmount + discountSymbol);
                infoDto.setDiscount_type((coupon.getType()==OfferType.PAID) ?
                        messageSource.getMessage(Constants.DISCOUNT_MSG,null,locale)
                        : messageSource.getMessage(Constants.FREE_MSG,null,locale));


                infoDto.setDiscount_type((coupon.getType()==OfferType.PAID) ?
                        messageSource.getMessage(Constants.ON_HEALTHTIP,null,locale)
                        : messageSource.getMessage(Constants.ON_VIDEO_CONSULTATION,null,locale));

                infoDto.setOffer_text((coupon.getCategory()== CouponCategory.HEALTHTIP) ?
                        messageSource.getMessage(Constants.OFFER_TEXT_ONE,null,locale)
                        : messageSource.getMessage(Constants.OFFER_TEXT_TWO,null,locale));

                offer_information.add(infoDto);
            }
            responseDTO.setOffer_information(offer_information);
            responseDTO.setCoupon_code(coupons.get(0).getCouponCode());
            responseDTO.setNote_message(messageSource.getMessage(Constants.NOTE_MESSAGE,null,locale));
            return ResponseEntity.status(HttpStatus.OK).body(new Response(
                    Constants.SUCCESS_CODE,
                    Constants.SUCCESS_CODE,
                    messageSource.getMessage(Constants.SUCCESS_MESSAGE,null,locale),
                    responseDTO
            ));
        }else{
            return ResponseEntity.status(HttpStatus.OK).body(new Response(
                    Constants.SUCCESS_CODE,
                    Constants.SUCCESS_CODE,
                    messageSource.getMessage(Constants.NO_CONTENT_FOUNT,null,locale)
            ));
        }
    }

    public HomeConsultationInformation getHomeConsultationInformation(Integer userId) {
        HomeConsultationInformation dto = new HomeConsultationInformation();
        Users users = usersRepository.findById(userId).orElse(null);
        dto.setName(users.getFirstName()+" "+users.getLastName());
        dto.setContact_number(users.getCountryCode()+users.getContactNumber());
        String photoPath = users.getProfilePicture() != null ? baseUrl+"uploaded_file/UserProfile/" + users.getUserId() + "/" + users.getProfilePicture() : "";
        dto.setProfile_picture(photoPath);
        return dto;
    }

    public ClinicInformation getClinicInformation(Integer userId) {
        ClinicInformation dto = new ClinicInformation();
        Users users = usersRepository.findById(userId).orElse(null);
        if(users!=null){
            dto.setName(users.getFirstName()+" "+users.getLastName());
            dto.setContact_number(users.getCountryCode()+users.getContactNumber());
            dto.setAddress(users.getHospitalAddress());
            UserLocation location = userLocationRepository.findByUserId(userId).orElse(null);
            if(location!=null){
                dto.setLatitude((location.getLatitude()!=null)?location.getLatitude():null);
                dto.setLongitude((location.getLongitude()!=null)?location.getLongitude():null);
            }
            String photoPath = users.getProfilePicture() != null ? baseUrl+"uploaded_file/UserProfile/" + users.getUserId() + "/" + users.getProfilePicture() : "";
            dto.setProfile_picture(photoPath);
            return dto;
        }
        return null;
    }

    public ResponseEntity<?> getProfile(Locale locale, Integer userId) {
        if (userId!=null && userId!=0) {
            Users users = usersRepository.findById(userId).orElse(null);
            if(users!=null){
                String photoPath = users.getProfilePicture() != null ? baseUrl+"uploaded_file/UserProfile/" + users.getUserId() + "/" + users.getProfilePicture() : "";
                String countryName = (users.getCountry()!=null)?users.getCountry().getName():"";
                State state = null;
                if(users.getState()!=null && users.getState()!=0){
                    state = stateRepository.findById(users.getState()).orElse(null);
                }
                City city = null;
                if(users.getCity()!=null && users.getCity()!=0){
                    city = cityRepository.findById(users.getCity()).orElse(null);
                }
                String stateName = (state!=null)?state.getName():"";
                String cityName = (city!=null)?city.getName():"";

                ProfileDto profile = new ProfileDto();
                profile.setFirst_name(users.getFirstName());
                profile.setLast_name(users.getLastName());
                profile.setFullName(users.getFirstName()+" "+users.getLastName());
                profile.setEmail((users.getEmail()!=null)?users.getEmail():"");
                profile.setContact_number(users.getContactNumber());
                profile.setPhoto(photoPath);
                profile.setCountry(countryName);
                profile.setCountry_code(users.getCountryCode());
                profile.setState(stateName);
                profile.setCity(cityName);
                profile.setResidence_address(users.getResidenceAddress());
                profile.setDob(users.getDob());
                profile.setGender(users.getGender());

                return ResponseEntity.status(HttpStatus.OK).body(new Response(
                        Constants.SUCCESS_CODE,
                        Constants.SUCCESS_CODE,
                        messageSource.getMessage(Constants.SUCCESS_MESSAGE,null,locale),
                        profile
                ));

            }else{
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new Response(
                        Constants.NO_CONTENT_FOUNT_CODE,
                        Constants.NO_CONTENT_FOUNT_CODE,
                        messageSource.getMessage(Constants.UNAUTHORIZED_MSG,null,locale)
                ));
            }
        }else{
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new Response(
                    Constants.NO_RECORD_FOUND_CODE,
                    Constants.BLANK_DATA_GIVEN_CODE,
                    messageSource.getMessage(Constants.BLANK_DATA_GIVEN,null,locale)
            ));
        }
    }

    public ResponseEntity<?> getStateList(Locale locale) {
        List<State> states = stateRepository.findStatesWithExistingCountry();
        if (!states.isEmpty()) {
            List<StateResponse> data = new ArrayList<>();
            for(State t : states){
                data.add(new StateResponse(t.getId(),t.getName(),(t.getCountry()!=null)?t.getCountry().getId():null));
            }

            return ResponseEntity.status(HttpStatus.OK).body(new Response(
                    Constants.SUCCESS_CODE,
                    Constants.SUCCESS_CODE,
                    messageSource.getMessage(Constants.STATE_FOUND_SUCCESSFULLY,null,locale),
                    data
            ));
        }else{
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new Response(
                    Constants.NO_RECORD_FOUND_CODE,
                    Constants.BLANK_DATA_GIVEN_CODE,
                    messageSource.getMessage(Constants.NO_STATE_FOUND,null,locale)
            ));
        }
    }

    public ResponseEntity<?> getCityList(Locale locale) {
        List<City> cities = cityRepository.findAllByNameAsc();
        if (cities.size()>0) {
            List<CityResponse> data = new ArrayList<>();
            for(City t:cities){
                data.add(new CityResponse(t.getId(),t.getName(),(t.getState()!=null)?t.getState().getId():null));
            }

            return ResponseEntity.status(HttpStatus.OK).body(new Response(
                    Constants.SUCCESS_CODE,
                    Constants.SUCCESS_CODE,
                    messageSource.getMessage(Constants.STATE_FOUND_SUCCESSFULLY,null,locale),
                    data
            ));
        }else{
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new Response(
                    Constants.NO_RECORD_FOUND_CODE,
                    Constants.BLANK_DATA_GIVEN_CODE,
                    messageSource.getMessage(Constants.NO_STATE_FOUND,null,locale)
            ));
        }
    }

    public ResponseEntity<?> editProfile(Locale locale, EditProfileRequest request) {
        if (request.getUser_id() != null && request.getUser_id() > 0 &&
                request.getFullName()!=null && !request.getFullName().isEmpty() &&
                request.getEmail()!=null && !request.getEmail().isEmpty() &&
                request.getGender()!=null && !request.getGender().isEmpty() &&
                request.getResidence_address()!=null && !request.getResidence_address().isEmpty() &&
                request.getDob() != null
                && request.getCity_id() != null && request.getCity_id() > 0) {
            Users user = usersRepository.findById(request.getUser_id()).orElse(null);
            if(user!=null){
                user.setUserId(request.getUser_id());

                String[] name = request.getFullName().split(" ");
                StringBuilder lastName = new StringBuilder();
                String firstName = "";
                if(name.length > 1) {
                    firstName = name[0];
                    for (int i = 1; i < name.length; i++) lastName.append(" ").append(name[i]);
                }
                else firstName = name[0];

                user.setFirstName(firstName);
                user.setLastName(lastName.toString());
                user.setEmail(request.getEmail());
                user.setGender(request.getGender());
                user.setResidenceAddress(request.getResidence_address());
                user.setDob(request.getDob());
                user.setUpdatedAt(new Timestamp(System.currentTimeMillis()));
                City city = cityRepository.findById(request.getCity_id()).orElse(null);
                if(city != null) {
                    user.setCity(city.getId());
                    user.setState(city.getState().getId());
                    user.setCountry(city.getState().getCountry());
//                    user.setCountryCode(String.valueOf(city.getState().getCountry().getPhoneCode()));
                }
                usersRepository.save(user);
                return ResponseEntity.status(HttpStatus.OK).body(new Response(
                        Constants.SUCCESS_CODE,
                        Constants.SUCCESS_CODE,
                        messageSource.getMessage(Constants.PATIENT_UPDATED_SUCCESSFULLY,null,locale),
                        null
                ));
            }else{
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new Response(
                        Constants.NO_CONTENT_FOUNT_CODE,
                        Constants.NO_CONTENT_FOUNT_CODE,
                        messageSource.getMessage(Constants.UNAUTHORIZED_MSG,null,locale)
                ));
            }
        }else{
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new Response(
                    Constants.NO_RECORD_FOUND_CODE,
                    Constants.BLANK_DATA_GIVEN_CODE,
                    messageSource.getMessage(Constants.BLANK_DATA_GIVEN,null,locale)
            ));
        }
    }

    public ResponseEntity<?> getLanguage(Locale locale) {
        List<Language> languages = languageRepository.findAllByStatus("A");
        if (!languages.isEmpty()) {
            List<Map<String, Object>> list = new ArrayList<>();
            for(Language l : languages){
                Map<String, Object> data = new HashMap<>();
                data.put("id", l.getId());
                data.put("name", l.getName());

                list.add(data);
            }
            return ResponseEntity.status(HttpStatus.OK).body(new Response(
                    Constants.SUCCESS_CODE,
                    Constants.SUCCESS_CODE,
                    messageSource.getMessage(Constants.LANGUAGE_LIST_FOUND,null,locale),
                    list
            ));
        }else{
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new Response(
                    Constants.NO_RECORD_FOUND_CODE,
                    Constants.BLANK_DATA_GIVEN_CODE,
                    messageSource.getMessage(Constants.NO_LANGUAGE_LIST_FOUND,null,locale)
            ));
        }
    }


    public ResponseEntity<?> getSpecialization(Locale locale) {
        List<Specialization> specializations = specializationRepository.findAllByStatus(Status.A);
        if (!specializations.isEmpty()) {
            List<SpecializationResponse> responses = getSpecializationResponses(locale, specializations);
            return ResponseEntity.status(HttpStatus.OK).body(new Response(
                    Constants.SUCCESS_CODE,
                    Constants.SUCCESS_CODE,
                    messageSource.getMessage(Constants.SPECIALIZATION_LIST_FOUND,null,locale),
                    responses
            ));
        }else{
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new Response(
                    Constants.NO_RECORD_FOUND_CODE,
                    Constants.BLANK_DATA_GIVEN_CODE,
                    messageSource.getMessage(Constants.NO_SPECIALIZATION_LIST_FOUND,null,locale)
            ));
        }
    }

    private List<SpecializationResponse> getSpecializationResponses(Locale locale, List<Specialization> specializations) {
        List<SpecializationResponse> responses = new ArrayList<>();
        String photo = baseUrl+defaultImage;
        for(Specialization specialization: specializations){
            SpecializationResponse data = new SpecializationResponse();
            if(specialization.getPhoto()!=null && !specialization.getPhoto().isEmpty()){
                String photoPath = baseUrl+"/uploaded_file/specialisation/"+specialization.getId()+"/"+specialization.getPhoto();
                data.setPhoto(photoPath);
            }else{
                data.setPhoto(photo);
            }

            if(locale.getLanguage().equals("en")){
                data.setName(specialization.getName());
            }else {data.setName(specialization.getNameSl());}
            data.setId(specialization.getId());

            responses.add(data);
        }
        return responses;
    }

    public ResponseEntity<?> getPaymentMethod(Locale locale) {

        List<PaymentMethodResponse.Option> currencyOptions = new ArrayList<>();
        for (Map.Entry<String, String> entry : paymentOptionConfig.getCurrency().entrySet()) {
            PaymentMethodResponse.Option option = new PaymentMethodResponse.Option();
            option.setValue(entry.getKey());
            option.setTitle(entry.getValue());
            currencyOptions.add(option);
        }

        List<PaymentMethodResponse.Option> paymentMethods = new ArrayList<>();
        for (Map.Entry<String, String> entry : paymentOptionConfig.getPaymentMethod().entrySet()) {
            PaymentMethodResponse.Option option = new PaymentMethodResponse.Option();
            option.setValue(entry.getKey());
            option.setTitle(entry.getValue());
            paymentMethods.add(option);
        }
        PaymentMethodResponse response = new PaymentMethodResponse();
        response.setCurrency_option(currencyOptions);
        response.setData(paymentMethods);
        response.setStatus(Constants.SUCCESS_CODE);
        response.setMessage(messageSource.getMessage(Constants.SUCCESS_MESSAGE,null,locale));

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    public List<PaymentMethodResponse.Option> getPaymentMethod() {
        List<PaymentMethodResponse.Option> currencyOptions = new ArrayList<>();
        for (Map.Entry<String, String> entry : paymentOptionConfig.getPaymentMethod().entrySet()) {
            PaymentMethodResponse.Option option = new PaymentMethodResponse.Option();
            option.setValue(entry.getKey());
            option.setTitle(entry.getValue());
            currencyOptions.add(option);
        }

        return currencyOptions;
    }

    public Consultation checkRealTimeBooking(Integer slotId, LocalDate date, Integer doctorId) {
        List<RequestType> requestTypes = List.of(RequestType.Inprocess,RequestType.Pending,RequestType.Book);
        List<Consultation> consultations = consultationRepository.findBySlotDateAndDoctorAndRequestType(slotId,date,doctorId,requestTypes);
        if(consultations.size()>0){
            return consultations.get(0);
        }
        return null;
    }

    public Consultation checkClientBooking(Integer slotId, LocalDate date, Integer userId) {
        List<RequestType> requestTypes = List.of(RequestType.Inprocess,RequestType.Pending,RequestType.Book);
        List<Consultation> consultations = consultationRepository.findBySlotDateAndPatientAndRequestType(slotId,date,userId,requestTypes);
        if(consultations.size()>0){
            return consultations.get(0);
        }
        return null;
    }

    public List<DoctorAvailability> nursesAssign(Integer slotId) {
        return doctorAvailabilityRepository.findBySlotIdAndUserType(slotId,UserType.Nurse);
    }

    public ReserveSlotDto getReservedSlot(int slotId, int doctorId, SlotMaster slotInfo, int numberSlotsToAllocate, int timeValue) {
        LocalTime time = slotInfo.getSlotStartTime();
        List<LocalTime> slot_start_time = new ArrayList<>();
        slot_start_time.add(slotInfo.getSlotStartTime());

        for (int i = 1; i < numberSlotsToAllocate; i++) {
            time = time.plusMinutes(timeValue);
            slot_start_time.add(time);
        }

        List<Integer> allocated_slots = slotMasterRepository.findBySlotTypeIdAndSlotDayAndSlotStartTimeIn(
                slotInfo.getSlotType().getId(),
                slotInfo.getSlotDay(),
                slot_start_time
        ).stream().map(SlotMaster::getSlotId).collect(Collectors.toList());

        ReserveSlotDto response = new ReserveSlotDto();
        response.setAllocated_slots(allocated_slots);
        response.setSlot_start_time(slot_start_time);
        return response;
    }

    public CouponCodeResponseDTO checkPromoCode(Integer userId, CouponCategory category, Float price, String couponCode,Locale locale) {
        CouponCodeResponseDTO response = new CouponCodeResponseDTO();
        DiscountDetailsDTO discountDetails = new DiscountDetailsDTO();

        // Simulate fetching Coupon from the database
        List<Coupon> checkCouponList = couponRepository.findByCouponCodeAndCategoryAndStatus(couponCode, category,1);
        Coupon checkCoupon = (checkCouponList.size()>0)?checkCouponList.get(0):null;
        if (checkCoupon==null) {
            response.setStatus("error");
            response.setMessage(messageSource.getMessage(Constants.COUPIN_CODE_INVALID,null,locale));
        } else if (checkCoupon.getNumberOfUsed() > checkCoupon.getOfferForNumberOfUsers()) {
            response.setStatus("error");
            response.setMessage(messageSource.getMessage(Constants.COUPIN_CODE_REACHED_LIMIT,null,locale));
        } else if (checkCoupon.getEndDate().isBefore(LocalDateTime.now())) {
            response.setStatus("error");
            response.setMessage(messageSource.getMessage(Constants.COUPIN_CODE_REACHED_LIMIT,null,locale));
        } else {
            //check if user avail this offer or not
            List<UsersUsedCouponCode> usersUsedCouponCode = usersUsedCouponCodeRepository.findByUserIdAndCouponId(userId,checkCoupon.getId());
            boolean hasUserUsedCoupon = usersUsedCouponCode.size()>0;

            if (hasUserUsedCoupon) {
                response.setStatus("info");
                response.setMessage(messageSource.getMessage(Constants.AVAILABLE_OFFER_MESSAGE,null,locale));
            } else {
                Float currentAmount = price;
                Float discountAmount = 0.0f;

                calculateDiscountAmount(checkCoupon, currentAmount);
                String discountAmountWithCurrency = formatDiscountAmount(discountAmount, currencySymbol);
                String discountAmountWithSlshCurrency = formatDiscountAmount(discountAmount * PaymentRate, currencySymbol);

                discountDetails.setDiscount_amount_with_currency(discountAmountWithCurrency);
                discountDetails.setDiscount_amount_with_usd_currency(discountAmountWithCurrency);
                discountDetails.setDiscount_amount_with_slsh_currency(discountAmountWithSlshCurrency);
                discountDetails.setAlert_msg_usd("process_payment_of " + discountAmountWithCurrency);
                discountDetails.setAlert_msg_slsh("process_payment_of " + discountAmountWithSlshCurrency);
                discountDetails.setDiscount_amount_slsh(discountAmount * PaymentRate);
                discountDetails.setDiscount_amount(discountAmount);
                discountDetails.setType(checkCoupon.getType());
                discountDetails.setCoupon_id(checkCoupon.getId());

                response.setStatus("success");
                response.setMessage(messageSource.getMessage(Constants.COUPON_CODE_SUCCESS_MESSAGE,null,locale));
                response.setData(discountDetails);

                //set UsersUsedCouponCode -> implies that user cannot be able to use this offer again
                UsersUsedCouponCode code = new UsersUsedCouponCode();
                code.setUserId(userId);
                code.setCouponId(checkCoupon.getId());
                code.setCreatedAt(LocalDateTime.now(ZoneId.of(zone)));

                usersUsedCouponCodeRepository.save(code);
            }
        }
        return response;
    }

    private double calculateDiscountAmount(Coupon checkCoupon, double currentAmount) {
        double discountAmount = 0;
        if (OfferType.FREE == checkCoupon.getType()){
            return discountAmount;
        }
        if (DiscountType.PERCENTAGE == checkCoupon.getDiscountType()) {
            discountAmount = (currentAmount * checkCoupon.getDiscountAmount()) / 100;
        } else {
            discountAmount = Math.max(0, currentAmount - checkCoupon.getDiscountAmount());
        }
        return discountAmount;
    }

    private String formatDiscountAmount(double amount, String currencySymbol) {
        return currencySymbol + String.format("%.2f", amount);
    }

    public ResponseEntity<?> thankYou(Locale locale, ThankYouRequest request) {
        DoctorDetailResponseDTO response = new DoctorDetailResponseDTO();
        DoctorDataDTO data = new DoctorDataDTO();

        Orders getType = ordersRepository.findById(request.getOrder_id()).orElse(null);

        if (getType != null && getType.getCaseId() != null) {
            Orders model = ordersRepository.findById(request.getOrder_id()).orElse(null);
            List<WalletTransaction> transactionList = walletTransactionRepository.findByOrderId(getType.getId());
            WalletTransaction transaction = null;
            if(!transactionList.isEmpty()){
                transaction = transactionList.get(0);
            }
            if (model != null) {
                String photo = getProfilePhoto(model);
                data.setFirst_name(model.getDoctorId().getFirstName() + " " + model.getDoctorId().getLastName());
                data.setConsultation_date(model.getCaseId().getConsultationDate());
                data.setTransaction_id((transaction!=null)?transaction.getTransactionId():"");
                data.setSlot_time(model.getCaseId().getSlotId().getSlotTime());
                data.setAmount(formatAmount(getType));
                data.setConsultation_type(model.getCaseId().getConsultationType());
                data.setProfile_photo(photo);
                data.setNurse(getNurseInfo(getType, model));
                data.setClinic(getClinicInformation(model.getId()));
                data.setConsult_type(formatConsultType(model.getCaseId().getConsultType()));

                return ResponseEntity.status(HttpStatus.OK).body(new Response(
                        Constants.SUCCESS_CODE,
                        Constants.SUCCESS_CODE,
                        messageSource.getMessage(Constants.DOCTOR_DETAILS_MSG,null,locale),
                        data
                ));
            } else {

                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new Response(
                        Constants.NO_CONTENT_FOUNT_CODE,
                        Constants.NO_CONTENT_FOUNT_CODE,
                        messageSource.getMessage(Constants.NO_RECORD_FOUND,null,locale)
                ));
            }
        } else {
            Orders model = ordersRepository.findById(request.getOrder_id()).orElse(null);
            List<WalletTransaction> transactionList = walletTransactionRepository.findByOrderId(getType.getId());
            WalletTransaction transaction = null;
            if(!transactionList.isEmpty()){
                transaction = transactionList.get(0);
            }
            if (model != null) {
                data.setPackage_name(model.getPackageId().getPackageName());
                data.setTransaction_id((transaction!=null)?transaction.getTransactionId():"");
                data.setSlot_time(model.getCaseId().getSlotId().getSlotTime());
                data.setAmount(formatAmount(getType));

                return ResponseEntity.status(HttpStatus.OK).body(new Response(
                        Constants.SUCCESS_CODE,
                        Constants.SUCCESS_CODE,
                        messageSource.getMessage(Constants.DOCTOR_DETAILS_MSG,null,locale),
                        data
                ));
            } else {

                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new Response(
                        Constants.NO_CONTENT_FOUNT_CODE,
                        Constants.NO_CONTENT_FOUNT_CODE,
                        messageSource.getMessage(Constants.NO_RECORD_FOUND,null,locale)
                ));
            }
        }
    }

    private String getProfilePhoto(Orders model) {
        String photoPath = baseUrl+"uploaded_file/UserProfile/"+model.getId()+"/"+model.getDoctorId().getProfilePicture(); // Construct photo path based on conditions
        return photoPath;
    }

    private String formatAmount(Orders getType) {
        String currency = getType.getCurrency() != null ? getType.getCurrency() : currencySymbolFdj;
        String amount = getType.getCurrencyAmount() != null ? currency + " " + getType.getCurrencyAmount() : currency + " " + getType.getAmount();
        return amount;
    }

    private NurseDto getNurseInfo(Orders orders, Orders model) {
        if(model.getCaseId()!=null && model.getCaseId().getAssignedTo()!=null){
            Users nurse = usersRepository.findById(model.getCaseId().getAssignedTo()).orElse(null);
            NurseDto dto = null;
            if(nurse!=null){
                dto = new NurseDto();
                String photoPath="";
                if(nurse.getProfilePicture()!=null && !nurse.getProfilePicture().isEmpty()){
                    photoPath = baseUrl+"uploaded_file/UserProfile/"+model.getId()+"/"+model.getDoctorId().getProfilePicture();
                }else{
                    photoPath = baseUrl+defaultImage;
                }
                dto.setProfile_picture(photoPath);
                dto.setName(nurse.getFirstName()+" "+nurse.getLastName());
                dto.setContact_number(nurse.getCountryCode()+""+nurse.getContactNumber());
            }
            return dto;
        }
        return null;
    }

    private String formatConsultType(String consultType) {
        if ("call".equalsIgnoreCase(consultType)) {
            return "video";
        }
        return consultType;
    }

    public List<DoctorRattingDTO> getDoctorByRatting(Locale locale) {
        List<DoctorRattingDTO> doctors = new ArrayList<>();
        String photo = baseUrl + defaultImage;

        List<Users> doctorsList = usersRepository.findActiveDoctorsWithVideoAndHospital();
        if (doctorsList != null && !doctorsList.isEmpty()) {
            for (Users doc : doctorsList) {
                Double rating = consultationRatingRepository.sumRatingsByDoctorId(doc.getUserId());
                Long ratingCount = consultationRatingRepository.countApprovedRatingsByDoctorId(doc.getUserId());

                Double finalCount = (ratingCount != null && ratingCount > 0) ? rating / ratingCount : 0;
                if (finalCount > 0) {
                    if (doc.getProfilePicture() != null && !doc.getProfilePicture().isEmpty()) {
                        photo = baseUrl + doc.getProfilePicture();
                    }

                    List<Charges> charges = chargesService.findByUserId(doc.getUserId());
                    Map<FeeType, String> consultationFees = new HashMap<>();
                    for (Charges charge : charges) {
                        consultationFees.put(charge.getFeeType(), currencySymbolFdj+" "+String.format("%.2f", charge.getFinalConsultationFees()));
                    }

                    DoctorRattingDTO doctorDTO = getDoctorRattingDTO(doc, photo, finalCount, consultationFees);

                    doctors.add(doctorDTO);
                }
            }

            if (doctors.isEmpty()) {
                doctors = getTopDoctors();
            }
        }
        return doctors;
    }

    private List<DoctorRattingDTO> getTopDoctors() {
        List<DoctorRattingDTO> doctors = new ArrayList<>();
        List<Integer> topDoctorIds = usersRepository.findTopDoctors();
        if (!topDoctorIds.isEmpty()) {
            List<Users> doctorsList = usersRepository.findDoctorsByIds(topDoctorIds);
            for (Users doc : doctorsList) {
                String photo = baseUrl + defaultImage;
                if (doc.getProfilePicture() != null && !doc.getProfilePicture().isEmpty()) {
                    photo = baseUrl + doc.getProfilePicture();
                }

                List<Charges> charges = chargesService.findByUserId(doc.getUserId());
                Map<FeeType, String> consultationFees = new HashMap<>();
                for (Charges charge : charges) {
                    consultationFees.put(charge.getFeeType(), currencySymbolFdj+" "+String.format("%.2f", charge.getFinalConsultationFees()));
                }

                DoctorRattingDTO doctorDTO = getDoctorRattingDTO(doc, photo, 0.0, consultationFees);

                doctors.add(doctorDTO);
            }
        }
        return doctors;
    }

    private static DoctorRattingDTO getDoctorRattingDTO(Users doc, String photo, double rating, Map<FeeType, String> consultationFees) {
        DoctorRattingDTO doctorDTO = new DoctorRattingDTO();
        doctorDTO.setId(doc.getUserId());
        doctorDTO.setFirst_name(doc.getFirstName());
        doctorDTO.setLast_name(doc.getLastName());
        doctorDTO.setHospital_id(doc.getHospitalId());
        doctorDTO.setHospital_name(doc.getClinicName());
        doctorDTO.setPhoto(photo);
        doctorDTO.setRating(rating);
        doctorDTO.setConsultation_fees(consultationFees);
        return doctorDTO;
    }

    public ResponseEntity<?> nearByDoctor(Locale locale, NearByDoctorRequest request) {
        List<NearByDoctorResponse> doctors = new ArrayList<>();
        String photo = baseUrl + defaultImage;

        if (request.getLatitude() != null && request.getLongitude() != null) {
            double radius = 6371; // Radius of Earth in km
            double maxLat = request.getLatitude() + Math.toDegrees(locationRadius / radius);
            double minLat = request.getLatitude() - Math.toDegrees(locationRadius / radius);
            double maxLng = request.getLongitude() + Math.toDegrees(locationRadius / radius / Math.cos(Math.toRadians(request.getLatitude())));
            double minLng = request.getLongitude() - Math.toDegrees(locationRadius / radius / Math.cos(Math.toRadians(request.getLatitude())));

            List<UserLocation> hospitalList = userLocationRepository.findWithinRadius(minLat,
                    maxLat, minLng, maxLng, request.getLatitude(), request.getLongitude(), locationRadius);
            if (hospitalList != null && !hospitalList.isEmpty()) {
                List<Integer> hospitalIds = new ArrayList<>();
                for (UserLocation address : hospitalList) {
                    hospitalIds.add(address.getUser().getUserId());
                }

                List<Users> doctorsList = usersRepository.findNearbyDoctors(hospitalIds);
                if (doctorsList != null && !doctorsList.isEmpty()) {
                    for (Users doc : doctorsList) {
                        if (doc.getProfilePicture() != null && !doc.getProfilePicture().isEmpty()) {
                            photo = baseUrl + doc.getProfilePicture();
                        }

                        List<Charges> charges = chargesService.findByUserId(doc.getUserId());
                        Map<FeeType, String> consultationFees = new HashMap<>();
                        for (Charges charge : charges) {
                            consultationFees.put(charge.getFeeType(), currencySymbolFdj+" "+String.format("%.2f", charge.getFinalConsultationFees()));
                        }

                        NearByDoctorResponse doctorDTO = new NearByDoctorResponse();
                        doctorDTO.setId(doc.getUserId());
                        doctorDTO.setFirst_name(doc.getFirstName());
                        doctorDTO.setLast_name(doc.getLastName());
                        doctorDTO.setHospital_id(doc.getHospitalId());
                        doctorDTO.setHospital_name(doc.getClinicName());
                        doctorDTO.setPhoto(photo);
                        doctorDTO.setConsultation_fees(consultationFees);

                        doctors.add(doctorDTO);
                    }
                    return ResponseEntity.status(HttpStatus.OK).body(new Response(
                            Constants.SUCCESS_CODE,
                            Constants.SUCCESS_CODE,
                            messageSource.getMessage(Constants.NEARBY_DOCTOR_RETRIEVED,null,locale),
                            doctors
                    ));
                } else {
                    return ResponseEntity.status(HttpStatus.OK).body(new Response(
                            Constants.SUCCESS_CODE,
                            Constants.SUCCESS_CODE,
                            messageSource.getMessage(Constants.NEARBY_DOCTOR_LIST_FOUND,null,locale),
                            new ArrayList<>()
                    ));
                }
            } else {
                return ResponseEntity.status(HttpStatus.OK).body(new Response(
                        Constants.SUCCESS_CODE,
                        Constants.SUCCESS_CODE,
                        messageSource.getMessage(Constants.HOSPITAL_DATA_NOT_FOUND,null,locale),
                        new ArrayList<>()
                ));
            }
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new Response(
                    Constants.NO_RECORD_FOUND_CODE,
                    Constants.NO_RECORD_FOUND_CODE,
                    messageSource.getMessage(Constants.LAT_LONG_NOT_FOUND,null,locale),
                    new ArrayList<>()
            ));
        }
    }

    public ResponseEntity<?> getAllCategoriesList(Locale locale) {
        List<CategoriesDto> list = new ArrayList<>();
        List<HealthTipCategoryMaster> categoryMasters = healthTipCategoryMasterRepository.findByStatus(Status.A);
        for(HealthTipCategoryMaster master:categoryMasters){
            String name = locale.getLanguage().equalsIgnoreCase("en")? master.getName() : master.getNameSl();
            list.add(new CategoriesDto(master.getCategoryId(),name));
        }
        if(list.size()>0){
            return ResponseEntity.status(HttpStatus.OK).body(new Response(
                    Constants.SUCCESS_CODE,
                    Constants.SUCCESS_CODE,
                    messageSource.getMessage(Constants.CATEGORIES_LIST_RETRIEVED,null,locale),
                    list
            ));
        }else {
            return ResponseEntity.status(HttpStatus.OK).body(new Response(
                    Constants.SUCCESS_CODE,
                    Constants.SUCCESS_CODE,
                    messageSource.getMessage(Constants.NO_CATEGORIES_LIST_FOUND,null,locale),
                    new ArrayList<>()
            ));
        }
    }

    public List<LabCategoryMaster> getAssignedLabsCategory() {
        return labCategoryMasterRepository.findActiveLabCategoryByLabPrice(CategoryStatus.Active);
    }

    public List<GetLabDto> getLabInfo(List<Integer> labcatIds) {
        List<GetLabDto> response = new ArrayList<>();
        List<LabPrice> labPrices = labPriceRepository.findBySubCatIdAndUserTypeAndStatus(labcatIds,UserType.Lab,"A");
        for(LabPrice price:labPrices){
            int lab_id = price.getLabUser().getUserId();
            Users labUser = usersRepository.findById(lab_id).orElse(null);
            if(labUser != null){
                GetLabDto temp = new GetLabDto();
                temp.setUser_id(lab_id);
                temp.setClinic_name(labUser.getClinicName());
                response.add(temp);
            }
        }
        return response;
    }

    public BillInfoDto getBillInfo(Integer labId, List<Integer> reportId, String collectionMode, String currencyOption, Locale locale) {
        Integer paymentRate = 1;
        String currencySym = currencySymbolFdj; // Adjust this to your actual symbol
        if (currencyOption!=null && currencyOption.equalsIgnoreCase("slsh")) {
            GlobalConfiguration configuration = globalConfigurationRepository.findByKey("WAAFI_PAYMENT_RATE");
            paymentRate = Integer.parseInt(configuration.getValue());
            currencySym = currencySymbolSLSH; // Adjust this to your actual symbol
        }

        Users lab = usersRepository.findById(labId).orElse(new Users());
        List<LabDetailDto> labDetailList = new ArrayList<>();
        LabDetailDto labDetail = new LabDetailDto(String.valueOf(labId), lab.getClinicName(), lab.getHospitalAddress());
        labDetailList.add(labDetail);

        String labVisitOnly = "";
        Integer diagnosisCost = 0;
        Boolean isHomeVisit = true;
        String onlyLabVisitMsg = null;
        String onlyLabVisitMsgApi = null;
        List<ReportDto> reportNames = new ArrayList<>();
        if (!reportId.isEmpty()) {
            for (Integer subCatId : reportId) {
                LabPrice labPrice = labPriceRepository.findByLabIdAndSubCatId(labId, subCatId);
                if(labPrice != null) diagnosisCost += (int)labPrice.getLabPrice().floatValue();
            }
            diagnosisCost *= paymentRate;
            List<ReportSubCatDto> dtos = checkHomeVisit(reportId);
            for(ReportSubCatDto d : dtos){
                ReportDto r = new ReportDto();
                r.setKey(d.getSub_cat_id());
                r.setValue(d.getSub_cat_name());

                reportNames.add(r);
            }
            labVisitOnly = dtos.stream()
                    .map(ReportSubCatDto::getSub_cat_name)
                    .collect(Collectors.joining(","));

            if (!labVisitOnly.isEmpty()) {
                isHomeVisit = false;
                onlyLabVisitMsg = messageService.gettingMessages("only_lab_visit_msg", labVisitOnly, locale);
                onlyLabVisitMsgApi = onlyLabVisitMsg;
            }
        }

        Integer collectionCharge = 0;
        if ("Home_Visit".equalsIgnoreCase(collectionMode)) {
            collectionCharge = homeChargeCollection * paymentRate;
        } else if ("Lab_Visit".equalsIgnoreCase(collectionMode)) {
            collectionCharge = 0;
        } else {
            collectionCharge = (onlyLabVisitMsg != null) ? 0 : homeChargeCollection * paymentRate;
        }

        float totalPrice = (float)diagnosisCost + (float) collectionCharge;

        BillInfoDto response = new BillInfoDto(
                lab.getClinicName(),
                labDetailList,
                labVisitOnly,
                isHomeVisit,
                onlyLabVisitMsg,
                onlyLabVisitMsgApi,
                currencySym + " " + diagnosisCost,
                currencySym + " " + collectionCharge,
                currencySym + " " +totalPrice,
                new HashMap<>(),
                reportNames,
                diagnosisCost,
                collectionCharge,
                (int)totalPrice
        );

        return response;
    }

    public List<ReportSubCatDto> checkHomeVisit(List<Integer> reportId) {
        List<LabSubCategoryMaster> subCategorys = labSubCategoryMasterRepository
                .findByIdinListAndHomeConsultant(reportId,YesNo.No);
        List<ReportSubCatDto> response = new ArrayList<>();
        for(LabSubCategoryMaster labSubCategoryMaster:subCategorys){
            response.add(new ReportSubCatDto(labSubCategoryMaster.getSubCatId(),labSubCategoryMaster.getSubCatName()));
        }
        return response;
    }

    public String getTotalConsultationAmount(Integer caseId) {
        Orders orders = ordersRepository.findByCaseId(caseId);
        if(orders != null) {
            String currency = (orders.getCurrency() != null && !orders.getCurrency().isEmpty()) ? orders.getCurrency() : currencySymbolFdj;
            Float amount = (orders.getCurrencyAmount() != null) ? orders.getCurrencyAmount() : orders.getAmount();
            return currency + " " + amount;
        }
        return null;
    }

    public List<AvailableNursesMapDto> availableNursesMap() {

        String query = """
            SELECT
                msisdn AS number,
                name AS nurseName,
                last_location_update_time AS time,
                last_latitude AS lati,
                last_longitude AS longi
            FROM
                nurses
            WHERE
                last_latitude IS NOT NULL AND
                DATE_FORMAT(last_location_update_time, "%Y-%m-%d") = CURRENT_DATE AND
                state IN ("active", "to-activate")
            ORDER BY
                last_location_update_time DESC
        """;

        List<AvailableNursesMapDto> nurseList = new ArrayList<>();

        try{
            Connection connection = DriverManager.getConnection(nurseURL, nurseUserName, nursePassword);
            PreparedStatement statement = connection.prepareStatement(query);
            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                AvailableNursesMapDto nurse = new AvailableNursesMapDto();
                nurse.setNumber(resultSet.getString("number"));
                nurse.setNurseName(resultSet.getString("nurseName"));
                nurse.setTime(resultSet.getTime("time").toLocalTime());
                nurse.setLati(resultSet.getFloat("lati"));
                nurse.setLongi(resultSet.getFloat("longi"));
                nurseList.add(nurse);
            }
            return nurseList;
        }catch (Exception e){
            e.printStackTrace();
            log.error("Error while fetching nurse details from another database : {} ", e);
            return null;
        }
    }

    //NOTE : Not in Baannoo check this function
    public void sendNurseOnDemandMsg(SendNurseOnDemandMsgRequest request, String scenario, UserType userType,Locale locale) {
        Users userModel = null;
        PartnerNurse nurseModel = null;
        if(request.getPatient_id()!=null && request.getPatient_id()!=0){
            userModel = usersRepository.findById(request.getPatient_id()).orElse(null);
        }
        if(request.getNurse_id()!=null && request.getNurse_id()!=0){
            nurseModel = partnerNurseRepository.findById(request.getNurse_id()).orElse(null);
        }
        Locale prevLang  = locale;

        String patientName = (userModel != null) ? userModel.getFirstName() + " " + userModel.getLastName():"";
        String nurseName = (nurseModel != null) ? nurseModel.getName() : "";

        if (userType == UserType.Patient && userModel.getNotificationLanguage() != null && !userModel.getNotificationLanguage().isEmpty()) {
            if (userModel.getNotificationLanguage().equalsIgnoreCase("sl") || userModel.getNotificationLanguage().equalsIgnoreCase("so") )
                locale = new Locale("so");
            else locale = Locale.ENGLISH;
        }else locale = new Locale("so");

        if (userType == UserType.NursePartner) {
            locale = new Locale("so");
        }

        String getReminderMsgData = scenario;

        String getReminderMsg = messageSource.getMessage(scenario, null, locale);
        NotificationData notificationData = new NotificationData();
        SmsData smsData = new SmsData();

        //Patient
        if (scenario.equalsIgnoreCase(Constants.NURSE_NOT_FOUND_PATIENT_NOD)) {

            getReminderMsg = getReminderMsgData.replace("{{PATIENT_NAME}}", patientName);

            notificationData.setFromId(request.getNurse_id());
            notificationData.setToId(request.getPatient_id());
            notificationData.setCaseId(request.getId());

            smsData.setFromId(request.getNurse_id());
            smsData.setToId(request.getPatient_id());
            smsData.setSmsFor(scenario);
            smsData.setCaseId(request.getId());
            smsData.setUserType(UserType.Patient.name());
        }

        //Agent Order failed message
        if(scenario.equalsIgnoreCase(Constants.AGENT_NOTIFICATION_FOR_FAILED_NOD)){
            List<Users> agents = usersRepository.findByStatusAndTypeOrderByAsc(Status.A.name(), UserType.Agentuser);
            if(!agents.isEmpty()){
                for(Users agent : agents){
                    getReminderMsg = getReminderMsgData.replace("{{PATIENT_NAME}}", patientName)
                            .replace("{{PATIENT_MOBILE}}", userModel.getContactNumber())
                            .replace("{{STATUS}}", request.getStatus());

                    notificationData.setFromId(request.getPatient_id());
                    notificationData.setToId(agent.getUserId());

                    smsData.setFromId(request.getPatient_id());
                    smsData.setToId(agent.getUserId());
                    smsData.setSmsFor(scenario);
                    smsData.setUserType("AGENTUSER");
                }
            }
        }

        //CANCEL_SMS_PATIENT_NOD
        if(scenario.equalsIgnoreCase(Constants.CANCEL_SMS_PATIENT_NOD)){
            getReminderMsg = getReminderMsgData.replace("{{PATIENT_NAME}}", patientName)
                    .replace("{{TRIP_ID}}", request.getTripId());

            notificationData.setFromId(request.getNurse_id());
            notificationData.setToId(request.getPatient_id());

            smsData.setFromId(request.getNurse_id());
            smsData.setToId(request.getPatient_id());
            smsData.setSmsFor(scenario);
            smsData.setUserType("PATIENT");
        }

        //CANCEL_SMS_NURSE_NOD -> NURSE
        if(scenario.equalsIgnoreCase(Constants.CANCEL_SMS_NURSE_NOD)){
            getReminderMsg = getReminderMsgData.replace("{{PATIENT_NAME}}", patientName)
                    .replace("{{NURSE_NAME}}", nurseName)
                    .replace("{{TRIP_ID}}", request.getTripId());

            notificationData.setToId(request.getNurse_id());
            notificationData.setFromId(request.getPatient_id());
            notificationData.setCaseId(request.getId());

            smsData.setToId(request.getNurse_id());
            smsData.setFromId(request.getPatient_id());
            smsData.setSmsFor(scenario);
            smsData.setCaseId(request.getId());
            smsData.setUserType("NURSEPARTNER");
        }

        //ORDER_CANCEL_BY_PATIENT_NURSE_NOD -> NURSE
        if(scenario.equalsIgnoreCase(Constants.ORDER_CANCEL_BY_PATIENT_NURSE_NOD)){
            getReminderMsg = getReminderMsgData.replace("{{PATIENT_NAME}}", patientName)
                    .replace("{{NURSE_NAME}}", nurseName)
                    .replace("{{TRIP_ID}}", request.getTripId()
                    .replace("{{AMOUNT}}", request.getZaadNumber()));

            notificationData.setToId(request.getNurse_id());
            notificationData.setFromId(request.getPatient_id());
            notificationData.setCaseId(request.getId());
            notificationData.setType(NotificationType.Nod);

            smsData.setToId(request.getNurse_id());
            smsData.setFromId(request.getPatient_id());
            smsData.setSmsFor(scenario);
            smsData.setCaseId(request.getId());
            smsData.setUserType("NURSEPARTNER");
        }

        //Patient Payment conformation
        if(scenario.equalsIgnoreCase(Constants.PAYMENT_CONFIRM_PATIENT_NOD)){
            getReminderMsg = getReminderMsgData.replace("{{PATIENT_NAME}}", patientName)
                    .replace("{{NURSE_NAME}}", nurseName)
                    .replace("{{TRIP_ID}}", request.getTripId()
                            .replace("{{AMOUNT}}", request.getZaadNumber())
                            .replace("{{CONTACT_NUMBER}}", nurseModel.getContactNumber())
                            .replace("{{DATE}}", String.valueOf(LocalDate.now(ZoneId.of(zone)))));

            notificationData.setFromId(request.getNurse_id());
            notificationData.setToId(request.getPatient_id());
            notificationData.setCaseId(request.getId());

            smsData.setFromId(request.getNurse_id());
            smsData.setToId(request.getPatient_id());
            smsData.setSmsFor(scenario);
            smsData.setCaseId(request.getId());
            smsData.setUserType("PATIENT");
        }

        //Nurse
        if(scenario.equalsIgnoreCase(Constants.CONFIRM_ONDEMAND_ORDER_NURSE)){
            getReminderMsg = getReminderMsgData.replace("{{PATIENT_NAME}}", patientName)
                    .replace("{{NURSE_NAME}}", nurseName)
                            .replace("{{CONTACT_NUMBER}}", userModel.getContactNumber())
                            .replace("{{DATE}}", String.valueOf(LocalDate.now(ZoneId.of(zone))));

            notificationData.setToId(request.getNurse_id());
            notificationData.setFromId(request.getPatient_id());
            notificationData.setCaseId(request.getId());
            notificationData.setType(NotificationType.Nod);

            smsData.setToId(request.getNurse_id());
            smsData.setFromId(request.getPatient_id());
            smsData.setSmsFor(scenario);
            smsData.setCaseId(request.getId());
            smsData.setUserType("NURSEPARTNER");
        }

        //Agent Order message
        if(scenario.equalsIgnoreCase(Constants.ORDER_NOTICE_AGENT_NOD)){
            List<Users> agents = usersRepository.findByStatusAndTypeOrderByAsc(Status.A.name(), UserType.Agentuser);
            if(!agents.isEmpty()){
                for(Users agent : agents){
                    getReminderMsg = getReminderMsgData.replace("{{PATIENT_NAME}}", patientName)
                            .replace("{{NURSE_NAME}}", nurseName)
                            .replace("{{DATE}}", String.valueOf(LocalDate.now(ZoneId.of(zone)))
                                    .replace("{{LAT}}", request.getLat())
                                    .replace("{{LONG}}", request.getLongi()));

                    notificationData.setToId(agent.getUserId());
                    notificationData.setFromId(request.getNurse_id());
                    notificationData.setCaseId(request.getId());

                    smsData.setFromId(request.getNurse_id());
                    smsData.setToId(agent.getUserId());
                    smsData.setSmsFor(scenario);
                    smsData.setCaseId(request.getId());
                    smsData.setUserType("AGENTUSER");
                }
            }

        }

        Notification notification = createNotification(notificationData, getReminderMsg);
        SmsLog smsLog = creatSmsLog(smsData, getReminderMsg);
        locale = prevLang;
        return;

    }

    public SmsLog creatSmsLog(SmsData smsData, String getReminderMsg) {
        SmsLog smsLog = new SmsLog();
        smsLog.setToId(smsData.getToId());
        smsLog.setFromId(smsData.getFromId());
        smsLog.setLabOrdersId(smsData.getLabOrderId());
        smsLog.setConsultDate(smsData.getConsultantTime());
        smsLog.setCaseId(smsData.getCaseId());
        smsLog.setSmsFor(smsData.getSmsFor());
        smsLog.setMsg(getReminderMsg);
        smsLog.setIsSent(0);
        smsLog.setUserType(smsLog.getUserType());

        return smsLogRepository.save(smsLog);
    }

    public Notification createNotification(NotificationData notificationData, String getReminderMsg) {
        Notification notification = new Notification();
        notification.setFromId(notificationData.getFromId());
        notification.setToId(notification.getToId());
        notification.setMessage(getReminderMsg);
        notification.setUrl(notificationData.getUrl());
        notification.setCaseId(notification.getCaseId());
        notification.setCreatedAt(LocalDateTime.now(ZoneId.of(zone)));
        notification.setIsRead("0");
        notification.setType(notificationData.getType() == null ? NotificationType.Consult : notificationData.getType());

        //push notification :TODO
        return notificationRepository.save(notification);


    }

    public void confirmAck(NodAckRequest data) {
        String type = data.getType();
        String searchId = data.getSearch_id();
        int reqId = data.getReqId();

        try {
            if ("service_request".equals(type) && reqId == 2) {
                NurseServiceState stateModel = nurseServiceStateRepository.findBySearchId(searchId);
                stateModel.setConfirmAck("1");

                nurseServiceStateRepository.save(stateModel);
                //TODO : need to check the log flow again
//            createTransactionLog("NursePartnerService", "confirmAck", "NOD web acknowledgement.", "NURSEWEBACK", data.toString());
            }
        } catch (Exception e) {
            e.printStackTrace();
            log.error("Error in nod acknowledgment : {}", e);
        }
    }

    public void createTransactionLog(String controllerName, String methodName, String returnType, String data, String handlingType) {
        String log = generateLogMessage(controllerName, methodName, "OUTPUT", returnType, data, handlingType);
        writeLogToFile(log);
    }

    private String generateLogMessage(String controllerName, String methodName, String transType, String returnType, String data, String handlingType) {
        LocalDateTime dateTime = LocalDateTime.now(ZoneId.of(zone));
        String date = dateTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) + dateTime.format(DateTimeFormatter.ofPattern("XXX"));

        return String.format("%s [%s] [TYPE : %s] [METHOD NAME : %s] %s: %s : %s%n",
                handlingType, date, transType, methodName, controllerName, returnType, data);
    }

    private void writeLogToFile(String log) {
        File logDir = new File(logPath + "/logs");
        if (!logDir.exists()) {
            logDir.mkdirs();
        }
        File logFile = new File(logDir, "transaction.log");
        try (FileWriter writer = new FileWriter(logFile, true)) {
            writer.write(log);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Float getSlshAmount(Float finalConsultationFees) {
        GlobalConfiguration globalConfiguration = globalConfigurationRepository.findByKey("WAAFI_PAYMENT_RATE");
        return finalConsultationFees * Float.valueOf(globalConfiguration.getValue());
    }

    public WalletTransaction getWalletBalance(Integer patientId) {
        List<WalletTransaction> walletTransaction = walletTransactionRepository.findByPatientIdDesc(patientId);
        if(!walletTransaction.isEmpty() && walletTransaction.size()>0){
            return walletTransaction.get(0);
        }
        return null;
    }

    public Integer createTransaction(WalletTransaction userTransaction, UserType patient, String transactionId,String paymentMethod) {
        Users systemUsers = usersRepository.findById(SystemUserId).orElse(null);
        if (transactionId == null) {
            transactionId = String.valueOf(System.currentTimeMillis());
        }

        WalletTransaction walletBalance = null;
        if (userTransaction.getPatientId() != null) {
            walletBalance = getWalletBalance(userTransaction.getPatientId().getUserId());
        }
        WalletTransaction sysWalletBalance = getWalletBalance(systemUsers.getUserId());

        WalletTransaction walletModel = new WalletTransaction();
        walletModel.setAmount(userTransaction.getAmount());
        walletModel.setTransactionId(transactionId);
        walletModel.setPaymentMethod(paymentMethod != null ? paymentMethod : currencySymbolFdj);
        walletModel.setPaymentGatewayType(userTransaction.getPaymentGatewayType());
        walletModel.setTransactionType(userTransaction.getTransactionType());
        walletModel.setTransactionDate(LocalDateTime.now());
        walletModel.setTransactionStatus(userTransaction.getTransactionStatus());
        walletModel.setRefTransactionId(userTransaction.getRefTransactionId());
        walletModel.setServiceType(userTransaction.getServiceType());
        walletModel.setIsDebitCredit(userTransaction.getIsDebitCredit());
        walletModel.setReferenceNumber(userTransaction.getReferenceNumber());
        walletModel.setIssuerTransactionId(userTransaction.getIssuerTransactionId());
        walletModel.setPatientId(userTransaction.getPatientId());
        walletModel.setOrderId(userTransaction.getOrderId());
        walletModel.setCreatedAt(LocalDateTime.now());
        walletModel.setPaymentNumber(userTransaction.getPaymentNumber());

        if (patient == UserType.Patient && userTransaction.getIsDebitCredit().equals("CREDIT")) {
            if (walletBalance != null) {
                walletModel.setPreviousBalance(walletBalance.getCurrentBalance());
                walletModel.setCurrentBalance(walletBalance.getCurrentBalance() + userTransaction.getAmount());
            } else {
                walletModel.setPreviousBalance(userTransaction.getPatientId().getTotalMoney());
                walletModel.setCurrentBalance(userTransaction.getAmount());
            }
            walletModel.setPayerId(userTransaction.getPatientId().getUserId());
            walletModel.setPayerMobile(userTransaction.getPayerMobile());
            walletModel.setPayeeId(SystemUserId);
            walletModel.setPayeeMobile(systemUsers.getContactNumber());
        }

        if (patient == UserType.PATIENT && userTransaction.getIsDebitCredit().equals("DEBIT")) {
            Float balance = walletBalance != null ? walletBalance.getCurrentBalance() : userTransaction.getPatientId().getTotalMoney();
            walletModel.setPreviousBalance(balance);
            walletModel.setCurrentBalance(balance - userTransaction.getAmount());
            walletModel.setPayerId(userTransaction.getPatientId().getUserId());
            walletModel.setPayerMobile(userTransaction.getPayerMobile());
            walletModel.setPayeeId(SystemUserId);
            walletModel.setPayeeMobile(systemUsers.getContactNumber());
        }

        if (patient == UserType.SYSTEM && userTransaction.getIsDebitCredit().equals("CREDIT")) {
            walletModel.setPatientId(systemUsers);
            walletModel.setPayerId(systemUsers.getUserId());
            walletModel.setPayerMobile(systemUsers.getContactNumber());
            walletModel.setPayeeId(userTransaction.getPayeeId());
            walletModel.setPayeeMobile(userTransaction.getPayeeMobile());
            if (sysWalletBalance != null) {
                walletModel.setPreviousBalance(sysWalletBalance.getCurrentBalance());
                walletModel.setCurrentBalance(sysWalletBalance.getCurrentBalance() + userTransaction.getAmount());
            } else {
                walletModel.setPreviousBalance(0.0f);
                walletModel.setCurrentBalance(userTransaction.getAmount());
            }
        }

        if (patient == UserType.SYSTEM && userTransaction.getIsDebitCredit().equals("DEBIT")) {
            walletModel.setPatientId(systemUsers);
            walletModel.setPayerId(systemUsers.getUserId());
            walletModel.setPayerMobile(systemUsers.getContactNumber());
            walletModel.setPayeeId(userTransaction.getPayeeId());
            walletModel.setPayeeMobile(userTransaction.getPayeeMobile());
            if (sysWalletBalance != null) {
                walletModel.setPreviousBalance(sysWalletBalance.getCurrentBalance());
                walletModel.setCurrentBalance(sysWalletBalance.getCurrentBalance() - userTransaction.getAmount());
            } else {
                walletModel.setPreviousBalance(0.0f);
                walletModel.setCurrentBalance(userTransaction.getAmount());
            }
        }

        // Additional user type checks (e.g., NURSEPARTNER, LAB) can be added similarly

        walletModel = walletTransactionRepository.save(walletModel);
        return walletModel.getId();

    }



    public void updateSystemUserWallet(Float finalConsultationFees,String type) {
        Users users = usersRepository.findById(SystemUserId).orElse(null);
        if(users!=null){
            if(type!=null && type.equalsIgnoreCase("sub")){
                users.setTotalMoney(users.getTotalMoney() - finalConsultationFees);
            }else{
                users.setTotalMoney(users.getTotalMoney() + finalConsultationFees);
            }
            usersRepository.save(users);
        }
    }

    public void sendHealthTipsMsg(Users model, String healthtipsSupscriptionConfirmation, String patient) {

    }

    public NurseAvailability checkNursesAvailable(Integer slotId, List<Integer> reservedSlotIds, Integer numberOfSlots, LocalDate consultationDate) {
        NurseAvailability response = new NurseAvailability();
        Map<Integer,Long> countNurse = new HashMap<>();
        List<Integer> nursesId = new ArrayList<>();

        Long checkReservedSlots = homecareReservedSlotRepository.countBySlotIdAndConsultDate(slotId, consultationDate);

        if (checkReservedSlots == 0) {
            List<Users> availableNurses = usersRepository.findByType(UserType.Nurse);
            for (Users nurse : availableNurses) {
                Long nursesCount = doctorAvailabilityRepository.countBySlotIdAndDoctorId(slotId, nurse.getUserId());
                countNurse.put(nurse.getUserId(),nursesCount);
            }
        }

        if (reservedSlotIds != null) {
            String status = countNurse.keySet().stream().anyMatch(reservedSlotIds::contains) ? "available" : "not_available";

            if ("available".equals(status)) {
                for (Integer id : countNurse.keySet()) {
                    if (reservedSlotIds.contains(id)) {
                        nursesId.add(id);
                    }
                }
                int nurseId = nursesId.get(new Random().nextInt(nursesId.size()));
                response.setNurse_id(nurseId);
                response.setStatus("available");
            } else {
                response.setNurse_id(0);
                response.setStatus("not_available");
            }
        } else {
            response.setNurse_id(0);
            response.setStatus("not_available");
        }

        return response;
    }

    public Boolean checkDoctorAvailability(SlotMaster slotInfo, Integer doctorId, Integer numberSlotsToAllocate, List<Integer> allocatedSlots, LocalDate consultationDate) {
        Long doctorSlotAvailableCount = doctorAvailabilityRepository.countBySlotTypeIdAndSlotIdAndDoctorId(slotInfo.getSlotType().getId(), allocatedSlots, doctorId);

        if (doctorSlotAvailableCount != Long.valueOf(numberSlotsToAllocate)) {
            return false;
        } else {
            Long slotListingCount = consultationRepository.countByRequestTypeAndSlotIdAndDoctorIdAndConsultationDate(
                    Arrays.asList(RequestType.Inprocess, RequestType.Pending, RequestType.Book),
                    allocatedSlots,
                    doctorId,
                    consultationDate
            );

            return slotListingCount == 0;
        }
    }

    public void exportReports(List<HealthTip> healthTips, String filePath) {
        // NOTE-TODO make this fucntion to creaet a csv file on this filepath which include file name too
        // NOT IN USE
        /*$file = ExportReport::exportReportsAPI($query, $fileName);
                            $arr = [
                                'file_url' => Yii::$app->params['BASE_URL'] . 'export_csv/' . $file_name,
                                'file_path' => $file
                            ];

                             public static function exportReportsAPI($sql, $file){
        $db = Yii::$app->db;

        $dbName = ExportReport::getDsnAttribute('dbname', $db->dsn);
        $host = ExportReport::getDsnAttribute('host', $db->dsn);
        $userName = Yii::$app->db->username;
        $password = Yii::$app->db->password;
        $cmd = "mysql -u " . $userName . " -p" . $password . " --database " . $dbName . " -h " . $host . " --default-character-set=utf8 -e '" . $sql . "' | sed \"s/^//;s/\t/,/g;s/$//\" >" . $file;

        shell_exec($cmd);
        if (file_exists($file)) {
            return $file;
        }
        exit;
    }
        * */
    }

    public PaymentServiceTypeResponse getPaymentServiceType(Locale locale) {
        PaymentServiceTypeResponse response = new PaymentServiceTypeResponse();

        response.setConsultation(messageSource.getMessage(Constants.WALLET_CONSULTATION,null,locale));
        response.setLab(messageSource.getMessage(Constants.WALLET_LAB,null,locale));
        response.setHealthtip(messageSource.getMessage(Constants.WALLET_HEALTHTIP,null,locale));
        response.setLoad_wallet_balance(messageSource.getMessage(Constants.WALLET_LOAD_BALANCE,null,locale));
        response.setPharmacy(messageSource.getMessage(Constants.WALLET_PHARMACY,null,locale));
        response.setNurse_on_demand(messageSource.getMessage(Constants.NURSE_ON_DEMAND,null,locale));
        response.setLab_cart(messageSource.getMessage(Constants.LAB_CART,null,locale));

        return response;
    }

    public void sendConsultationMsg(Consultation consultation, String cancleConsultRequestFromPatient, UserType patient) {
        String message = "";
        String to = "";
        if(cancleConsultRequestFromPatient.equalsIgnoreCase("CANCLE_CONSULT_REQUEST_FROM_PATIENT")){
            message = languageService.gettingMessages("CANCLE_CONSULT_REQUEST_FROM_PATIENT",
                    consultation.getDoctorId().getFirstName()+" "+consultation.getDoctorId().getLastName(),
                    consultation.getConsultationDate()+" "+consultation.getSlotId().getSlotTime()
            );
            to = consultation.getPatientId().getContactNumber();
        }else if(cancleConsultRequestFromPatient.equalsIgnoreCase("CANCLE_CONSULT_REQUEST")){
            message = languageService.gettingMessages("CANCLE_CONSULT_REQUEST_FROM_PATIENT",
                    consultation.getDoctorId().getFirstName()+" "+consultation.getDoctorId().getLastName(),
                    consultation.getConsultationDate()+" "+consultation.getSlotId().getSlotTime()
            );
            to = consultation.getDoctorId().getContactNumber();
        }else if(cancleConsultRequestFromPatient.equalsIgnoreCase("CANCLE_NOTIFICATION_HOSPITAL")){
            String hospitalName = (consultation.getDoctorId().getFirstName()!=null && !consultation.getDoctorId().getFirstName().isEmpty())?
                    consultation.getDoctorId().getFirstName()+" "+consultation.getDoctorId().getLastName():consultation.getDoctorId().getClinicName();

            message = languageService.gettingMessages("CANCLE_NOTIFICATION_HOSPITAL",hospitalName,
                    consultation.getPatientId().getFirstName()+" "+consultation.getPatientId().getLastName(),
                    consultation.getDoctorId().getFirstName()+" "+consultation.getDoctorId().getLastName(),
                    consultation.getConsultationDate()+" "+consultation.getSlotId().getSlotTime()
            );
            Users users = usersRepository.findById(consultation.getDoctorId().getHospitalId()).orElse(null);
            if(users!=null){
                to = users.getContactNumber();
            }
        }
        sdfsmsService.sendOTPSMS(to,message);
    }

    public CompleteAndPendingReportsDto getCompleteAndPendingReports(Integer caseId) {
        List<LabConsultation> labConsultations = labConsultationRepository.findByCaseId(caseId);
        CompleteAndPendingReportsDto response = new CompleteAndPendingReportsDto();
        int completed=0;
        for(LabConsultation temp:labConsultations){
            if(temp.getLabOrdersId()!=null){
                completed++;
            }
        }
        response.setCompleted_report(completed);
        response.setPending_report(labConsultations.size() - completed);

        return response;
    }

    public void addUserWalletBalance(Integer transactionId, Users patient, UserType userType,
                                     String isDebitCredit, Float amount) {

        String CREDIT = "CREDIT";
        String DEBIT = "DEBIT";
        String NURSEPARTNER = "NURSEPARTNER";

        Float balanceCredit = null;
        Float balanceDebit = null;
        Float previousBalance = 0f;
        Float balance = 0f;

        Wallet walletModel = new Wallet();

        List<Wallet> transList = walletRepository.findLastTransacton(patient.getUserId());

        Wallet trans = null;
        for(Wallet w:transList){
            trans = w;
        }
        Float exBalance = trans != null ? trans.getBalance() : patient.getTotalMoney();
        Float exPrevBalance = trans != null ? trans.getPreviousBalance() : patient.getTotalMoney();

        if (CREDIT.equals(isDebitCredit)) {
            balanceCredit = amount;
            balance = exBalance + amount;
            previousBalance = exBalance;
        } else if (DEBIT.equals(isDebitCredit)) {
            balanceDebit = amount;
            balance = exBalance - amount;
            previousBalance = exBalance;
        }

        walletModel.setWalletId(patient.getWalletId());
        walletModel.setWalletNumber(Integer.parseInt(patient.getContactNumber()));
        walletModel.setTransactionId(transactionId);
        walletModel.setUserId(patient.getUserId());
        walletModel.setUserType(patient.getType());
        walletModel.setBalance(balance);
        walletModel.setPreviousBalance(previousBalance);
        walletModel.setBalanceCredit(balanceCredit);
        walletModel.setBalanceDebit(balanceDebit);
        walletModel.setStatus(1);
        walletModel.setCreatedAt(LocalDateTime.now());

        walletRepository.save(walletModel);
    }
}
