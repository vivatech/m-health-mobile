package com.service.mobile.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.service.mobile.config.Constants;
import com.service.mobile.config.Utility;
import com.service.mobile.dto.dto.*;
import com.service.mobile.dto.enums.*;
import com.service.mobile.dto.request.*;
import com.service.mobile.dto.response.*;
import com.service.mobile.model.*;
import com.service.mobile.repository.*;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.time.DateUtils;
import org.apache.commons.lang3.StringEscapeUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.autoconfigure.web.format.DateTimeFormatters;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.io.File;
import java.sql.Timestamp;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.security.SecureRandom;
import java.util.Locale;
import java.util.stream.Collectors;

import static com.service.mobile.config.Constants.*;

@Service
@Slf4j
public class PatientService {
    @Autowired
    private UserOTPRepository userOTPRepository;
    @Autowired
    private DoctorSpecializationRepository doctorSpecializationRepository;
    @Autowired
    private OrdersRepository ordersRepository;
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

    @Autowired
    private EVCPlusPaymentService evcPlusPaymentService;
    @Value("${app.fixed.otp}")
    private boolean OTP_FIXED;
    @Autowired
    private Utility utility;
    @Value("${app.otp.expiry.minutes}")
    private Long expiryTime;
    @Autowired
    private ChargesRepository chargesRepository;
    @Value("${app.transaction.mode}")
    private Integer transactionMode;
    @Value("${app.ZoneId}")
    private String zoneId;

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
                authModel.setCreatedDate(new Date());
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
                        turnUsername.getValue(), turnPassword.getValue(), user.getGender(), user.getDob().toString(), user.getResidenceAddress()
                );
                res = new Response(
                        Constants.SUCCESS_CODE,
                        Constants.SUCCESS_CODE,
                        messageSource.getMessage(Constants.USER_LOGIN_IS_SUCCESS,null,locale),
                        response
                );
                return ResponseEntity.ok(res);

            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new Response(
                        Constants.NO_RECORD_FOUND_CODE,
                        Constants.NO_RECORD_FOUND_CODE,
                        messageSource.getMessage(Constants.MOBILE_USER_NOT_FOUND,null,locale)
                ));
            }
        } else {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new Response(
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
                        Constants.SUCCESS_CODE,
                        messageSource.getMessage(Constants.HEALTH_TIP_PACKAGE_NOT_FOUND,null,locale),
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
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new Response(
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
                        messageSource.getMessage(Constants.HEALTH_TIP_PACKAGE_SUBSCRIBED_FOR_USER,null,locale),
                        true
                ));
            } else {
                return ResponseEntity.status(HttpStatus.OK).body(new Response(
                        Constants.SUCCESS_CODE,
                        Constants.SUCCESS_CODE,
                        messageSource.getMessage(Constants.HEALTH_TIP_PACKAGE_FORUSER_NOT_FOUND,null,locale),
                        false
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

    public ResponseEntity<?> bookDoctor(Locale locale, BookDoctorRequest request) throws JsonProcessingException {
        Users users = usersRepository.findById(request.getUser_id()).orElse(null);
        if (users == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new Response(
                    Constants.UNAUTHORIZED_MSG,
                    NO_CONTENT_FOUNT_CODE,
                    messageSource.getMessage(Constants.UNAUTHORIZED_MSG, null, locale)
            ));
        } else {
            if (request.getDoctor_id() != null && request.getSlot_id() != null
                    && request.getDate() != null && request.getTime_slot() != null
                    && request.getPayment_method() != null && !request.getPayment_method().isEmpty()
                    && request.getConsult_type() != null && !request.getConsult_type().isEmpty()) {

                //Check weather doctor already consult with any other patient with given slots and time
                Consultation consultation = publicService.checkRealTimeBooking(request.getSlot_id(), request.getDate(), request.getDoctor_id());
                //Check weather patient already consult with any other doctor with given slots and time
                Consultation patient = publicService.checkClientBooking(request.getSlot_id(), request.getDate(), request.getUser_id());
                Users doctor = usersRepository.findById(request.getDoctor_id()).orElse(null);
                SlotMaster slotMaster = slotMasterRepository.findById(request.getSlot_id()).orElse(null);
                if (consultation == null){
                    if (patient == null) {
                        LocalDate currentDate = LocalDate.now();
                        ZoneId somaliaZoneId = ZoneId.of(zoneId);
                        LocalTime currentTime = LocalTime.now(somaliaZoneId);

                        List<Consultation> consultations = consultationRepository.findByDoctorIdAndSlotIdAndRequestTypeAndDate(request.getDoctor_id(), request.getSlot_id(), RequestType.Book, request.getDate());
                        if (currentDate.isAfter(request.getDate()) ||
                                (currentDate.isEqual(request.getDate()) && currentTime.isAfter(slotMaster.getSlotStartTime()))) {
                            return ResponseEntity.status(HttpStatus.OK).body(new Response(
                                    Constants.NO_CONTENT_FOUNT_CODE,
                                    Constants.NO_CONTENT_FOUNT_CODE,
                                    messageSource.getMessage(Constants.CANNOT_BOOK_APPOINTMENT, null, locale)
                            ));
                        } else if (consultations.size() > 0) {
                            return ResponseEntity.status(HttpStatus.OK).body(new Response(
                                    Constants.NO_CONTENT_FOUNT_CODE,
                                    Constants.NO_CONTENT_FOUNT_CODE,
                                    messageSource.getMessage(Constants.SORRY_DOCTOR_ALREADY_BOOKED, null, locale)
                            ));
                        } else {
                            List<DoctorAvailability> nurses = new ArrayList<>();
                            if (request.getConsult_type().equalsIgnoreCase("visit_home")) {
                                nurses = publicService.nursesAssign(request.getSlot_id());
                            }
                            if (nurses.size() <= 0 && request.getConsult_type().equalsIgnoreCase("visit_home")) {
                                return ResponseEntity.status(HttpStatus.OK).body(new Response(
                                        Constants.NO_CONTENT_FOUNT_CODE,
                                        Constants.NO_CONTENT_FOUNT_CODE,
                                        messageSource.getMessage(Constants.NURSE_ARE_BUSY, null, locale)
                                ));
                            }

                            float finalAmount = 0.00f;
                            FeeType type = (request.getConsult_type().equalsIgnoreCase("video")) ? FeeType.call : FeeType.visit;

                            Charges charges = chargesRepository.findCharges(doctor.getUserId(), type);
                            if (charges != null) {
                                finalAmount = charges.getFinalConsultationFees();
                            }

                            Coupon coupon = null;
                            if (request.getCoupon_code() != null && !request.getCoupon_code().isEmpty()) {
                                coupon = couponRepository.findByNameStatusAndCategory(request.getCoupon_code(), "CONSULTATION", 1);
                                if (coupon != null) {
                                    finalAmount = paymentCalculation(coupon, finalAmount);
                                }
                            }

                            Consultation response = new Consultation();
                            response.setPatientId(users);
                            response.setDoctorId(doctor);
                            response.setConsultationDate(request.getDate());
                            response.setConsultType(request.getConsult_type());
                            response.setSlotId(slotMaster);
                            response.setMessage(request.getMessage());
                            response.setRequestType(RequestType.Inprocess);
                            response.setConsultationType(request.getConsultation_type());
                            response.setPaymentMethod(request.getPayment_method());
                            response.setAddedType(AddedType.Patient);
                            response.setAddedBy(request.getUser_id());
                            response.setCreatedAt(LocalDateTime.now());
                            response.setConsultStatus(ConsultStatus.pending);
                            response.setReportSuggested("0");
                            consultationRepository.save(response);
                            Orders orders = saveIntoOrdersTable(users, doctor, request, response, finalAmount, coupon, charges);

                            if ("visit_home".equals(request.getConsult_type()) && request.getAllocated_nurse() != null) {
                                consultation.setAssignedTo(request.getAllocated_nurse());
                                consultationRepository.save(response);
                            }

                            //local [somalian payment]
                            String transactionType = "";
                            if (request.getConsult_type().equalsIgnoreCase("video"))
                                transactionType = "paid_against_video";
                            else transactionType = "paid_against_clinic_visit";

                            WalletTransaction transaction = saveWalletTransaction(request, response, orders, transactionType, "consultation", finalAmount);

                            createConsultationTransaction(orders, response, request, "consultation", transaction);

                            Object datas = null;
                            Map<String, Object> data = new HashMap<>();
                            data.put("order_id",orders.getId());
                            datas = data;
                            return ResponseEntity.status(HttpStatus.OK).body(new Response(
                                    SUCCESS_MESSAGE,
                                    Constants.SUCCESS_CODE,
                                    messageSource.getMessage(Constants.PAYMENT_SUCCESS, null, locale),
                                    datas
                            ));
                        }
                    } else {
                        return ResponseEntity.status(HttpStatus.OK).body(new Response(
                                Constants.NO_CONTENT_FOUNT_CODE,
                                Constants.NO_CONTENT_FOUNT_CODE,
                                messageSource.getMessage(Constants.SAME_SLOT_BOOKED, null, locale)
                        ));
                    }
                } else {
                    return ResponseEntity.status(HttpStatus.OK).body(new Response(
                            Constants.NO_CONTENT_FOUNT_CODE,
                            Constants.NO_CONTENT_FOUNT_CODE,
                            messageSource.getMessage(Constants.SLOT_NOT_AVAILABLE, null, locale)
                    ));
                }
            }
            else {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new Response(
                        Constants.NO_CONTENT_FOUNT,
                        Constants.NO_CONTENT_FOUNT_CODE,
                        messageSource.getMessage(Constants.NO_CONTENT_FOUNT, null, locale)
                ));
            }
        }
    }

    private void createConsultationTransaction(Orders orders, Consultation consultation, BookDoctorRequest request, String serviceType, WalletTransaction transaction) throws JsonProcessingException {
        Map<String, Object> transactionDetail = new HashMap<>();
        transactionDetail.put("ref_transaction_id", transaction.getTransactionId());
        transactionDetail.put("reference_number", transaction.getReferenceNumber());

        if(transactionMode != null && transactionMode == 1){
            transaction.setTransactionId(generateDateTime());
            transaction.setTransactionStatus("Completed");
            orders.setStatus(OrderStatus.Completed);
            consultation.setRequestType(RequestType.Book);
            consultation.setPaymentMethod(request.getPayment_method());
        }
        else if(transactionMode!=null && transactionMode==2){
            transaction.setTransactionId(generateDateTime());
            transaction.setTransactionStatus("Cancel");
            orders.setStatus(OrderStatus.Cancelled);
            consultation.setRequestType(RequestType.Cancel);
            consultation.setCancelMessage("Payment FAILED");
            consultation.setPaymentMethod(request.getPayment_method());
        }
        else {
            //todo : manage the transactions based on the request.getPaymentMethod()
            Map<String, Object> payment = evcPlusPaymentService.processPayment("API_PURCHASE", transactionDetail, orders.getAmount(), transaction.getPayerMobile(), consultation.getPatientId().getUserId().toString(), "USD", serviceType);
            if(payment.get("status").equals(200)){
                transaction.setTransactionId(generateDateTime());
                transaction.setTransactionStatus("Completed");
                orders.setStatus(OrderStatus.Completed);
                consultation.setRequestType(RequestType.Book);
                consultation.setPaymentMethod(request.getPayment_method());
            }
            else{
                transaction.setTransactionId(generateDateTime());
                transaction.setTransactionStatus("Cancel");
                orders.setStatus(OrderStatus.Cancelled);
                consultation.setRequestType(RequestType.Cancel);
                consultation.setCancelMessage("Payment FAILED");
                consultation.setPaymentMethod(request.getPayment_method());
            }
        }
        ordersRepository.save(orders);
        consultationRepository.save(consultation);
        walletTransactionRepository.save(transaction);
    }

    private WalletTransaction saveWalletTransaction(BookDoctorRequest request, Consultation patient, Orders orders, String transactionType, String serviceType, float finalAmount) {
        WalletTransaction transaction = new WalletTransaction();
        transaction.setPaymentMethod(request.getPayment_method());
        transaction.setPatientId(patient.getPatientId());
        transaction.setOrderId(orders.getId());
        transaction.setPaymentGatewayType(request.getPayment_method());
        transaction.setTransactionDate(LocalDateTime.now());
        transaction.setTransactionType(transactionType);
        transaction.setAmount(orders.getAmount());
        transaction.setIsDebitCredit("debit");
        transaction.setPayeeId(1); // payee is super admin and his id is 1
        transaction.setPayerId(patient.getPatientId().getUserId());
        transaction.setReferenceNumber(patient.getPatientId().getUserId().toString());  //this is same as payer no

        Users adminContactNumber = usersRepository.findById(1).orElse(null);
        transaction.setPayeeMobile(adminContactNumber.getContactNumber());
        transaction.setPayerMobile(request.getPayment_number() == null || request.getPayment_number().isEmpty() ? patient.getPatientId().getContactNumber() : request.getPayment_number());

        transaction.setCreatedAt(LocalDateTime.now());
        transaction.setUpdatedAt(LocalDateTime.now());
        transaction.setRefTransactionId(orders.getId().toString());
        transaction.setPaymentNumber(request.getPayment_number() == null || request.getPayment_number().isEmpty() ? null : request.getPayment_number());

        //        todo : need to implement mh_wallet
        transaction.setCurrentBalance(0.0F); // by-default
        transaction.setPreviousBalance(0.0f); // by-default
        transaction.setServiceType(serviceType);
        transaction.setTransactionId(generateDateTime());
        transaction.setTransactionStatus("Pending");

        return transaction;
    }
    String generateDateTime() {
        return new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()) + (int) (Math.random() * 1000);
    }

    private float paymentCalculation(Coupon coupon, float amount) {
        float discountAmount = amount;
        if(coupon != null && discountAmount > 0.0f){
            if(coupon.getType().equals(OfferType.FREE)) discountAmount = coupon.getDiscountAmount();
            else if(coupon.getDiscountType().equals(DiscountType.PERCENTAGE)){
                discountAmount = (coupon.getDiscountAmount() * discountAmount) /100;
            }
            else {
                if(coupon.getDiscountAmount() > discountAmount) discountAmount = 0.0f;
                else discountAmount = discountAmount - coupon.getDiscountAmount();
            }
        }
        return discountAmount;
    }

    private Orders saveIntoOrdersTable(Users patient, Users doctor, BookDoctorRequest request, Consultation consultation, Float finalAmount, Coupon coupon, Charges charges) {
        Orders order = new Orders();
        order.setCommissionType(CommissionType.cost);
        order.setDoctorAmount(finalAmount);
        if(charges != null) {
            order.setCommissionType(charges.getCommissionType());
            order.setCommission(charges.getCommission());
            order.setDoctorAmount(charges.getConsultationFees());
        }

        order.setCaseId(consultation);
        order.setPatientId(patient);
        order.setDoctorId(doctor);
        order.setAmount(finalAmount);
        order.setCouponId(coupon);
        order.setCurrency(request.getCurrency_option());

        order.setCreatedAt(LocalDateTime.now());
        order.setUpdatedAt(LocalDateTime.now());
        order.setStatus(OrderStatus.Inprogress);
        return ordersRepository.save(order);
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
        if(!message.isEmpty()){
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
        if (request.getUser_id() == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new Response(
                    Constants.UNAUTHORIZED_MSG,
                    NO_CONTENT_FOUNT_CODE,
                    messageSource.getMessage(UNAUTHORIZED_MSG, null, locale)
            ));
        }
        if (request.getCoupon_code() == null || request.getCoupon_code().isEmpty()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new Response(
                    NO_CONTENT_FOUNT_CODE,
                    NO_CONTENT_FOUNT,
                    messageSource.getMessage(PLEASE_ENTER_COUPON_CODE, null, locale)
            ));
        }
        try {
            CouponCodeResponseDTO data = publicService.checkPromoCode(request.getUser_id(), request.getCategory(), request.getPrice(), request.getCoupon_code(), locale);

            if (data.getStatus().equalsIgnoreCase("success")) {
                return ResponseEntity.status(HttpStatus.OK).body(new Response(
                        Constants.SUCCESS_CODE,
                        Constants.SUCCESS_CODE,
                        data.getMessage(),
                        data.getData()
                ));
            } else {
                return ResponseEntity.status(HttpStatus.OK).body(new Response(
                        Constants.NO_CONTENT_FOUNT_CODE,
                        Constants.NO_CONTENT_FOUNT_CODE,
                        data.getMessage()
                ));
            }
        } catch (Exception e) {
            log.error("Error in coupon code : {}", e);
            return null;
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
                String[] catIdsString = request.getCat_ids().toString().split(",");
                List<Integer> catIds = new ArrayList<>();
                for(String s:catIdsString){catIds.add(Integer.parseInt(s));}
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
                Format formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

                HealthTipCategoryMaster cat = val.getHealthTipCategoryMaster();
                HealthTipPackageCategories packageCat = val;
                List<HealthTipPackageUser> healthTipPackageUser = healthTipPackageUserRepository.findByUserIdAndPackageId(request.getUser_id(), packageCat.getHealthTipPackage().getPackageId());
                String isPurchased = (!healthTipPackageUser.isEmpty()) ? "Yes" : "No";

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
                tempData.setExpiry_date(formatter.format(packageExpiredDate));
                tempData.setIs_purchased(isPurchased);
                tempData.setPurchased_package_user_id(isPurchased.equalsIgnoreCase("Yes") ? healthTipPackageUser.get(0).getId().toString() : "");
                tempData.setMaxPackagefee((int)Math.round(maxFee));
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
            dto.setCountry_code(users.getCountryCode());
            dto.setContact_number(users.getContactNumber());
            dto.setTotal_money(users.getTotalMoney());
            String[] data = {currencySymbolFdj};
            dto.setData(data);
            return ResponseEntity.status(HttpStatus.OK).body(dto);
        }else{
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new Response(
                    NO_CONTENT_FOUNT_CODE,
                    NO_CONTENT_FOUNT_CODE,
                    messageSource.getMessage(Constants.UNAUTHORIZED_MSG,null,locale)
            ));
        }
    }

    public ResponseEntity<?> healthTipPackageBooking(Locale locale, HealthTipPackageBookingRequest request) throws JsonProcessingException {
        Users model = usersRepository.findById(request.getUser_id()).orElse(new Users());
        Coupon coupon = null;
        String currencyOption = (request.getCurrency_option()!=null && !request.getCurrency_option().isEmpty())?
        request.getCurrency_option():"USD";

        HealthTipPackage packageModel = healthTipPackageRepository.findById(request.getPackage_id()).orElse(null);
        if(packageModel!=null){
            Float packagePrice = (request.getType().equalsIgnoreCase("video")?
                    (packageModel.getPackagePriceVideo() == null ? 0.0f :packageModel.getPackagePriceVideo()):(packageModel.getPackagePrice() == null ? 0.0f : packageModel.getPackagePrice()));

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

                if(amount!=null && amount>=0){
                    //Health tip order entry
                    HealthTipOrders order = savingHealthtipOrder(model, packageModel, packagePrice, amount, currencyOption, coupon);

                    // Wallet Transaction
                    Integer patientId = request.getUser_id();
                    //wallet transaction entry
                    WalletTransaction transaction = saveIntoWalletTransactionForHealthtip(request, patientId, order);
                    //payment through evcplus system
                    boolean paymentResponse = createHealthtipPayment(order, transaction);
                    if(paymentResponse){
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
                        userPackage.setIsCancel(YesNo.No);
                        userPackage.setIsVideo(request.getType().equals("video") ? YesNo.Yes : YesNo.No);
                        healthTipPackageUserRepository.save(userPackage);

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

                        publicService.sendHealthTipsMsg(model, "HEALTHTIPS_SUPSCRIPTION_CONFIRMATION", "PATIENT");

                        Map<String, Object> data = new HashMap<>();
                        data.put("order_id", order.getId());
                        return ResponseEntity.status(HttpStatus.OK).body(new Response(
                                Constants.SUCCESS_CODE,
                                Constants.SUCCESS_CODE,
                                messageSource.getMessage(Constants.HTIP_CAT_SUBSCRIBED,null,locale),
                                data
                        ));
                    }else{
                        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new Response(
                                Constants.NO_CONTENT_FOUNT_CODE,
                                Constants.NO_CONTENT_FOUNT_CODE,
                                "Payment failed"
                        ));
                    }
                }else{
                    return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new Response(
                            Constants.NO_CONTENT_FOUNT_CODE,
                            Constants.NO_CONTENT_FOUNT_CODE,
                            "Payment failed"
                    ));
                }
            }
        }else{
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new Response(
                    Constants.BLANK_DATA_GIVEN_CODE,
                    Constants.BLANK_DATA_GIVEN_CODE,
                    messageSource.getMessage(Constants.BLANK_DATA_GIVEN,null,locale)
            ));
        }
    }

    private HealthTipOrders savingHealthtipOrder(Users model, HealthTipPackage packageModel, Float packagePrice, Float amount, String currencyOption, Coupon coupon) {
        HealthTipOrders order = new HealthTipOrders();
        order.setPatientId(model);
        order.setHealthTipPackage(packageModel);
        order.setAmount(packagePrice);
        order.setCurrencyAmount(amount);
        order.setCurrency(currencyOption);
        order.setStatus(OrderStatus.Pending);
        order.setCoupon(coupon);
        order.setCreatedAt(LocalDateTime.now());
        return healthTipOrdersRepository.save(order);
    }

    private boolean createHealthtipPayment(HealthTipOrders order, WalletTransaction transaction) throws JsonProcessingException {
        Map<String, Object> transactionDetail = new HashMap<>();
        transactionDetail.put("ref_transaction_id", transaction.getTransactionId());
        transactionDetail.put("reference_number", transaction.getReferenceNumber());
        boolean status = false;
        transactionDetail.put("status",false);

        if(transactionMode != null && transactionMode == 1){
            transaction.setTransactionId(generateDateTime());
            transaction.setTransactionStatus("Completed");
            order.setStatus(OrderStatus.Completed);
            status = true;
        }
        else if(transactionMode!=null && transactionMode==2){
            transaction.setTransactionId(generateDateTime());
            transaction.setTransactionStatus("Cancel");
            order.setStatus(OrderStatus.Cancelled);
        }
        else {
            if(order.getAmount() > 0) {
                Map<String, Object> payment = evcPlusPaymentService.processPayment("API_PURCHASE", transactionDetail, order.getAmount(), transaction.getPayerMobile(), order.getPatientId().toString(), "USD", "healthtip");
                if(payment.get("status").equals(200)){
                    transaction.setTransactionId(generateDateTime());
                    transaction.setTransactionStatus("Completed");
                    order.setStatus(OrderStatus.Completed);
                    status = true;
                }
                else{
                    transaction.setTransactionId(generateDateTime());
                    transaction.setTransactionStatus("Cancel");
                    order.setStatus(OrderStatus.Cancelled);
                }
            }else{
                transaction.setTransactionId(generateDateTime());
                transaction.setTransactionStatus("Completed");
                order.setStatus(OrderStatus.Completed);
                status = true;
            }
        }
        healthTipOrdersRepository.save(order);
        walletTransactionRepository.save(transaction);
        return status;
    }

    private WalletTransaction saveIntoWalletTransactionForHealthtip(HealthTipPackageBookingRequest request, Integer patientId, HealthTipOrders orders) {
        Users patient = usersRepository.findById(patientId).orElse(null);
        WalletTransaction transaction = new WalletTransaction();
        transaction.setPaymentMethod(request.getPayment_method());
        transaction.setPatientId(patient);
        transaction.setOrderId(orders.getId());
        transaction.setPaymentGatewayType(request.getPayment_method());
        transaction.setTransactionDate(LocalDateTime.now());
        transaction.setTransactionType("wallet_balance_load");
        transaction.setAmount(orders.getAmount());
        transaction.setIsDebitCredit("debit");
        transaction.setPayeeId(1); // payee is super admin and his id is 1
        transaction.setPayerId(patient.getUserId());
        transaction.setReferenceNumber(patient.getUserId().toString());  //this is same as payer no

        Users adminContactNumber = usersRepository.findById(1).orElse(null);
        transaction.setPayeeMobile(adminContactNumber.getContactNumber());
        transaction.setPayerMobile(request.getPayment_number() == null || request.getPayment_number().isEmpty() ? patient.getContactNumber() : request.getPayment_number());

        transaction.setCreatedAt(LocalDateTime.now());
        transaction.setUpdatedAt(LocalDateTime.now());
        transaction.setRefTransactionId(orders.getId().toString());
        transaction.setPaymentNumber(request.getPayment_number() == null || request.getPayment_number().isEmpty() ? null : request.getPayment_number());

        //        todo : need to implement mh_wallet
        transaction.setCurrentBalance(0.0F); // by-default
        transaction.setPreviousBalance(0.0f); // by-default
        transaction.setServiceType("Service_Type"); //Service_Type_Lab_Report
        transaction.setTransactionId(generateDateTime());
        transaction.setTransactionStatus("Pending");

        return transaction;
    }


    public ResponseEntity<?> cancelHealthTipPackage(Locale locale, CancelHealthTipPackageRequest request) {
        HealthTipPackage healthTipPackages = null;
        if (request.getUser_id() == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new Response(
                    NO_CONTENT_FOUNT_CODE,
                    NO_CONTENT_FOUNT_CODE,
                    messageSource.getMessage(Constants.UNAUTHORIZED_MSG, null, locale)
            ));
        } else if (request.getPackage_id() == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new Response(
                    Constants.NO_RECORD_FOUND_CODE,
                    Constants.BLANK_DATA_GIVEN_CODE,
                    messageSource.getMessage(Constants.BLANK_DATA_GIVEN, null, locale)
            ));
        } else if (request.getPurchased_package_user_id() == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new Response(
                    NO_CONTENT_FOUNT_CODE,
                    NO_CONTENT_FOUNT_CODE,
                    messageSource.getMessage(Constants.HEALTH_TIP_PACKAGE_NOT_SUBSCRIBED, null, locale)
            ));
        } else {
            healthTipPackages = healthTipPackageRepository.findById(request.getPackage_id()).orElse(null);
            if (healthTipPackages != null) {
                HealthTipPackageUser packageUser = healthTipPackageUserService.getByIdAndExpiery(request.getPurchased_package_user_id(), YesNo.No);
                if (packageUser != null) {
                    packageUser.setExpiredAt(LocalDateTime.now());
                    packageUser.setIsCancel(YesNo.Yes);
                    packageUser.setIsExpire(YesNo.Yes);

                    healthTipPackageUserService.save(packageUser);
                    return ResponseEntity.status(HttpStatus.OK).body(new Response(
                            Constants.SUCCESS_CODE,
                            Constants.SUCCESS_CODE,
                            messageSource.getMessage(Constants.HEALTH_TIP_CANCELLED, null, locale)
                    ));
                } else {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new Response(
                            NO_CONTENT_FOUNT_CODE,
                            NO_CONTENT_FOUNT_CODE,
                            messageSource.getMessage(Constants.HEALTH_TIP_PACKAGE_NOT_SUBSCRIBED, null, locale)
                    ));
                }
            } else {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new Response(
                        Constants.NO_RECORD_FOUND_CODE,
                        Constants.BLANK_DATA_GIVEN_CODE,
                        messageSource.getMessage(Constants.BLANK_DATA_GIVEN, null, locale)
                ));
            }
        }
    }

    public ResponseEntity<?> getHealthTipsList(Locale locale, HealthTipsListRequest request) {
        if (request.getUser_id() == null || request.getPage() == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new Response(
                    BLANK_DATA_GIVEN,
                    BLANK_DATA_GIVEN_CODE,
                    messageSource.getMessage(BLANK_DATA_GIVEN, null, locale)
            ));
        }

        try {
            List<Integer> healthTipPackageIds = healthTipPackageUserService.findPackageIdsByUserIdAndExpire(request.getUser_id(), YesNo.No);

            if (!healthTipPackageIds.isEmpty()) {
                List<Integer> categoriesIds = healthTipPackageCategoriesRepository.findCategoriesIdsByPackageIds(healthTipPackageIds);

                StringBuilder sb = new StringBuilder("Select h FROM HealthTip h WHERE h.status = " + Status.A + " AND h.healthTipCategory.categoryId IN :categoriesIds");
                if (request.getTitle() != null && !request.getTitle().isEmpty()) {
                    sb.append(" AND h.topic LIKE '%").append(request.getTitle()+"%'");
                }
                if (request.getCategory_id() != null) {
                    sb.append(" AND h.healthTipCategory.categoryId = ").append(request.getCategory_id());
                }
                if (request.getPackage_id() != null && request.getPackage_id() != 0) {
                    healthTipPackageIds = new ArrayList<>();
                    healthTipPackageIds.add(request.getPackage_id());
                    categoriesIds = healthTipPackageCategoriesRepository.findCategoriesIdsByPackageIds(healthTipPackageIds);
                    sb.append(" AND h.healthTipCategory.categoryId IN :categoriesIds");
                }

                Query query = entityManager.createQuery(sb.toString(), HealthTip.class);
                query.setParameter("categoriesIds", categoriesIds);
                List<HealthTip> healthTips = query.getResultList();
                int total = healthTips.size();

                int page = request.getPage();
                int pageSize = 5;

                query.setFirstResult(page * pageSize);
                query.setMaxResults(pageSize);

                healthTips = query.getResultList();

                List<HealthTipsListResponse> data = new ArrayList<>();
                if (healthTips != null) {
                    for (HealthTip healthTip : healthTips) {
                        HealthTipsListResponse temp = new HealthTipsListResponse();
                        HealthTipPackageCategories packageCategories = healthTipPackageCategoriesRepository.findByCategoriesId(healthTip.getHealthTipCategory().getCategoryId()).orElse(null);

                        if(packageCategories != null){
                            temp.setPackage_id(packageCategories.getHealthTipPackage().getPackageId());
                        }

                        String video = null;
                        String videoThump = null;
                        if (healthTip.getVideo() != null && !healthTip.getVideo().isEmpty() && packageCategories != null) {
                            List<HealthTipPackageUser> user = healthTipPackageUserService.findByUserIdAndPackageId(request.getUser_id(), packageCategories.getHealthTipPackage().getPackageId());
                            if (user != null && !user.isEmpty()) {
                                if (user.get(0).getIsVideo().equals(YesNo.Yes)) {
                                    video = baseUrl + "/video/" + healthTip.getVideo();
                                    videoThump = baseUrl + "/healthTip/" + healthTip.getHealthTipId() + "/thumb/" + healthTip.getVideoThumb();
                                }
                            }
                        }

                        String encodedHtml = "&lt;p&gt;This is an example of &amp;quot;encoded&amp;quot; HTML.&lt;/p&gt;";
                        String decodedHtml = null;
                        if (healthTip.getDescription() != null && !healthTip.getDescription().isEmpty()) {
                            decodedHtml = StringEscapeUtils.unescapeHtml4(healthTip.getDescription());
                        }
                        String photo = null;
                        if (healthTip.getPhoto() != null && !healthTip.getPhoto().isEmpty()) {
                            photo = baseUrl + "/healthTip/" + healthTip.getHealthTipId() + "/" + healthTip.getPhoto();
                        }

                        temp.setName(healthTip.getTopic());
                        temp.setIs_video((video != null && !video.isEmpty()));
                        temp.setVideo(video);
                        temp.setVideo_thumb(videoThump);
                        temp.setDescription(decodedHtml);
                        temp.setDescription_formated(healthTip.getDescription());
                        temp.setPhoto(photo);
                        temp.setCategory_name((locale.getLanguage().equalsIgnoreCase("en")) ?
                                packageCategories.getHealthTipCategoryMaster().getName() :
                                packageCategories.getHealthTipCategoryMaster().getNameSl());
                        temp.setCategory_id(healthTip.getHealthTipCategory().getCategoryId());
                        temp.setStatus((healthTip.getStatus() == Status.A) ? "Active" :
                                (healthTip.getStatus() == Status.I) ? "Inactive" : "");
                        temp.setTotal_count(total);
                        data.add(temp);
                    }
                    if (data.size() > 0) {
                        return ResponseEntity.status(HttpStatus.OK).body(new Response(
                                Constants.SUCCESS_CODE,
                                Constants.SUCCESS_CODE,
                                messageSource.getMessage(Constants.HEALTH_TIP_FOUND_SUCCESSFULLY, null, locale),
                                data
                        ));
                    } else {
                        return ResponseEntity.status(HttpStatus.OK).body(new Response(
                                Constants.NO_RECORD_FOUND_CODE,
                                SUCCESS_CODE,
                                messageSource.getMessage(NO_RECORD_FOUND, null, locale)
                        ));
                    }
                } else {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new Response(
                            Constants.NO_RECORD_FOUND,
                            Constants.NO_RECORD_FOUND_CODE,
                            messageSource.getMessage(Constants.NO_RECORD_FOUND, null, locale)
                    ));
                }
            } else {
                return ResponseEntity.status(HttpStatus.OK).body(new Response(
                        Constants.SUCCESS_CODE,
                        Constants.SUCCESS_CODE,
                        messageSource.getMessage(Constants.HEALTH_TIP_PACKAGE_NOT_SUBSCRIBED, null, locale)
                ));
            }
        } catch (Exception e) {
            log.error("Error in get-healthtip-list : {}", e);
            return null;
        }
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
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new Response(
                NO_CONTENT_FOUNT_CODE,
                NO_CONTENT_FOUNT_CODE,
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
        if (request.getUser_id() == null || request.getPage() == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new Response(
                    NO_CONTENT_FOUNT_CODE,
                    NO_CONTENT_FOUNT_CODE,
                    messageSource.getMessage(UNAUTHORIZED_MSG, null, locale)
            ));
        }
        try {
            StringBuilder sb = new StringBuilder("Select h From HealthTipPackageUser h WHERE h.user.userId = " + request.getUser_id());
            if (request.getCreated_date() != null) {
                sb.append(" AND DATE(h.createdAt) = :date");
            }
            if (request.getStatus() != null && !request.getStatus().isEmpty()) {
                if (request.getStatus().equalsIgnoreCase("Unsubscribed")) {
                    sb.append(" AND h.isCancel =" + YesNo.Yes + " AND h.healthTipPackage.type = " + PackageType.Free);
                } else if (request.getStatus().equalsIgnoreCase("Cancelled")) {
                    sb.append(" AND h.isCancel =" + YesNo.Yes + " AND h.healthTipPackage.type = " + PackageType.Paid);
                } else {
                    sb.append(" AND h.isCancel =" + YesNo.No);
                }
            }
            if (request.getType() != null) {
                sb.append(" AND h.healthTipPackage.type = " + request.getType());
            }
            if (request.getPackage_name() != null && !request.getPackage_name().isEmpty()) {
                sb.append(" AND h.healthTipPackage.packageName LIKE '%" + request.getPackage_name().trim() + "%'");
            }
            if (request.getCategory_id() != null && !request.getCategory_id().isEmpty()) {
                List<Integer> integerList = Arrays.stream(request.getCategory_id().split(","))
                        .map(Integer::parseInt).toList();
                List<Integer> catIds = healthTipPackageCategoriesRepository.findByCategoriesIds(integerList);
                sb.append(" AND h.healthTipPackage.packageId IN :" + catIds);
            }
            sb.append(" ORDER BY h.id DESC");
            Query query = entityManager.createQuery(sb.toString(), HealthTipPackageUser.class);
            if(request.getCreated_date() != null) query.setParameter("date",request.getCreated_date());
            List<HealthTipPackageUser> healthTipPackageUsers = query.getResultList();
            int total = healthTipPackageUsers.size();
            int page = request.getPage();
            int pageSize = 10;
            query.setFirstResult(page * pageSize);
            query.setMaxResults(pageSize);

            healthTipPackageUsers = query.getResultList();

            if (!healthTipPackageUsers.isEmpty()) {
                List<HealthTipPackageHistoryResponse> responses = new ArrayList<>();
                for (HealthTipPackageUser data : healthTipPackageUsers) {
                    List<HealthTipPackageCategories> packageCategories = healthTipPackageCategoriesRepository
                            .findByPackageIds(data.getHealthTipPackage().getPackageId());
                    HealthTipCategoryMaster categoryMaster = healthTipCategoryMasterRepository.findById(packageCategories.get(0).getHealthTipCategoryMaster().getCategoryId()).orElse(null);

                    String categoryName = "";
                    String cancelFlg = "";

                    if (locale.getLanguage().equalsIgnoreCase("en")) {
                        categoryName = categoryMaster.getName();
                    } else {
                        categoryName = categoryMaster.getNameSl();
                    }

                    if (data.getIsExpire() == YesNo.Yes) {
                        if (data.getIsCancel() == YesNo.Yes) {
                            if (data.getHealthTipPackage().getType() == PackageType.Paid) {
                                cancelFlg = messageSource.getMessage(Constants.CANCELLED_MSG, null, locale);
                            } else {
                                cancelFlg = messageSource.getMessage(Constants.UNSUBSCRIBED_MSG, null, locale);
                            }
                        }
                    } else {
                        cancelFlg = messageSource.getMessage(Constants.ACTIVE_MSG, null, locale);
                    }
                    HealthTipPackageHistoryResponse temp = new HealthTipPackageHistoryResponse();

                    temp.setCategory_name(categoryName);
                    String freq = data.getHealthTipPackage().getHealthTipDuration().getDurationType().name();
                    String packageType = data.getHealthTipPackage().getType().name();
                    temp.setPackage_type(messageSource.getMessage(packageType, null, locale));
                    temp.setFrequency(messageSource.getMessage(freq, null, locale));
                    temp.setPackage_price("");
                    temp.setIs_expire(data.getIsExpire());
                    temp.setCancel_flg(cancelFlg);
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");
                    temp.setCreated_at(formatter.format(data.getCreatedAt()));
                    temp.setExpired_at(formatter.format(data.getExpiredAt()));
                    temp.setTotal_count(total);

                    responses.add(temp);
                }

                return ResponseEntity.status(HttpStatus.OK).body(new Response(
                        Constants.SUCCESS_CODE,
                        Constants.SUCCESS_CODE,
                        messageSource.getMessage(Constants.HEALTH_TIP_PACKAGE_FETCH_SUCCESSFULLY, null, locale),
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
            log.error("Error while fetching data in Health tip package history : {}", e);
            return null;
        }
    }

    public ResponseEntity<?> addRating(Locale locale, AddRatingRequest request) {
        List<ConsultationRating> ratings = consultationRatingRepository.getByCaseId(request.getCase_id());
        if (!ratings.isEmpty()) {
            for(ConsultationRating r:ratings){
                r.setComment(request.getComment());
                r.setRating(request.getRating());
                r.setUpdatedAt(LocalDateTime.now());
                consultationRatingRepository.save(r);
            }
        } else {
            ConsultationRating raiting = new ConsultationRating();
            Users u = usersRepository.findById(request.getUser_id()).orElse(null);
            Users doctor = usersRepository.findById(request.getDoctor_id()).orElse(null);
            if(u!=null && doctor!=null){
                raiting.setPatientId(u);
                raiting.setDoctorId(doctor);
                raiting.setStatus(ConsultationStatus.Pending);
                raiting.setRating(request.getRating() != null ? request.getRating() : 0.00f);
                raiting.setComment(request.getComment());
                raiting.setCaseId(request.getCase_id());
                raiting.setCreatedAt(LocalDateTime.now());
                raiting.setUpdatedAt(LocalDateTime.now());

                consultationRatingRepository.save(raiting);
            }
        }
        Response response = new Response();
        response.setCode(Constants.SUCCESS_CODE);
        response.setStatus(Constants.SUCCESS_CODE);
        response.setMessage(messageSource.getMessage(Constants.RATING_ADDED,null,locale));
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    public ResponseEntity<?> getSloats(Locale locale, GetSloatsRequest request) {
        if(request.getCase_id()!=null && request.getCase_id()!=0){

            Consultation consultation = consultationRepository.findById(request.getCase_id()).orElse(null);
            if(consultation!=null){

                GetSloatsResponse response = getGetSloatsResponse(consultation);

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
        }else{
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new Response(
                    Constants.NO_RECORD_FOUND_CODE,
                    Constants.NO_RECORD_FOUND_CODE,
                    messageSource.getMessage(Constants.NO_DETAILS_FOUND,null,locale)
            ));
        }
    }

    private GetSloatsResponse getGetSloatsResponse(Consultation consultation) {
        SlotMaster slots = consultation.getSlotId();

        GetSloatsResponse response = new GetSloatsResponse();
        Users users= consultation.getPatientId();
        String photo = "";
        if(users.getProfilePicture()!=null && !users.getProfilePicture().isEmpty()){
            photo = baseUrl+ "uploaded_file/UserProfile/" + consultation.getPatientId().getUserId() + "/" + users.getProfilePicture();
        }

        response.setSlot_day(slots.getSlotDay());
        response.setSlot_time(slots.getSlotTime());
        response.setSlot_type(slots.getSlotType().getId());
        response.setConsultation_date(consultation.getConsultationDate());
        response.setTo(consultation.getDoctorId().getUserId());
        response.setName(users.getFirstName() + " " + users.getLastName());
        response.setStatus(consultation.getRequestType());
        response.setConsultation_type(consultation.getConsultationType());
        response.setAdded_type(consultation.getAddedType());
        response.setSpecialization("");
        response.setProfile_picture(photo);
        return response;
    }

    public ResponseEntity<?> myOrders(Locale locale, HealthTipPackageHistoryRequest request) {
        if (request.getUser_id() == null || request.getPage() == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new Response(
                    UNAUTHORIZED_MSG,
                    NO_CONTENT_FOUNT_CODE,
                    messageSource.getMessage(UNAUTHORIZED_MSG, null, locale)
            ));
        }
        try {
            StringBuilder sb = getStringBuilder(request);
            Query query = entityManager.createQuery(sb.toString(), Orders.class);

            query.setParameter("userId", request.getUser_id());
            if(request.getConsultation_date() != null){
                query.setParameter("consultationDate", request.getConsultation_date());
            }
            if (request.getDoctor_name() != null && !request.getDoctor_name().isEmpty()) {
                query.setParameter("dn", "%" + request.getDoctor_name() + "%");
            }
            if(request.getCase_id() != null && request.getCase_id() != 0){
                query.setParameter("caseId", request.getCase_id());
            }
            List<Orders> orders = query.getResultList();
            Long total = (long) orders.size();
            // Apply pagination
            int page = request.getPage();
            int pageSize = 10;
            query.setFirstResult(page * pageSize);
            query.setMaxResults(pageSize);

            orders = query.getResultList();

            List<OrderData> orderDataList = new ArrayList<>();
            String currentTime = LocalDateTime.now(ZoneId.of(zoneId)).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));

            for (Orders order : orders) {
                // Get Rating
                ConsultationRating rating = consultationRatingRepository.getByCaseIdAndDoctorId(order.getCaseId().getCaseId(), order.getDoctorId().getUserId());
                // Prepare photo URL
                String photoPath = "";
                if (order.getDoctorId().getProfilePicture() != null) {
                    photoPath = baseUrl + "uploaded_file/UserProfile/" +
                            order.getDoctorId().getUserId() + "/" + order.getDoctorId().getProfilePicture();
                }

                // Get Transaction IDs
                List<WalletTransaction> walletHistories = walletTransactionRepository.findByOrderId(order.getId());
                String transactionIdString = walletHistories.stream()
                        .map(WalletTransaction::getTransactionId)
                        .collect(Collectors.joining(","));

                // Get Consultation Data
                Consultation consultation = order.getCaseId();
                SlotMaster slotData = consultation.getSlotId();

                // Prepare Specialization String
                List<DoctorSpecialization> specializations = doctorSpecializationRepository.findByUserId(order.getDoctorId().getUserId());
                String specializationString = specializations.stream()
                        .map(specialization -> specialization.getSpecializationId().getName())
                        .collect(Collectors.joining(","));

                // Check if rating is allowed
                boolean isRatingAllowed = false;
                String finalTime = consultation.getConsultationDate() + " " + (slotData != null ? slotData.getSlotTime() : "");
                if (currentTime.compareTo(finalTime) < 0 || consultationRatingRepository.countByCaseIdAndPatientId(order.getCaseId().getCaseId(), request.getUser_id()) > 0) {
                    isRatingAllowed = true;
                }

                // Prepare PDF URL
                String pdfUrl = "";
                if (currentTime.compareTo(finalTime) > 0) {
                    pdfUrl = baseUrl + "/uploaded_file/pdf/" + order.getCaseId() + "/" + order.getCaseId() + ".pdf";
                }

                String recConsultationType;
                if (consultation.getConsultationType().equals(ConsultationType.Paid)) {
                    recConsultationType = messageSource.getMessage(Constants.PAID_MSG, null, locale);
                } else {
                    recConsultationType = messageSource.getMessage(Constants.FREE_MSG, null, locale);
                }

                // Prepare Response Data
                OrderData tempData = new OrderData();
                tempData.setId(order.getId());
                tempData.setCase_id(order.getCaseId().getCaseId());
                tempData.setDoctor_id(order.getDoctorId().getUserId());
                tempData.setPhoto(photoPath);
                tempData.setTransaction_id(transactionIdString);
                tempData.setConsultation_date(consultation.getConsultationDate());
                tempData.setReport_suggested(consultation.getReportSuggested());
                tempData.setConsultation_type(consultation.getConsultType());
                tempData.setRec_consultation_type(recConsultationType);
                tempData.setAdded_type(String.valueOf(consultation.getAddedType()));
                tempData.setSlot_time(slotData != null ? slotData.getSlotTime() : "");
                tempData.setSpecialization(specializationString);
                tempData.setPackage_name(order.getPackageId() != null ? order.getPackageId().getPackageName() : "");
                tempData.setDoctor_name(order.getDoctorId().getFirstName() + " " + order.getDoctorId().getLastName());
                tempData.setCreated_at(consultation.getCreatedAt().toString());
                tempData.setRating(rating != null ? rating.getRating().toString() : "");
                tempData.setReview(rating != null ? rating.getComment() : "");
                tempData.setIs_rating(isRatingAllowed ? 1 : 0);
                tempData.setPdf_url(pdfUrl);
                tempData.setStatus(order.getCaseId().getRequestType());
                tempData.setCancel_message(order.getCaseId().getCancelMessage());
                tempData.setTotal_count(total);

                orderDataList.add(tempData);
            }

            // Prepare Response
            if (!orderDataList.isEmpty()) {
                return ResponseEntity.status(HttpStatus.OK).body(new Response(
                        Constants.SUCCESS_CODE,
                        Constants.SUCCESS_CODE,
                        messageSource.getMessage(Constants.ORDERS_FETCH_SUCCESSFULLY, null, locale),
                        orderDataList
                ));
            } else {
                return ResponseEntity.status(HttpStatus.OK).body(new Response(
                        Constants.SUCCESS_CODE,
                        Constants.SUCCESS_CODE,
                        messageSource.getMessage(NO_RECORD_FOUND, null, locale)
                ));
            }
        } catch (Exception e) {
            log.error("Error in my-orders api : {}", e);
            return null;
        }
    }

    private StringBuilder getStringBuilder(HealthTipPackageHistoryRequest request) {
        StringBuilder sb = new StringBuilder("SELECT u FROM Orders u WHERE u.patientId.userId = :userId AND u.caseId.caseId IS NOT NULL ");
        if(request.getConsultation_date()!=null) {
            sb.append("AND u.caseId.consultationDate = :consultationDate ");
        }
        if(request.getDoctor_name() != null && !request.getDoctor_name().isEmpty()){
            sb.append("AND (u.doctorId.firstName LIKE :dn OR u.doctorId.lastName LIKE :dn) ");
        }
        if(request.getCase_id() != null && request.getCase_id() != 0){
            sb.append("AND u.caseId.caseId = :caseId ");
        }
        sb.append("ORDER BY u.id DESC");
        return sb;
    }

    public ResponseEntity<?> getResendOTP(Locale locale, ResendOtpRequest request) {
        if(request.getContact_number()==null || request.getContact_number().isEmpty()){
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new Response(
                    NO_CONTENT_FOUNT_CODE,
                    NO_CONTENT_FOUNT_CODE,
                    messageSource.getMessage(Constants.MOBILE_USER_NOT_FOUND,null,locale)
            ));
        }
        Users users = usersRepository.findByContactNumber(request.getContact_number()).orElse(null);
        if(users != null){
            //generate OTP
            Random random = new Random();
            int otp = OTP_FIXED ? 123456 : random.nextInt(900000) + 100000;
            log.info("OTP : {}", otp);

            //save otp into user otp table
            saveOtpIntoUserOtpTableAndUsersTable(users, otp);
            return ResponseEntity.status(HttpStatus.OK).body(new Response(
                    Constants.SUCCESS_CODE,
                    Constants.SUCCESS_CODE,
                    messageSource.getMessage(Constants.OTP_SEND_SUCCESSFUL,null,locale)
            ));
        }
        else{
            return ResponseEntity.status(HttpStatus.OK).body(new Response(
                    Constants.SUCCESS_CODE,
                    Constants.SUCCESS_CODE,
                    messageSource.getMessage(Constants.MOBILE_USER_NOT_FOUND,null,locale)
            ));
        }
    }
    private void saveOtpIntoUserOtpTableAndUsersTable(Users users, int otp) {
        UserOTP otps = new UserOTP();
        otps.setOtp(utility.md5Hash(String.valueOf(otp)));
        otps.setIsFrom(Constants.Login);
        otps.setUserId(users.getUserId());
        otps.setExpiredAt(LocalDateTime.now().plusMinutes(expiryTime));
        otps.setStatus(Constants.STATUS_INACTIVE);
        otps.setType(Constants.PATIENT);

        userOTPRepository.save(otps);

        //save into users table
        users.setOtp(otp);
        users.setOtpTime(Timestamp.valueOf(LocalDateTime.now().plusMinutes(expiryTime)));
        usersRepository.save(users);
    }
    public ResponseEntity<?> getTransactionType(String projectBase, Locale locale) {
        List<KeyValueDto> response = new ArrayList<>();
        if(projectBase!=null && projectBase.equalsIgnoreCase("baano")){
            List<String> sample = List.of("all","consultation","lab","healthtip");
            for(String s:sample){
                try {
                    response.add(new KeyValueDto(
                            messageSource.getMessage("app."+s, null, locale),
                            s));
                }catch (Exception e){ log.error(" while fetching language file ERROR:{}",e.getMessage());}
            }
        }else{
            List<String> sample = List.of("all","consultation","nurse_on_demand","lab","healthtip");
            for(String s:sample){
                try {
                    response.add(new KeyValueDto(
                            messageSource.getMessage("app."+s, null, locale),
                            s));
                }catch (Exception e){ log.error(" while fetching language file ERROR:{}",e.getMessage());}
            }
        }
        return ResponseEntity.status(HttpStatus.OK).body(new Response(
                Constants.SUCCESS_CODE,
                Constants.SUCCESS_CODE,
                messageSource.getMessage(SUCCESS_MESSAGE, null, locale),
                response
        ));
    }
}
