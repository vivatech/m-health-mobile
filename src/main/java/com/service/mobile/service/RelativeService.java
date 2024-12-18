package com.service.mobile.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.service.mobile.config.Constants;
import com.service.mobile.dto.dto.UserRelativeDto;
import com.service.mobile.dto.enums.*;
import com.service.mobile.dto.request.CreateRelativeProfileRequest;
import com.service.mobile.dto.request.GetSingleRelativeProfileRequest;
import com.service.mobile.dto.response.Response;
import com.service.mobile.model.*;
import com.service.mobile.repository.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;

import static com.service.mobile.constants.Constants.*;

@Service
@Slf4j
public class RelativeService {

    @Autowired
    private UserRelativeRepository userRelativeRepository;

    @Autowired
    private MessageSource messageSource;

    @Autowired
    private GlobalConfigurationRepository globalConfigurationRepository;

    @Autowired
    private ConsultationRepository consultationRepository;

    @Autowired
    private SlotMasterRepository slotMasterRepository;

    @Autowired
    private UsersRepository usersRepository;

    @Autowired
    private OrdersRepository ordersRepository;

    @Autowired
    private WalletTransactionRepository walletTransactionRepository;

    @Autowired
    private RefundRequestRepository refundRequestRepository;

    @Autowired
    private HealthTipPackageUserRepository healthTipPackageUserRepository;

    @Value("${app.base.url}")
    private String baseUrl;

    @Value("${storage.location}")
    private String uploadDir;

    @Autowired
    private PublicService publicService;
    @Autowired
    private PackageUserRepository packageUserRepository;
    @Value("${app.ZoneId}")
    private String zone;
    @Value("${app.transaction.mode}")
    private Integer transactionMode;
    @Autowired
    private EVCPlusPaymentService plusPaymentService;

    public ResponseEntity<?> relativeList(Locale locale, Integer userId) {
        List<UserRelative> relatives = userRelativeRepository.findByCreatedBy(userId);
        List<UserRelativeDto> list = new ArrayList<>();
        for(UserRelative data:relatives){
            UserRelativeDto dto = new UserRelativeDto();
            dto.setId(data.getId());
            dto.setUser_id(data.getUserId());
            dto.setName(data.getName());
            dto.setDob(data.getDob());
            dto.setRelation_with_patient(data.getRelationWithPatient());
            dto.setStatus(data.getStatus());
            dto.setCreated_by(data.getCreatedBy());
            dto.setCreated_at(data.getCreatedAt());
            dto.setUpdated_at(data.getUpdatedAt());
            String profile = baseUrl+"/uploaded_file/relatives/"+data.getId()+"/"+data.getProfilePicture();
            dto.setProfile_picture(profile);

            list.add(dto);
        }
        return ResponseEntity.status(HttpStatus.OK).body(new Response(
                Constants.SUCCESS_CODE,
                Constants.SUCCESS_CODE,
                messageSource.getMessage(Constants.SUCCESS_MESSAGE,null,locale),
                list
        ));
    }

    public ResponseEntity<?> createRelativeProfile(Locale locale, CreateRelativeProfileRequest request) throws IOException {
        UserRelative userRelative = new UserRelative();
        userRelative.setUserId(request.getUser_id());
        userRelative.setName(request.getName());
        userRelative.setDob(request.getDob());
        userRelative.setRelationWithPatient(request.getRelation_with_patient());
        userRelative.setCreatedBy(request.getUser_id());
        userRelative.setStatus("A");
        userRelative.setCreatedAt(LocalDateTime.now());
        userRelative.setUpdatedAt(LocalDateTime.now());
        userRelative = userRelativeRepository.save(userRelative);
        if (request.getProfile_picture() != null && !request.getProfile_picture().isEmpty()) {
            validateFile(request.getProfile_picture());
            String fileName = saveProfilePicture(request.getProfile_picture(), userRelative.getId());
            userRelative.setProfilePicture(fileName);
        }

        userRelative = userRelativeRepository.save(userRelative);
        return ResponseEntity.status(HttpStatus.OK).body(new Response(
                Constants.SUCCESS_CODE,
                Constants.SUCCESS_CODE,
                messageSource.getMessage(Constants.SUCCESS_MESSAGE,null,locale),
                userRelative.getId()
        ));
    }

    private void validateFile(MultipartFile file) {
        String[] allowedExtensions = {"gif", "png", "jpg"};
        String fileExtension = file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf(".") + 1);
        if (!Arrays.asList(allowedExtensions).contains(fileExtension)) {
            throw new IllegalArgumentException("profile_pic_ext_not_allowed");
        }

        if (file.getSize() > 5000000) {
            throw new IllegalArgumentException("max_profile_pic_size");
        }
    }

    private String saveProfilePicture(MultipartFile file, Byte userId) throws IOException {
        try {
            String uploadDir = baseUrl+ "/uploaded_file/relatives/" + userId + "/";
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            String filename = StringUtils.cleanPath(file.getOriginalFilename());
            Path filePath = uploadPath.resolve(filename);
            Files.copy(file.getInputStream(), filePath);
            return filename;
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    public ResponseEntity<?> updateRelativeProfile(Locale locale, CreateRelativeProfileRequest request) throws IOException {
        UserRelative userRelative = userRelativeRepository.findById(request.getRelative_id()).orElse(null);
        if(userRelative!=null){
            userRelative.setUserId(request.getUser_id());
            userRelative.setName(request.getName());
            userRelative.setDob(request.getDob());
            userRelative.setRelationWithPatient(request.getRelation_with_patient());
            userRelative.setCreatedBy(request.getUser_id());
            userRelative.setStatus("A");
            userRelative.setCreatedAt(LocalDateTime.now());
            userRelative.setUpdatedAt(LocalDateTime.now());

            if (request.getProfile_picture() != null && !request.getProfile_picture().isEmpty()) {
                validateFile(request.getProfile_picture());
                String fileName = saveProfilePicture(request.getProfile_picture(), userRelative.getId());
                userRelative.setProfilePicture(fileName);
            }

            userRelative = userRelativeRepository.save(userRelative);
            return ResponseEntity.status(HttpStatus.OK).body(new Response(
                    Constants.SUCCESS_CODE,
                    Constants.SUCCESS_CODE,
                    messageSource.getMessage(Constants.SUCCESS_MESSAGE,null,locale),
                    userRelative.getId()
            ));
        }else{
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new Response(
                    Constants.NO_RECORD_FOUND_CODE,
                    Constants.NO_RECORD_FOUND_CODE,
                    messageSource.getMessage(Constants.SOMETHING_WENT_WRONG,null,locale),
                    userRelative.getId()
            ));
        }
    }

    public ResponseEntity<?> getSingleRelativeProfile(Locale locale, GetSingleRelativeProfileRequest request) {
        UserRelative data = userRelativeRepository.findById(request.getId()).orElse(null);
        if(data!=null){
            UserRelativeDto dto = new UserRelativeDto();
            dto.setId(data.getId());
            dto.setUser_id(data.getUserId());
            dto.setName(data.getName());
            dto.setDob(data.getDob());
            dto.setRelation_with_patient(data.getRelationWithPatient());
            dto.setStatus(data.getStatus());
            dto.setCreated_by(data.getCreatedBy());
            dto.setCreated_at(data.getCreatedAt());
            dto.setUpdated_at(data.getUpdatedAt());
            String profile = baseUrl+"/uploaded_file/relatives/"+data.getId()+"/"+data.getProfilePicture();
            dto.setProfile_picture(profile);

            return ResponseEntity.status(HttpStatus.OK).body(new Response(
                    Constants.SUCCESS_CODE,
                    Constants.SUCCESS_CODE,
                    messageSource.getMessage(Constants.SUCCESS_MESSAGE,null,locale),
                    dto
            ));
        }else{
            return ResponseEntity.status(HttpStatus.OK).body(new Response(
                    Constants.SUCCESS_CODE,
                    Constants.SUCCESS_CODE,
                    messageSource.getMessage(Constants.SUCCESS_MESSAGE,null,locale)
            ));
        }
    }

    public ResponseEntity<?> relativeType(Locale locale, GetSingleRelativeProfileRequest request) {
        List<UserRelative> relatives = userRelativeRepository.findAll();
        List<UserRelativeDto> list = new ArrayList<>();
        for(UserRelative data:relatives){
            UserRelativeDto dto = new UserRelativeDto();
            dto.setId(data.getId());
            dto.setUser_id(data.getUserId());
            dto.setName(data.getName());
            dto.setDob(data.getDob());
            dto.setRelation_with_patient(data.getRelationWithPatient());
            dto.setStatus(data.getStatus());
            dto.setCreated_by(data.getCreatedBy());
            dto.setCreated_at(data.getCreatedAt());
            dto.setUpdated_at(data.getUpdatedAt());
            String profile = baseUrl+"/uploaded_file/relatives/"+data.getId()+"/"+data.getProfilePicture();
            dto.setProfile_picture(profile);

            list.add(dto);
        }
        return ResponseEntity.status(HttpStatus.OK).body(new Response(
                Constants.SUCCESS_CODE,
                Constants.SUCCESS_CODE,
                messageSource.getMessage(Constants.SUCCESS_MESSAGE,null,locale),
                list
        ));
    }

    public ResponseEntity<?> cancelConsultation(Locale locale, GetSingleRelativeProfileRequest request) throws JsonProcessingException {
        try {
            String timeLimit = globalConfigurationRepository.findByKey("CANCEL_CONSULT_PATIENT").getValue();
            Consultation consultation = consultationRepository.findById(request.getCase_id()).orElse(null);
            if (consultation != null) {
                SlotMaster slot = consultation.getSlotId();
                String[] timeArray = slot.getSlotTime().split(":");
                LocalDateTime consultationDateTime = LocalDateTime.of(consultation.getConsultationDate(), LocalTime.parse(timeArray[0] + ":" + timeArray[1] + ":00"));
                LocalDateTime now = LocalDateTime.now(ZoneId.of(zone));

                long minutesDifference = ChronoUnit.MINUTES.between(now, consultationDateTime);

                if (minutesDifference <= Integer.parseInt(timeLimit)) {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new Response(
                            Constants.NO_CONTENT_FOUNT_CODE,
                            Constants.NO_CONTENT_FOUNT_CODE,
                            messageSource.getMessage(Constants.CANCEL_REQUEST_ALREADY_LEFT, null, locale)
                    ));
                } else {
                    Long free_consult_cnt = 0L;
                    if (consultation.getConsultType().equalsIgnoreCase("Paid")) {
                        LocalDate newOrderDate = request.getNewOrderDate();
                        List<Consultation> last_consult_datas = consultationRepository.findByRequestTypeAndCreatedAtAndPatientIdAndDoctorIdAndConstaitionTypeAndConstationDate(
                                RequestType.Book, request.getNewOrderDate(), consultation.getPatientId().getUserId(), consultation.getDoctorId().getUserId(),
                                ConsultationType.Paid, consultation.getConsultationDate()
                        );
                        if (!last_consult_datas.isEmpty()) {
                            Consultation last_consult_data = last_consult_datas.get(0);

                            free_consult_cnt = consultationRepository.countByPatientIdAndDoctorIdAndConsultationDateAndConsultationTypeAndRequestType(
                                    consultation.getPatientId(), consultation.getDoctorId().getUserId(),
                                    last_consult_data.getConsultationDate(), RequestType.Cancel, ConsultationType.Free
                            );
                        }
                    }
                    if (consultation.getConsultationType() == ConsultationType.Paid && free_consult_cnt > 0) {
                        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new Response(
                                Constants.NO_CONTENT_FOUNT_CODE,
                                Constants.NO_CONTENT_FOUNT_CODE,
                                messageSource.getMessage(Constants.DONE_FREE_BOOKING, null, locale),
                                new Object()
                        ));
                    } else {
                        if (request.getCancel_message() == null || request.getCancel_message().isEmpty()) {
                            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new Response(
                                    Constants.BLANK_DATA_GIVEN,
                                    Constants.BLANK_DATA_GIVEN_CODE,
                                    messageSource.getMessage(Constants.CANCEL_REQUEST_CANT_BLANK, null, locale)
                            ));
                        }
                        Users users = usersRepository.findById(request.getUser_id()).orElse(null);
                        if (users != null) {
                            if (consultation.getPaymentMethod() != null && (
                                    consultation.getPaymentMethod().equalsIgnoreCase("waafi") ||
                                            consultation.getPaymentMethod().equalsIgnoreCase("zaad") ||
                                            consultation.getPaymentMethod().equalsIgnoreCase("evc")
                            )) {
                                Orders orders = ordersRepository.findByCaseId(request.getCase_id());
                                WalletTransaction exitsTransaction = walletTransactionRepository.findByOrderIdAndServiceType(orders.getId());
                                WalletTransaction transaction = createWalletTransactionEntry(orders, exitsTransaction);

                                Map<String, Object> transactionDetail = new HashMap<>();
                                transactionDetail.put("ref_transaction_id", transaction.getTransactionId());
                                transactionDetail.put("reference_number", transaction.getReferenceNumber()); //

                                if (transactionMode != null && transactionMode == 1) {
                                    transaction.setTransactionId(generateDateTime());
                                    transaction.setTransactionStatus("Complete");
                                    orders.setStatus(OrderStatus.Cancelled);
                                    consultation.setRequestType(RequestType.Cancel);
                                    consultation.setCancelMessage(request.getCancel_message());
                                } else if (transactionMode != null && transactionMode == 2) {
                                    transaction.setTransactionId(generateDateTime());
                                    transaction.setTransactionStatus(CANCELLED);
                                } else {
                                    if (orders.getAmount() > 0) {
                                        Map<String, Object> payment = plusPaymentService.processPayment("API_REFUND", transactionDetail, orders.getAmount(), transaction.getPayerMobile().toString(), consultation.getPatientId().getUserId().toString(), Currency_USD, Service_Type_Consultation);
                                        if (payment.get("status").equals(200)) {
                                            transaction.setTransactionId(generateDateTime());
                                            transaction.setTransactionStatus("Complete");
                                            orders.setStatus(OrderStatus.Cancelled);
                                            consultation.setRequestType(RequestType.Cancel);
                                            consultation.setCancelMessage(request.getCancel_message());
                                        } else {
                                            transaction.setTransactionId(generateDateTime());
                                            transaction.setTransactionStatus(CANCELLED);
                                        }
                                    } else {
                                        transaction.setTransactionId(generateDateTime());
                                        transaction.setTransactionStatus("Complete");
                                        orders.setStatus(OrderStatus.Cancelled);
                                        consultation.setRequestType(RequestType.Cancel);
                                        consultation.setCancelMessage(request.getCancel_message());
                                    }
                                }
                                ordersRepository.save(orders);
                                consultationRepository.save(consultation);
                                walletTransactionRepository.save(transaction);

                                if (transaction.getTransactionStatus().equalsIgnoreCase(CANCELLED)) {
                                    return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new Response(
                                            Constants.NO_CONTENT_FOUNT,
                                            Constants.NO_CONTENT_FOUNT_CODE,
                                            messageSource.getMessage(Constants.NO_TRANSFER_ID_FOUND, null, locale),
                                            new ArrayList<>()
                                    ));
                                }
                                RefundRequest refundRequest = getRefundRequest(orders, transaction);

                                refundRequestRepository.save(refundRequest);

                                //sms
//                            publicService.sendConsultationMsg(consultation,"CANCLE_CONSULT_REQUEST_FROM_PATIENT",UserType.PATIENT);
//                            publicService.sendConsultationMsg(consultation,"CANCLE_CONSULT_REQUEST",UserType.DOCTOR);
//                            publicService.sendConsultationMsg(consultation,"CANCLE_NOTIFICATION_HOSPITAL",UserType.HOSPITAL);
//
                                return ResponseEntity.status(HttpStatus.OK).body(new Response(
                                        Constants.SUCCESS_CODE,
                                        Constants.SUCCESS_CODE,
                                        messageSource.getMessage(Constants.REQUEST_CANCELLED_SUCCESSFULLY, null, locale),
                                        new ArrayList<>()
                                ));
                            } else {
                                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new Response(
                                        Constants.NO_CONTENT_FOUNT,
                                        Constants.NO_CONTENT_FOUNT_CODE,
                                        messageSource.getMessage(Constants.SOMETHING_WENT_WRONG, null, locale)
                                ));
                            }
                        } else {
                            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new Response(
                                    Constants.UNAUTHORIZED_CODE,
                                    Constants.UNAUTHORIZED_CODE,
                                    messageSource.getMessage(Constants.UNAUTHORIZED_MSG, null, locale)
                            ));
                        }
                    }
                }

            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new Response(
                        Constants.NO_CONTENT_FOUNT_CODE,
                        Constants.NO_CONTENT_FOUNT_CODE,
                        messageSource.getMessage(Constants.CANCEL_REQUEST_CANT_BLANK, null, locale)
                ));
            }
        } catch (Exception e) {
            log.error("Error while cancel-consultation : {}",e);
            return null;
        }
    }

    private RefundRequest getRefundRequest(Orders orders, WalletTransaction transaction) {
        RefundRequest refundRequest = new RefundRequest();
        refundRequest.setOrderId(orders);
        refundRequest.setTransactionId(transaction);
        refundRequest.setAmount(transaction.getAmount());
        refundRequest.setPaymentMethod(transaction.getPaymentMethod());
        refundRequest.setStatus(ConsultationStatus.Pending);
        refundRequest.setRejectBy(AddedType.Patient);
        refundRequest.setCreatedAt(LocalDateTime.now(ZoneId.of(zone)));
        return refundRequest;
    }

    private WalletTransaction createWalletTransactionEntry(Orders order, WalletTransaction existTransaction) {
        WalletTransaction transaction = new WalletTransaction();
        transaction.setOrderId(order.getId());
        transaction.setAmount(order.getAmount());
        transaction.setTransactionStatus(Transaction_PENDING);
        transaction.setRefTransactionId(order.getId().toString());

        transaction.setPaymentMethod(existTransaction.getPaymentMethod());
        transaction.setPatientId(order.getPatientId());

        transaction.setPaymentGatewayType(existTransaction.getPaymentGatewayType());
        transaction.setTransactionDate(LocalDateTime.now(ZoneId.of(zone)));
        transaction.setTransactionType(REFUND_TRANSFER);

        transaction.setIsDebitCredit(DEBIT);
        transaction.setPayeeId(order.getPatientId().getUserId()); // payee is Patient id
        transaction.setPayerId(1); //SuperAdmin
        transaction.setReferenceNumber(order.getPatientId().getUserId().toString());  //Since status is same for both case

        Users adminContactNumber = usersRepository.findById(1).orElse(null);
        transaction.setPayeeMobile(order.getPatientId().getContactNumber());
        transaction.setPayerMobile(adminContactNumber.getContactNumber());

        transaction.setCreatedAt(LocalDateTime.now(ZoneId.of(zone)));
        transaction.setUpdatedAt(LocalDateTime.now(ZoneId.of(zone)));


        transaction.setCurrentBalance(0.0F); // by-default
        transaction.setPreviousBalance(0.0f); // by-default
        transaction.setServiceType(Service_Type_Consultation);
        transaction.setTransactionId(generateDateTime());


        return walletTransactionRepository.save(transaction);
    }
    String generateDateTime() {
        return new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()) + (int) (Math.random() * 1000);
    }

    private static RefundRequest getRefundRequest(Orders ordersModels, List<WalletTransaction> getTransactionDetails) {
        RefundRequest refundRequest = new RefundRequest();
        refundRequest.setOrderId(ordersModels);
        refundRequest.setAmount(ordersModels.getAmount());
        refundRequest.setTransactionId(getTransactionDetails.get(0));
        refundRequest.setPaymentMethod(getTransactionDetails.get(0).getPaymentMethod());
        refundRequest.setStatus(ConsultationStatus.Pending);
        refundRequest.setRejectBy(AddedType.Patient);
        return refundRequest;
    }

}
