package com.service.mobile.service;

import com.service.mobile.config.Constants;
import com.service.mobile.dto.dto.*;
import com.service.mobile.dto.enums.*;
import com.service.mobile.dto.request.*;
import com.service.mobile.dto.response.*;
import com.service.mobile.model.*;
import com.service.mobile.repository.*;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
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

import java.io.File;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.security.SecureRandom;
import java.util.Locale;

@Service
@Slf4j
public class PatientService {
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

    @Value("${app.default.image}")
    private String defaultImage;

    @Value("${app.currency.symbol.fdj}")
    private String currencySymbolFdj;


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
                        //TODO write this functionality
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
            List<Integer> allocated_slots = reserveSlotDto.getAllocated_slots();
            List<LocalTime> slot_start_time = reserveSlotDto.getSlot_start_time();

            //TODO conver this to java code
            /*LocalTime slotTime = slot_start_time.get(0);

            publicService.checkDoctorAvailablity()
            $slot_time = date('h:i',strtotime(current($slotTimes))).':'. date('h:i',strtotime(end($slotTimes)));

                    $display_time = date('h:i A',strtotime(current($slotTimes))).' - '. date('h:i A',strtotime(end($slotTimes)));
                    //check doctor has selected this slots in his availability or not
                    $checkDoctorAvailability = $this->HelperComponent()->checkDoctorAvailablity($_slot_info,$data['doctor_id'],$number_slots_to_allocated,$reserved_slot_ids,$data['consultation_date']);

                    if($checkDoctorAvailability){
                        $_nurse_avaliabe = $this->HelperComponent()->checkNursesAvaliable($reserved_slot_ids,$number_slots_to_allocated,$data['consultation_date']);
                       //var_dump($_nurse_avaliabe); exit;
                        if($_nurse_avaliabe['status'] =='avaliable') {
                            $alocated_nurse = $_nurse_avaliabe['nurse_id'];
                            $nurse_avaliabe = 'avaliable';
                            $data = [
                                'number_slots_to_allocated' => $number_slots_to_allocated,
                                'slot_time' => $slot_time,
                                'display_time' => $display_time,
                                'is_nurse_avaliabe' => $nurse_avaliabe,
                                'allocated_nurse' => $alocated_nurse,
                                'allocated_slots[]'=>$reserved_slot_ids
                            ];
                            http_response_code(200);
                            $response = [
                                'status' => '200',
                                'message' => Yii::t('app', 'success'),
                                'data' => $data,
                            ];
                        }else{
                            http_response_code(403);
                            $response = [
                                'status' => '403',
                                'message' => Yii::t('app', 'home_consult_not_avl_this_time'),
                                'data' => (object)array(),
                            ];
                        }
                    }else{
                        http_response_code(403);
                        $response = [
                            'status' => '403',
                            'message' => Yii::t('app', 'home_consult_not_avl_this_time'),
                            'data' => (object)array(),
                        ];
                    }
                }

            */
            return null;
        }
    }

    //TODO check the publicService function
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
        String name = request.getName();
        Integer userId = request.getUser_id();
        Users user = usersRepository.findById(userId).orElse(null);
        float totalMoney = (user != null) ? user.getTotalMoney() : 0;

        //TODO check this also and make the dto usable
//        List<HealthTipCategoryMaster> categories = healthTipCategoryMasterRepository.findHealthtipPackages(name, userId);

//        List<HealthtipPackageDTO> packageData = new ArrayList<>();
//        if (categories != null && !categories.isEmpty()) {
//            double maxFee = healthTipPackageRepository.findMaxPackagePrice().orElse(100.0);
//            GlobalConfiguration waafiPaymentRate = globalConfigurationRepository.findByKey("WAAFI_PAYMENT_RATE");
//            Double paymentRate = (waafiPaymentRate!=null && waafiPaymentRate.getValue()!=null)?
//                    Float.valueOf(waafiPaymentRate.getValue()):0.0;
//
//            for (HealthTipCategoryMaster category : categories) {
//                HealthTipPackageUser userPackage = healthTipPackageUserRepository.findActivePackageForUser(userId, category.getCategoryId());
//                boolean isPurchased = (userPackage != null);
//
//                String image = (category.getPhoto() != null && !category.getPhoto().isEmpty()) ?
//                        category.getPhoto() : "/uploaded_file/view-healthtip.png";
//                HealthtipPackageDTO packageDTO = new HealthtipPackageDTO(category, totalMoney, maxFee, paymentRate, image, isPurchased, userPackage);
//                packageData.add(packageDTO);
//            }
//        }
//        return ResponseEntity.status(HttpStatus.OK).body(new Response(
//                Constants.SUCCESS_CODE,
//                Constants.SUCCESS_CODE,
//                messageSource.getMessage(Constants.SUCCESS_MESSAGE,null,locale),
//                packageData
//        ));
        return null;
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

    //TODO : make this api
    public ResponseEntity<?> healthTipPackageBooking(Locale locale, HealthTipPackageBookingRequest request) {
        return null;
    }

    public ResponseEntity<?> cancelHealthTipPackage(Locale locale, CancelHealthTipPackageRequest request) {
        List<HealthTipPackage> healthTipPackages = new ArrayList<>();
        if(request.getPackage_id()!=null && request.getPackage_id()!=0){
            healthTipPackages = healthTipPackageRepository.findByPackageId(request.getPackage_id());
        }
        if(healthTipPackages.size()>0){
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
        return null;
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
        return null;
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
}
