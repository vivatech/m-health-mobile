package com.service.mobile.service;

import com.service.mobile.config.Constants;
import com.service.mobile.dto.dto.*;
import com.service.mobile.dto.enums.*;
import com.service.mobile.dto.request.*;
import com.service.mobile.dto.response.*;
import com.service.mobile.model.*;
import com.service.mobile.repository.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
@Slf4j
public class PatientLabService {
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

    @Value("${app.currency.symbol.fdj}")
    private String currencySymbolFdj;

    @Value("${app.base.url}")
    private String baseUrl;

    public ResponseEntity<?> labRequest(LabRequestDto request, Locale locale) {
        if(request.getName()==null){request.setName("");}
        Page<Consultation> consultations = null;
        Pageable pageable= PageRequest.of(request.getPage(), 5);
        if(request.getDate()!=null){
            consultations = consultationRepository
                    .findByPatientReportSuggestedAndRequestTypeAndNameAndDate(request.getUser_id(),"1", RequestType.Book,request.getName(),request.getDate(),pageable);
        }else {
            consultations = consultationRepository
                    .findByPatientReportSuggestedAndRequestTypeAndName(request.getUser_id(),"1", RequestType.Book,request.getName(),pageable);
        }
        if(consultations!=null && !consultations.getContent().isEmpty()){
            List<LabRequestsResponse> responses = new ArrayList<>();
            for(Consultation c:consultations.getContent()){
                List<LabOrders> labOrdersList = labOrdersRepository.findByConsultationId(c.getCaseId());
                List<String> labReportDoc = new ArrayList<>();
                LabOrders labOrders = null;
                String status = null;
                if(!labOrdersList.isEmpty()){
                    labOrders = labOrdersList.get(0);
                }
                if(labOrders!=null && labOrders.getPaymentStatus()==OrderStatus.Pending){
                    status = labOrders.getPaymentStatus().toString();
                }else if(labOrders!=null && labOrders.getPaymentStatus()==OrderStatus.Completed){
                    List<LabReportDoc> labReportDocs = labReportDocRepository.findByCaseIdAndStatus(c.getCaseId(),Status.A);
                    for(LabReportDoc lrd:labReportDocs){
                        labReportDoc.add(baseUrl + "lab/" + labOrders.getCaseId().getCaseId() + "/" + lrd.getLabReportDocName());
                    }
                    status = labOrders.getPaymentStatus().toString();
                }else if(labOrders!=null && labOrders.getPaymentStatus()==OrderStatus.Inprogress){
                    status = labOrders.getPaymentStatus().toString();
                }else{
                    status = OrderStatus.New.toString();
                }
                String requestResultText = "";
                if(c.getCaseId()!=null){
                    CompleteAndPendingReportsDto countData = publicService.getCompleteAndPendingReports(c.getCaseId());
                    String tempRequestResultText = messageSource.getMessage(Constants.REQUEST_RESULT_TEXT,null,locale);
                    tempRequestResultText = tempRequestResultText.replace("{total}",countData.getCompleted_report().toString());
                    tempRequestResultText = tempRequestResultText.replace("{pending}",countData.getCompleted_report().toString());
                    requestResultText = tempRequestResultText;
                    if(countData.getCompleted_report()>0 && countData.getPending_report()>0){
                        status = "Partailly Completed";
                    }
                }
                LabRequestsResponse data = new LabRequestsResponse();

                data.setCase_id(c.getCaseId());
                data.setDate(c.getConsultationDate());
                data.setTime(c.getSlotId().getSlotTime());
                data.setStatus(status);
                data.setLabReportDoc(labReportDoc);
                data.setTotal_count(consultations.getTotalElements());
                data.setRequest_result_text(requestResultText);

                responses.add(data);

            }
            return ResponseEntity.status(HttpStatus.OK).body(new Response(
                    Constants.SUCCESS_CODE,
                    Constants.SUCCESS_CODE,
                    messageSource.getMessage(Constants.LAB_CONSULTATION_FOUND_SUCCESSFULLY,null,locale),
                    responses
            ));
        }else{
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new Response(
                    Constants.NO_RECORD_FOUND_CODE,
                    Constants.NO_RECORD_FOUND_CODE,
                    messageSource.getMessage(Constants.NO_RECORD_FOUND,null,locale)
            ));
        }


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
                response.setCase_id(data.getCaseId().getCaseId());
                response.setLab_orders_id(data.getLabOrdersId().getId());
                response.setCategory_id(data.getCategoryId().getCatId());
                response.setSub_cat_id(data.getSubCatId().getSubCatId());
                response.setRelative_id(null);
                response.setLab_consult_patient_id(data.getPatient().getUserId());
                response.setLab_consult_doctor_id(data.getDoctor().getUserId());
                response.setLab_consult_doc_prescription(data.getDoctorPrescription());
                response.setLab_consult_created_at(data.getLabConsultCreatedAt());

                LabCategoryMaster category = data.getCategoryId();
                LabSubCategoryMaster subCat = data.getSubCatId();

                categoryDTO.setCat_id(category.getCatId());
                categoryDTO.setCat_name(category.getCatName());
                categoryDTO.setCat_name_sl(category.getCatNameSl());
                categoryDTO.setCat_status(category.getCatStatus());
                categoryDTO.setCat_created_at(category.getCatCreatedAt());
                categoryDTO.setCat_updated_at(category.getCatUpdatedAt());

                subCategoryDTO.setSub_cat_id(subCat.getSubCatId());
                subCategoryDTO.setCat_id(subCat.getLabCategory().getCatId());
                subCategoryDTO.setSub_cat_name(subCat.getSubCatName());
                subCategoryDTO.setSub_cat_name_sl(subCat.getSubCatNameSl());
                subCategoryDTO.setSub_cat_status(subCat.getSubCatStatus());
                subCategoryDTO.setIs_home_consultant_available(subCat.getIsHomeConsultantAvailable());
                subCategoryDTO.setSub_cat_created_at(subCat.getSubCatCreatedAt());
                subCategoryDTO.setSub_cat_updated_at(subCat.getSubCatUpdatedAt());

                response.setCategory(categoryDTO);
                response.setSubcategory(subCategoryDTO);
                list.add(response);
            }catch (Exception e){

            }

        }
        return ResponseEntity.status(HttpStatus.OK).body(new Response(
                Constants.SUCCESS_CODE,
                Constants.SUCCESS_CODE,
                messageSource.getMessage(Constants.ADD_REPORT_FOUND_SUCCESS,null,locale)
        ));
    }

    public ResponseEntity<?> addReports(AddReportRequest request, Locale locale) {
        List<LabConsultation> consultations = labConsultationRepository.findByPatientIdCategoryIdSubCategoryIdLabOrderAndCaseNull(
                request.getUser_id(),request.getCategory_id(),request.getSub_cat_id());
        if(consultations.isEmpty()){
            LabConsultation consultation = new LabConsultation();
            Users patient = usersRepository.findById(request.getUser_id()).orElse(null);
            LabCategoryMaster category = labCategoryMasterRepository.findById(request.getCategory_id()).orElse(null);
            LabSubCategoryMaster subCategory = labSubCategoryMasterRepository.findById(request.getSub_cat_id()).orElse(null);

            consultation.setPatient(patient);
            consultation.setCategoryId(category);
            consultation.setSubCatId(subCategory);
            labConsultationRepository.save(consultation);

            return ResponseEntity.status(HttpStatus.OK).body(new Response(
                    Constants.SUCCESS_CODE,
                    Constants.SUCCESS_CODE,
                    messageSource.getMessage(Constants.RECORD_CREATED_SUCCESS,null,locale)
            ));
        }else{
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new Response(
                    Constants.NO_CONTENT_FOUNT_CODE,
                    Constants.NO_CONTENT_FOUNT_CODE,
                    messageSource.getMessage(Constants.RECORD_ALREADY_EXISTS,null,locale)
            ));
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
        SelectLabResponse data = new SelectLabResponse();
        List<LabConsultation> consultations = searchLabReportsForPatient(request);
        if(!consultations.isEmpty()){
            List<Integer> labcatIds = new ArrayList<>();
            List<ReportSubCatDto> categoriesDtos = new ArrayList<>();
            for(LabConsultation consultation:consultations){
                if(consultation.getSubCatId()!=null){
                    ReportSubCatDto temp = new ReportSubCatDto();
                    temp.setSub_cat_id(consultation.getSubCatId().getSubCatId());
                    temp.setSub_cat_name(consultation.getSubCatId().getSubCatName());

                    categoriesDtos.add(temp);
                    labcatIds.add(consultation.getSubCatId().getSubCatId());
                }
            }
            List<GetLabDto> labList = publicService.getLabInfo(labcatIds);
            List<LabsDto> labs = new ArrayList<>();
            for(GetLabDto labDto:labList){
                LabsDto temp = new LabsDto();
                temp.setId(labDto.getUser_id());
                temp.setName(labDto.getClinic_name());
                labs.add(temp);
            }

            ConsultDetailSummaryDto summary =consultDetailSummary(request.getCase_id(),request.getUser_id());

            data.setReports(categoriesDtos);
            data.setLabs(labs);
            data.setSummary(summary);

            return ResponseEntity.status(HttpStatus.OK).body(new Response(
                    Constants.SUCCESS_CODE,
                    Constants.SUCCESS_CODE,
                    messageSource.getMessage(Constants.LAB_FOUND_SUCCESSFULLY,null,locale),
                    data
            ));
        }else{
            return ResponseEntity.status(HttpStatus.OK).body(new Response(
                    Constants.SUCCESS_CODE,
                    Constants.SUCCESS_CODE,
                    messageSource.getMessage(Constants.NO_RECORD_FOUND,null,locale)
            ));
        }
    }

    private ConsultDetailSummaryDto consultDetailSummary(Integer caseId, Integer userId) {
        Consultation consultation = consultationRepository.findById(caseId).orElse(null);
        ConsultDetailSummaryDto response = new ConsultDetailSummaryDto();
        if(consultation!=null){
            response.setCase_id(caseId);
            String[] explode = consultation.getSlotId().getSlotTime().split(":");
            response.setTime(explode[0]+":"+explode[1]+"-"+explode[2]+":"+explode[3]);
            response.setDate(consultation.getConsultationDate());
            response.setDoctor_name(consultation.getDoctorId().getFirstName()+ " "+consultation.getDoctorId().getLastName());
            List<LabOrders> labOrders = labOrdersRepository.findByConsultationId(consultation.getCaseId());
            OrderStatus status = OrderStatus.New;
            for(LabOrders orders:labOrders){
                if(orders.getPaymentStatus()== OrderStatus.Pending){ status = OrderStatus.Pending; }
                else if(orders.getPaymentStatus()== OrderStatus.Completed){ status = OrderStatus.Completed; }
                else if(orders.getPaymentStatus()== OrderStatus.Inprogress){ status = OrderStatus.Inprogress; }
                else{status = OrderStatus.New; }
            }
            response.setStatus(status);
        }
        return response;
    }

    public List<LabConsultation> searchLabReportsForPatient(SelectLabRequest request){
        List<LabConsultation> consultations = new ArrayList<>();
        if(request.getCase_id()!=null && request.getCase_id()!=0){
            consultations = labConsultationRepository.findByCaseId(request.getCase_id());
        }else{
            consultations = labConsultationRepository.findByPatientId(request.getUser_id());
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
            return ResponseEntity.status(HttpStatus.NO_CONTENT).body(new Response(
                    Constants.BLANK_DATA_GIVEN_CODE,
                    Constants.BLANK_DATA_GIVEN_CODE,
                    messageSource.getMessage(Constants.LAB_ID_REQUIRED,null,locale)
            ));
        }else if(request.getReport_id().isEmpty()){
            return ResponseEntity.status(HttpStatus.NO_CONTENT).body(new Response(
                    Constants.BLANK_DATA_GIVEN_CODE,
                    Constants.BLANK_DATA_GIVEN_CODE,
                    messageSource.getMessage(Constants.REPORT_ID_REQUIRED,null,locale)
            ));
        }else{
            BillInfoDto dto = publicService.getBillInfo(request.getLab_id(),request.getReport_id(),request.getCollection_mode(),request.getCurrency_option());
            List<ReportDto> report = new ArrayList<>();
            int i = 0;
            for (Map.Entry<Integer, String> entry : dto.getReportName().entrySet()) {
                report.add(new ReportDto(entry.getKey(), entry.getValue()));
                i++;
            }
            dto.setReportNameDto(report);
            return ResponseEntity.status(HttpStatus.OK).body(new Response(
                    Constants.SUCCESS_CODE,
                    Constants.SUCCESS_CODE,
                    messageSource.getMessage(Constants.SUCCESS_MESSAGE,null,locale),
                    dto
            ));
        }
    }

    public ResponseEntity<?> selectTimeSlot(BillInfoRequest request, Locale locale) {
        Integer userId = request.getUser_id();
        Integer caseId = request.getCase_id();
        ConsultDetailSummaryDto summary = getConsultDetailSummary(caseId);
        List<ConsultDetailSummaryDto> summaryDtos = new ArrayList<>();
        summaryDtos.add(summary);

        Users userdata = usersRepository.findById(userId).orElse(null);

        List<Integer> getSubCatIds = request.getReport_id();
        List<PaymentMethodResponse.Option> paymentMethod = new ArrayList<>();

        if (getSubCatIds != null && !getSubCatIds.isEmpty()) {
            List<ReportSubCatDto> labVisitOnly = publicService.checkHomeVisit(getSubCatIds);
            if (labVisitOnly.isEmpty()) {
                paymentMethod.add(new PaymentMethodResponse.Option("Pay_Home", "Pay at Home"));
            }
        }

        List<PaymentMethodResponse.Option> getPaymentMethod = publicService.getPaymentMethod();
        paymentMethod.addAll(getPaymentMethod);

        ProfileDto userData = new ProfileDto();
        userData.setAddress(userdata.getResidenceAddress());
        userData.setContact_number(userdata.getContactNumber());
        userData.setCountry_code(userdata.getCountryCode());


        SelectTimeSlotResponseDto responseData = new SelectTimeSlotResponseDto();
        responseData.setUserdata(userData);
        responseData.setSummary(summaryDtos);
        responseData.setPayment_method(paymentMethod);

        return ResponseEntity.status(HttpStatus.OK).body(new Response(
                Constants.SUCCESS_CODE,
                Constants.SUCCESS_CODE,
                messageSource.getMessage(Constants.SUCCESS_MESSAGE,null,locale),
                responseData
        ));
    }

    private ConsultDetailSummaryDto getConsultDetailSummary(Integer caseId) {
        List<LabOrders> list = labOrdersRepository.findByConsultationId(caseId);
        for(LabOrders orders:list){
            Consultation consultation = orders.getCaseId();
            if(consultation !=null){
                ConsultDetailSummaryDto response = new ConsultDetailSummaryDto();
                if (consultation != null) {
                    String[] consultTime = consultation.getSlotId().getSlotTime().split(":");
                    response.setCase_id(consultation.getCaseId());
                    response.setTime(consultTime[0] + ":" + consultTime[1] + " - " + consultTime[2] + ":" + consultTime[3]);
                    response.setDate(consultation.getConsultationDate());
                    response.setDoctor_name(consultation.getDoctorId() != null
                            ? consultation.getDoctorId().getFirstName() + " " + consultation.getDoctorId().getLastName()
                            : "-");

                    OrderStatus status;
                    if(orders.getPaymentStatus()== OrderStatus.Pending){ status = OrderStatus.Pending; }
                    else if(orders.getPaymentStatus()== OrderStatus.Completed){ status = OrderStatus.Completed; }
                    else if(orders.getPaymentStatus()== OrderStatus.Inprogress){ status = OrderStatus.Inprogress; }
                    else{status = OrderStatus.New; }
                    response.setStatus(status);
                }
                return response;
            }
        }
        return null;
    }


    public ResponseEntity<?> addLabRequest(AddLabRequestDto data, Locale locale) {
        Integer caseId = data.getCase_id();
        List<Integer> subCatId = data.getSub_cat_id();
        Consultation consultDetail = consultationRepository.findById(data.getCase_id()).orElse(null);
        Users labUser = usersRepository.findById(data.getLab_id()).orElse(null);
        Users patient = usersRepository.findById(data.getUser_id()).orElse(null);

        // Check only for doctor prescribed request
        List<LabConsultation> checkConsultation = labConsultationRepository.findBySubCategoryIdCaseIdLadIdNotNull(subCatId,caseId);

        if (checkConsultation.isEmpty() || caseId == null) {
            String currencyOption = data.getCurrency_option() != null ? data.getCurrency_option() : "USD";
            LabOrders labOrder = new LabOrders();
            labOrder.setCaseId(consultDetail);
            labOrder.setLab(labUser);
            labOrder.setReportDate(data.getReport_date());
            labOrder.setReportTimeSlot(data.getReport_time_slot());
            labOrder.setPaymentMethod(data.getPayment_method());
            labOrder.setAddress(data.getAddress());
            labOrder.setSampleCollectionMode(data.getSample_collection_mode());

            BillInfoDto billData = publicService.getBillInfo(data.getLab_id(), subCatId, data.getSample_collection_mode(),"");

            Float finalConsultationFees = billData.getTotal();
            Float currencyAmount = 0F;
            if ("slsh".equalsIgnoreCase(currencyOption)) {
                currencyAmount = getSlshAmount(finalConsultationFees);
            }

            Float amount = currencyAmount > 0 ? currencyAmount : finalConsultationFees;
            labOrder.setCurrencyAmount(amount);
            labOrder.setCurrency(currencyOption);

            if (!"Pay_Home".equals(data.getPayment_method())) {
                Users user = patient;

                OrderPaymentResponse payment = publicService.orderPayment(user.getUserId(), amount, 0, currencyOption, "evc",null, data.getPayer_mobile());

                if (payment != null && payment.getStatus() == 100) {
                    return buildErrorResponse("403", payment.getMessage());
                } else {
                    eVCPlusPaymentService.processPayment(user, finalConsultationFees, payment, data.getPayer_mobile());
                }
            }

            String transactionId = String.valueOf(System.currentTimeMillis());
            labOrder.setPatientId(patient);
            labOrder.setDoctor(consultDetail != null ? consultDetail.getDoctorId() : null);
            labOrder.setLab(labUser);
            labOrder.setReportCharge(billData.getReportCharge());
            labOrder.setExtraCharges(billData.getExtraCharges());
            labOrder.setAmount(billData.getTotal());
            labOrder.setPaymentStatus(data.getPayment_method().toString().equalsIgnoreCase("Pay_Home")? OrderStatus.Pending : OrderStatus.Completed);
            labOrder.setTransactionId(!"Pay_Home".equals(data.getPayment_method()) ? transactionId : null);
            labOrder.setReportDate(data.getReport_date());
            labOrder.setAddress(data.getAddress());
            labOrder.setSampleCollectionMode(data.getSample_collection_mode());
            labOrder.setReportTimeSlot(data.getReport_time_slot());
            labOrder.setPaymentMethod(data.getPayment_method());
            labOrdersRepository.save(labOrder);

            updateLabConsultations(subCatId, caseId, data.getUser_id(), labOrder.getId());
            if (!"Pay_Home".equals(data.getPayment_method())) {
                processTransactions(data, finalConsultationFees, transactionId, patient);
            }

            sendNotifications(labOrder, data.getSampleCollectionMode(), caseId != null);

            return ResponseEntity.status(HttpStatus.OK).body(new Response(
                    Constants.SUCCESS_CODE,
                    Constants.SUCCESS_CODE,
                    messageSource.getMessage(Constants.ORDER_CREATED_SUCCESS,null,locale)
            ));
        }
        else {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new Response(
                    Constants.NO_CONTENT_FOUNT_CODE,
                    Constants.NO_CONTENT_FOUNT_CODE,
                    messageSource.getMessage(Constants.ORDER_ALREADY_CREATED,null,locale)
            ));
        }
    }

    private Float getSlshAmount(Float amount) {
        GlobalConfiguration configuration = globalConfigurationRepository.findByKey("WAAFI_PAYMENT_RATE");
        return amount * Float.parseFloat(configuration.getValue());
    }

    public ResponseEntity<?> getLabOrder(GetLabOrderRequest request, Locale locale) {
        Pageable pageable = PageRequest.of(request.getPage(),10);
        List<LabOrders> orders = new ArrayList<>();
        Long total = 0l;
        if(request.getFrom_date()!=null){
            if(request.getTo_date()==null){ request.setTo_date(LocalDate.now()); }
            Page<LabOrders> data = labOrdersRepository.findByPatientIdAndDate(
                    request.getUser_id(),
                    request.getFrom_date(),
                    request.getTo_date(),
                    pageable
            );
            orders = data.getContent();
            total = data.getTotalElements();
        }else{
            Page<LabOrders> data = labOrdersRepository.findByPatientId(request.getUser_id(),pageable);
            orders = data.getContent();
            total = data.getTotalElements();
        }
        if(!orders.isEmpty()){
            List<OrderDto> dataList = new ArrayList<>();
            for(LabOrders order:orders){
                LabReportRequest labReportRequest = order.getReportId();
                LabDetailDto labDetail = new LabDetailDto();
                labDetail.setId(order.getLab().getUserId());
                labDetail.setName(order.getLab().getClinicName());
                labDetail.setAddress(order.getLab().getHospitalAddress());

                // Order Details
                OrderDetailsDto orderDetails = new OrderDetailsDto();
                orderDetails.setReport_date(order.getReportDate());
                orderDetails.setReport_time_slot(order.getReportTimeSlot().toLowerCase());
                orderDetails.setAddress(order.getAddress());

                List<String> reportListArray = new ArrayList<>();
                List<LabConsultation> labConsultations = labConsultationRepository.findByLabOrderId(order.getId());
                for(LabConsultation consultation:labConsultations){
                    reportListArray.add(consultation.getSubCatId().getSubCatName());
                }
                orderDetails.setReportList(reportListArray);

                String currency = currencySymbolFdj;
                orderDetails.setReport_charge(currency + " " + order.getReportCharge());
                orderDetails.setExtra_charges(currency + " " + order.getExtraCharges());
                orderDetails.setTotal(currency + " " + order.getAmount());

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
                orderDto.setOrder_amount(order.getAmount());
                orderDto.setSample_collection_mode(order.getSampleCollectionMode().toLowerCase());
                orderDto.setStatus(order.getStatus());
                orderDto.setCase_id(order.getCaseId().getCaseId());
                orderDto.setLabDetail(labDetail);
                orderDto.setOrderDetails(orderDetails);
                orderDto.setLab_name(order.getLab().getClinicName() != null
                        ? order.getLab().getClinicName()
                        : order.getLab().getFirstName() + " " + order.getLab().getLastName());
                orderDto.setDoc_prescription( labReportRequest.getLabConsultId().getDoctorPrescription());
                orderDto.setCreated_at(order.getCreatedAt());
                orderDto.setRefund_status(refundStatus);
                orderDto.setTotal_count(total);

                dataList.add(orderDto);
            }
            return ResponseEntity.status(HttpStatus.OK).body(new Response(
                    Constants.SUCCESS_CODE,
                    Constants.SUCCESS_CODE,
                    messageSource.getMessage(Constants.LAB_ORDER_FOUND_SUCCESSFULLY,null,locale),
                    dataList
            ));
        }else{
            return ResponseEntity.status(HttpStatus.OK).body(new Response(
                    Constants.SUCCESS_CODE,
                    Constants.SUCCESS_CODE,
                    messageSource.getMessage(Constants.NO_LAB_ORDER_FOUND,null,locale)
            ));
        }
    }

    public ResponseEntity<?> getLabReportsByCaseId(Locale locale, GetSingleRelativeProfileRequest request) {
        List<LabConsultation> consultations = new ArrayList<>();
        if(request.getCategory_id()!=null && request.getCategory_id()!=0){
            if(request.getSubcategory_id()!=null && request.getSubcategory_id()!=0){
                consultations = labConsultationRepository.findByPatientIdCaseIdCategoryIdSubCategoryId(
                        request.getUser_id(),request.getCase_id(),
                        request.getCategory_id(),request.getSubcategory_id()
                );
            }else{
                consultations = labConsultationRepository.findByPatientIdCaseIdCategoryId(
                        request.getUser_id(),request.getCase_id(),
                        request.getCategory_id()
                );
            }
        }else{
            if(request.getSubcategory_id()!=null && request.getSubcategory_id()!=0){
                consultations = labConsultationRepository.findByPatientIdCaseIdSubCategoryId(
                        request.getUser_id(),request.getCase_id(),
                        request.getSubcategory_id()
                );
            }else{
                consultations = labConsultationRepository.findByPatientIdCaseId(
                        request.getUser_id(),request.getCase_id()
                );
            }
        }

        Consultation consultation = consultationRepository.findById(request.getCase_id()).orElse(null);
        List<LabReportsByCaseIdReportResponse> reports = new ArrayList<>();
        if(!consultations.isEmpty()){
            for(LabConsultation labc:consultations){
                List<LabReportDoc> reportDocs = new ArrayList<>();
                if(labc.getLabOrdersId()!=null){
                    reportDocs = labReportDocRepository.findByCaseIdAndStatusAndLabOrdersId(
                            labc.getCaseId().getCaseId(), Status.A,labc.getLabOrdersId().getId());
                }else{
                    reportDocs = labReportDocRepository.findByCaseIdAndStatus(
                            labc.getCaseId().getCaseId(), Status.A);
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
                    List<LabPrice> labPriceList = labPriceRepository.findByLabIdAndCatIdAndSubCatId(
                            lrr.getLabId().getUserId(),
                            labc.getCategoryId().getCatId(),
                            labc.getSubCatId().getSubCatId()
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
                    request.getCase_id(),request.getUser_id(), AddedType.Patient,Status.A
                );
                String userStatus = (!labReportDocs.isEmpty())?"Patient":"Lab";

                LabReportsByCaseIdReportResponse dto = new LabReportsByCaseIdReportResponse();
                dto.setLab_consult_id(labc.getLabConsultId());
                dto.setCase_id(labc.getCaseId().getCaseId());
                dto.setCategory_name(labc.getCategoryId().getCatName());
                dto.setCategory_id(labc.getCategoryId().getCatId());
                dto.setSub_category_name(labc.getSubCatId().getSubCatName());
                dto.setSubcategory_id(labc.getSubCatId().getSubCatId());
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
            response.setDoctor_name(consultation.getDoctorId().getFirstName()+" "+consultation.getDoctorId().getLastName());
            response.setCase_id(consultation.getCaseId());
            response.setConsultation_date(consultation.getConsultationDate());

            return ResponseEntity.status(HttpStatus.OK).body(new Response(
                    Constants.SUCCESS_CODE,
                    Constants.SUCCESS_CODE,
                    messageSource.getMessage(Constants.LAB_REPORT_FOUND_SUCCESSFULLY,null,locale)
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
