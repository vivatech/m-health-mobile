package com.service.mobile.service;

import com.service.mobile.config.Constants;
import com.service.mobile.dto.dto.*;
import com.service.mobile.dto.enums.*;
import com.service.mobile.dto.request.*;
import com.service.mobile.dto.response.*;
import com.service.mobile.model.*;
import com.service.mobile.repository.*;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.time.DateUtils;
import org.apache.commons.lang3.StringEscapeUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.management.Query;
import java.io.File;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.security.SecureRandom;
import java.util.Locale;

@Service
@Slf4j
public class PatientService {
    @Autowired
    private WalletTransactionRepository walletTransactionRepository;
    @Autowired
    private UsersUsedCouponCodeRepository usersUsedCouponCodeRepository;
    @Autowired
    private HealthTipOrdersRepository healthTipOrdersRepository;
    @Autowired
    private CouponRepository couponRepository;
    @Autowired
    private SpecializationRepository specializationRepository;
    @Autowired
    private ConsultationRatingRepository consultationRatingRepository;
    @Autowired
    private HealthTipRepository healthTipRepository;
    @Autowired
    private HealthTipPackageCategoriesRepository healthTipPackageCategoriesRepository;
    @Autowired
    private HealthTipPackageUserRepository healthTipPackageUserRepository;

    @Autowired
    private HealthTipCategoryMasterRepository healthTipCategoryMasterRepository;

    @Autowired
    private UserLocationRepository userLocationRepository;

    @Autowired
    private HomeCareDurationRepository homeCareDurationRepository;

    @Autowired
    private SlotMasterRepository slotMasterRepository;

    @Autowired
    private HealthTipPackageRepository healthTipPackageRepository;

    @Autowired
    private ConsultationRepository consultationRepository;

    @Autowired
    private PackageUserRepository packageUserRepository;

    @Autowired
    private UsersRepository usersRepository;

    @Autowired
    private SMSService smsService;

    @Autowired
    private UsersPromoCodeRepository usersPromoCodeRepository;

    @Autowired
    private UsersCreatedWithPromoCodeRepository usersCreatedWithPromoCodeRepository;

    @Autowired
    private GlobalConfigurationRepository globalConfigurationRepository;

    @Autowired
    private AuthKeyRepository authKeyRepository;

    @Autowired
    PasswordEncoder encoder;

    @Autowired
    private MessageSource messageSource;

    @Autowired
    private HealthTipPackageUserService healthTipPackageUserService;

    @Autowired
    private PublicService publicService;

    @Value("${app.development.environment}")
    private Boolean isDevelopmentEnvironment;

    @Value("${app.base.url}")
    private String baseUrl;

    @Value("${app.csv.path}")
    private String csvPath;

    @Value("${app.categories.path}")
    private String categoryUrl;

    @Value("${app.default.image}")
    private String defaultImage;

    @Value("${app.currency.symbol.fdj}")
    private String currencySymbolFdj;

    @Value("${app.currency.symbol}")
    private String currencySymbol;

    @Value("${app.system.user.id}")
    private Integer SystemUserId;

    @Autowired
    private WalletService walletService;

    @PersistenceContext
    private EntityManager entityManager;

    public ResponseEntity<?> actionUpdateFullname(UpdateFullNameRequest request, Locale locale) {
        if(request !=null&&request.getUser_id()!=null)

        {
            Users user = usersRepository.findById(request.getUser_id()).orElse(null);

            if (user != null) {
                user.setFirstName(splitName(request.getFullName())[0]);
                user.setLastName(splitName(request.getFullName())[1]);

                if (request.getPromo_code_of() != null && !request.getPromo_code_of().isEmpty()) {
                    UsersPromoCode promoCodeUser = usersPromoCodeRepository.findByPromoCode(request.getPromo_code_of());

                    if (promoCodeUser != null) {
                        UsersCreatedWithPromoCode userWithPromoCode = new UsersCreatedWithPromoCode();
                        userWithPromoCode.setUserId(request.getUser_id());
                        userWithPromoCode.setCreatedBy(promoCodeUser.getUserId());
                        usersCreatedWithPromoCodeRepository.save(userWithPromoCode);

                        // Send notification SMS
                        Users marketingUser = usersRepository.findById(promoCodeUser.getUserId()).orElse(null);
                        if (marketingUser != null) {
                            String smsNumber = marketingUser.getCountryCode() + marketingUser.getContactNumber();
                            String notificationMsg = smsService.getValue("MARKETING_USER_NOTIFICATION");
                            notificationMsg = notificationMsg.replace("{PATIENT_NAME}", user.getFirstName() + " " + user.getLastName());
                            notificationMsg = notificationMsg.replace("{NAME}", marketingUser.getFirstName() + " " + marketingUser.getLastName());

                            if (isDevelopmentEnvironment!=null && !isDevelopmentEnvironment) {
                                smsService.sendSMS(smsNumber, notificationMsg);
                            } else {
                                logSMS(notificationMsg);
                            }
                        }
                    }
                }

                String authKey = generateRandomString();
                AuthKey authModel = new AuthKey();
                authModel.setUserId(request.getUser_id());
                authModel.setAuthKey(authKey);
                authModel.setDeviceToken(request.getDevice_token());
                authModel.setLoginType(user.getType());
                authModel.setCreatedDate(LocalDateTime.now());
                authKeyRepository.save(authModel);

                GlobalConfiguration signalingServer = globalConfigurationRepository.findByKey("SIGNALING_SERVER");
                GlobalConfiguration verificationToken = globalConfigurationRepository.findByKey("VERIFICATION_TOKEN");
                GlobalConfiguration turnUsername = globalConfigurationRepository.findByKey("TURN_USERNAME");
                GlobalConfiguration turnPassword = globalConfigurationRepository.findByKey("TURN_PASSWORD");

                Response res = new Response();
                UpdateFullnameResponse response = new UpdateFullnameResponse(
                        request.getUser_id().toString(), authKey, user.getType().toString(),
                        "", user.getFirstName() + " " + user.getLastName(), user.getFirstName(), user.getLastName(),
                        user.getEmail(), user.getContactNumber(), signalingServer.getValue(), verificationToken.getValue(),
                        turnUsername.getValue(), turnPassword.getValue()
                );
                res.setData(response);
                res.setMessage(messageSource.getMessage(Constants.USER_LOGIN_IS_SUCCESS,null,locale));
                return ResponseEntity.ok(res);

            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new Response(
                        Constants.NO_RECORD_FOUND_CODE,
                        Constants.NO_RECORD_FOUND_CODE,
                        messageSource.getMessage(Constants.MOBILE_USER_NOT_FOUND,null,locale)
                ));
            }
        } else {
            return ResponseEntity.status(HttpStatus.NO_CONTENT).body(new Response(
                    Constants.NO_RECORD_FOUND_CODE,
                    Constants.BLANK_DATA_GIVEN_CODE,
                    messageSource.getMessage(Constants.BLANK_DATA_GIVEN,null,locale)
            ));
        }
    }

    private String[] splitName(String fullName) {
        String[] parts = fullName.split(" ", 2);
        return new String[]{parts[0], parts.length > 1 ? parts[1] : null};
    }
    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static final SecureRandom RANDOM = new SecureRandom();

    private String generateRandomString() {
        StringBuilder sb = new StringBuilder(20);
        for (int i = 0; i < 20; i++) {
            int index = RANDOM.nextInt(CHARACTERS.length());
            sb.append(CHARACTERS.charAt(index));
        }
        return encoder.encode(sb.toString());
    }

    private void logSMS(String message) {
        log.info("SMS :"+message);
    }

    public ResponseEntity<?> getActiveHealthTipsPackage(
            UpdateFullNameRequest request,
            Locale locale) {

        Response response;

        if (request.getUser_id() != null) {
            String lang = locale.getLanguage();
            List<ActiveHealthTipsPackageResponse> packageData = healthTipPackageUserService.getActiveHealthTipsPackage(request.getUser_id(),lang);

            if (!packageData.isEmpty()) {
                response = new Response(
                        Constants.SUCCESS_CODE,
                        messageSource.getMessage(Constants.HEALTH_TIP_PACKAGE_NOT_FOUND,null,locale),
                        Constants.SUCCESS_CODE,
                        packageData
                );
            } else {
                response = new Response(
                        Constants.SUCCESS_CODE,
                        Constants.SUCCESS_CODE,
                        messageSource.getMessage(Constants.HEALTH_TIP_PACKAGE_NOT_FOUND,null,locale)
                );
            }

            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(HttpStatus.NO_CONTENT).body(new Response(
                    Constants.NO_RECORD_FOUND_CODE,
                    Constants.BLANK_DATA_GIVEN_CODE,
                    messageSource.getMessage(Constants.BLANK_DATA_GIVEN,null,locale)
            ));
        }
    }


    public ResponseEntity<?> checkUserHealthTipPackage(Locale locale, Integer userId) {
        if (userId!=null && userId!=0) {
            List<Integer> healthTipPackageUsers = healthTipPackageUserService.getIdByUserIdAndExpiery(userId, YesNo.No);
            if (healthTipPackageUsers.size()>0) {

                return ResponseEntity.status(HttpStatus.OK).body(new Response(
                        Constants.SUCCESS_CODE,
                        Constants.SUCCESS_CODE,
                        messageSource.getMessage(Constants.HEALTH_TIP_PACKAGE_SUBSCRIBED_FOR_USER,null,locale)
                ));
            } else {
                return ResponseEntity.status(HttpStatus.OK).body(new Response(
                        Constants.SUCCESS_CODE,
                        Constants.SUCCESS_CODE,
                        messageSource.getMessage(Constants.HEALTH_TIP_PACKAGE_FORUSER_NOT_FOUND,null,locale)
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

    public ResponseEntity<?> getSortBy(Locale locale) {
        String[] data = new String[2];
        data[0]= messageSource.getMessage(Constants.YEAR_OF_EXPERIENCE,null,locale);
        data[1]= messageSource.getMessage(Constants.RECOMMENDATION,null,locale);

        return ResponseEntity.status(HttpStatus.OK).body(new Response(
                Constants.SUCCESS_CODE,
                Constants.SUCCESS_CODE,
                messageSource.getMessage(Constants.SORT_LIST_RETRIEVED,null,locale),
                data
        ));
    }

    public ResponseEntity<?> getAvailability(Locale locale) {
        String[] data = new String[3];
        data[0]= messageSource.getMessage(Constants.TODAY,null,locale);
        data[1]= messageSource.getMessage(Constants.TOMORROW,null,locale);
        data[2]= messageSource.getMessage(Constants.WITHIN_SEVEN_DAYS,null,locale);

        return ResponseEntity.status(HttpStatus.OK).body(new Response(
                Constants.SUCCESS_CODE,
                Constants.SUCCESS_CODE,
                messageSource.getMessage(Constants.SORT_LIST_RETRIEVED,null,locale),
                data
        ));
    }

    public ResponseEntity<?> bookDoctor(Locale locale, BookDoctorRequest request) {
        LocalDateTime consultantDateTime = getConsultantDateTime(request.getDate(), request.getTime_slot());
        Consultation consultation = publicService.checkRealTimeBooking(request.getSlot_id(),request.getDate(),request.getDoctor_id());
        Consultation patient = publicService.checkClientBooking(request.getSlot_id(),request.getDate(),request.getUser_id());

        Users users = usersRepository.findById(request.getUser_id()).orElse(null);
        Users doctor = usersRepository.findById(request.getDoctor_id()).orElse(null);
        HealthTipPackage healthTipPackage = healthTipPackageRepository.findById(request.getPackage_id()).orElse(null);
        SlotMaster slotMaster = slotMasterRepository.findById(request.getSlot_id()).orElse(null);
        if(request.getPayment_method()!=null && !request.getPayment_method().isEmpty()){
            if(consultation!=null){
                if(patient!=null){
                    LocalDateTime expire = null;
                    List<PackageUser> packageUser = packageUserRepository.findByUserIdAndPackageId(request.getUser_id(),request.getPackage_id());
                    expire = (packageUser.size()>0)?packageUser.get(0).getExpiredAt():null;
                    LocalDate currentDate = LocalDate.now();

                    List<Consultation> consultations = consultationRepository.findByDoctorIdAndSlotIdAndRequestTypeAndDate(request.getDoctor_id(),request.getSlot_id(), RequestType.Book,request.getDate());
                    if(currentDate.isAfter(request.getDate()) || currentDate.isEqual(request.getDate())){
                        return ResponseEntity.status(HttpStatus.OK).body(new Response(
                                Constants.NO_CONTENT_FOUNT_CODE,
                                Constants.NO_CONTENT_FOUNT_CODE,
                                messageSource.getMessage(Constants.CANNOT_BOOK_APPOINTMENT,null,locale)
                                ));
                    }else if(expire!=null && consultantDateTime.isAfter(expire)){
                        return ResponseEntity.status(HttpStatus.OK).body(new Response(
                                Constants.NO_CONTENT_FOUNT_CODE,
                                Constants.NO_CONTENT_FOUNT_CODE,
                                messageSource.getMessage(Constants.CANNOT_BOOK_APPOINTMENT,null,locale)
                                ));
                    }else if(consultations.size()>0){
                        return ResponseEntity.status(HttpStatus.OK).body(new Response(
                                Constants.NO_CONTENT_FOUNT_CODE,
                                Constants.NO_CONTENT_FOUNT_CODE,
                                messageSource.getMessage(Constants.SORRY_DOCTOR_ALREADY_BOOKED,null,locale)
                                ));
                    }else{
                        List<DoctorAvailability> nurses = new ArrayList<>();
                        if(request.getConsult_type().equalsIgnoreCase("visit_home")){
                            nurses = publicService.nursesAssign(request.getSlot_id());
                        }
                        if(nurses.size()<=0 && request.getConsult_type().equalsIgnoreCase("visit_home")){
                            return ResponseEntity.status(HttpStatus.OK).body(new Response(
                                    Constants.NO_CONTENT_FOUNT_CODE,
                                    Constants.NO_CONTENT_FOUNT_CODE,
                                    messageSource.getMessage(Constants.NURSE_ARE_BUSY,null,locale)
                            ));
                        }

                        Consultation response = new Consultation();
                        consultation.setPatientId(users);
                        consultation.setDoctorId(doctor);
                        consultation.setConsultationDate(request.getDate());

                        consultation.setPackageId(healthTipPackage);
                        consultation.setConsultType(request.getConsult_type());
                        consultation.setSlotId(slotMaster);
                        consultation.setMessage(request.getMessage());
                        consultation.setRequestType(RequestType.Inprocess);
                        consultation.setConsultationType(request.getConsultation_type());
                        consultation.setPaymentMethod(request.getPayment_method());
                        consultation.setAddedType(AddedType.Patient);
                        consultation.setAddedBy(request.getUser_id());
                        consultation.setCreatedAt(LocalDateTime.now());

                        if ("visit_home".equals(request.getConsult_type())) {
                            consultation.setAssignedTo(request.getAllocated_nurse());
                        }

                        consultationRepository.save(consultation);
                        //NOTE-TODO write this functionality
                        /*
                        * $data = $this->actionPayment($consul_model->case_id,$data['user_id'],$currency_option,$coupon_code,$payment_number);
                            $response = json_decode($data);

                            if($response->status!=200){
                                $consul_model->request_type = "Failed";
                                $this->HelperComponent()->sendAgentNotificationMsg($consul_model,'AGENT_NOTIFICATION_FOR_FAILED_CONSULTATION');
                                Yii::$app->db->createCommand()->delete('mh_consultation', ['case_id' => $consul_model->case_id])->execute();
                            }
                        * */
                        return null;
                    }
                }else{
                    return ResponseEntity.status(HttpStatus.OK).body(new Response(
                            Constants.NO_CONTENT_FOUNT_CODE,
                            Constants.NO_CONTENT_FOUNT_CODE,
                            messageSource.getMessage(Constants.SAME_SLOT_BOOKED,null,locale)
                    ));
                }
            }else{
                return ResponseEntity.status(HttpStatus.OK).body(new Response(
                        Constants.NO_CONTENT_FOUNT_CODE,
                        Constants.NO_CONTENT_FOUNT_CODE,
                        messageSource.getMessage(Constants.SLOT_NOT_AVAILABLE,null,locale)
                        ));
            }
        }else{
            return ResponseEntity.status(HttpStatus.OK).body(new Response(
                    Constants.NO_CONTENT_FOUNT_CODE,
                    Constants.NO_CONTENT_FOUNT_CODE,
                    messageSource.getMessage(Constants.SELECT_PAYMENT_METHOD,null,locale)
            ));
        }


    }

    private LocalDateTime getConsultantDateTime(LocalDate date, String timeSlot) {
        String[] timeArray = timeSlot.split(":");
        String consultantDate = date + " " + timeArray[3] + ":" + timeArray[4] + ":00";
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime consultantDateTime = LocalDateTime.parse(consultantDate, formatter);
        return consultantDateTime;
    }

    public ResponseEntity<?> checkHomeVisitAvailability(Locale locale, HomeVisitAvailabilityRequest request) {
        String message = "";
        if(request.getSlot_id()==null || (request.getSlot_id()!=null && request.getSlot_id()==0)){
            message = messageSource.getMessage(Constants.SLOT_ID_REQUIRED,null,locale);
        }else if(request.getDoctor_id()==null || (request.getDoctor_id()!=null && request.getDoctor_id()==0)){
            message = messageSource.getMessage(Constants.DOCTOR_ID_REQUIRED,null,locale);
        }else if(request.getConsultation_date()==null){
            message = messageSource.getMessage(Constants.CONSULTATION_DATE_REQUIRED,null,locale);
        }
        if(message!=null){
            return ResponseEntity.status(HttpStatus.OK).body(new Response(
                    Constants.NO_CONTENT_FOUNT_CODE,
                    Constants.NO_CONTENT_FOUNT_CODE,
                    message
            ));
        }else{
            SlotMaster slotMaster = slotMasterRepository.findById(request.getSlot_id()).orElse(null);
            HomeCareDuration duration = homeCareDurationRepository.findById(1).orElse(null);
            SlotType slotType = slotMaster.getSlotType();

            Integer numberOfSlots = duration.getDuration()/Integer.parseInt(slotType.getValue());
            ReserveSlotDto reserveSlotDto = publicService.getReservedSlot(
                    request.getSlot_id(),
                    request.getDoctor_id(),
                    slotMaster,
                    numberOfSlots,
                    Integer.parseInt(slotType.getValue())
                    );
            List<Integer> reservedSlotIds = reserveSlotDto.getAllocated_slots();
            List<LocalTime> slotTimes = reserveSlotDto.getSlot_start_time();

            DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
            DateTimeFormatter displayFormatter = DateTimeFormatter.ofPattern("hh:mm a");

            String slotTime = timeFormatter.format(slotTimes.get(0)) + " - " + timeFormatter.format(slotTimes.get(slotTimes.size() - 1));
            String displayTime = displayFormatter.format(slotTimes.get(0)) + " - " + displayFormatter.format(slotTimes.get(slotTimes.size() - 1));

            Boolean doctorAvailability = publicService.checkDoctorAvailability(
                    slotMaster, request.getDoctor_id(), numberOfSlots, reservedSlotIds, request.getConsultation_date());

            Response response = new Response();

            if (doctorAvailability) {
                NurseAvailability nurseAvailability = publicService.checkNursesAvailable(
                        null,reservedSlotIds, numberOfSlots, request.getConsultation_date());

                if (nurseAvailability.getStatus().equalsIgnoreCase("avaliable")) {
                    SlotReservationDetails reservationDetails = new SlotReservationDetails();
                    reservationDetails.setNumber_slots_to_allocated(numberOfSlots);
                    reservationDetails.setSlot_time(slotTime);
                    reservationDetails.setDisplay_time(displayTime);
                    reservationDetails.setIs_nurse_avaliabe("available");
                    reservationDetails.setAllocated_nurse(nurseAvailability.getNurse_id());
                    reservationDetails.setAllocated_slots(reservedSlotIds);


                    return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new Response(
                            Constants.NO_CONTENT_FOUNT_CODE,
                            Constants.NO_CONTENT_FOUNT_CODE,
                            messageSource.getMessage(Constants.HOME_CONSULTATION_NOT_AVAILABLE_THIS_TIME,null,locale),
                            reservationDetails
                    ));
                } else {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new Response(
                            Constants.NO_CONTENT_FOUNT_CODE,
                            Constants.NO_CONTENT_FOUNT_CODE,
                            messageSource.getMessage(Constants.HOME_CONSULTATION_NOT_AVAILABLE_THIS_TIME,null,locale)
                    ));
                }
            } else {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new Response(
                        Constants.NO_CONTENT_FOUNT_CODE,
                        Constants.NO_CONTENT_FOUNT_CODE,
                        messageSource.getMessage(Constants.HOME_CONSULTATION_NOT_AVAILABLE_THIS_TIME,null,locale)
                ));
            }
        }
    }

    public ResponseEntity<?> applyCouponCode(Locale locale, ApplyCouponCodeRequest request) {
        if(request.getCoupon_code()!=null && !request.getCoupon_code().isEmpty()){
            CouponCodeResponseDTO data = publicService.checkPromoCode(
                    request.getUser_id(),
                    request.getCategory(),
                    request.getPrice(),
                    request.getCoupon_code(),
                    locale
            );
            if(data.getStatus().equalsIgnoreCase("success")){
                return ResponseEntity.status(HttpStatus.OK).body(new Response(
                        Constants.SUCCESS_CODE,
                        Constants.SUCCESS_CODE,
                        messageSource.getMessage(Constants.SUCCESS_MESSAGE,null,locale),
                        data
                ));
            }else{
                return ResponseEntity.status(HttpStatus.OK).body(new Response(
                        Constants.NO_CONTENT_FOUNT_CODE,
                        Constants.NO_CONTENT_FOUNT_CODE,
                        data.getMessage()
                ));
            }
        }else {
            return ResponseEntity.status(HttpStatus.OK).body(new Response(
                    Constants.NO_CONTENT_FOUNT_CODE,
                    Constants.NO_CONTENT_FOUNT_CODE,
                    messageSource.getMessage(Constants.PLEASE_ENTER_COUPON_CODE,null,locale)
            ));
        }
    }

    public ResponseEntity<?> getClinicList(Locale locale) {
        List<Users> users = usersRepository.findByStatusAndTypeOrderByAsc("A", UserType.Clinic);
        if(users.size()>0){
            List<ClinicListResponse> responses = new ArrayList<>();
            for(Users user:users){
                ClinicListResponse data = new ClinicListResponse();
                String photoPath = baseUrl+defaultImage;
                if(user.getProfilePicture()!=null && !user.getProfilePicture().isEmpty()){
                    photoPath = baseUrl+"uploaded_file/UserProfile/"+user.getUserId()+"/"+user.getProfilePicture();
                }
                UserLocation location = userLocationRepository.findByUserId(user.getUserId()).orElse(null);
                LocationDto dto = null;
                if(location!=null){
                    dto = new LocationDto();
                    dto.setId(location.getId());
                    dto.setLongitude(location.getLongitude());
                    dto.setLatitude(location.getLatitude());
                    dto.setUser_id(user.getUserId());
                }
                data.setClinic_id(user.getUserId());
                data.setClinic_name(user.getClinicName());
                data.setImage(photoPath);
                data.setAddress(user.getHospitalAddress());
                data.setLocation(dto);
                data.setLocation_image(baseUrl +"uploaded_file/map.png");

                responses.add(data);
            }
            return ResponseEntity.status(HttpStatus.OK).body(new Response(
                    Constants.SUCCESS_CODE,
                    Constants.SUCCESS_CODE,
                    messageSource.getMessage(Constants.SUCCESS_MESSAGE,null,locale),
                    responses
            ));

        }else{
            return ResponseEntity.status(HttpStatus.OK).body(new Response(
                    Constants.SUCCESS_CODE,
                    Constants.SUCCESS_CODE,
                    messageSource.getMessage(Constants.SUCCESS_MESSAGE,null,locale)
            ));
        }
    }

    public ResponseEntity<?> healthTipPackageList(Locale locale, HealthTipPackageListRequest request) {
        String name = (request.getName()!=null && !request.getName().isEmpty()) ? request.getName() : "";
        request.setName(name);
        Users users = usersRepository.findById(request.getUser_id()).orElse(new Users());
        Float totalMoney = users.getTotalMoney();
        List<HealthTipPackageCategories> healthTipCategoryMasters = new ArrayList<>();
        Pageable pageable = PageRequest.of(request.getPage(),10);
        Long total = 0L;

        if(request.getFrom_price()!=null && request.getTo_price()!=null){
            if(request.getCat_ids()!=null && !request.getCat_ids().isEmpty()){
                String[] catIds = request.getCat_ids().toString().split(",");
                if(request.getSort_by_price()!=null && !request.getSort_by_price().isEmpty()){
                    //from, to, cat, sort price order
                    if(request.getSort_by_price().equalsIgnoreCase("ASC")){
                        Page<HealthTipPackageCategories> query = healthTipPackageCategoriesRepository.findByStatusPriceFromToCategoryIdsAndPriceAndSort(
                                Status.A,request.getFrom_price(),request.getTo_price(),catIds,pageable
                        );
                        healthTipCategoryMasters = query.getContent();
                        total = query.getTotalElements();
                    }else{
                        Page<HealthTipPackageCategories> query = healthTipPackageCategoriesRepository.findByStatusPriceFromToCategoryIdsAndPriceAndSortDesc(
                                Status.A,request.getFrom_price(),request.getTo_price(),catIds,pageable
                        );
                        healthTipCategoryMasters = query.getContent();
                        total = query.getTotalElements();
                    }
                }else{
                    //from, to, cat, sort.priority
                    Page<HealthTipPackageCategories> query = healthTipPackageCategoriesRepository.findByStatusPriceFromToCategoryIdsAndPriceAndSortPriority(
                            Status.A,request.getFrom_price(),request.getTo_price(),catIds,pageable
                    );
                    healthTipCategoryMasters = query.getContent();
                    total = query.getTotalElements();
                }
            }else{
                if(request.getSort_by_price()!=null && !request.getSort_by_price().isEmpty()){
                    //from, to, sort price order
                    if(request.getSort_by_price().equalsIgnoreCase("ASC")){
                        Page<HealthTipPackageCategories> query = healthTipPackageCategoriesRepository.findByStatusPriceFromToAndPriceAndSort(
                                Status.A,request.getFrom_price(),request.getTo_price(),request.getSort_by_price(),pageable
                        );
                        healthTipCategoryMasters = query.getContent();
                        total = query.getTotalElements();
                    }else{
                        Page<HealthTipPackageCategories> query = healthTipPackageCategoriesRepository.findByStatusPriceFromToAndPriceAndSortDesc(
                                Status.A,request.getFrom_price(),request.getTo_price(),request.getSort_by_price(),pageable
                        );
                        healthTipCategoryMasters = query.getContent();
                        total = query.getTotalElements();
                    }

                }else{
                    //from, to, sort.priority
                    Page<HealthTipPackageCategories> query = healthTipPackageCategoriesRepository.findByStatusPriceFromToAndPriceAndSortPriority(
                            Status.A,request.getFrom_price(),request.getTo_price(),pageable
                    );
                    healthTipCategoryMasters = query.getContent();
                    total = query.getTotalElements();
                }
            }
        }
        else{
            if(request.getCat_ids()!=null && !request.getCat_ids().isEmpty()){
                String[] catIds = request.getCat_ids().toString().split(",");
                if(request.getSort_by_price()!=null && !request.getSort_by_price().isEmpty()){
                    //cat, sort price order
                    if(request.getSort_by_price().equalsIgnoreCase("ASC")){
                        Page<HealthTipPackageCategories> query = healthTipPackageCategoriesRepository.findByStatusCategoryIdsAndPriceAndSort(
                                Status.A,catIds,pageable
                        );
                        healthTipCategoryMasters = query.getContent();
                        total = query.getTotalElements();
                    }else{
                        Page<HealthTipPackageCategories> query = healthTipPackageCategoriesRepository.findByStatusCategoryIdsAndPriceAndSortDesc(
                                Status.A,catIds,pageable
                        );
                        healthTipCategoryMasters = query.getContent();
                        total = query.getTotalElements();
                    }
                }else{
                    //from, to, cat, sort.priority
                    Page<HealthTipPackageCategories> query = healthTipPackageCategoriesRepository.findByStatusCategoryIdsAndPriceAndSortPriority(
                            Status.A,catIds,pageable
                    );
                    healthTipCategoryMasters = query.getContent();
                    total = query.getTotalElements();
                }
            }else{
                if(request.getSort_by_price()!=null && !request.getSort_by_price().isEmpty()){
                    //sort price order
                    if(request.getSort_by_price().equalsIgnoreCase("ASC")){
                        Page<HealthTipPackageCategories> query = healthTipPackageCategoriesRepository.findByStatusAndPriceAndSort(
                                Status.A,pageable
                        );
                        healthTipCategoryMasters = query.getContent();
                        total = query.getTotalElements();
                    }else{
                        Page<HealthTipPackageCategories> query = healthTipPackageCategoriesRepository.findByStatusAndPriceAndSortDesc(
                                Status.A,pageable
                        );
                        healthTipCategoryMasters = query.getContent();
                        total = query.getTotalElements();
                    }
                }else{
                    //from, to, sort.priority
                    Page<HealthTipPackageCategories> query = healthTipPackageCategoriesRepository.findByStatusSortPriority(
                            Status.A,pageable
                    );
                    healthTipCategoryMasters = query.getContent();
                    total = query.getTotalElements();
                }
            }
        }

        if (!healthTipCategoryMasters.isEmpty()) {
            Double maxFee = healthTipPackageRepository.findMaxPackagePrice();

            List<PackageData> packageDataList = new ArrayList<>();
            for (HealthTipPackageCategories val : healthTipCategoryMasters) {
                HealthTipDuration duration = val.getHealthTipPackage().getHealthTipDuration();
                Date packageExpiredDate;
                if (duration.getDurationType() == DurationType.Daily) {
                    packageExpiredDate = DateUtils.addDays(new Date(), duration.getDurationValue());
                } else {
                    packageExpiredDate = DateUtils.addMonths(new Date(), duration.getDurationValue());
                }

                HealthTipCategoryMaster cat = val.getHealthTipCategoryMaster();
                HealthTipPackageCategories packageCat = val;
                HealthTipPackageUser healthTipPackageUser = healthTipPackageUserRepository.findByUserIdAndPackageId(request.getUser_id(), packageCat.getHealthTipPackage().getPackageId()).orElse(null);
                String isPurchased = (healthTipPackageUser!=null) ? "Yes" : "No";

                String image = val.getHealthTipCategoryMaster().getPhoto() != null &&
                        !val.getHealthTipCategoryMaster().getPhoto().isEmpty() &&
                        new File(categoryUrl + "/" + val.getHealthTipCategoryMaster().getCategoryId() + "/" + val.getHealthTipCategoryMaster().getPhoto()).exists()
                        ? baseUrl + "uploaded_file/category/" + val.getHealthTipCategoryMaster().getCategoryId() + "/" + val.getHealthTipCategoryMaster().getPhoto() :
                        baseUrl + "uploaded_file/view-healthtip.png";

                Float priceWithVideo = val.getHealthTipPackage().getType() != PackageType.Free ? val.getHealthTipPackage().getPackagePriceVideo() : 0.0F;

                Float paymentRate = Float.valueOf(globalConfigurationRepository.findByKey("WAAFI_PAYMENT_RATE").getValue());

                String description;
                String categoryName;
                if (locale.getLanguage().equals("sl")) {
                    description = val.getHealthTipCategoryMaster().getDescriptionSl() != null ?
                            val.getHealthTipCategoryMaster().getDescriptionSl() : val.getHealthTipCategoryMaster().getDescription();
                    categoryName = val.getHealthTipCategoryMaster().getNameSl() != null ?
                            val.getHealthTipCategoryMaster().getNameSl() : val.getHealthTipCategoryMaster().getName();
                } else {
                    description = val.getHealthTipCategoryMaster().getDescription();
                    categoryName = val.getHealthTipCategoryMaster().getName();
                }

                PackageData tempData = new PackageData();
                tempData.setPackage_id(packageCat.getHealthTipPackage().getPackageId());
                tempData.setType(val.getHealthTipPackage().getType());
                tempData.setTopic(description);
                tempData.setDuration_value(duration.getDurationValue());
                tempData.setDuration_type(duration.getDurationType());
                tempData.setPackage_price(currencySymbol+ String.format("%.2f", val.getHealthTipPackage().getPackagePrice()));
                tempData.setPackage_price_with_video_without_currency(String.format("%.2f", val.getHealthTipPackage().getPackagePrice()));
                tempData.setPackage_price_with_video(currencySymbol + String.format("%.2f", priceWithVideo));
                tempData.setPackage_price_with_video_without_currency(String.format("%.2f", priceWithVideo));
                tempData.setPackage_price_slsh("SLSH " + String.format("%.2f", val.getHealthTipPackage().getPackagePrice() * paymentRate));
                tempData.setPackage_price_slsh_without_currency(String.format("%.2f", val.getHealthTipPackage().getPackagePrice() * paymentRate));
                tempData.setPackage_price_with_video_slsh("SLSH " + String.format("%.2f", priceWithVideo * paymentRate));
                tempData.setPackage_price_with_video_slsh_without_currency(String.format("%.2f", priceWithVideo * paymentRate));
                tempData.setTotal_money(totalMoney);
                tempData.setExpiry_date(packageExpiredDate);
                tempData.setIs_purchased(isPurchased);
                tempData.setPurchased_package_user_id(isPurchased.equals("Yes") ? healthTipPackageUser.getId().toString() : "");
                tempData.setMaxPackagefee(maxFee);
                tempData.setTotal_count(total);
                tempData.setImage(image);
                tempData.setCategory_name(categoryName);
                tempData.setCategory_id(val.getHealthTipCategoryMaster().getCategoryId());

                packageDataList.add(tempData);
            }

            return ResponseEntity.status(HttpStatus.OK).body(new Response(
                    Constants.SUCCESS_CODE,
                    Constants.SUCCESS_CODE,
                    messageSource.getMessage(Constants.FOUND_NUMBER_PACKAGE,null,locale).replace("{{count}}", total.toString()),
                    packageDataList
            ));
        } else {
            return ResponseEntity.status(HttpStatus.OK).body(new Response(
                    Constants.SUCCESS_CODE,
                    Constants.SUCCESS_CODE,
                    messageSource.getMessage(Constants.NO_RECORD_FOUND,null,locale)
            ));
        }
    }

    public ResponseEntity<?> getBalance(Locale locale, Integer userId) {
        Users users = usersRepository.findById(userId).orElse(null);
        if(users!=null){
            BalanceResponseDTO dto = new BalanceResponseDTO();
            dto.setMessage(messageSource.getMessage(Constants.BALANCE_GET_SUCCESSFULLY,null,locale));
            dto.setStatus(Constants.SUCCESS_CODE);
            dto.setCountryCode(users.getCountryCode());
            dto.setContactNumber(users.getContactNumber());
            dto.setTotalMoney(users.getTotalMoney());
            dto.setData(currencySymbolFdj);
            return ResponseEntity.status(HttpStatus.OK).body(dto);
        }else{
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new Response(
                    Constants.UNAUTHORIZED_CODE,
                    Constants.UNAUTHORIZED_CODE,
                    messageSource.getMessage(Constants.UNAUTHORIZED_MSG,null,locale)
            ));
        }
    }

    public ResponseEntity<?> healthTipPackageBooking(Locale locale, HealthTipPackageBookingRequest request) {
        Users model = usersRepository.findById(request.getUser_id()).orElse(new Users());
        Coupon coupon = null;
        String currencyOption = (request.getCurrency_option()!=null && !request.getCurrency_option().isEmpty())?
        request.getCurrency_option():"USD";

        HealthTipPackage packageModel = healthTipPackageRepository.findById(request.getPackage_id()).orElse(null);
        if(packageModel!=null){
            Float packagePrice = (request.getType().equalsIgnoreCase("video")?
                    packageModel.getPackagePriceVideo():packageModel.getPackagePrice());

            if(packageModel.getType()==PackageType.Paid &&
                    (packagePrice==null || (packagePrice!=null && packagePrice<=0.0f))){
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new Response(
                        Constants.NO_CONTENT_FOUNT_CODE,
                        Constants.NO_CONTENT_FOUNT_CODE,
                        messageSource.getMessage(Constants.UNABLE_BOOK_PKG,null,locale)
                ));
            }else if(packageModel.getType() == PackageType.Paid &&
                    packagePrice > model.getTotalMoney() && request.getPayment_method().equalsIgnoreCase("wallet")){
                PackageNameDurationPrice finalPackageData = new PackageNameDurationPrice();
                finalPackageData.setPackage_price(currencySymbolFdj+ " "+packagePrice);
                finalPackageData.setPackage_duration(packageModel.getHealthTipDuration().getDurationId());
                finalPackageData.setPackage_name(packageModel.getPackageName());

                return ResponseEntity.status(HttpStatus.OK).body(new Response(
                        Constants.SUCCESS_CODE,
                        Constants.SUCCESS_CODE,
                        messageSource.getMessage(Constants.NOT_ENOUGH_BALANCE,null,locale),
                        finalPackageData
                ));
            }else{
                Float finalConsultationFees = packagePrice;
                Integer couponCodeId = null;
                String paymentMethod = null;
                if(request.getCoupon_code()!=null && !request.getCoupon_code().isEmpty()){
                    CouponCodeResponseDTO couponCodeResponse = publicService.checkPromoCode(
                            request.getUser_id(),
                            CouponCategory.HEALTHTIP,
                            finalConsultationFees,
                            request.getCoupon_code(),
                            locale
                    );
                    if(couponCodeResponse.getStatus()!=null && couponCodeResponse.getStatus().equalsIgnoreCase("SUCCESS")){
                        finalConsultationFees = couponCodeResponse.getData().getDiscount_amount();
                        couponCodeId = couponCodeResponse.getData().getCoupon_id();
                        if(couponCodeId!=null && couponCodeId!=0){
                            coupon = couponRepository.findById(couponCodeId).orElse(null);
                        }
                        if(couponCodeResponse.getData().getType()==OfferType.FREE){
                            paymentMethod = "free";
                        }
                    }
                }
                Float currencyAmount = 0.0f;
                if(currencyOption.equalsIgnoreCase("slsh")) {
                    currencyAmount = publicService.getSlshAmount(finalConsultationFees);
                }
                Float amount = (currencyAmount!=null && currencyAmount!=0)?
                        currencyAmount:finalConsultationFees;
                String paymentNumber = (request.getPayment_number()!=null && !request.getPayment_number().isEmpty())?
                        request.getPayment_number():model.getContactNumber();
                OrderPaymentResponse payment = null;
                if(amount!=null && amount>0){
                    paymentMethod = request.getPayment_method();
                    if((paymentMethod.equalsIgnoreCase("waafi") ||
                            paymentMethod.equalsIgnoreCase("zaad") ||
                            paymentMethod.equalsIgnoreCase("evc"))){
                        payment = publicService.orderPayment(request.getUser_id(),amount,0,currencyOption,"evc",new ArrayList<>(),paymentNumber);
                    }
                }

                if((amount!=null && amount<=0) || (payment!=null && payment.getStatus()==200)){
                    HealthTipOrders order = new HealthTipOrders();
                    order.setPatientId(model);
                    order.setHealthTipPackage(packageModel);
                    order.setAmount(packagePrice);
                    order.setCurrencyAmount(amount);
                    order.setCurrency(currencyOption);
                    order.setStatus(OrderStatus.Completed);
                    order.setCoupon(coupon);
                    healthTipOrdersRepository.save(order);

                    // Handle Coupon Usage
                    if (couponCodeId != null && couponCodeId !=0) {
                        UsersUsedCouponCode usedCoupon = new UsersUsedCouponCode();
                        usedCoupon.setUserId(request.getUser_id());
                        usedCoupon.setCouponId(couponCodeId);
                        usedCoupon.setCreatedAt(LocalDateTime.now());
                        usersUsedCouponCodeRepository.save(usedCoupon);

                        // Increment coupon usage (assuming Coupon entity and repository are defined)
                        Coupon couponUsed = couponRepository.findById(couponCodeId).orElseThrow();
                        couponUsed.setNumberOfUsed(couponUsed.getNumberOfUsed() + 1);
                        couponRepository.save(couponUsed);
                    }

                    // Wallet Transaction
                    Integer patientId = request.getUser_id();
                    if (finalConsultationFees!=null && finalConsultationFees != 0) {
                        String transactionId = payment.getData().getTransactionId();
                        Users payerMobile = usersRepository.findById(SystemUserId).orElse(null);
                        WalletTransaction userWalletBalance = publicService.getWalletBalance(patientId);
                        WalletTransaction sysWalletBalance = publicService.getWalletBalance(SystemUserId);

                        WalletTransaction userTransaction = new WalletTransaction();
                        userTransaction.setOrderId(order.getId());
                        userTransaction.setAmount(finalConsultationFees);
                        userTransaction.setTransactionType("wallet_balance_load");
                        userTransaction.setTransactionStatus("Completed");
                        userTransaction.setServiceType("healthtip");
                        userTransaction.setIsDebitCredit("CREDIT");
                        userTransaction.setPatientId(model);
                        userTransaction.setPayerMobile(payerMobile.getContactNumber());
                        userTransaction.setPaymentNumber(paymentNumber);
                        userTransaction =walletTransactionRepository.save(userTransaction);

                        publicService.createTransaction(userTransaction,UserType.PATIENT,transactionId,null);

                        // Add transaction to user wallet balance
                        walletService.addUserWalletBalance(userTransaction, payerMobile, "PATIENT", "DEBIT", finalConsultationFees);
                        publicService.updateSystemUserWallet(finalConsultationFees,null);

                        // Update patient total money after payment
                        model.setTotalMoney(model.getTotalMoney() - packagePrice);
                        usersRepository.save(model);

                        WalletTransaction systemTransaction = new WalletTransaction();
                        systemTransaction.setOrderId(order.getId());
                        systemTransaction.setAmount(finalConsultationFees);
                        systemTransaction.setTransactionType("system_credit_healthtips_subscription");
                        systemTransaction.setTransactionStatus("Completed");
                        systemTransaction.setServiceType("healthtip");
                        systemTransaction.setIsDebitCredit("CREDIT");
                        systemTransaction.setPatientId(model);
                        systemTransaction.setPayerMobile(payerMobile.getContactNumber());
                        systemTransaction.setPaymentNumber(paymentNumber);
                        walletTransactionRepository.save(systemTransaction);

                        walletService.addUserWalletBalance(systemTransaction, payerMobile, "SYSTEM", "CREDIT", finalConsultationFees);
                        publicService.updateSystemUserWallet(finalConsultationFees,null);
                    }

                    // Add/Update User Package Details
                    HealthTipPackageUser userPackage = new HealthTipPackageUser();
                    userPackage.setHealthTipPackage(packageModel);
                    userPackage.setUser(model);
                    LocalDateTime now = LocalDateTime.now();
                    LocalDateTime expiredAt = LocalDateTime.now();

                    if (packageModel.getHealthTipDuration().getDurationType() == DurationType.Daily) {
                        expiredAt = now.plus(packageModel.getHealthTipDuration().getDurationValue(), ChronoUnit.DAYS);
                    } else {
                        expiredAt = now.plus(packageModel.getHealthTipDuration().getDurationValue() * 30L, ChronoUnit.DAYS);
                    }
                    userPackage.setExpiredAt(expiredAt);
                    userPackage.setCreatedAt(LocalDateTime.now());
                    userPackage.setIsExpire(YesNo.No);
                    userPackage.setIsVideo(request.getType().equals("video") ? YesNo.Yes : YesNo.No);
                    healthTipPackageUserRepository.save(userPackage);

                    publicService.sendHealthTipsMsg(model, "HEALTHTIPS_SUPSCRIPTION_CONFIRMATION", "PATIENT");

                    return ResponseEntity.status(HttpStatus.OK).body(new Response(
                            Constants.SUCCESS_CODE,
                            Constants.SUCCESS_CODE,
                            messageSource.getMessage(Constants.HTIP_CAT_SUBSCRIBED,null,locale)
                    ));
                }else{
                    return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new Response(
                            Constants.NO_CONTENT_FOUNT_CODE,
                            Constants.NO_CONTENT_FOUNT_CODE,
                            (payment!=null)?payment.getMessage():"Payment failed"
                    ));
                }

            }
        }else{
            return ResponseEntity.status(HttpStatus.NO_CONTENT).body(new Response(
                    Constants.BLANK_DATA_GIVEN_CODE,
                    Constants.BLANK_DATA_GIVEN_CODE,
                    messageSource.getMessage(Constants.BLANK_DATA_GIVEN,null,locale)
            ));
        }
    }

    public ResponseEntity<?> cancelHealthTipPackage(Locale locale, CancelHealthTipPackageRequest request) {
        HealthTipPackage healthTipPackages = null;
        if(request.getPackage_id()!=null && request.getPackage_id()!=0){
            healthTipPackages = healthTipPackageRepository.findById(request.getPackage_id()).orElse(null);
        }
        if(healthTipPackages!=null){
            HealthTipPackageUser packageUser = healthTipPackageUserService.getByIdAndExpiery(request.getPurchased_package_user_id(),YesNo.No);
            if(packageUser!=null){
                packageUser.setExpiredAt(LocalDateTime.now());
                packageUser.setIsCancel(YesNo.Yes);
                packageUser.setIsExpire(YesNo.Yes);

                healthTipPackageUserService.save(packageUser);
                return ResponseEntity.status(HttpStatus.OK).body(new Response(
                        Constants.SUCCESS_CODE,
                        Constants.SUCCESS_CODE,
                        messageSource.getMessage(Constants.HEALTH_TIP_CANCELLED,null,locale)
                ));
            }else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new Response(
                        Constants.UNAUTHORIZED_CODE,
                        Constants.UNAUTHORIZED_CODE,
                        messageSource.getMessage(Constants.HEALTH_TIP_PACKAGE_NOT_SUBSCRIBED,null,locale)
                ));
            }
        }else {
            return ResponseEntity.status(HttpStatus.NO_CONTENT).body(new Response(
                    Constants.NO_RECORD_FOUND_CODE,
                    Constants.BLANK_DATA_GIVEN_CODE,
                    messageSource.getMessage(Constants.BLANK_DATA_GIVEN,null,locale)
            ));
        }

    }

    public ResponseEntity<?> getHealthTipsList(Locale locale, HealthTipsListRequest request) {
        Pageable pageable = PageRequest.of(request.getPage(), request.getSize());
        List<Integer> healthTipPackageIds = healthTipPackageUserService.findPackageIdsByUserIdAndExpire(request.getUser_id(),YesNo.No);
        if(healthTipPackageIds.size()>0){
            List<Integer> categoriesIds = healthTipPackageCategoriesRepository.findCategoriesIdsByPackageIds(healthTipPackageIds);
            if(request.getTitle()==null){
                request.setTitle("");
            }
            Page<HealthTip> healthTips=null;
            if(request.getPackage_id()!=null && request.getPackage_id()!=0){
                if(request.getCategory_id()!=null && request.getCategory_id()!=0){
                    healthTips = healthTipRepository.findByTitlePackageCategories(
                            request.getTitle(),
                            categoriesIds,
                            request.getCategory_id(),
                            pageable
                    );
                }else{
                    healthTips = healthTipRepository.findByTitleCategories(
                            request.getTitle(),
                            categoriesIds,
                            pageable
                    );
                }
            }else{
                Integer categoriesId = request.getCategory_id();
                if(request.getCategory_id()!=null && request.getCategory_id()!=0){
                    healthTips = healthTipRepository.findByTitleCategorieId(
                            request.getTitle(),
                            request.getCategory_id(),
                            pageable
                    );
                }else{
                    healthTips = healthTipRepository.findByTitleCategories(
                            request.getTitle(),
                            categoriesIds,
                            pageable
                    );
                }
            }
            List<HealthTipsListResponse> data = new ArrayList<>();
            if(healthTips!=null){
                for(HealthTip healthTip:healthTips.getContent()){
                    HealthTipsListResponse temp = new HealthTipsListResponse();
                    HealthTipPackageCategories packageCategories = healthTipPackageCategoriesRepository.findByCategoriesId(healthTip.getHealthTipCategory().getCategoryId()).orElse(null);

                    temp.setPackage_id(packageCategories.getHealthTipPackage().getPackageId());
                    String video = null;
                    String videoThump = null;
                    if(healthTip.getVideo()!=null && !healthTip.getVideo().isEmpty()){
                        HealthTipPackageCategories healthTipPackageCategories = healthTipPackageCategoriesRepository.findByCategoriesId(healthTip.getHealthTipCategory().getCategoryId()).orElse(null);
                        if(healthTipPackageCategories!=null){
                            HealthTipPackageUser user = healthTipPackageUserService.findByUserIdAndPackageId(request.getUser_id(),healthTipPackageCategories.getHealthTipPackage().getPackageId()).orElse(null);
                            if(user!=null){
                                video = baseUrl + "/video/" +healthTip.getVideo();
                                videoThump = baseUrl + "/healthTip/" +healthTip.getHealthTipId()+"/thumb/"+healthTip.getVideoThumb();
                            }
                        }
                    }

                    String encodedHtml = "&lt;p&gt;This is an example of &amp;quot;encoded&amp;quot; HTML.&lt;/p&gt;";
                    String decodedHtml =null;
                    if(healthTip.getDescription()!=null && !healthTip.getDescription().isEmpty()){
                        decodedHtml = StringEscapeUtils.unescapeHtml4(healthTip.getDescription());
                    }
                    String photo = null;
                    if(healthTip.getPhoto()!=null && !healthTip.getPhoto().isEmpty()){
                        photo = baseUrl + "/healthTip/"+healthTip.getHealthTipId()+"/"+healthTip.getPhoto();
                    }

                    temp.setName(healthTip.getTopic());
                    temp.setIs_video((video!=null && !video.isEmpty()));
                    temp.setVideo(video);
                    temp.setVideo_thumb(videoThump);
                    temp.setDescription(decodedHtml);
                    temp.setDescription_formated(healthTip.getDescription());
                    temp.setPhoto(photo);
                    temp.setCategory_name((locale.getLanguage().equalsIgnoreCase("en"))?
                            packageCategories.getHealthTipCategoryMaster().getName():
                            packageCategories.getHealthTipCategoryMaster().getNameSl());
                    temp.setCategory_id(healthTip.getHealthTipCategory().getCategoryId());
                    temp.setStatus((healthTip.getStatus() == Status.A)?"Active":
                            (healthTip.getStatus() == Status.I)?"Inactive":"");
                    temp.setTotal_count(healthTips.getTotalPages());
                    data.add(temp);
                }
                if(data.size()>0){
                    return ResponseEntity.status(HttpStatus.OK).body(new Response(
                            Constants.SUCCESS_CODE,
                            Constants.SUCCESS_CODE,
                            messageSource.getMessage(Constants.HEALTH_TIP_FOUND_SUCCESSFULLY,null,locale),
                            data
                    ));
                }else{
                    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new Response(
                            Constants.NO_RECORD_FOUND_CODE,
                            Constants.NO_RECORD_FOUND_CODE,
                            messageSource.getMessage(Constants.MOBILE_USER_NOT_FOUND,null,locale)
                    ));
                }
            }else{
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new Response(
                        Constants.NO_RECORD_FOUND_CODE,
                        Constants.NO_RECORD_FOUND_CODE,
                        messageSource.getMessage(Constants.MOBILE_USER_NOT_FOUND,null,locale)
                ));
            }
        }
        return ResponseEntity.status(HttpStatus.OK).body(new Response(
                Constants.SUCCESS_CODE,
                Constants.SUCCESS_CODE,
                messageSource.getMessage(Constants.HEALTH_TIP_PACKAGE_NOT_SUBSCRIBED,null,locale)
        ));
    }

    public ResponseEntity<?> healthTipsExport(Locale locale, HealthTipsListRequest request) {
        List<Integer> packageIds = healthTipPackageUserRepository.findPackageIdsByUserIdAndExpire(request.getUser_id(), YesNo.No);

        if (packageIds != null && !packageIds.isEmpty()) {

            List<Integer> categoryIds = healthTipPackageCategoriesRepository.findCategoryIdsByPackageIds(packageIds);

            if (categoryIds != null && !categoryIds.isEmpty()) {

                List<HealthTip> healthTips = new ArrayList<>();
                if(request.getTitle()==null){
                    request.setTitle("");
                }

                if(request.getCategory_id()!=null && request.getCategory_id()!=0){
                    if(request.getPackage_id()!=null){
                        List<Integer> templist = new ArrayList<>();
                        templist.add(request.getPackage_id());
                        List<Integer> catIds = healthTipPackageCategoriesRepository.findCategoryIdsByPackageIds(templist);
                        healthTips = healthTipRepository.findByCategory(catIds,request.getTitle());
                    }else{
                        List<Integer> templist = new ArrayList<>();
                        templist.add(request.getCategory_id());
                        healthTips = healthTipRepository.findByCategory(templist,request.getTitle());
                    }
                }else{
                    if(request.getPackage_id()!=null){
                        List<Integer> templist = new ArrayList<>();
                        templist.add(request.getPackage_id());
                        List<Integer> catIds = healthTipPackageCategoriesRepository.findCategoryIdsByPackageIds(templist);
                        healthTips = healthTipRepository.findByCategory(catIds,request.getTitle());
                    }else{
                        healthTips = healthTipRepository.findAllByTopic(request.getTitle());
                    }

                }

                if (healthTips != null && !healthTips.isEmpty()) {
                    String fileName = "HealthtipsReport--" + System.currentTimeMillis() + ".csv";
                    String filePath = csvPath + fileName;

                    publicService.exportReports(healthTips, filePath);

                    Map<String, String> responseData = new HashMap<>();
                    responseData.put("file_url", baseUrl + "export_csv/" + fileName);
                    responseData.put("file_path", filePath);

                    return ResponseEntity.status(HttpStatus.OK).body(new Response(
                            Constants.SUCCESS_CODE,
                            Constants.SUCCESS_CODE,
                            messageSource.getMessage(Constants.HEALTH_TIP_FOUND_SUCCESSFULLY,null,locale),
                            responseData
                    ));
                } else {
                    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new Response(
                            Constants.NO_RECORD_FOUND_CODE,
                            Constants.NO_RECORD_FOUND_CODE,
                            messageSource.getMessage(Constants.NO_HEALTHTIP_FOUND,null,locale)
                    ));
                }
            }
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new Response(
                Constants.UNAUTHORIZED_CODE,
                Constants.UNAUTHORIZED_CODE,
                messageSource.getMessage(Constants.HEALTH_TIP_PACKAGE_NOT_SUBSCRIBED,null,locale)
        ));
    }

    public ResponseEntity<?> deleteExportFile(Locale locale, DeleteExportFileRequest request) {
        String file = request.getFile();
        File fileToDelete = new File(file);
        if (fileToDelete.exists()) {
            if (fileToDelete.delete()) {
                return ResponseEntity.status(HttpStatus.OK).body(new Response(
                        Constants.SUCCESS_CODE,
                        Constants.SUCCESS_CODE,
                        messageSource.getMessage(Constants.FILE_DELETED_SUCCESSFULLY,null,locale)
                ));
            } else {
                return ResponseEntity.status(HttpStatus.OK).body(new Response(
                        Constants.SUCCESS_CODE,
                        Constants.SUCCESS_CODE,
                        messageSource.getMessage(Constants.FILE_NOT_FOUND,null,locale)
                ));
            }
        } else {
            return ResponseEntity.status(HttpStatus.OK).body(new Response(
                    Constants.SUCCESS_CODE,
                    Constants.SUCCESS_CODE,
                    messageSource.getMessage(Constants.FILE_NOT_FOUND,null,locale)
            ));
        }
    }

    public ResponseEntity<?> healthTipPackageHistory(Locale locale, HealthTipPackageHistoryRequest request) {
        List<HealthTipPackageUser> healthTipPackageUsers = new ArrayList<>();
        Pageable pageable = PageRequest.of(request.getPage(),10);
        Long total = 0L;
        if(request.getPackage_name()==null){request.setPackage_name("");}

        if(request.getCreated_date()!=null){
            if(request.getType()!=null){
                if(request.getCategory_id()!=null && request.getCategory_id()!=0){
                    if(request.getStatus()!=null){
                        if(request.getStatus().equalsIgnoreCase("Unsubscribed") ||
                                request.getStatus().equalsIgnoreCase("Cancelled")){
                            Page<HealthTipPackageUser> page = healthTipPackageUserRepository
                                    .findByCreatedAtIsCanceledTypePackageNameCategoryIdUserId(
                                            request.getCreated_date(), YesNo.Yes, request.getType(),
                                            request.getPackage_name(),  request.getCategory_id(),
                                            request.getUser_id(), pageable
                                    );
                            healthTipPackageUsers = page.getContent();
                            total = page.getTotalElements();
                        }
                        else {
                            Page<HealthTipPackageUser> page = healthTipPackageUserRepository.
                                    findByCreatedAtIsCanceledTypePackageNameCategoryIdUserId(
                                            request.getCreated_date(), YesNo.No,
                                            request.getType(), request.getPackage_name(),
                                            request.getCategory_id(), request.getUser_id(), pageable
                                    );
                            healthTipPackageUsers = page.getContent();
                            total = page.getTotalElements();

                        }
                    }
                    else{
                        Page<HealthTipPackageUser> page = healthTipPackageUserRepository.
                                findByCreatedAtTypePackageNameCategoryIdUserId(
                                        request.getCreated_date(), request.getType(), request.getPackage_name(),
                                        request.getCategory_id(),  request.getUser_id(), pageable
                                );
                        healthTipPackageUsers = page.getContent();
                        total = page.getTotalElements();
                    }
                }
                else{
                    if(request.getStatus()!=null){
                        if(request.getStatus().equalsIgnoreCase("Unsubscribed") ||
                                request.getStatus().equalsIgnoreCase("Cancelled")){
                            Page<HealthTipPackageUser> page = healthTipPackageUserRepository
                                    .findByCreatedAtIsCanceledTypePackageNameUserId(
                                            request.getCreated_date(), YesNo.Yes, request.getType(),
                                            request.getPackage_name(), request.getUser_id(), pageable
                                    );
                            healthTipPackageUsers = page.getContent();
                            total = page.getTotalElements();
                        }
                        else{
                            Page<HealthTipPackageUser> page = healthTipPackageUserRepository.
                                    findByCreatedAtIsCanceledTypePackageNameUserId(
                                            request.getCreated_date(), YesNo.No, request.getType(),
                                            request.getPackage_name(), request.getUser_id(), pageable
                                    );
                            healthTipPackageUsers = page.getContent();
                            total = page.getTotalElements();
                        }
                    }
                    else{
                        Page<HealthTipPackageUser> page = healthTipPackageUserRepository.
                                findByCreatedAtTypePackageNameUserId(
                                        request.getCreated_date(),  request.getType(), request.getPackage_name(),
                                        request.getUser_id(), pageable
                                );
                        healthTipPackageUsers = page.getContent();
                        total = page.getTotalElements();
                    }
                }
            }else{
                if(request.getCategory_id()!=null && request.getCategory_id()!=0){
                    if(request.getStatus()!=null){
                        if(request.getStatus().equalsIgnoreCase("Unsubscribed")){
                            Page<HealthTipPackageUser> page = healthTipPackageUserRepository
                                    .findByCreatedAtIsCanceledTypePackageNameCategoryIdUserId(
                                            request.getCreated_date(), YesNo.Yes, PackageType.Free,
                                            request.getPackage_name(), request.getCategory_id(),
                                            request.getUser_id(), pageable
                                    );
                            healthTipPackageUsers = page.getContent();
                            total = page.getTotalElements();
                        }
                        else if(request.getStatus().equalsIgnoreCase("Cancelled")){
                            Page<HealthTipPackageUser> page = healthTipPackageUserRepository
                                    .findByCreatedAtIsCanceledTypePackageNameCategoryIdUserId(
                                            request.getCreated_date(), YesNo.Yes,
                                            PackageType.Paid, request.getPackage_name(),
                                            request.getCategory_id(), request.getUser_id(), pageable
                                    );
                            healthTipPackageUsers = page.getContent();
                            total = page.getTotalElements();
                        }
                        else{
                            Page<HealthTipPackageUser> page = healthTipPackageUserRepository.
                                    findByCreatedAtIsCanceledPackageNameCategoryIdUserId(
                                            request.getCreated_date(), YesNo.No, request.getPackage_name(),
                                            request.getCategory_id(), request.getUser_id(), pageable
                                    );
                            healthTipPackageUsers = page.getContent();
                            total = page.getTotalElements();
                        }
                    }else{
                        Page<HealthTipPackageUser> page = healthTipPackageUserRepository.
                                findByCreatedAtPackageNameCategoryIdUserId(
                                        request.getCreated_date(), request.getPackage_name(), request.getCategory_id(),
                                        request.getUser_id(), pageable
                                );
                        healthTipPackageUsers = page.getContent();
                        total = page.getTotalElements();
                    }
                }else{
                    if(request.getStatus()!=null){
                        if(request.getStatus().equalsIgnoreCase("Unsubscribed")){
                            Page<HealthTipPackageUser> page = healthTipPackageUserRepository
                                    .findByCreatedAtIsCanceledTypePackageNameCategoryIdUserId(
                                            request.getCreated_date(), YesNo.Yes, PackageType.Free,
                                            request.getPackage_name(), request.getCategory_id(), request.getUser_id(),
                                            pageable
                                    );
                            healthTipPackageUsers = page.getContent();
                            total = page.getTotalElements();
                        }
                        else if(request.getStatus().equalsIgnoreCase("Cancelled")){
                            Page<HealthTipPackageUser> page = healthTipPackageUserRepository
                                    .findByCreatedAtIsCanceledTypePackageNameCategoryIdUserId(
                                            request.getCreated_date(), YesNo.Yes, PackageType.Paid,
                                            request.getPackage_name(), request.getCategory_id(),
                                            request.getUser_id(), pageable
                                    );
                            healthTipPackageUsers = page.getContent();
                            total = page.getTotalElements();
                        }
                        else{
                            Page<HealthTipPackageUser> page = healthTipPackageUserRepository.
                                    findByCreatedAtIsCanceledPackageNameCategoryIdUserId(
                                            request.getCreated_date(), YesNo.No, request.getPackage_name(),
                                            request.getCategory_id(), request.getUser_id(),
                                            pageable
                                    );
                            healthTipPackageUsers = page.getContent();
                            total = page.getTotalElements();
                        }
                    }else{
                        Page<HealthTipPackageUser> page = healthTipPackageUserRepository.
                                findByCreatedAtPackageNameCategoryIdUserId(
                                        request.getCreated_date(), request.getPackage_name(),
                                        request.getCategory_id(), request.getUser_id(),
                                        pageable
                                );
                        healthTipPackageUsers = page.getContent();
                        total = page.getTotalElements();
                    }
                }
            }
        }
        else{
            if(request.getType()!=null){
                if(request.getCategory_id()!=null && request.getCategory_id()!=0){
                    if(request.getStatus()!=null){
                        if(request.getStatus().equalsIgnoreCase("Unsubscribed") ||
                                request.getStatus().equalsIgnoreCase("Cancelled")){
                            Page<HealthTipPackageUser> page = healthTipPackageUserRepository
                                    .findByIsCanceledTypePackageNameCategoryIdUserId(
                                            YesNo.Yes, request.getType(), request.getPackage_name(),
                                            request.getCategory_id(), request.getUser_id(), pageable
                                    );
                            healthTipPackageUsers = page.getContent();
                            total = page.getTotalElements();
                        }
                        else{
                            Page<HealthTipPackageUser> page = healthTipPackageUserRepository.
                                    findByIsCanceledTypePackageNameCategoryIdUserId(
                                            YesNo.No, request.getType(), request.getPackage_name(),
                                            request.getCategory_id(), request.getUser_id(), pageable
                                    );
                            healthTipPackageUsers = page.getContent();
                            total = page.getTotalElements();

                        }
                    }else{
                        Page<HealthTipPackageUser> page = healthTipPackageUserRepository.
                                findByTypePackageNameCategoryIdUserId(
                                        request.getType(), request.getPackage_name(), request.getCategory_id(),
                                        request.getUser_id(), pageable
                                );
                        healthTipPackageUsers = page.getContent();
                        total = page.getTotalElements();
                    }
                }
                else{
                    if(request.getStatus()!=null){
                        if(request.getStatus().equalsIgnoreCase("Unsubscribed") ||
                                request.getStatus().equalsIgnoreCase("Cancelled")){
                            Page<HealthTipPackageUser> page = healthTipPackageUserRepository
                                    .findByIsCanceledTypePackageNameUserId(
                                            YesNo.Yes, request.getType(), request.getPackage_name(),
                                            request.getUser_id(), pageable
                                    );
                            healthTipPackageUsers = page.getContent();
                            total = page.getTotalElements();
                        }
                        else{
                            Page<HealthTipPackageUser> page = healthTipPackageUserRepository.
                                    findByIsCanceledTypePackageNameUserId(
                                            YesNo.No, request.getType(), request.getPackage_name(),
                                            request.getUser_id(), pageable
                                    );
                            healthTipPackageUsers = page.getContent();
                            total = page.getTotalElements();

                        }
                    }else{
                        Page<HealthTipPackageUser> page = healthTipPackageUserRepository.
                                findByTypePackageNameUserId(
                                        request.getType(), request.getPackage_name(),
                                        request.getUser_id(), pageable
                                );
                        healthTipPackageUsers = page.getContent();
                        total = page.getTotalElements();
                    }
                }
            }
            else{
                if(request.getCategory_id()!=null && request.getCategory_id()!=0){
                    if(request.getStatus()!=null){
                        if(request.getStatus().equalsIgnoreCase("Unsubscribed")){
                            Page<HealthTipPackageUser> page = healthTipPackageUserRepository
                                    .findByIsCanceledTypePackageNameCategoryIdUserId(
                                            YesNo.Yes,
                                            PackageType.Free,
                                            request.getPackage_name(),
                                            request.getCategory_id(),
                                            request.getUser_id(),
                                            pageable
                                    );
                            healthTipPackageUsers = page.getContent();
                            total = page.getTotalElements();
                        }
                        else if(request.getStatus().equalsIgnoreCase("Cancelled")){
                            Page<HealthTipPackageUser> page = healthTipPackageUserRepository
                                    .findByIsCanceledTypePackageNameCategoryIdUserId(
                                            YesNo.Yes,
                                            PackageType.Paid,
                                            request.getPackage_name(),
                                            request.getCategory_id(),
                                            request.getUser_id(),
                                            pageable
                                    );
                            healthTipPackageUsers = page.getContent();
                            total = page.getTotalElements();
                        }
                        else{
                            Page<HealthTipPackageUser> page = healthTipPackageUserRepository.
                                    findByIsCanceledPackageNameCategoryIdUserId(
                                            YesNo.No, request.getPackage_name(), request.getCategory_id(),
                                            request.getUser_id(), pageable
                                    );
                            healthTipPackageUsers = page.getContent();
                            total = page.getTotalElements();

                        }
                    }else{
                        Page<HealthTipPackageUser> page = healthTipPackageUserRepository.
                                findByPackageNameCategoryIdUserId(
                                        request.getPackage_name(), request.getCategory_id(),
                                        request.getUser_id(), pageable
                                );
                        healthTipPackageUsers = page.getContent();
                        total = page.getTotalElements();
                    }
                }else{
                    if(request.getStatus()!=null){
                        if(request.getStatus().equalsIgnoreCase("Unsubscribed")){
                            Page<HealthTipPackageUser> page = healthTipPackageUserRepository
                                    .findByIsCanceledTypePackageNameCategoryIdUserId(
                                            YesNo.Yes,
                                            PackageType.Free,
                                            request.getPackage_name(),
                                            request.getCategory_id(),
                                            request.getUser_id(),
                                            pageable
                                    );
                            healthTipPackageUsers = page.getContent();
                            total = page.getTotalElements();
                        }
                        else if(request.getStatus().equalsIgnoreCase("Cancelled")){
                            Page<HealthTipPackageUser> page = healthTipPackageUserRepository
                                    .findByIsCanceledTypePackageNameCategoryIdUserId(
                                            YesNo.Yes,
                                            PackageType.Paid,
                                            request.getPackage_name(),
                                            request.getCategory_id(),
                                            request.getUser_id(),
                                            pageable
                                    );
                            healthTipPackageUsers = page.getContent();
                            total = page.getTotalElements();
                        }
                        else{
                            Page<HealthTipPackageUser> page = healthTipPackageUserRepository.
                                    findByIsCanceledPackageNameCategoryIdUserId(
                                            YesNo.No, request.getPackage_name(),
                                            request.getCategory_id(), request.getUser_id(), pageable
                                    );
                            healthTipPackageUsers = page.getContent();
                            total = page.getTotalElements();

                        }
                    }else{
                        Page<HealthTipPackageUser> page = healthTipPackageUserRepository.
                                findByPackageNameCategoryIdUserId(
                                        request.getPackage_name(), request.getCategory_id(),
                                        request.getUser_id(), pageable
                                );
                        healthTipPackageUsers = page.getContent();
                        total = page.getTotalElements();
                    }
                }
            }
        }

        if(healthTipPackageUsers!=null && !healthTipPackageUsers.isEmpty()){
            List<HealthTipPackageHistoryResponse> responses = new ArrayList<>();
            for(HealthTipPackageUser data:healthTipPackageUsers){
                List<HealthTipPackageCategories> packageCategories = healthTipPackageCategoriesRepository
                        .findByPackageIds(data.getHealthTipPackage().getPackageId());
                String categoryName = "";
                String cancelFlg = "";
                for(HealthTipPackageCategories hc: packageCategories){
                    if(locale.getLanguage().equalsIgnoreCase("en")){
                        categoryName = hc.getHealthTipCategoryMaster().getName();
                    }else{
                        categoryName = hc.getHealthTipCategoryMaster().getNameSl();
                    }
                }
                if(data.getIsExpire()==YesNo.Yes){
                    if(data.getIsCancel()==YesNo.Yes){
                        if(data.getHealthTipPackage().getType() == PackageType.Paid){
                            cancelFlg = messageSource.getMessage(Constants.CANCELLED_MSG,null,locale);
                        }else{
                            cancelFlg = messageSource.getMessage(Constants.UNSUBSCRIBED_MSG,null,locale);
                        }
                    }
                }else{
                    cancelFlg = messageSource.getMessage(Constants.ACTIVE_MSG,null,locale);
                }
                HealthTipPackageHistoryResponse temp = new HealthTipPackageHistoryResponse();

                temp.setCategory_name(categoryName);
                temp.setPackage_type(data.getHealthTipPackage().getType());
                temp.setFrequency(data.getHealthTipPackage().getHealthTipDuration().getDurationType());
                temp.setPackage_price(data.getHealthTipPackage().getPackagePrice());
                temp.setIs_expire(data.getIsExpire());
                temp.setCancel_flg(cancelFlg);
                temp.setCreated_at(data.getCreatedAt());
                temp.setExpired_at(data.getExpiredAt());
                temp.setTotal_count(total);

                responses.add(temp);
            }


            return ResponseEntity.status(HttpStatus.OK).body(new Response(
                    Constants.SUCCESS_CODE,
                    Constants.SUCCESS_CODE,
                    messageSource.getMessage(Constants.NO_RECORD_FOUND,null,locale),
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

    public ResponseEntity<?> addRating(Locale locale, AddRatingRequest request) {
        List<ConsultationRating> ratings = consultationRatingRepository.getByCaseId(request.getCase_id());
        if (ratings.size() > 0) {
            for(ConsultationRating r:ratings){
                r.setComment(request.getComment());
                r.setRating(request.getRating());
                consultationRatingRepository.save(r);
            }
        } else {
            ConsultationRating raiting = new ConsultationRating();
            Users u = usersRepository.findById(request.getUser_id()).orElse(null);
            Users doctor = usersRepository.findById(request.getDoctor_id()).orElse(null);

            raiting.setPatientId(u);
            raiting.setDoctorId(doctor);
            raiting.setStatus(ConsultationStatus.Pending);
            raiting.setRating(request.getRating() != null ? request.getRating() : 0.00f);
            raiting.setComment(request.getComment());
            raiting.setCaseId(request.getCase_id());

            consultationRatingRepository.save(raiting);
        }
        Response response = new Response();
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    public ResponseEntity<?> getSloats(Locale locale, GetSloatsRequest request,String type) {
        Consultation consultation = consultationRepository.findById(request.getCase_id()).orElse(null);
        if(consultation!=null){

            SlotMaster slots = consultation.getSlotId();

            GetSloatsResponse response = new GetSloatsResponse();
            if(type.equalsIgnoreCase("doctor")){
                Users users= consultation.getPatientId();
                String photo = "";
                if(users.getProfilePicture()!=null && !users.getProfilePicture().isEmpty()){
                    photo = baseUrl+ "uploaded_file/UserProfile/" + consultation.getPatientId().getUserId() + "/" + users.getProfilePicture();
                }

                response.setSlot_day(slots.getSlotDay());
                response.setSlot_time(slots.getSlotTime());
                response.setSlot_type(slots.getSlotType());
                response.setConsultation_date(consultation.getConsultationDate());
                response.setTo(consultation.getPatientId().getUserId());
                response.setName(users.getFirstName() + " " + users.getLastName());
                response.setStatus(consultation.getRequestType());
                response.setConsultation_type(consultation.getConsultationType());
                response.setAdded_type(consultation.getAddedType());
                response.setSpecialization("");
                response.setProfile_picture(photo);
            }else {
                Users users= consultation.getDoctorId();
                String specializationName = "";
                if(users.getSpecializationId()!=null && users.getSpecializationId()!=0){
                    Specialization specialization = specializationRepository.findById(users.getSpecializationId()).orElse(null);
                    if(specialization!=null){specializationName = specialization.getName();}
                }
                String photo = "";
                if(users.getProfilePicture()!=null && !users.getProfilePicture().isEmpty()){
                    photo = baseUrl+ "uploaded_file/UserProfile/" + consultation.getDoctorId().getUserId() + "/" + users.getProfilePicture();
                }

                response.setSlot_day(slots.getSlotDay());
                response.setSlot_time(slots.getSlotTime());
                response.setSlot_type(slots.getSlotType());
                response.setConsultation_date(consultation.getConsultationDate());
                response.setTo(consultation.getPatientId().getUserId());
                response.setName(users.getFirstName() + " " + users.getLastName());
                response.setStatus(consultation.getRequestType());
                response.setConsultation_type(consultation.getConsultationType());
                response.setAdded_type(consultation.getAddedType());
                response.setSpecialization(specializationName);
                response.setProfile_picture(photo);
            }

            return ResponseEntity.status(HttpStatus.OK).body(new Response(
                    Constants.SUCCESS_CODE,
                    Constants.SUCCESS_CODE,
                    messageSource.getMessage(Constants.SLOT_DETAILS_FOUND,null,locale),
                    response
            ));
        }else{
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new Response(
                    Constants.NO_RECORD_FOUND_CODE,
                    Constants.NO_RECORD_FOUND_CODE,
                    messageSource.getMessage(Constants.NO_DETAILS_FOUND,null,locale)
            ));
        }
    }
}
