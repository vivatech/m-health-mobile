package com.service.mobile.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.service.mobile.config.Constants;
import com.service.mobile.config.PaymentOptionConfig;
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
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@Slf4j
public class PatientLabService {
    @Autowired
    private WalletTransactionRepository walletTransactionRepository;
    @Autowired
    private GlobalConfigurationRepository globalConfigurationRepository;

    @Autowired
    private LabPriceRepository labPriceRepository;

    @Autowired
    private LabReportRequestRepository labReportRequestRepository;

    @Autowired
    private LabRefundRequestRepository labRefundRequestRepository;

    @Autowired
    private LabReportDocRepository labReportDocRepository;

    @Autowired
    private LabOrdersRepository labOrdersRepository;

    @Autowired
    private LabSubCategoryMasterRepository labSubCategoryMasterRepository;

    @Autowired
    private EVCPlusPaymentService eVCPlusPaymentService;

    @Autowired
    private UsersRepository usersRepository;

    @Autowired
    private LabConsultationRepository labConsultationRepository;

    @Autowired
    private LabCategoryMasterRepository labCategoryMasterRepository;

    @Autowired
    private ConsultationRepository consultationRepository;

    @Autowired
    private MessageSource messageSource;

    @Autowired
    private PublicService publicService;

    @Autowired
    private LanguageService languageService;

    @Autowired
    private SDFSMSService sdfsmsService;

    @Value("${app.currency.symbol.fdj}")
    private String currencySymbolFdj;

    @Value("${app.base.url}")
    private String baseUrl;

    @Value("${app.system.user.id}")
    private Integer SystemUserId;
    @Value("${app.transaction.mode}")
    private Integer transactionMode;
    @Value("${app.ZoneId}")
    private String zone;
    @Autowired
    private PaymentOptionConfig paymentOptionConfig;
    @PersistenceContext
    private EntityManager entityManager;

    public ResponseEntity<?> labRequest(LabRequestDto request, Locale locale) {
        if (request.getUser_id() == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new Response(
                    Constants.UNAUTHORIZED_MSG,
                    Constants.BLANK_DATA_GIVEN_CODE,
                    messageSource.getMessage(Constants.UNAUTHORIZED_MSG, null, locale),
                    new ArrayList<>()
            ));
        }
        try {
            StringBuilder sb = getStringBuilder(request);
            Query query = entityManager.createQuery(sb.toString(), Consultation.class);
            List<Consultation> consultations = query.getResultList();
            long total = consultations.size();
            query.setFirstResult((request.getPage() == null || request.getPage() ==0?0 : request.getPage()) * 5);
            query.setMaxResults(5);
            consultations = query.getResultList();

            if(!consultations.isEmpty()) {
                List<LabRequestsResponse> responses = new ArrayList<>();
                for (Consultation c : consultations) {

                    List<LabOrders> labOrdersList = labOrdersRepository.findByConsultationId(c.getCaseId());
                    List<String> labReportDoc = new ArrayList<>();
                    LabOrders labOrders = null;
                    String status = null;
                    if (!labOrdersList.isEmpty()) {
                        labOrders = labOrdersList.get(0);
                    }
                    if (labOrders != null && labOrders.getPaymentStatus() == OrderStatus.Pending) {
                        status = labOrders.getPaymentStatus().toString();
                    } else if (labOrders != null && labOrders.getPaymentStatus() == OrderStatus.Completed) {
                        List<LabReportDoc> labReportDocs = labReportDocRepository.findByCaseIdAndStatus(c.getCaseId(), Status.A);
                        for (LabReportDoc lrd : labReportDocs) {
                            labReportDoc.add(baseUrl + "lab/" + labOrders.getCaseId().getCaseId() + "/" + lrd.getLabReportDocName());
                        }
                        status = labOrders.getPaymentStatus().toString();
                    } else if (labOrders != null && labOrders.getPaymentStatus() == OrderStatus.Inprogress) {
                        status = labOrders.getPaymentStatus().toString();
                    } else {
                        status = OrderStatus.New.toString();
                    }

                    String requestResultText = "";
                    if (c.getCaseId() != null) {
                        CompleteAndPendingReportsDto countData = publicService.getCompleteAndPendingReports(c.getCaseId());
                        String tempRequestResultText = messageSource.getMessage(Constants.REQUEST_RESULT_TEXT, null, locale);
                        tempRequestResultText = tempRequestResultText.replace("{total}", countData.getCompleted_report().toString());
                        tempRequestResultText = tempRequestResultText.replace("{pending}", countData.getPending_report().toString());
                        requestResultText = tempRequestResultText;
                        if (countData.getCompleted_report() > 0 && countData.getPending_report() > 0) {
                            status = "Partailly Completed";
                        }
                    }
                    LabRequestsResponse data = new LabRequestsResponse();

                    data.setCase_id(c.getCaseId());
                    data.setDoctor_name(c.getDoctorId().getFirstName() + " " + c.getDoctorId().getLastName());
                    data.setDate(c.getConsultationDate());
                    data.setTime(c.getSlotId().getSlotTime());
                    data.setStatus(status);
                    data.setLabReportDoc(labReportDoc);
                    data.setTotal_count(total);
                    data.setRequest_result_text(requestResultText);

                    responses.add(data);
                }

                return ResponseEntity.status(HttpStatus.OK).body(new Response(
                        Constants.SUCCESS_CODE,
                        Constants.SUCCESS_CODE,
                        messageSource.getMessage(Constants.LAB_CONSULTATION_FOUND_SUCCESSFULLY,null,locale),
                        responses
                ));
            }
            else{
                return ResponseEntity.status(HttpStatus.OK).body(new Response(
                        Constants.SUCCESS_CODE,
                        Constants.SUCCESS_CODE,
                        messageSource.getMessage(Constants.NO_RECORD_FOUND, null, locale)
                ));
            }
        }catch (Exception e){
            log.error("Error in lab request api : {}",e);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new Response(
                    Constants.SOMETHING_WENT_WRONG,
                    Constants.NO_CONTENT_FOUNT_CODE,
                    messageSource.getMessage(Constants.SOMETHING_WENT_WRONG, null, locale)
            ));
        }
    }

    private static StringBuilder getStringBuilder(LabRequestDto request) {
        StringBuilder sb = new StringBuilder("SELECT c FROM Consultation c WHERE c.patientId.userId = " + request.getUser_id()
                + " AND c.reportSuggested = '1' AND c.requestType IN (" + RequestType.Book + ")");

        if (request.getName() != null && !request.getName().isEmpty()) {
            sb.append(" AND (c.doctorId.firstName LIKE '%" + request.getName().trim()
                    + "%') OR (c.doctorId.lastName LIKE '%" + request.getName().trim() + "%')");
        }
        if (request.getDate() != null) {
            sb.append(" AND c.consultationDate = DATE(" + request.getDate()+")");
        }
        sb.append(" ORDER BY c.caseId DESC");
        return sb;
    }

    public ResponseEntity<?> addedReports(Integer userId, Locale locale) {
        List<LabConsultation> categoryList = labConsultationRepository.findByPatientIdANdLabOrderIsNullAndCaseIsNull(userId);
        List<AddedReportsDto> list = new ArrayList<>();

        for(LabConsultation data:categoryList){
            try{

                AddedReportsDto response = new AddedReportsDto();
                AddedReportsCategoryDTO categoryDTO = new AddedReportsCategoryDTO();
                AddedReportsSubCategoryDTO subCategoryDTO = new AddedReportsSubCategoryDTO();

                response.setLab_consult_id(data.getLabConsultId());
                response.setCase_id((data.getCaseId()!=null)?data.getCaseId().getCaseId():null);
                response.setLab_orders_id((data.getLabOrdersId()!=null)?data.getLabOrdersId().getId():null);
                response.setCategory_id((data.getCategoryId()!=null)?data.getCategoryId().getCatId():null);
                response.setSub_cat_id(data.getSubCatId());
                response.setRelative_id(null);
                response.setLab_consult_patient_id((data.getPatient()!=null)?data.getPatient().getUserId():null);
                response.setLab_consult_doctor_id((data.getDoctor()!=null)?data.getDoctor().getUserId():null);
                response.setLab_consult_doc_prescription(data.getDoctorPrescription());
                response.setLab_consult_created_at(data.getLabConsultCreatedAt());

                LabCategoryMaster category = data.getCategoryId();
                LabSubCategoryMaster subCat = null;
                if(data.getSubCatId()!=null && data.getSubCatId()!=0){
                    subCat = labSubCategoryMasterRepository.findById(data.getSubCatId()).orElse(null);
                }
                if(category!=null){
                    categoryDTO.setCat_id(category.getCatId());
                    categoryDTO.setCat_name(category.getCatName());
                    categoryDTO.setCat_name_sl(category.getCatNameSl());
                    categoryDTO.setCat_status(category.getCatStatus());
                    categoryDTO.setCat_created_at(category.getCatCreatedAt());
                    categoryDTO.setCat_updated_at(category.getCatUpdatedAt());
                }
                if(subCat!=null) {
                    subCategoryDTO.setSub_cat_id(subCat.getSubCatId());
                    subCategoryDTO.setCat_id(subCat.getLabCategory().getCatId());
                    subCategoryDTO.setSub_cat_name(subCat.getSubCatName());
                    subCategoryDTO.setSub_cat_name_sl(subCat.getSubCatNameSl());
                    subCategoryDTO.setSub_cat_status(subCat.getSubCatStatus());
                    subCategoryDTO.setIs_home_consultant_available(subCat.getIsHomeConsultantAvailable());
                    subCategoryDTO.setSub_cat_created_at(subCat.getSubCatCreatedAt());
                    subCategoryDTO.setSub_cat_updated_at(subCat.getSubCatUpdatedAt());
                }

                response.setCategory(categoryDTO);
                response.setSubcategory(subCategoryDTO);
                list.add(response);
            }catch (Exception e){

            }

        }
        return ResponseEntity.status(HttpStatus.OK).body(new Response(
                Constants.SUCCESS_CODE,
                Constants.SUCCESS_CODE,
                messageSource.getMessage(Constants.ADD_REPORT_FOUND_SUCCESS,null,locale),
                list
        ));
    }

    public ResponseEntity<?> addReports(AddReportRequest request, Locale locale) {

        if (request.getCategory_id() == null){
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new Response(
                    Constants.NO_CONTENT_FOUNT_CODE,
                    Constants.BLANK_DATA_GIVEN_CODE,
                    messageSource.getMessage(Constants.CATEGORY_CANNOT_BLANK, null, locale)
            ));
        }else if(request.getSub_cat_id() == null){
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new Response(
                    Constants.NO_CONTENT_FOUNT_CODE,
                    Constants.BLANK_DATA_GIVEN_CODE,
                    messageSource.getMessage(Constants.SUB_CATEGORY_CANNOT_BLANK, null, locale)
            ));
        }
        else if (request.getUser_id() == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new Response(
                    Constants.NO_RECORD_FOUND_CODE,
                    Constants.BLANK_DATA_GIVEN_CODE,
                    messageSource.getMessage(Constants.BLANK_DATA_GIVEN, null, locale)
            ));
        }
        else {
            List<LabConsultation> consultations = labConsultationRepository.findByPatientIdCategoryIdSubCategoryIdLabOrderAndCaseNull(
                    request.getUser_id(), request.getCategory_id(), request.getSub_cat_id());
            if (consultations.isEmpty()) {
                LabConsultation consultation = new LabConsultation();
                Users patient = usersRepository.findById(request.getUser_id()).orElse(null);
                LabCategoryMaster category = labCategoryMasterRepository.findById(request.getCategory_id()).orElse(null);
                LabSubCategoryMaster subCategory = labSubCategoryMasterRepository.findById(request.getSub_cat_id()).orElse(null);

                consultation.setPatient(patient);
                consultation.setCategoryId(category);
                consultation.setSubCatId((subCategory != null) ? subCategory.getSubCatId() : null);
                consultation.setLabConsultCreatedAt(LocalDateTime.now());
                labConsultationRepository.save(consultation);

                return ResponseEntity.status(HttpStatus.OK).body(new Response(
                        Constants.SUCCESS_CODE,
                        Constants.SUCCESS_CODE,
                        messageSource.getMessage(Constants.RECORD_CREATED_SUCCESS, null, locale)
                ));
            } else {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new Response(
                        Constants.NO_CONTENT_FOUNT_CODE,
                        Constants.NO_CONTENT_FOUNT_CODE,
                        messageSource.getMessage(Constants.RECORD_ALREADY_EXISTS, null, locale)
                ));
            }
        }
    }

    public ResponseEntity<?> deleteAddedReport(DeleteAddedReportRequest request, Locale locale) {
        if(request.getLab_consult_id()!=null && request.getLab_consult_id()!=0){
            LabConsultation consultation = labConsultationRepository.findById(request.getLab_consult_id()).orElse(null);
            if(consultation!=null){
                labConsultationRepository.deleteById(request.getLab_consult_id());
                return ResponseEntity.status(HttpStatus.OK).body(new Response(
                        Constants.SUCCESS_CODE,
                        Constants.SUCCESS_CODE,
                        messageSource.getMessage(Constants.RECORD_DELETED,null,locale)
                ));
            }else{
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new Response(
                        Constants.NO_CONTENT_FOUNT_CODE,
                        Constants.NO_CONTENT_FOUNT_CODE,
                        messageSource.getMessage(Constants.LAB_CONSULTATION_ID_IS_REQUIRED,null,locale)
                ));
            }
        }else {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new Response(
                    Constants.NO_CONTENT_FOUNT_CODE,
                    Constants.NO_CONTENT_FOUNT_CODE,
                    messageSource.getMessage(Constants.LAB_CONSULTATION_ID_IS_REQUIRED,null,locale)
            ));
        }
    }

    public ResponseEntity<?> selectLab(SelectLabRequest request, Locale locale) {
        log.info("Entering into select lab api : {}", request);
        if(StringUtils.isEmpty(request.getUser_id())){
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new Response(
                    Constants.NO_CONTENT_FOUNT_CODE,
                    Constants.NO_CONTENT_FOUNT_CODE,
                    messageSource.getMessage(Constants.BLANK_DATA_GIVEN,null,locale)
            ));
        }
        SelectLabResponse data = new SelectLabResponse();
        List<LabConsultation> consultations = searchLabReportsForPatient(request);

        if (!consultations.isEmpty()) {
            List<Integer> labcatIds = new ArrayList<>();
            List<ReportSubCatDto> categoriesDtos = new ArrayList<>();
            for (LabConsultation consultation : consultations) {
                if (consultation.getSubCatId() != null && consultation.getSubCatId() != 0) {
                    LabSubCategoryMaster subCategoryMaster = labSubCategoryMasterRepository.findById(consultation.getSubCatId()).orElse(null);
                    if (subCategoryMaster != null) {
                        ReportSubCatDto temp = new ReportSubCatDto();
                        temp.setSub_cat_id(subCategoryMaster.getSubCatId());
                        temp.setSub_cat_name(subCategoryMaster.getSubCatName());

                        categoriesDtos.add(temp);
                        labcatIds.add(subCategoryMaster.getSubCatId());
                    }
                }
            }
            List<GetLabDto> labList = publicService.getLabInfo(labcatIds);
            List<LabsDto> labs = new ArrayList<>();
            for (GetLabDto labDto : labList) {
                LabsDto temp = new LabsDto();
                temp.setId(labDto.getUser_id());
                temp.setName(labDto.getClinic_name());
                labs.add(temp);
            }

            ConsultDetailSummaryDto summary = consultDetailSummary(request.getCase_id());

            data.setReports(categoriesDtos);
            data.setLabs(labs);
            data.setSummary(summary);

            return ResponseEntity.status(HttpStatus.OK).body(new Response(
                    Constants.SUCCESS_CODE,
                    Constants.SUCCESS_CODE,
                    messageSource.getMessage(Constants.LAB_FOUND_SUCCESSFULLY, null, locale),
                    data
            ));
        } else {
            return ResponseEntity.status(HttpStatus.OK).body(new Response(
                    Constants.SUCCESS_CODE,
                    Constants.SUCCESS_CODE,
                    messageSource.getMessage(Constants.NO_RECORD_FOUND, null, locale)
            ));
        }
    }

    private ConsultDetailSummaryDto consultDetailSummary(String caseId) {
        Consultation consultation = null;
        if(!StringUtils.isEmpty(caseId)){
            consultation = consultationRepository.findById(Integer.parseInt(caseId)).orElse(null);
        }
        ConsultDetailSummaryDto response = new ConsultDetailSummaryDto();
        if(consultation!=null){
            response.setCase_id(consultation.getCaseId());
            String[] explode = consultation.getSlotId().getSlotTime().split(":");
            response.setTime(explode[0]+":"+explode[1]+"-"+explode[2]+":"+explode[3]);
            response.setDate(consultation.getConsultationDate());
            response.setDoctor_name(consultation.getDoctorId().getFirstName()+ " "+consultation.getDoctorId().getLastName());
            LabOrders labOrders = labOrdersRepository.findByConsultationId(consultation.getCaseId()).get(0);
            if(labOrders != null){
                response.setStatus(labOrders.getPaymentStatus().name());
            }else response.setStatus("New");
        }
        return response;
    }

    public List<LabConsultation> searchLabReportsForPatient(SelectLabRequest request){
        List<LabConsultation> consultations = new ArrayList<>();
        if(!StringUtils.isEmpty(request.getCase_id())){
            consultations = labConsultationRepository.findByCaseId(Integer.parseInt(request.getCase_id()));
        }else{
            consultations = labConsultationRepository.findByPatientId(Integer.parseInt(request.getUser_id()));
        }
        return consultations;
    }

    public ResponseEntity<?> getLabs(GetLabsRequest request, Locale locale) {
        GetLabsResponse response = new GetLabsResponse();
        List<GetLabDto> data = publicService.getLabInfo(request.getReport_id());
        List<LabsDto> result = new ArrayList<>();
        for(GetLabDto temp:data){
            LabsDto t = new LabsDto();
            t.setName(temp.getClinic_name());
            t.setId(temp.getUser_id());
            result.add(t);
        }
        response.setLabs(result);
        return ResponseEntity.status(HttpStatus.OK).body(new Response(
                Constants.SUCCESS_CODE,
                Constants.SUCCESS_CODE,
                messageSource.getMessage(Constants.LAB_FOUND_SUCCESSFULLY,null,locale),
                response
        ));
    }

    public ResponseEntity<?> getBillInfo(BillInfoRequest request, Locale locale) {
        if(request.getLab_id()==null){
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new Response(
                    Constants.BLANK_DATA_GIVEN_CODE,
                    Constants.BLANK_DATA_GIVEN_CODE,
                    messageSource.getMessage(Constants.LAB_ID_REQUIRED,null,locale)
            ));
        }else if(request.getReport_id().isEmpty()){
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new Response(
                    Constants.BLANK_DATA_GIVEN_CODE,
                    Constants.BLANK_DATA_GIVEN_CODE,
                    messageSource.getMessage(Constants.REPORT_ID_REQUIRED,null,locale)
            ));
        }else{
            BillInfoDto dto = publicService.getBillInfo(request.getLab_id(),request.getReport_id(),request.getCollection_mode(),request.getCurrency_option(), locale);
            return ResponseEntity.status(HttpStatus.OK).body(new Response(
                    Constants.SUCCESS_CODE,
                    Constants.SUCCESS_CODE,
                    messageSource.getMessage(Constants.SUCCESS_MESSAGE,null,locale),
                    dto
            ));
        }
    }

    public ResponseEntity<?> selectTimeSlot(BillInfoRequest request, Locale locale) {
        if (request.getUser_id() == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new Response(
                    Constants.UNAUTHORIZED_MSG,
                    Constants.BLANK_DATA_GIVEN_CODE,
                    messageSource.getMessage(Constants.UNAUTHORIZED_MSG, null, locale)
            ));
        }
        try {
            //consultation details
            ConsultDetailSummaryDto summary = new ConsultDetailSummaryDto();
            if (request.getCase_id() != null) {
                summary = getConsultDetailSummary(request.getCase_id(), locale);
            }
            //user details
            Users userdata = usersRepository.findById(request.getUser_id()).orElse(null);

            List<PaymentMethodResponse.Option> paymentMethod = new ArrayList<>();

            if (request.getReport_id() != null && !request.getReport_id().isEmpty()) {
                List<ReportSubCatDto> labVisitOnly = publicService.checkHomeVisit(request.getReport_id());
                if (labVisitOnly.isEmpty()) {
                    paymentMethod.add(new PaymentMethodResponse.Option("Pay_Home", "Pay at Home"));
                }
            }
            //add payment method
            List<PaymentMethodResponse.Option> getPaymentMethod = publicService.getPaymentMethod();
            paymentMethod.addAll(getPaymentMethod);

            Map<String, Object> userData = new HashMap<>();
            userData.put("address", userdata.getResidenceAddress());
            userData.put("contact_number", userdata.getContactNumber());
            userData.put("country_code", userdata.getCountryCode());

            SelectTimeSlotResponseDto responseData = new SelectTimeSlotResponseDto();
            responseData.setUserdata(userData);
            responseData.setSummary(summary);
            responseData.setPayment_method(paymentMethod);

            return ResponseEntity.status(HttpStatus.OK).body(new Response(
                    Constants.SUCCESS_CODE,
                    Constants.SUCCESS_CODE,
                    messageSource.getMessage(Constants.SUCCESS_MESSAGE, null, locale),
                    responseData
            ));
        } catch (Exception e) {
            log.error("Error while selecting lab : {}", e);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new Response(
                    Constants.UNAUTHORIZED_MSG,
                    Constants.NO_CONTENT_FOUNT_CODE,
                    e.getMessage()
            ));
        }
    }

    private ConsultDetailSummaryDto getConsultDetailSummary(Integer caseId, Locale locale) {
        try {
            Consultation consultation = consultationRepository.findById(caseId).orElse(null);
            ConsultDetailSummaryDto response = new ConsultDetailSummaryDto();
            if (consultation != null) {
                String[] consultTime = consultation.getSlotId().getSlotTime().split(":");
                response.setCase_id(consultation.getCaseId());
                response.setTime(consultTime[0] + ":" + consultTime[1] + " - " + consultTime[2] + ":" + consultTime[3]);
                response.setDate(consultation.getConsultationDate());
                response.setDoctor_name(consultation.getDoctorId() != null
                        ? consultation.getDoctorId().getFirstName() + " " + consultation.getDoctorId().getLastName()
                        : "-");

                List<LabOrders> labOrders = labOrdersRepository.findByConsultationId(caseId);
                if (!labOrders.isEmpty()) {
                    LabOrders order = labOrders.get(0);
                    response.setStatus(messageSource.getMessage(order.getPaymentStatus().toString(), null, locale));
                } else response.setStatus(messageSource.getMessage("New", null, locale));
            }
            return response;
        } catch (Exception e) {
            log.error("Error in consultation : {}", e);
            return null;
        }
    }


    public ResponseEntity<?> addLabRequest(AddLabRequestDto data, Locale locale) throws JsonProcessingException {
        if(data.getUser_id() == null || data.getLab_id() == null || data.getSub_cat_id() == null || data.getSub_cat_id().isEmpty()){
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new Response(
                    Constants.NO_CONTENT_FOUNT_CODE,
                    Constants.NO_CONTENT_FOUNT_CODE,
                    messageSource.getMessage(Constants.NO_CONTENT_FOUNT,null,locale)
            ));
        }
        try{
            List<Integer> subCatId = data.getSub_cat_id();
            // Check only for doctor prescribed request
            Consultation consultDetail = null;
            List<LabConsultation> checkConsultation = new ArrayList<>();
            Integer caseId = null;
            if(data.getCase_id() != null){
                caseId = data.getCase_id();
                consultDetail = consultationRepository.findById(data.getCase_id()).orElse(null);
                checkConsultation = labConsultationRepository.findBySubCategoryIdCaseIdLadIdNotNull(subCatId,caseId);
            }

            Users labUser = usersRepository.findById(Integer.valueOf(data.getLab_id())).orElse(null);
            Users patient = usersRepository.findById(data.getUser_id()).orElse(null);

            String currencyOption = data.getCurrency_option() != null ? data.getCurrency_option() : "USD";

            LabOrders labOrder = new LabOrders();
            labOrder.setCaseId(data.getCase_id() == null ? null : consultDetail);
            labOrder.setPatientId(patient);
            labOrder.setLab(labUser);
            labOrder.setReportDate(data.getReport_date());
            labOrder.setReportTimeSlot(data.getReport_time_slot());
            labOrder.setPaymentMethod(data.getPayment_method());
            labOrder.setAddress(data.getAddress());
            labOrder.setSampleCollectionMode(data.getSample_collection_mode());
            labOrder.setStatus(OrderStatus.Pending);

            BillInfoDto billData = publicService.getBillInfo(Integer.valueOf(data.getLab_id()), subCatId, data.getSample_collection_mode(),"USD", locale);

            float finalConsultationFees = billData.getTotal();
            Float currencyAmount = 0F;
            if ("slsh".equalsIgnoreCase(currencyOption)) {
                currencyAmount = getSlshAmount(finalConsultationFees);
            }

            Float amount = currencyAmount > 0 ? currencyAmount : finalConsultationFees;
            labOrder.setCurrencyAmount(amount);
            labOrder.setCurrency(currencyOption);

            if (!"Pay_Home".equals(data.getPayment_method())) {
                Users user = patient;
                WalletTransaction userTransaction = getWalletTransaction(data, finalConsultationFees, user);

                if(transactionMode != null && transactionMode == 1){
                    userTransaction.setTransactionStatus("Completed");
                    labOrder.setPaymentStatus(data.getPayment_method().toString().equalsIgnoreCase("Pay_Home")? OrderStatus.Pending : OrderStatus.Completed);
                }
                else if(transactionMode != null && transactionMode ==2){
                    userTransaction.setTransactionStatus("Failed");
                    labOrder.setPaymentStatus(OrderStatus.Failed);
                }
                else {
                    Map<String, Object> transactionDetail = new HashMap<>();
                    transactionDetail.put("ref_transaction_id", userTransaction.getTransactionId());
                    transactionDetail.put("reference_number", userTransaction.getReferenceNumber());

                    Map<String, Object> payment = eVCPlusPaymentService.processPayment(
                            "API_PURCHASE", transactionDetail
                            , amount, data.getPayer_mobile()
                            , patient.getUserId().toString(), currencyOption, "paid_against_lab_report");

                    if (payment != null && !payment.get("status").equals(200)) {
                        userTransaction.setTransactionStatus("Failed");
                        labOrder.setPaymentStatus(OrderStatus.Failed);
                        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new Response(
                                Constants.NO_CONTENT_FOUNT_CODE,
                                Constants.NO_CONTENT_FOUNT_CODE,
                                "Failed"
                        ));
                    } else {
                        userTransaction.setTransactionStatus("Completed");
                        labOrder.setPaymentStatus(data.getPayment_method().toString().equalsIgnoreCase("Pay_Home")? OrderStatus.Pending : OrderStatus.Completed);
                        Integer userTrans = publicService.createTransaction(userTransaction, UserType.Patient, null, null);
                        publicService.addUserWalletBalance(userTrans, patient, UserType.Patient, "CREDIT", finalConsultationFees);
                        patient.setTotalMoney(patient.getTotalMoney() + finalConsultationFees);
                        patient = usersRepository.save(patient);
                    }
                    walletTransactionRepository.save(userTransaction);
                }
            }

            String transactionId = generateDateTime();

            labOrder.setReportCharge(billData.getReportCharge().floatValue());
            labOrder.setExtraCharges(billData.getExtraCharges().floatValue());
            labOrder.setAmount(billData.getTotal().floatValue());
            labOrder.setTransactionId(!"Pay_Home".equals(data.getPayment_method()) ? transactionId : null);
            labOrder.setCreatedAt(LocalDateTime.now(ZoneId.of(zone)));
            labOrder.setUpdatedAt(LocalDateTime.now(ZoneId.of(zone)));
            //values with no mean
            labOrder.setCommission(0.00f);
            labOrder.setCommissionSlsh(0.00f);
            labOrder.setIsTransferred(TransferStatus.NO);

            labOrder = labOrdersRepository.save(labOrder);

            updateLabConsultations(subCatId, caseId, data.getUser_id(), labOrder);
//            if (!"Pay_Home".equals(data.getPayment_method())) {
//                processTransactions(data, finalConsultationFees, transactionId, patient);
//            }

//            sendNotifications(labOrder, data.getSample_collection_mode(), caseId != null);

            return ResponseEntity.status(HttpStatus.OK).body(new Response(
                    Constants.SUCCESS_CODE,
                    Constants.SUCCESS_CODE,
                    messageSource.getMessage(Constants.ORDER_CREATED_SUCCESS,null,locale),
                    new ArrayList<>()
            ));
        }catch (Exception e) {
            e.printStackTrace();
            log.error("Error in add lab request : {}", e.getMessage());
        }
        return null;
    }

    private WalletTransaction getWalletTransaction(AddLabRequestDto data, float finalConsultationFees, Users user) {
        WalletTransaction userTransaction = new WalletTransaction();
        userTransaction.setOrderId(null);
        userTransaction.setAmount(finalConsultationFees);
        userTransaction.setServiceType("lab");
        userTransaction.setTransactionType("wallet_balance_load");
        userTransaction.setTransactionStatus("Pending");
        userTransaction.setIsDebitCredit("debit");
        userTransaction.setPatientId(user);
        userTransaction.setPayerMobile(data.getPayer_mobile());
        userTransaction.setPaymentNumber(data.getPayer_mobile());
        return userTransaction;
    }

//    private void sendNotifications(LabOrders labOrder, String sampleCollectionMode, boolean b) {
//        String lrn_wd_hv_p = languageService.gettingMessages("LAB_REQUEST_NOTIFICATION_PATIENT_HV",
//                labOrder.getPatientId().getFirstName()+" "+labOrder.getPatientId().getLastName(),
//                labOrder.getLab().getClinicName());
//
//        String lrn_wd_hv_l = languageService.gettingMessages("LAB_REQUEST_NOTIFICATION_LAB_HV",
//                labOrder.getLab().getClinicName(),
//                labOrder.getPatientId().getFirstName()+" "+labOrder.getPatientId().getLastName(),
//                labOrder.getAddress(),labOrder.getReportDate());
//
//        String lrn_wd_cv_p = languageService.gettingMessages("LAB_REQUEST_NOTIFICATION_PATIENT_WITH_DOCTOR_CV",
//                labOrder.getPatientId().getFirstName()+" "+labOrder.getPatientId().getLastName(),
//                labOrder.getDoctor().getFirstName()+" "+labOrder.getDoctor().getLastName());
//
//        String lrn_wd_cv_l = languageService.gettingMessages("LAB_REQUEST_NOTIFICATION_LAB_WITH_DOCTOR",
//                labOrder.getLab().getClinicName(),labOrder.getPatientId().getFirstName()+" "+labOrder.getPatientId().getLastName()
//                ,labOrder.getReportDate());
//
//
//
//        String lrn_wod_hv_p = languageService.gettingMessages("LAB_REQUEST_NOTIFICATION_PATIENT_HV",
//                labOrder.getPatientId().getFirstName()+" "+labOrder.getPatientId().getLastName(),labOrder.getLab().getClinicName());
//
//        String lrn_wod_hv_l = languageService.gettingMessages("LAB_REQUEST_NOTIFICATION_LAB_HV",
//                labOrder.getLab().getClinicName(),labOrder.getPatientId().getFirstName()+" "+labOrder.getPatientId().getLastName(),
//                labOrder.getAddress(),labOrder.getReportDate());
//
//        String lrn_wod_cv_p = languageService.gettingMessages("LAB_REQUEST_NOTIFICATION_PATIENT_WITHOUT_DOCTOR_CV",
//                labOrder.getPatientId().getFirstName()+" "+labOrder.getPatientId().getLastName(),
//                labOrder.getAddress(),labOrder.getAddress(),labOrder.getReportDate());
//        String lrn_wod_cv_l = languageService.gettingMessages("LAB_REQUEST_NOTIFICATION_LAB_WITH_DOCTOR",
//                labOrder.getLab().getClinicName(),
//                labOrder.getPatientId().getFirstName()+" "+labOrder.getPatientId().getLastName(),labOrder.getReportDate());
//
//        sdfsmsService.sendOTPSMS(labOrder.getPatientId().getContactNumber(),lrn_wd_hv_p);
//        sdfsmsService.sendOTPSMS(labOrder.getPatientId().getContactNumber(),lrn_wd_cv_p);
//        sdfsmsService.sendOTPSMS(labOrder.getPatientId().getContactNumber(),lrn_wod_hv_p);
//        sdfsmsService.sendOTPSMS(labOrder.getPatientId().getContactNumber(),lrn_wod_cv_p);
//
//        sdfsmsService.sendOTPSMS(labOrder.getLab().getContactNumber(),lrn_wd_hv_l);
//        sdfsmsService.sendOTPSMS(labOrder.getLab().getContactNumber(),lrn_wd_cv_l);
//        sdfsmsService.sendOTPSMS(labOrder.getLab().getContactNumber(),lrn_wod_hv_l);
//        sdfsmsService.sendOTPSMS(labOrder.getLab().getContactNumber(),lrn_wod_cv_l);
//    }

    private void processTransactions(AddLabRequestDto data, Float finalConsultationFees,
                                     String transactionId, Users patient) {
        WalletTransaction userTransaction = new WalletTransaction();
        userTransaction.setOrderId(null);
        userTransaction.setAmount(finalConsultationFees);
        userTransaction.setServiceType("lab");
        userTransaction.setTransactionType("paid_wallet_balance_lab");
        userTransaction.setTransactionStatus("Completed");
        userTransaction.setIsDebitCredit("CREDIT");
        userTransaction.setPatientId(patient);
        userTransaction.setPayerMobile(data.getPayer_mobile());
        userTransaction.setPaymentNumber(data.getPayer_mobile());

        Integer userTrans = publicService.createTransaction(userTransaction,UserType.Patient,null,null);
        publicService.addUserWalletBalance(userTrans,patient,UserType.Patient,"CREDIT",finalConsultationFees);

        Users payeeMobile = usersRepository.findById(SystemUserId).orElse(null);

        WalletTransaction userTransaction2 = new WalletTransaction();
        userTransaction2.setOrderId(null);
        userTransaction2.setAmount(finalConsultationFees);
        userTransaction2.setServiceType("lab");
        userTransaction2.setTransactionType("system_credit_consultation_lab");
        userTransaction2.setTransactionStatus("Completed");
        userTransaction2.setIsDebitCredit("CREDIT");
        userTransaction2.setPatientId(patient);
        userTransaction2.setPayerMobile(data.getPayer_mobile());
        userTransaction2.setPaymentNumber(data.getPayer_mobile());

        Integer userTrans2 = publicService.createTransaction(userTransaction2,UserType.Patient,null,null);
        publicService.addUserWalletBalance(userTrans2,payeeMobile,UserType.Patient,"CREDIT",finalConsultationFees);

        publicService.updateSystemUserWallet(finalConsultationFees,null);

        patient.setTotalMoney(patient.getTotalMoney() + finalConsultationFees);
        usersRepository.save(patient);
    }

    private void updateLabConsultations(List<Integer> subCatId, Integer caseId, Integer userId, LabOrders id) {
        for(Integer i:subCatId){
            List<LabConsultation> labConsultations = new ArrayList<>();
            if(caseId!=null){
                labConsultations = labConsultationRepository.findBySubCategoryIdCaseId(i,caseId);
            }else{
                labConsultations = labConsultationRepository.findBySubCategoryIdCaseIdNullLabOrderNullPatientId(i,userId);
            }
            for(LabConsultation l:labConsultations){
                l.setLabOrdersId(id);
            }
            labConsultationRepository.saveAll(labConsultations);
        }
    }

    String generateDateTime() {
        return new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()) + (int) (Math.random() * 1000);
    }

    private Float getSlshAmount(Float amount) {
        GlobalConfiguration configuration = globalConfigurationRepository.findByKey("WAAFI_PAYMENT_RATE");
        return amount * Float.parseFloat(configuration.getValue());
    }

    public ResponseEntity<?> getLabOrder(GetLabOrderRequest request, Locale locale) {
        if(request.getUser_id() == null || request.getUser_id().isEmpty()
            || request.getPage() == null || request.getPage().isEmpty()){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new Response(
                    Constants.UNAUTHORIZED_MSG,
                    Constants.NO_CONTENT_FOUNT_CODE,
                    messageSource.getMessage(Constants.BLANK_DATA_GIVEN, null, locale)
            ));
        }
        Pageable pageable = PageRequest.of(Integer.parseInt(request.getPage()),10);
        Page<LabOrders> labOrders = labOrdersRepository.findByPatientId(Integer.parseInt(request.getUser_id()),pageable);
        //filter by from and to dates
        if(!StringUtils.isEmpty(request.getFrom_date()) && !StringUtils.isEmpty(request.getTo_date())){
            labOrders = labOrdersRepository.findByPatientIdAndDate(Integer.valueOf(request.getUser_id()), LocalDate.parse(request.getFrom_date()), LocalDate.parse(request.getTo_date()), pageable);
        }
        if(labOrders.getContent().isEmpty()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new Response(
                    Constants.NO_RECORD_FOUND,
                    Constants.NO_CONTENT_FOUNT_CODE,
                    messageSource.getMessage(Constants.NO_LAB_ORDER_FOUND, null, locale),
                    new ArrayList<>()
            ));
        }
        List<OrderDto> dataList = new ArrayList<>();
        for(LabOrders order:labOrders.getContent()){
            LabReportRequest labReportRequest = null;
            if(order.getReportId()!=null && order.getReportId()!=0){
                labReportRequest = labReportRequestRepository.findById(order.getReportId()).orElse(null);
            }
            Map<String, Object> labDetail = new HashMap<>();
            if(order.getLab() != null) {
                labDetail.put("id", order.getLab().getUserId());
                labDetail.put("name", order.getLab().getClinicName());
                labDetail.put("address", order.getLab().getHospitalAddress());
            }

            // Order Details
            OrderDetailsDto orderDetails = new OrderDetailsDto();
            orderDetails.setReport_date(order.getReportDate());
            orderDetails.setReport_time_slot((order.getReportTimeSlot()!=null)?order.getReportTimeSlot().toLowerCase():null);
            orderDetails.setAddress(order.getAddress());

            List<String> reportListArray = new ArrayList<>();
            List<LabConsultation> labConsultations = labConsultationRepository.findByLabOrderId(order.getId());
            for(LabConsultation consultation:labConsultations){
                if(consultation.getSubCatId()!=null && consultation.getSubCatId()!=0){
                    LabSubCategoryMaster subCategoryMaster =
                            labSubCategoryMasterRepository.findById(consultation.getSubCatId()).orElse(null);
                    if(subCategoryMaster!=null){
                        reportListArray.add(subCategoryMaster.getSubCatName());
                    }
                }
            }
            orderDetails.setReportList(reportListArray);

            String currency = currencySymbolFdj;
            orderDetails.setReport_charge(currency + " " + (order.getReportCharge() == null ? 0 : (int)order.getReportCharge().floatValue()));
            orderDetails.setExtra_charges(currency + " " + (order.getExtraCharges() == null ? 0 : (int)order.getExtraCharges().floatValue()));
            orderDetails.setTotal(currency + " " + (int)order.getAmount().floatValue());

            List<String> labReportDoc = new ArrayList<>();
            List<LabReportDoc> docList = labReportDocRepository.findByLabOrderId(order.getId());
            for(LabReportDoc doc:docList){
                labReportDoc.add(baseUrl + "lab/" + order.getCaseId() + "/" + doc.getLabReportDocName());
            }

            orderDetails.setLabReportDoc(labReportDoc);

            // Refund status
            List<LabRefundRequest> labRefundRequest = labRefundRequestRepository.findByLabOrderId(order.getId());
            String refundStatus = "";
            for(LabRefundRequest refund:labRefundRequest){
                if(refund!=null){
                    refundStatus = refund.getStatus().toString();
                }else{
                    refundStatus ="";
                }
            }
            // Order DTO
            OrderDto orderDto = new OrderDto();
            orderDto.setOrder_id(order.getId());
            orderDto.setOrder_amount((int) order.getAmount().floatValue());

            String sampleCollection = null;
            if(!StringUtils.isEmpty(order.getSampleCollectionMode())){
                sampleCollection = order.getSampleCollectionMode().replace("_", " ");
            }
            orderDto.setSample_collection_mode(sampleCollection);
            orderDto.setStatus(order.getStatus());
            orderDto.setCase_id((order.getCaseId()!=null)?order.getCaseId().getCaseId():null);
            orderDto.setLabDetail(labDetail);
            orderDto.setOrderDetails(orderDetails);
            if(order.getLab()!=null){
                orderDto.setLab_name(order.getLab().getClinicName() != null
                        ? order.getLab().getClinicName()
                        : order.getLab().getFirstName() + " " + order.getLab().getLastName());
            }
            if(labReportRequest!=null){
                orderDto.setDoc_prescription((labReportRequest.getLabConsultId()!=null)?labReportRequest.getLabConsultId().getDoctorPrescription():null);
            }
            String createdDate = order.getCreatedAt().toString();
            orderDto.setCreated_at(createdDate.replace("T", " "));
            orderDto.setRefund_status(refundStatus);
            orderDto.setTotal_count(labOrders.getTotalElements());

            dataList.add(orderDto);
        }
        return ResponseEntity.status(HttpStatus.OK).body(new Response(
                Constants.SUCCESS_CODE,
                Constants.SUCCESS_CODE,
                messageSource.getMessage(Constants.LAB_ORDER_FOUND_SUCCESSFULLY,null,locale),
                dataList
        ));
    }

    public ResponseEntity<?> getLabReportsByCaseId(Locale locale, GetSingleRelativeProfileRequest request) {
        log.info("Entering into get-lab-report-by-caseid api : {}", request);
        List<LabConsultation> consultations = new ArrayList<>();
        int userId = Integer.parseInt(request.getUser_id());
        int categoryId = Integer.parseInt(request.getCategory_id());
        int subCategoryId = Integer.parseInt(request.getSubcategory_id());
        int caseId = Integer.parseInt(request.getCase_id());

        if(!StringUtils.isEmpty(request.getCase_id())){
            if(!StringUtils.isEmpty(request.getCategory_id())){
                if(!StringUtils.isEmpty(request.getSubcategory_id())){
                    consultations = labConsultationRepository.findByPatientIdCaseIdCategoryIdSubCategoryId(
                            userId, caseId, categoryId, subCategoryId);
                }else{
                    consultations = labConsultationRepository.findByPatientIdCaseIdCategoryId(
                            userId, caseId, userId);
                }
            }else{
                if(!StringUtils.isEmpty(request.getSubcategory_id())){
                    consultations = labConsultationRepository.findByPatientIdCaseIdSubCategoryId(userId, caseId, subCategoryId);
                }else{
                    consultations = labConsultationRepository.findByPatientIdCaseId(userId, caseId);
                }
            }
        }
        else{
            if(!StringUtils.isEmpty(request.getCategory_id())){
                if(!StringUtils.isEmpty(request.getSubcategory_id())){
                    consultations = labConsultationRepository.findByPatientIdCategoryIdSubCategoryId(userId, categoryId, subCategoryId);
                }else{
                    consultations = labConsultationRepository.findByPatientIdCategoryId(userId, categoryId);
                }
            }else{
                if(!StringUtils.isEmpty(request.getSubcategory_id())){
                    consultations = labConsultationRepository.findByPatientIdSubCategoryId(userId, subCategoryId);
                }else{
                    consultations = labConsultationRepository.findByPatientId(userId);
                }
            }
        }

        Consultation consultation = null;
        if(!StringUtils.isEmpty(request.getCase_id())){
            consultation = consultationRepository.findById(caseId).orElse(null);
        }
        List<LabReportsByCaseIdReportResponse> reports = new ArrayList<>();
        if(!consultations.isEmpty()){
            for(LabConsultation labc:consultations){
                List<LabReportDoc> reportDocs = new ArrayList<>();
                if(labc.getCaseId()!=null){
                    if(labc.getLabOrdersId()!=null){
                        reportDocs = labReportDocRepository.findByCaseIdAndStatusAndLabOrdersId(
                                labc.getCaseId().getCaseId(), Status.A,labc.getLabOrdersId().getId());
                    }else{
                        reportDocs = labReportDocRepository.findByCaseIdAndStatus(
                                labc.getCaseId().getCaseId(), Status.A);
                    }
                }else{
                    if(labc.getLabOrdersId()!=null){
                        reportDocs = labReportDocRepository.findByStatusAndLabOrdersId(
                                Status.A,labc.getLabOrdersId().getId());
                    }else{
                        reportDocs = labReportDocRepository.findByStatus(Status.A);
                    }
                }
                List<LabReportRequest> labReportRequest = labReportRequestRepository.findByLabConsultationId(labc.getLabConsultId());
                List<DocumentDto> documentDtos = new ArrayList<>();
                List<LabReportRequestDto> labReportRequestDtos = new ArrayList<>();
                for(LabReportDoc lrd:reportDocs){
                    DocumentDto temp = new DocumentDto();

                    temp.setReport_doc_id(lrd.getId());
                    temp.setDoc_name(baseUrl+"/uploaded_file/lab/"+lrd.getCaseId()+"/"+lrd.getLabReportDocName());
                    temp.setDoc_display_name(lrd.getLabReportDocDisplayName());
                    temp.setReport_doc_type(lrd.getLabReportDocType());
                    temp.setAdded_type(lrd.getAddedType());
                    temp.setAdded_by(lrd.getAddedBy());
                    temp.setCreated_date(lrd.getCreatedAt().toLocalDate());
                    temp.setCreated_time(lrd.getCreatedAt().toLocalTime());

                    documentDtos.add(temp);
                }

                for(LabReportRequest lrr:labReportRequest){
                    LabReportRequestDto temp = new LabReportRequestDto();
                    LabSubCategoryMaster subCategoryMaster = null;
                    if(labc.getSubCatId()!=null && labc.getSubCatId()!=0){
                        subCategoryMaster = labSubCategoryMasterRepository.findById(labc.getSubCatId()).orElse(null);
                    }
                    List<LabPrice> labPriceList = labPriceRepository.findByLabIdAndCatIdAndSubCatId(
                            lrr.getLabId().getUserId(),
                            labc.getCategoryId().getCatId(),
                            (subCategoryMaster!=null)?subCategoryMaster.getSubCatId():null
                    );
                    LabPrice labPrice = null;
                    for(LabPrice p:labPriceList){
                        labPrice = p;
                    }
                    temp.setReq_id(lrr.getLabReportReqId());
                    temp.setLab_id(lrr.getLabId().getUserId());
                    temp.setLab_name(
                            (lrr.getLabId().getClinicName()!=null
                                    && !lrr.getLabId().getClinicName().isEmpty())?lrr.getLabId().getClinicName():
                                    lrr.getLabId().getFirstName()+" "+lrr.getLabId().getLastName()
                    );
                    temp.setRequest_status(lrr.getLabReportReqStatus());
                    temp.setPayment_status(lrr.getLabReportPaymentStatus());
                    if(labPrice!=null){
                        temp.setLab_price(labPrice.getLabPrice());
                    }
                    labReportRequestDtos.add(temp);
                }

                OrderStatus repStatus = OrderStatus.Pending;
                if(labc.getLabOrdersId()!=null){
                    repStatus = labc.getLabOrdersId().getPaymentStatus();
                }

                List<LabReportDoc> labReportDocs = labReportDocRepository.findByCaseIdAndAddedByAddedTypeAndStatus(
                   caseId, userId, AddedType.Patient,Status.A);
                String userStatus = (!labReportDocs.isEmpty())?"Patient":"Lab";

                LabReportsByCaseIdReportResponse dto = new LabReportsByCaseIdReportResponse();
                LabSubCategoryMaster subCategory = null;
                if(labc.getSubCatId()!=null && labc.getSubCatId()!=0){
                    subCategory = labSubCategoryMasterRepository.findById(labc.getSubCatId()).orElse(null);
                }
                dto.setLab_consult_id(labc.getLabConsultId());
                dto.setCase_id((labc.getCaseId()!=null)?labc.getCaseId().getCaseId():null);
                dto.setCategory_name((labc.getCategoryId()!=null)?labc.getCategoryId().getCatName():null);
                dto.setCategory_id((labc.getCategoryId()!=null)?labc.getCategoryId().getCatId():null);
                dto.setSub_category_name((subCategory!=null)?subCategory.getSubCatName():null);
                dto.setSubcategory_id((subCategory!=null)?subCategory.getSubCatId():null);
                dto.setDoc_prescription(labc.getDoctorPrescription());
                dto.setRep_status(repStatus);
                dto.setUsr_status(userStatus);
                dto.setDocuments(documentDtos);
                dto.setLab_list(labReportRequestDtos);
                dto.setCreated_date(labc.getLabConsultCreatedAt().toLocalDate());
                dto.setCreated_time(labc.getLabConsultCreatedAt().toLocalTime());

                reports.add(dto);
            }
            LabReportsByCaseIdResponse response = new LabReportsByCaseIdResponse();

            response.setReports(reports);
            if(consultation!=null){
                response.setDoctor_name((consultation.getDoctorId()!=null)?consultation.getDoctorId().getFirstName()+" "+consultation.getDoctorId().getLastName():null);
                response.setCase_id(consultation.getCaseId());
                response.setConsultation_date(consultation.getConsultationDate());
            }

            return ResponseEntity.status(HttpStatus.OK).body(new Response(
                    Constants.SUCCESS_CODE,
                    Constants.SUCCESS_CODE,
                    messageSource.getMessage(Constants.LAB_REPORT_FOUND_SUCCESSFULLY,null,locale),
                    response
            ));
        }else{
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new Response(
                    Constants.NO_CONTENT_FOUNT_CODE,
                    Constants.NO_CONTENT_FOUNT_CODE,
                    messageSource.getMessage(Constants.NO_LAB_REPORT_FOUND,null,locale)
            ));
        }
    }
}
