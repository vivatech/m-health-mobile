package com.service.mobile.service;

import com.service.mobile.config.Constants;
import com.service.mobile.config.PaymentOptionConfig;
import com.service.mobile.dto.OfferInformationDTO;
import com.service.mobile.dto.dto.*;
import com.service.mobile.dto.enums.*;
import com.service.mobile.dto.request.*;
import com.service.mobile.dto.response.*;
import com.service.mobile.model.*;
import com.service.mobile.model.State;
import com.service.mobile.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import org.springframework.data.domain.Pageable;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Timestamp;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class PublicService {

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

        Response response = new Response();
        response.setData(responseData);
        response.setMessage(messageSource.getMessage(Constants.GLOBAL_VARIABLES_FETCH,null,locale));
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
            response.setData(page.getDescription());
            response.setMessage(msg);
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
            return ResponseEntity.status(HttpStatus.NO_CONTENT).body(new Response(
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
        Response response = new Response();
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
            return ResponseEntity.status(HttpStatus.NO_CONTENT).body(new Response(
                    Constants.NO_RECORD_FOUND_CODE,
                    Constants.BLANK_DATA_GIVEN_CODE,
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

    public ResponseEntity<?> getProfile(Locale locale, Integer userId) {
        if (userId!=null && userId!=0) {
            Users users = usersRepository.findById(userId).orElse(null);
            if(users!=null){
                String photoPath = users.getProfilePicture() != null ? baseUrl+"uploaded_file/UserProfile/" + users.getUserId() + "/" + users.getProfilePicture() : "";
                String countryName = (users.getCountry()!=null)?users.getCountry().getName():"";
                String stateName = (users.getState()!=null)?users.getState().getName():"";
                String cityName = (users.getCity()!=null)?users.getCity().getName():"";

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
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new Response(
                        Constants.UNAUTHORIZED_CODE,
                        Constants.UNAUTHORIZED_CODE,
                        messageSource.getMessage(Constants.UNAUTHORIZED_MSG,null,locale)
                ));
            }
        }else{
            return ResponseEntity.status(HttpStatus.NO_CONTENT).body(new Response(
                    Constants.NO_RECORD_FOUND_CODE,
                    Constants.BLANK_DATA_GIVEN_CODE,
                    messageSource.getMessage(Constants.BLANK_DATA_GIVEN,null,locale)
            ));
        }
    }

    public ResponseEntity<?> getStateList(Locale locale) {
        List<State> states = stateRepository.findAllByNameAsc();
        if (states.size()>0) {
            List<StateResponse> data = new ArrayList<>();
            for(State t:states){
                data.add(new StateResponse(t.getId(),t.getName(),(t.getCountry()!=null)?t.getCountry().getId():null));
            }

            return ResponseEntity.status(HttpStatus.OK).body(new Response(
                    Constants.SUCCESS_CODE,
                    Constants.SUCCESS_CODE,
                    messageSource.getMessage(Constants.STATE_FOUND_SUCCESSFULLY,null,locale),
                    data
            ));
        }else{
            return ResponseEntity.status(HttpStatus.NO_CONTENT).body(new Response(
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
            return ResponseEntity.status(HttpStatus.NO_CONTENT).body(new Response(
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
                    user.setCity(city);
                    user.setState(city.getState());
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
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new Response(
                        Constants.UNAUTHORIZED_CODE,
                        Constants.UNAUTHORIZED_CODE,
                        messageSource.getMessage(Constants.UNAUTHORIZED_MSG,null,locale)
                ));
            }
        }else{
            return ResponseEntity.status(HttpStatus.NO_CONTENT).body(new Response(
                    Constants.NO_RECORD_FOUND_CODE,
                    Constants.BLANK_DATA_GIVEN_CODE,
                    messageSource.getMessage(Constants.BLANK_DATA_GIVEN,null,locale)
            ));
        }
    }

    public ResponseEntity<?> getLanguage(Locale locale) {
        List<Language> languages = languageRepository.findAllByStatus("A");
        if (languages.size()>0) {
            return ResponseEntity.status(HttpStatus.OK).body(new Response(
                    Constants.SUCCESS_CODE,
                    Constants.SUCCESS_CODE,
                    messageSource.getMessage(Constants.LANGUAGE_LIST_FOUND,null,locale),
                    languages
            ));
        }else{
            return ResponseEntity.status(HttpStatus.NO_CONTENT).body(new Response(
                    Constants.NO_RECORD_FOUND_CODE,
                    Constants.BLANK_DATA_GIVEN_CODE,
                    messageSource.getMessage(Constants.NO_LANGUAGE_LIST_FOUND,null,locale)
            ));
        }
    }


    public ResponseEntity<?> getSpecialization(Locale locale) {
        List<Specialization> specializations = specializationRepository.findAllByStatus("A");
        if (specializations.size()>0) {
            List<SpecializationResponse> responses = new ArrayList<>();
            String photo = baseUrl+defaultImage;
            for(Specialization specialization:specializations){
                SpecializationResponse data = new SpecializationResponse();
                String photoPath = baseUrl+"/uploaded_file/specialisation/"+specialization.getId()+"/"+specialization.getPhoto();

                data.setPhoto(photoPath);
                if(locale.getLanguage().equals("en")){
                    data.setName(specialization.getName());
                }else {data.setName(specialization.getNameSl());}
                data.setId(specialization.getId());

                responses.add(data);
            }
            return ResponseEntity.status(HttpStatus.OK).body(new Response(
                    Constants.SUCCESS_CODE,
                    Constants.SUCCESS_CODE,
                    messageSource.getMessage(Constants.SPECIALIZATION_LIST_FOUND,null,locale),
                    responses
            ));
        }else{
            return ResponseEntity.status(HttpStatus.NO_CONTENT).body(new Response(
                    Constants.NO_RECORD_FOUND_CODE,
                    Constants.BLANK_DATA_GIVEN_CODE,
                    messageSource.getMessage(Constants.NO_SPECIALIZATION_LIST_FOUND,null,locale)
            ));
        }
    }

    public ResponseEntity<?> getPaymentMethod(Locale locale) {

        List<PaymentMethodResponse.Option> currencyOptions = new ArrayList<>();
        for (Map.Entry<String, String> entry : paymentOptionConfig.getCurrency().entrySet()) {
            PaymentMethodResponse.Option option = new PaymentMethodResponse.Option();
            option.setValue(entry.getKey());
            option.setTitle(messageSource.getMessage(entry.getValue().toLowerCase(), null, locale));
            currencyOptions.add(option);
        }

        List<PaymentMethodResponse.Option> paymentMethods = new ArrayList<>();
        for (Map.Entry<String, String> entry : paymentOptionConfig.getPaymentMethod().entrySet()) {
            PaymentMethodResponse.Option option = new PaymentMethodResponse.Option();
            option.setValue(entry.getKey());
            option.setTitle(messageSource.getMessage(entry.getValue(), null, locale));
            paymentMethods.add(option);
        }

        return ResponseEntity.status(HttpStatus.OK).body(new Response(
                Constants.SUCCESS_CODE,
                Constants.SUCCESS_CODE,
                messageSource.getMessage(Constants.SUCCESS_MESSAGE,null,locale),
                currencyOptions
        ));
    }

    public List<PaymentMethodResponse.Option> getPaymentMethod() {
        List<PaymentMethodResponse.Option> currencyOptions = new ArrayList<>();
        for (Map.Entry<String, String> entry : paymentOptionConfig.getCurrency().entrySet()) {
            PaymentMethodResponse.Option option = new PaymentMethodResponse.Option();
            option.setValue(entry.getKey());
            option.setTitle(entry.getValue().toLowerCase());
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

                return ResponseEntity.status(HttpStatus.NO_CONTENT).body(new Response(
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

                    DoctorRattingDTO doctorDTO = new DoctorRattingDTO();
                    doctorDTO.setId(doc.getUserId());
                    doctorDTO.setFirst_name(doc.getFirstName());
                    doctorDTO.setLast_name(doc.getLastName());
                    doctorDTO.setHospital_id(doc.getHospitalId());
                    doctorDTO.setHospital_name(doc.getClinicName());
                    doctorDTO.setPhoto(photo);
                    doctorDTO.setRating(finalCount);
                    doctorDTO.setConsultation_fees(consultationFees);

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

                DoctorRattingDTO doctorDTO = new DoctorRattingDTO();
                doctorDTO.setId(doc.getUserId());
                doctorDTO.setFirst_name(doc.getFirstName());
                doctorDTO.setLast_name(doc.getLastName());
                doctorDTO.setHospital_id(doc.getHospitalId());
                doctorDTO.setHospital_name(doc.getClinicName());
                doctorDTO.setPhoto(photo);
                doctorDTO.setRating(0.0);
                doctorDTO.setConsultation_fees(consultationFees);

                doctors.add(doctorDTO);
            }
        }
        return doctors;
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
        List<HealthTipCategoryMaster> categoryMasters = healthTipCategoryMasterRepository.findAll();
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
        List<LabPrice> labPrices = labPriceRepository.findBySubCatIdAndUserTypeAndStatus(labcatIds,UserType.Lab,Status.A);
        for(LabPrice price:labPrices){
            GetLabDto temp = new GetLabDto();
            temp.setUser_id(price.getLabUser().getUserId());
            temp.setClinic_name(price.getLabUser().getClinicName());
            response.add(temp);
        }
        return response;
    }

    public BillInfoDto getBillInfo(Integer labId, List<Integer> reportId, String collectionMode, String currencyOption) {
        Integer paymentRate = 1;
        String currencySym = currencySymbolFdj; // Adjust this to your actual symbol
        if (currencyOption.equalsIgnoreCase("slsh")) {
            GlobalConfiguration configuration = globalConfigurationRepository.findByKey("WAAFI_PAYMENT_RATE");
            paymentRate = Integer.parseInt(configuration.getValue());
            currencySym = currencySymbolSLSH; // Adjust this to your actual symbol
        }

        Users lab = usersRepository.findById(labId).orElse(new Users());
        LabDetailDto labDetail = new LabDetailDto(labId, lab.getClinicName(), lab.getHospitalAddress());

        String labVisitOnly = "";
        Float diagnosisCost = 0.0f;
        Boolean isHomeVisit = true;
        String onlyLabVisitMsg = null;
        String onlyLabVisitMsgApi = null;
        Map<Integer, String> reportName = new HashMap<>();
        if (!reportId.isEmpty()) {
            for (Integer subCatId : reportId) {
                List<LabPrice> labPrices = labPriceRepository.findByLabIdAndSubCatId(labId, subCatId);
                for (LabPrice p : labPrices) {
                    diagnosisCost += (p.getLabPrice()) * paymentRate;
                }
            }

            List<LabSubCategoryMaster> subCategory = labSubCategoryMasterRepository.findByIdinList(reportId);
            reportName = subCategory.stream()
                    .collect(Collectors.toMap(LabSubCategoryMaster::getSubCatId, LabSubCategoryMaster::getSubCatName));

            List<ReportSubCatDto> dtos = checkHomeVisit(reportId);
            labVisitOnly = dtos.stream()
                    .map(ReportSubCatDto::getSub_cat_name)
                    .collect(Collectors.joining(","));

            if (!labVisitOnly.isEmpty()) {
                isHomeVisit = false;
                onlyLabVisitMsg = "Only lab visit available for: " + labVisitOnly;
                onlyLabVisitMsgApi = onlyLabVisitMsg;
            }
        }

        Float collectionCharge = 0.0f;
        if ("Home_Visit".equalsIgnoreCase(collectionMode)) {
            collectionCharge = 50.0f * paymentRate;
        } else if ("Lab_Visit".equalsIgnoreCase(collectionMode)) {
            collectionCharge = 0.0f;
        } else {
            collectionCharge = (onlyLabVisitMsg != null) ? 0.0f : 50.0f * paymentRate;
        }

        Float totalPrice = diagnosisCost + collectionCharge;

        BillInfoDto response = new BillInfoDto(
                lab.getClinicName(),
                labDetail,
                labVisitOnly,
                isHomeVisit,
                onlyLabVisitMsg,
                onlyLabVisitMsgApi,
                currencySym + " " + String.format("%.2f", diagnosisCost),
                currencySym + " " + String.format("%.2f", collectionCharge),
                currencySym + " " + String.format("%.2f", totalPrice),
                reportName,
                null,
                diagnosisCost,
                collectionCharge,
                totalPrice
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
        String currency = (orders.getCurrency()!=null && !orders.getCurrency().isEmpty())?orders.getCurrency():currencySymbolFdj;
        Float amount = (orders.getCurrencyAmount()!=null)?orders.getCurrencyAmount():orders.getAmount();
        return currency+" "+amount;
    }

    public List<AvailableNursesMapDto> availableNursesMap() {
        /*NOTE-TODO make this code
        *
        * */
        return new ArrayList<>();
    }

    //NOTE : Not in Baannoo check this function
    public void sendNurseOnDemandMsg(SendNurseOnDemandMsgRequest request, String scenario, UserType userType,Locale locale) {
//        Users userModel = null;
//        PartnerNurse nurseModel = null;
//        if(request.getPatient_id()!=null && request.getPatient_id()!=0){
//            userModel = usersRepository.findById(request.getPatient_id()).orElse(null);
//        }
//        if(request.getNurse_id()!=null && request.getNurse_id()!=0){
//            nurseModel = partnerNurseRepository.findById(request.getNurse_id()).orElse(null);
//        }
//        String prevLang  = locale.getLanguage();
//
//        String patientName = (userModel != null) ? userModel.getFirstName() + " " + userModel.getLastName():"";
//
//        if (userType == UserType.Patient && userModel.getNotificationLanguage() != null && !userModel.getNotificationLanguage().isEmpty()) {
//            locale = new Locale(userModel.getNotificationLanguage());
//        } else {
//            locale = new Locale("sl");
//        }
//
//        if (userType == UserType.NursePartner) {
//            locale = new Locale("sl");
//        }
//
//        String getReminderMsgData = scenario;
//
//        String getReminderMsg = messageSource.getMessage(scenario, null, locale);
//        NotificationData notificationData = new NotificationData();
//        SmsData smsData = new SmsData();
//
//        if (scenario.equalsIgnoreCase("NURSE_NOT_FOUND_PATIENT_NOD")) {
//
//            getReminderMsg = getReminderMsgData.replace("{{PATIENT_NAME}}", userModel.getFirstName() + " " + userModel.getLastName());
//
//            notificationData.setFromId(request.getNurse_id());
//            notificationData.setToId(request.getPatient_id());
//            notificationData.setCaseId(request.getId());
//
//            smsData.setFromId(request.getNurse_id());
//            smsData.setToId(request.getPatient_id());
//            smsData.setSmsFor(scenario);
//            smsData.setCaseId(request.getId());
//            smsData.setUserType(UserType.Patient.name());
//        }
//
//        Notifications.createNotification(notificationData, getReminderMsg);
//        SmsLog.createSmsLog(smsData, getReminderMsg);
//
//        LocaleContextHolder.setLocale(new Locale(prevLang));
//        return ResponseEntity.ok().body("Notification and SMS sent successfully");


    }

    public void confirmAck(NodAckRequest data) {
        String type = data.getType();
        String searchId = data.getSearch_id();
        int reqId = data.getReqId();

        if ("service_request".equals(type) && reqId == 2) {
            List<NurseServiceState> stateModel = nurseServiceStateRepository.findBySearchId(searchId);
            for(NurseServiceState s:stateModel){
                s.setConfirmAck(ConfirmAck.YES);
            }
            nurseServiceStateRepository.saveAll(stateModel);
            createTransactionLog("NursePartnerService", "confirmAck", "NOD web acknowledgement.", "NURSEWEBACK", data.toString());
        }
    }

    public void createTransactionLog(String controllerName, String methodName, String returnType, String data, String handlingType) {
        String log = generateLogMessage(controllerName, methodName, "OUTPUT", returnType, data, handlingType);
        writeLogToFile(log);
    }

    private String generateLogMessage(String controllerName, String methodName, String transType, String returnType, String data, String handlingType) {
        Instant now = Instant.now().truncatedTo(ChronoUnit.MICROS);
        LocalDateTime dateTime = LocalDateTime.ofInstant(now, ZoneId.systemDefault());
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

    //TODO check this with Shamshad sir
    public OrderPaymentResponse orderPayment(Integer userId, Float amount, int i, String currencyOption, String evc, ArrayList<Object> objects, String paymentNumber) {
        return null;
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
        Long doctorSlotAvailableCount = doctorAvailabilityRepository.countBySlotTypeIdAndSlotIdAndDoctorId(slotInfo.getSlotType(), allocatedSlots, doctorId);

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

    public void sendConsultationMsg(Consultation consultation, String cancleConsultRequestFromPatient, String patient) {
        // TODO-NOTE make this function
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
}
