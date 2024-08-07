package com.service.mobile.service;

import com.service.mobile.config.Constants;
import com.service.mobile.dto.dto.UserRelativeDto;
import com.service.mobile.dto.enums.*;
import com.service.mobile.dto.request.CreateRelativeProfileRequest;
import com.service.mobile.dto.request.GetSingleRelativeProfileRequest;
import com.service.mobile.dto.response.Response;
import com.service.mobile.model.*;
import com.service.mobile.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Service
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
        String fileName = file.getOriginalFilename();
        String filePath = uploadDir + "/uploaded_file/relatives/" + userId + "/";
        File dir = new File(filePath);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        file.transferTo(new File(filePath + fileName));
        return fileName;
    }

    public ResponseEntity<?> updateRelativeProfile(Locale locale, CreateRelativeProfileRequest request) throws IOException {
        UserRelative userRelative = userRelativeRepository.findById(request.getId()).orElse(null);
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

    public ResponseEntity<?> cancelConsultation(Locale locale, GetSingleRelativeProfileRequest request) {
        String timeLimit = globalConfigurationRepository.findByKey("CANCEL_CONSULT_PATIENT").getValue();
        Consultation consultation = consultationRepository.findById(request.getCase_id()).orElse(null);
        if(consultation!=null){
            SlotMaster slot = consultation.getSlotId();
            LocalDateTime consultationDateTime = LocalDateTime.parse(consultation.getConsultationDate() + "T" + slot.getSlotTime() + ":00");
            LocalDateTime now = LocalDateTime.now();

            long minutesDifference = ChronoUnit.MINUTES.between(now, consultationDateTime);

            if (minutesDifference <= Integer.parseInt(timeLimit)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new Response(
                        Constants.NO_CONTENT_FOUNT_CODE,
                        Constants.NO_CONTENT_FOUNT_CODE,
                        messageSource.getMessage(Constants.CANCEL_REQUEST_ALREADY_LEFT,null,locale)
                ));
            }else{
                Long free_consult_cnt = 0L;
                if(consultation.getConsultType().equalsIgnoreCase("Paid")){
                    LocalDate newOrderDate = request.getNewOrderDate();
                    List<Consultation> last_consult_datas = consultationRepository.findByRequestTypeAndCreatedAtAndPatientIdAndDoctorIdAndConstaitionTypeAndConstationDate(
                            RequestType.Book,request.getNewOrderDate(),consultation.getPatientId().getUserId(),consultation.getDoctorId().getUserId(),
                            ConsultationType.Paid,consultation.getConsultationDate()
                    );
                    if(!last_consult_datas.isEmpty()){
                        Consultation last_consult_data = last_consult_datas.get(0);

                        free_consult_cnt = consultationRepository.countByPatientIdAndDoctorIdAndConsultationDateAndConsultationTypeAndRequestType(
                                consultation.getPatientId(),consultation.getDoctorId().getUserId(),
                                last_consult_data.getConsultationDate(),RequestType.Cancel
                        );
                    }
                }
                if(consultation.getConsultationType()==ConsultationType.Paid && free_consult_cnt >0){
                    return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new Response(
                            Constants.NO_CONTENT_FOUNT_CODE,
                            Constants.NO_CONTENT_FOUNT_CODE,
                            messageSource.getMessage(Constants.DONE_FREE_BOOKING,null,locale)
                    ));
                }else{
                    Users users = usersRepository.findById(request.getUser_id()).orElse(null);
                    if(users!=null){
                        if(consultation.getPaymentMethod() !=null && (
                            consultation.getPaymentMethod().equalsIgnoreCase("waafi") ||
                            consultation.getPaymentMethod().equalsIgnoreCase("zaad") ||
                            consultation.getPaymentMethod().equalsIgnoreCase("evc")
                        )) {
                            //TODO-NOTE call the payment method and build remaing logic
                        }
                        consultation.setCancelMessage(request.getCancel_message());
                        consultation.setRequestType(RequestType.Cancel);
                        consultation = consultationRepository.save(consultation);

                        Orders ordersModels = ordersRepository.findByCaseId(request.getCase_id());
                        ordersModels.setStatus(OrderStatus.Cancelled);
                        ordersModels = ordersRepository.save(ordersModels);

                        if(consultation.getConsultType().equalsIgnoreCase("Paid")){
                            if(consultation.getPackageId()==null){
                                List<WalletTransaction> getTransactionDetails = walletTransactionRepository.
                                        findByPatientIdIsDebitList(ordersModels.getPatientId().getUserId(),"Debit");
                                if(!getTransactionDetails.isEmpty()){
                                    RefundRequest refundRequest = new RefundRequest();
                                    refundRequest.setOrderId(ordersModels);
                                    refundRequest.setAmount(ordersModels.getAmount());
                                    refundRequest.setTransactionId(getTransactionDetails.get(0));
                                    refundRequest.setPaymentMethod(getTransactionDetails.get(0).getPaymentMethod());
                                    refundRequest.setStatus(ConsultationStatus.Pending);
                                    refundRequest.setRejectBy(AddedType.Patient);
                                    refundRequestRepository.save(refundRequest);

                                }else{
                                    return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new Response(
                                            Constants.NO_CONTENT_FOUNT_CODE,
                                            Constants.NO_CONTENT_FOUNT_CODE,
                                            messageSource.getMessage(Constants.NO_TRANSFER_ID_FOUND,null,locale)
                                    ));
                                }
                            }else{
                                HealthTipPackageUser packageUser = healthTipPackageUserRepository.findByUserIdAndPackageId(request.getUser_id(),consultation.getPackageId().getPackageId()).orElse(null);
                                if(packageUser!=null){
                                    if(consultation.getConsultType().equalsIgnoreCase("chat")){
                                        packageUser.setTotalChat(
                                                (packageUser.getTotalChat()!=null && packageUser.getTotalChat()>0)?
                                                        packageUser.getTotalChat()+1 : 0);
                                        packageUser = healthTipPackageUserRepository.save(packageUser);
                                    }
                                    if(consultation.getConsultType().equalsIgnoreCase("video")){
                                        packageUser.setTotalVideoCall(
                                                (packageUser.getTotalVideoCall()!=null && packageUser.getTotalVideoCall()>0)?
                                                        packageUser.getTotalVideoCall()+1 : 0);
                                        packageUser = healthTipPackageUserRepository.save(packageUser);
                                    }
                                }
                            }
                            publicService.sendConsultationMsg(consultation,"CANCLE_CONSULT_REQUEST_FROM_PATIENT","PATIENT");
                            publicService.sendConsultationMsg(consultation,"CANCLE_CONSULT_REQUEST","DOCTOR");
                            publicService.sendConsultationMsg(consultation,"CANCLE_NOTIFICATION_HOSPITAL","HOSPITAL");

                            return ResponseEntity.status(HttpStatus.OK).body(new Response(
                                    Constants.SUCCESS_CODE,
                                    Constants.SUCCESS_CODE,
                                    messageSource.getMessage(Constants.REQUEST_CANCELLED_SUCCESSFULLY,null,locale)
                            ));
                        }
                    }else{
                        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new Response(
                                Constants.UNAUTHORIZED_CODE,
                                Constants.UNAUTHORIZED_CODE,
                                messageSource.getMessage(Constants.UNAUTHORIZED_MSG,null,locale)
                        ));
                    }
                }
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new Response(
                        Constants.UNAUTHORIZED_CODE,
                        Constants.UNAUTHORIZED_CODE,
                        messageSource.getMessage(Constants.UNAUTHORIZED_MSG,null,locale)
                ));
            }

        }else{
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new Response(
                    Constants.NO_CONTENT_FOUNT_CODE,
                    Constants.NO_CONTENT_FOUNT_CODE,
                    messageSource.getMessage(Constants.CANCEL_REQUEST_CANT_BLANK,null,locale)
            ));
        }
    }
    
}
