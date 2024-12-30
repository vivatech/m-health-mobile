package com.service.mobile.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.service.mobile.config.Constants;
import com.service.mobile.dto.dto.*;
import com.service.mobile.dto.enums.*;
import com.service.mobile.dto.enums.State;
import com.service.mobile.dto.request.*;
import com.service.mobile.dto.response.GetNurseLocationInfoResponse;
import com.service.mobile.dto.response.NurseDataDto;
import com.service.mobile.dto.response.NurseReviewRatingRequest;
import com.service.mobile.dto.response.Response;
import com.service.mobile.model.*;
import com.service.mobile.repository.*;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
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
public class NursePartnerService {
    @Autowired
    private NodTrackLocationRepository nodTrackLocationRepository;

    @Autowired
    private MessageSource messageSource;

    @Autowired
    private PublicService publicService;

    @Autowired
    private PartnerNurseRepository partnerNurseRepository;

    @Autowired
    private NurseServiceRepository nurseServiceRepository;

    @Autowired
    private NodLogRepository nodLogRepository;

    @Autowired
    private WalletTransactionRepository walletTransactionRepository;

    @Value("${app.base.url}")
    private String baseUrl;

    @Value("${app.currency.symbol}")
    private String currencySymbol;
    @Value("${app.currency.symbol.fdj}")
    private String currencyFDJ;
    @Autowired
    private GlobalConfigurationRepository globalConfigurationRepository;
    @Autowired
    private NurseServiceStateRepository nurseServiceStateRepository;
    @Autowired
    private UsersRepository usersRepository;
    @Autowired
    private NurseDemandOrdersRepository nurseDemandOrdersRepository;
    @Autowired
    private NurseServiceOrderRepository nurseServiceOrderRepository;
    @Value("${app.ZoneId}")
    private String zone;
    @PersistenceContext
    private EntityManager entityManager;
    @Autowired
    private EVCPlusPaymentService evcPlusPaymentService;
    @Value("${app.transaction.mode}")
    private Integer transactionMode;

    public ResponseEntity<?> ptOnlineNurses(Locale locale, Integer userId) {

        try {
            List<AvailableNursesMapDto> map = publicService.availableNursesMap();
            List<AvailableNursesMapDto> result = new ArrayList<>();
            List<String> contactNumber = new ArrayList<>();
            List<String> contactNumbersLocal = new ArrayList<>();
            for (AvailableNursesMapDto dto : map) {
                contactNumber.add(dto.getNumber());
            }
            List<String> partnerNurses = partnerNurseRepository.findByContactNumberIn(contactNumber);
            if (partnerNurses != null && !partnerNurses.isEmpty()) {
                contactNumbersLocal.addAll(partnerNurses);

                for (AvailableNursesMapDto nurse : map) {
                    if (contactNumbersLocal.contains(nurse.getNumber())) {
                        result.add(nurse);
                    }
                }
            }
            return ResponseEntity.status(HttpStatus.OK).body(new Response(
                    Constants.SUCCESS_CODE,
                    Constants.SUCCESS_CODE,
                    messageSource.getMessage(Constants.SUCCESS_MESSAGE, null, locale),
                    result
            ));
        } catch (Exception e) {
            e.printStackTrace();
            log.error("Error found in search online nurses : {}", e);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new Response(
                    Constants.NO_CONTENT_FOUNT_CODE,
                    Constants.NO_CONTENT_FOUNT_CODE,
                    messageSource.getMessage(Constants.SOMETHING_WENT_WRONG, null, locale)
            ));
        }
    }

    public ResponseEntity<?> nurseService(Locale locale) {
        try{
            List<NurseService> services = nurseServiceRepository.findByStatus("A");
            if (services != null && !services.isEmpty()) {
                List<ServiceResponse> data = getServiceResponses(locale, services);

                return ResponseEntity.status(HttpStatus.OK).body(new Response(
                        Constants.SUCCESS_CODE,
                        Constants.SUCCESS_CODE,
                        messageSource.getMessage(Constants.NURSE_SERVICE_FETCHED,null,locale),
                        data
                ));
            }
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new Response(
                    Constants.BLANK_DATA_GIVEN_CODE,
                    Constants.BLANK_DATA_GIVEN_CODE,
                    messageSource.getMessage(Constants.BLANK_DATA_GIVEN,null,locale)
            ));
        }catch (Exception e) {
            e.printStackTrace();
            log.error("Error found in nurseService api :{}", e);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new Response(
                    Constants.NO_CONTENT_FOUNT_CODE,
                    Constants.NO_CONTENT_FOUNT_CODE,
                    messageSource.getMessage(Constants.SOMETHING_WENT_WRONG, null, locale)
            ));
        }
    }

    private List<ServiceResponse> getServiceResponses(Locale locale, List<NurseService> services) {
        List<ServiceResponse> data = new ArrayList<>();

        for (NurseService serv : services) {
            ServiceResponse serve = new ServiceResponse();
            serve.setId(serv.getId());

            if ("en".equals(locale.getLanguage())) {
                serve.setService_name(serv.getSeviceName());
                serve.setDescription(serv.getDescription());
            } else {
                serve.setService_name(serv.getSeviceNameSl());
                serve.setDescription(serv.getDescriptionSl());
            }

            serve.setService_price(currencySymbol + " " + serv.getTotalServicePrice());

            if (serv.getServiceImage() != null && !serv.getServiceImage().isEmpty()) {
                serve.setService_image(baseUrl + "/uploaded_file/nurse_services/" + serv.getId() + "/" + serv.getServiceImage());
            } else {
                serve.setService_image(baseUrl + "/uploaded_file/noimagefound.png");
            }

            data.add(serve);
        }
        return data;
    }

    public ResponseEntity<?> logsNurseNotFound(Locale locale, LogsNurseNotFoundRequest request) {
        try {
            Integer userId = request.getUser_id();
            String state = request.getState();
            String searchId = request.getSearch_id();
            String reason = request.getReason();
            String latPatient = request.getLat_patient();
            String longPatient = request.getLong_patient();

            if ("no nurses".equals(reason)) {
                SendNurseOnDemandMsgRequest tempRequest = new SendNurseOnDemandMsgRequest();
                tempRequest.setPatient_id(request.getUser_id());
                tempRequest.setNurse_id(0);
                tempRequest.setId(0);
                publicService.sendNurseOnDemandMsg(tempRequest, Constants.NURSE_NOT_FOUND_PATIENT_NOD, UserType.Patient, locale);
            }

            if (userId != null && userId != 0 &&
                    state != null && !state.isEmpty() &&
                    reason != null && !reason.isEmpty() &&
                    latPatient != null && !latPatient.isEmpty() &&
                    longPatient != null && !longPatient.isEmpty() &&
                    searchId != null && !searchId.isEmpty()) {
                Long transactionCount = walletTransactionRepository.countByPatientId(userId);
                String orderType = (transactionCount > 0) ? "1" : "0";

                NodLog nodLog = new NodLog();
                nodLog.setUserId(userId);
                nodLog.setSearchId(searchId);
                nodLog.setLat(latPatient);
                nodLog.setLng(longPatient);
                nodLog.setOrderType(orderType);
                nodLog.setReason(reason);
                nodLog.setChannel(Channel.Mobile);
                nodLog.setStatus("Failed");
                nodLog.setCreatedAt(LocalDateTime.now(ZoneId.of(zone)));
                nodLog.setTransactionType("NOD");
                nodLogRepository.save(nodLog);

                SendNurseOnDemandMsgRequest tempRequest = new SendNurseOnDemandMsgRequest();
                tempRequest.setPatient_id(userId);
                tempRequest.setNurse_id(0);
                tempRequest.setId(0);
                tempRequest.setStatus(reason);
                publicService.sendNurseOnDemandMsg(tempRequest, Constants.AGENT_NOTIFICATION_FOR_FAILED_NOD, UserType.Patient, locale);

                return ResponseEntity.status(HttpStatus.OK).body(new Response(
                        Constants.SUCCESS_CODE,
                        Constants.SUCCESS_CODE,
                        messageSource.getMessage(Constants.SUCCESS_MESSAGE, null, locale),
                        new ArrayList<>()
                ));
            } else {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new Response(
                        Constants.BLANK_DATA_GIVEN_CODE,
                        Constants.BLANK_DATA_GIVEN_CODE,
                        messageSource.getMessage(Constants.REQUEST_PARAM_MISSING, null, locale),
                        new ArrayList<>()
                ));
            }
        } catch (Exception e) {
            e.printStackTrace();
            log.error("Error found in logsNurseNotFound api :{}", e);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new Response(
                    Constants.NO_CONTENT_FOUNT_CODE,
                    Constants.NO_CONTENT_FOUNT_CODE,
                    messageSource.getMessage(Constants.SOMETHING_WENT_WRONG, null, locale)
            ));
        }
    }

    public ResponseEntity<?> getNurseLocationInfo(Locale locale, GetNurseLocationInfoRequest request) {
        try {
            if (request.getP_latutude() != null && !request.getP_latutude().isEmpty() &&
                    request.getP_longitude() != null && !request.getP_longitude().isEmpty() &&
                    request.getN_latutude() != null && !request.getN_latutude().isEmpty() &&
                    request.getN_longitude() != null && !request.getN_longitude().isEmpty() &&
                    request.getNurse_mobile() != null && !request.getNurse_mobile().isEmpty() &&
                    request.getSearch_id() != null && !request.getSearch_id().isEmpty() &&
                    request.getUser_id() != null && request.getUser_id() != 0 &&
                    request.getDistance() != null && !request.getDistance().isEmpty()
            ) {
                if (request.getService_id() == null || request.getService_id().isEmpty()) {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new Response(
                            Constants.NO_RECORD_FOUND_CODE,
                            Constants.NO_RECORD_FOUND_CODE,
                            messageSource.getMessage(Constants.SERVICE_NOT_FOUND, null, locale),
                            new ArrayList<>()
                    ));
                }
                GetNurseLocationInfoResponse response = new GetNurseLocationInfoResponse();
                Float servicePrice = 0.0f;

                List<NamePriceDto> serviceArr = new ArrayList<>();

                PartnerNurse nurse = partnerNurseRepository.findByContactNumberIdDesc(request.getNurse_mobile());

                GlobalConfiguration ondemand_rate = globalConfigurationRepository.findByKey("ONDEMAND_RATE");
                GlobalConfiguration min_distance_fee = globalConfigurationRepository.findByKey("ONDEMAND_MIN_DISTANCE_FEE");
                GlobalConfiguration sls_rate = globalConfigurationRepository.findByKey("WAAFI_PAYMENT_RATE");

                String[] serviceIds = request.getService_id().split(",");
                List<Integer> serviceIntIds = new ArrayList<>();
                for (String s : serviceIds) {
                    serviceIntIds.add(Integer.parseInt(s));
                }
                List<NurseService> service = nurseServiceRepository.findByIdsAndStatus(serviceIntIds, "A");

                if (!service.isEmpty()) {
                    for (NurseService n : service) {
                        servicePrice = servicePrice + n.getTotalServicePrice();

                        NamePriceDto temp = new NamePriceDto();
                        temp.setName(n.getSeviceName());
                        temp.setPrice(currencySymbol + " " + n.getTotalServicePrice());
                        serviceArr.add(temp);
                    }
                    Float distanceFees = Float.valueOf(request.getDistance()) * Float.valueOf(ondemand_rate.getValue());
                    if (Float.valueOf(min_distance_fee.getValue()) > distanceFees) {
                        distanceFees = Float.valueOf(min_distance_fee.getValue());
                    }

                    Float amount = distanceFees + servicePrice;
                    Float slshAmount = amount * Float.valueOf(sls_rate.getValue());

                    response.setServices(serviceArr);
                    response.setServices_price(currencySymbol + " " + (Math.round(servicePrice * 100.0) / 100.0));
                    response.setDistance_fee(currencySymbol + " " + (Math.round(distanceFees * 100.0) / 100.0));
                    response.setAmount(currencySymbol + " " + amount);
                    response.setSlsh_amount("SLSH " + (Math.round(slshAmount * 100.0) / 100.0));
                }

                if (nurse != null) response.setNurse(getNurseDeatils(nurse));

                NurseServiceState nurseServiceState = getNurseServiceState(request, nurse);
                nurseServiceState = nurseServiceStateRepository.save(nurseServiceState);
                response.setState_id(nurseServiceState.getId());

                return ResponseEntity.status(HttpStatus.OK).body(new Response(
                        Constants.SUCCESS_CODE,
                        Constants.SUCCESS_CODE,
                        messageSource.getMessage(Constants.RECORD_FETCHED, null, locale),
                        response
                ));

            } else {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new Response(
                        Constants.BLANK_DATA_GIVEN_CODE,
                        Constants.BLANK_DATA_GIVEN_CODE,
                        messageSource.getMessage(Constants.BLANK_DATA_GIVEN, null, locale)
                ));
            }
        } catch (Exception e) {
            e.printStackTrace();
            log.error("Error found in getNurseLocationInfo api :{}", e);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new Response(
                    Constants.NO_CONTENT_FOUNT_CODE,
                    Constants.NO_CONTENT_FOUNT_CODE,
                    messageSource.getMessage(Constants.SOMETHING_WENT_WRONG, null, locale)
            ));
        }
    }

    private IdNameMobileDto getNurseDeatils(PartnerNurse nurse) {
        IdNameMobileDto nurseData = new IdNameMobileDto();
        nurseData.setId(nurse.getId());
        nurseData.setMobile(nurse.getContactNumber());
        nurseData.setName(nurse.getName());
        return nurseData;
    }

    private NurseServiceState getNurseServiceState(GetNurseLocationInfoRequest request, PartnerNurse nurse) {
        NurseServiceState nurseServiceState = new NurseServiceState();
        nurseServiceState.setPatientId(request.getUser_id());
        nurseServiceState.setNurseId(nurse == null ? null : nurse.getId());
        nurseServiceState.setLatPatient(request.getP_latutude());
        nurseServiceState.setLongPatient(request.getN_longitude());
        nurseServiceState.setLatNurse(request.getN_latutude());
        nurseServiceState.setLongNurse(request.getN_longitude());
        nurseServiceState.setState(State.Processing);
        nurseServiceState.setDistance(request.getDistance());
        nurseServiceState.setSearchId(request.getSearch_id());
        nurseServiceState.setConfirmAck("0");
        nurseServiceState.setCreatedAt(LocalDateTime.now(ZoneId.of(zone)));
        nurseServiceState.setUpdatedAt(LocalDateTime.now(ZoneId.of(zone)));
        return nurseServiceState;
    }

    //NOTE-TODO (NOT IN BAANNOO)
    public ResponseEntity<?> processPayment(Locale locale, ProcessPaymentRequest request) throws JsonProcessingException {
        if (request.getUser_id() == null || request.getNurse_mobile() == null || request.getNurse_mobile().isEmpty()
                || request.getCurrency() == null || request.getCurrency().isEmpty() || request.getService_id() == null
                || request.getService_id().isEmpty() || request.getSearch_id() == null || request.getSearch_id().isEmpty()
                || request.getState_id() == null || request.getPayment_method() == null || request.getPayment_method().isEmpty()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new Response(
                    Constants.BLANK_DATA_GIVEN_CODE,
                    Constants.BLANK_DATA_GIVEN_CODE,
                    messageSource.getMessage(Constants.ORDER_NOT_FOUND, null, locale),
                    new ArrayList<>()
            ));
        }
        try {
            GlobalConfiguration ondemand_rate = globalConfigurationRepository.findByKey("ONDEMAND_RATE");
            GlobalConfiguration min_distance_fee = globalConfigurationRepository.findByKey("ONDEMAND_MIN_DISTANCE_FEE");
            GlobalConfiguration sls_rate = globalConfigurationRepository.findByKey("WAAFI_PAYMENT_RATE");

            Users patient = usersRepository.findById(request.getUser_id()).orElse(null);
            PartnerNurse nurse = partnerNurseRepository.findByContactNumberIdDesc(request.getNurse_mobile());
            List<Integer> serviceIds = Arrays.stream(request.getService_id().split(",")).map(Integer::parseInt).toList();
            List<NurseService> serviceList = nurseServiceRepository.findByIdsAndStatus(serviceIds, "A");
            NurseServiceState nurseServiceState = nurseServiceStateRepository.findById(request.getState_id()).orElse(null);
            float distanceFees = 0f;
            if (nurseServiceState != null) {
                distanceFees = Float.valueOf(nurseServiceState.getDistance()) * Float.valueOf(ondemand_rate.getValue());
            }
            if (Float.valueOf(min_distance_fee.getValue()) > distanceFees)
                distanceFees = Float.valueOf(min_distance_fee.getValue());
            float amountTotal = 0f;
            float amountPartial = 0f;
            List<String> serviceNamesList = new ArrayList<>();
            String serviceNames = null;
            if (!serviceList.isEmpty()) {
                amountTotal = serviceList.stream().map(NurseService::getTotalServicePrice).filter(Objects::nonNull).reduce(0.0f, Float::sum);
                amountPartial = serviceList.stream().map(NurseService::getServicePrice).filter(Objects::nonNull).reduce(0.0f, Float::sum);
                serviceNamesList = serviceList.stream().map(NurseService::getSeviceName).toList();
                serviceNames = String.join(",", serviceNamesList);
            }

            float amount = Math.round((distanceFees + amountTotal) * 100.0 / 100.0);
            float gatewayAmount = amount;

            float slshAmount = amount * Float.valueOf(sls_rate.getValue());

            float serviceAmount = Math.round((distanceFees * amountPartial) * 100.0f / 100.0f);
            float slshServiceAmount = serviceAmount * Float.valueOf(sls_rate.getValue());

            float commission = amount - serviceAmount;
            if (request.getCurrency().equalsIgnoreCase("slsh")) gatewayAmount = slshAmount;

            //payment initiated
            NurseDemandOrders orders = createNurseOrders(request, patient, nurse, amountPartial, distanceFees, commission, serviceAmount, slshServiceAmount, amount, slshAmount, Float.valueOf(sls_rate.getValue()));
            WalletTransaction transaction = createWalletTransaction(request, patient, orders.getId(), "paid_against_nod", orders.getAmount(), "nurse_on_demand");

            String status = createNodTransaction(orders, transaction, gatewayAmount, patient, "nurse_on_demand");

            if (status.equalsIgnoreCase("pass")) {
                nurseServiceState.setDeviceEnv(DeviceEnv.Mobile);
                nurseServiceState.setState(State.Booked);
                nurseServiceState.setOrderId(orders.getId());

                nurseServiceStateRepository.save(nurseServiceState);
                //TODO : TransactionLog

                //create Nurse service Order
                assert patient != null;
                createNurseServiceOrder(serviceList, orders.getId(), patient.getUserId(), nurse.getId());

                //sms and notification
                SendNurseOnDemandMsgRequest r = new SendNurseOnDemandMsgRequest();
                r.setTripId(request.getSearch_id());
                r.setZaadNumber(request.getCurrency() + " " + gatewayAmount);
                r.setPatient_id(patient.getUserId());
                r.setNurse_id(nurse.getId());
                r.setId(orders.getId());
                r.setLat(nurseServiceState.getLatPatient());
                r.setLongi(nurseServiceState.getLongPatient());
                publicService.sendNurseOnDemandMsg(r, Constants.PAYMENT_CONFIRM_PATIENT_NOD, UserType.Patient, locale);
                publicService.sendNurseOnDemandMsg(r, Constants.CONFIRM_ONDEMAND_ORDER_NURSE, UserType.NursePartner, locale);
                publicService.sendNurseOnDemandMsg(r, Constants.ORDER_NOTICE_AGENT_NOD, UserType.Patient, locale);


                //response
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                String paidAmount = currencySymbol + " " + orders.getAmount();
                if (!orders.getCurrency().equalsIgnoreCase("USD")) paidAmount = "SLSH " + orders.getSlshAmount();
                Map<String, Object> response = new HashMap<>();
                response.put("nurse_picture", nurse.getProfilePicture() == null || nurse.getProfilePicture().isEmpty()
                        ? "" : baseUrl + "uploaded_file/NursePartner/" + nurse.getId() + "/" + nurse.getProfilePicture());
                response.put("nurse_name", nurse.getName());
                response.put("nurse_contact", nurse.getContactNumber());
                response.put("service_id", orders.getTripId());
                response.put("date", formatter.format(orders.getCreatedAt()));
                response.put("amount", paidAmount);
                response.put("order_id", orders.getId());
                response.put("service_data", serviceNames);

                return ResponseEntity.status(HttpStatus.OK).body(new Response(
                        Constants.SUCCESS_CODE,
                        Constants.SUCCESS_CODE,
                        messageSource.getMessage(Constants.ORDER_CREATED_SUCCESS, null, locale),
                        response
                ));
            }
            Map<String, Integer> res = new HashMap<>();
            res.put("order_id", orders.getId());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new Response(
                    Constants.BLANK_DATA_GIVEN_CODE,
                    Constants.BLANK_DATA_GIVEN_CODE,
                    messageSource.getMessage(Constants.SOMETHING_WENT_WRONG_IN_PAYMENT, null, locale),
                    res
            ));
        } catch (Exception e) {
            e.printStackTrace();
            log.error("Error in Payment of NOD : {}", e);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new Response(
                    Constants.NO_RECORD_FOUND_CODE,
                    Constants.NO_RECORD_FOUND_CODE,
                    messageSource.getMessage(Constants.SOMETHING_WENT_WRONG, null, locale),
                    new ArrayList<>()
            ));
        }
    }

    private void createNurseServiceOrder(List<NurseService> serviceList, Integer orderId, Integer patientId, Integer nurseId) {
        if(!serviceList.isEmpty()) {
            for (NurseService n : serviceList) {
               NurseServiceOrder serviceOrder = new NurseServiceOrder();
               serviceOrder.setId(new NurseServiceOrderKey(orderId, n.getId()));
               serviceOrder.setPatientId(patientId);
               serviceOrder.setNurseId(nurseId);

                nurseServiceOrderRepository.save(serviceOrder);
            }
        }
    }

    private String createNodTransaction(NurseDemandOrders orders, WalletTransaction transaction, float gatewayAmount, Users patient, String serviceType) throws JsonProcessingException {
        Map<String, Object> transactionDetail = new HashMap<>();
        transactionDetail.put("ref_transaction_id", transaction.getTransactionId());
        transactionDetail.put("reference_number", transaction.getReferenceNumber());

        String status = "fail";

        if(transactionMode != null && transactionMode == 1){
            transaction.setTransactionId(generateDateTime());
            transaction.setTransactionStatus("Completed");
            orders.setStatus(StatusFullName.Completed);
            orders.setPaymentStatus("Completed");
            status = "pass";
        }
        else if(transactionMode!=null && transactionMode==2){
            transaction.setTransactionId(generateDateTime());
            transaction.setTransactionStatus("Cancel");
            orders.setPaymentStatus("Failed");
            status = "fail";
        }
        else {
            if(gatewayAmount > 0.0f) {
                Map<String, Object> payment = evcPlusPaymentService.processPayment("API_PURCHASE", transactionDetail, orders.getAmount(), transaction.getPayerMobile(), patient.getUserId().toString(), "USD", serviceType);
                if (payment.get("status").equals(200)) {
                    transaction.setTransactionId(generateDateTime());
                    transaction.setTransactionStatus("Completed");
                    orders.setStatus(StatusFullName.Completed);
                    orders.setPaymentStatus("Completed");
                    status = "pass";
                } else {
                    transaction.setTransactionId(generateDateTime());
                    transaction.setTransactionStatus("Cancel");
                    orders.setPaymentStatus("Failed");
                    status = "fail";
                }
            }
            else{
                transaction.setTransactionId(generateDateTime());
                transaction.setTransactionStatus("Completed");
                orders.setStatus(StatusFullName.Inprogress);
                orders.setPaymentStatus("Completed");
                status = "pass";
            }
        }

        nurseDemandOrdersRepository.save(orders);
        walletTransactionRepository.save(transaction);
        return status;
    }

    private WalletTransaction createWalletTransaction(ProcessPaymentRequest request, Users patient, Integer orderId, String transactionType, Float amount, String serviceType) {
        WalletTransaction transaction = new WalletTransaction();
        transaction.setPaymentMethod(request.getPayment_method());
        transaction.setPatientId(patient);
        transaction.setOrderId(orderId);
        transaction.setPaymentGatewayType(request.getPayment_method());
        transaction.setTransactionDate(LocalDateTime.now(ZoneId.of(zone)));
        transaction.setTransactionType(transactionType);
        transaction.setAmount(amount);
        transaction.setIsDebitCredit("debit");
        transaction.setPayeeId(1); // payee is super admin and his id is 1
        transaction.setPayerId(patient.getUserId());
        transaction.setReferenceNumber(patient.getUserId().toString());  //this is same as payer no

        Users adminContactNumber = usersRepository.findByType(UserType.Superadmin).get(0);
        transaction.setPayeeMobile(adminContactNumber.getContactNumber());
        transaction.setPayerMobile(request.getPayment_number() == null || request.getPayment_number().isEmpty() ? patient.getContactNumber() : request.getPayment_number());

        transaction.setCreatedAt(LocalDateTime.now(ZoneId.of(zone)));
        transaction.setUpdatedAt(LocalDateTime.now(ZoneId.of(zone)));
        transaction.setRefTransactionId(orderId.toString());
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

    private NurseDemandOrders createNurseOrders(ProcessPaymentRequest request, Users patient, PartnerNurse nurse, float amountPartial, float distanceFees, float commission, float serviceAmount, float slshServiceAmount, float amount, float slshAmount, Float slsRate) {
        NurseDemandOrders orders = new NurseDemandOrders();
        orders.setTripId(request.getSearch_id());
        orders.setPatientId(patient);
        orders.setNurseId(nurse);
        orders.setNurseMobile(nurse != null ? nurse.getContactNumber() : null);
        orders.setServiceFee(amountPartial);
        orders.setDistanceFee(distanceFees);
        orders.setCommission(commission);
        orders.setServiceAmount(serviceAmount);
        orders.setSlshServiceAmount(slshServiceAmount);
        orders.setAmount(amount);
        orders.setSlshAmount(slshAmount);
        orders.setSlsRate(slsRate);
        orders.setCurrency(request.getCurrency());
        orders.setStatus(StatusFullName.Pending);
        orders.setPaymentStatus("Pending");
        orders.setPaymentMethod(request.getPayment_method());
        orders.setPaymentNumber(request.getPayment_number());
        orders.setCreatedAt(LocalDateTime.now(ZoneId.of(zone)));
        orders.setIsTransfered(IsTransfered.NO);

        return nurseDemandOrdersRepository.save(orders);
    }


    public ResponseEntity<?> nodack(Locale locale, NodAckRequest request) {
        try {
            publicService.confirmAck(request);
            return ResponseEntity.status(HttpStatus.OK).body(new Response(
                    Constants.SUCCESS_CODE,
                    Constants.SUCCESS_CODE,
                    messageSource.getMessage(Constants.SUCCESS_MESSAGE, null, locale)
            ));
        }catch (Exception e){
            e.printStackTrace();
            log.error("Error found in nodack api :{}",e);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new Response(
                    Constants.NO_CONTENT_FOUNT_CODE,
                    Constants.NO_CONTENT_FOUNT_CODE,
                    messageSource.getMessage(Constants.SOMETHING_WENT_WRONG, null, locale)
            ));
        }
    }

    public ResponseEntity<?> ndPatientOrderDetail(Locale locale, Integer userId) {
        Users patient = usersRepository.findById(userId).orElse(null);
        if(userId!=null){
            List<State> statesD = Arrays.asList(State.Expired, State.Completed, State.Cancelled);
            List<NurseDemandOrders> orders = nurseDemandOrdersRepository.findByPatientNurseNotNullPaymentStatusStatus(
                    patient.getUserId(), PaymentStatus.Completed, StatusFullName.Inprogress, statesD
            );
            List<NdPatientOrderDetailDto> response = new ArrayList<>();
            if(!orders.isEmpty()){
                for(NurseDemandOrders n:orders){
                    NdPatientOrderDetailDto dto = new NdPatientOrderDetailDto();

                    dto.setOrder_id(n.getId());
                    dto.setTrip_id(n.getTripId());
                    dto.setNurse_name(n.getNurseId().getName());
                    dto.setNurse_contact(n.getNurseId().getContactNumber());
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                    dto.setDate(formatter.format(n.getCreatedAt()));


                    NurseServiceState state = nurseServiceStateRepository.findByOrderId(n.getId());
                    if(state!=null){dto.setState(state.getState());}

                    if(n.getCurrency().equalsIgnoreCase("USD")){
                        dto.setOrder_amount(n.getCurrency() + " "+n.getAmount());
                    }else{
                        dto.setOrder_amount(n.getCurrency() + " "+n.getSlshAmount());
                    }

                    List<String> servicesNames = nurseServiceRepository.findByOrderId(n.getId());
                    if(!servicesNames.isEmpty()) dto.setService_type(String.join(",", servicesNames));

                    response.add(dto);
                }

                return ResponseEntity.status(HttpStatus.OK).body(new Response(
                        Constants.SUCCESS_CODE,
                        Constants.SUCCESS_CODE,
                        messageSource.getMessage(Constants.ORDERS_FETCH_SUCCESSFULLY,null,locale),
                        response
                ));

            }else{
                return ResponseEntity.status(HttpStatus.OK).body(new Response(
                        Constants.SUCCESS_CODE,
                        Constants.SUCCESS_CODE,
                        messageSource.getMessage(Constants.ORDER_NOT_FOUND,null,locale)
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

    public ResponseEntity<?> ndPatientCancelOrder(Locale locale, CancelOrderRequest request) {
        if (request.getUser_id() == null || request.getOrder_id() == null
                || request.getMessage() == null || request.getMessage().isEmpty()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new Response(
                    Constants.NO_CONTENT_FOUNT_CODE,
                    Constants.NO_CONTENT_FOUNT_CODE,
                    messageSource.getMessage(Constants.BLANK_DATA_GIVEN, null, locale),
                    new ArrayList<>()
            ));
        }
        try {
            NurseDemandOrders orders = nurseDemandOrdersRepository.findById(request.getOrder_id()).orElse(null);
            NurseServiceState state = nurseServiceStateRepository.findByOrderId(request.getOrder_id());
            if (state != null && orders != null) {
                SendNurseOnDemandMsgRequest tempRequest = new SendNurseOnDemandMsgRequest();
                tempRequest.setPatient_id(request.getUser_id());
                tempRequest.setNurse_id(state.getNurseId());
                tempRequest.setId(0);
                tempRequest.setTripId(orders.getTripId());

                if (state.getState().equals(State.Arrived) || state.getState().equals(State.Onway)) {
                    //setting refunded amount
                    float refundAmount = orders.getAmount() - orders.getDistanceFee();
                    orders.setRefundAmount(refundAmount);

                    //Partial refund Message
                    tempRequest.setZaadNumber(currencyFDJ + " " + refundAmount);
                    publicService.sendNurseOnDemandMsg(tempRequest, Constants.ORDER_CANCEL_BY_PATIENT_NURSE_NOD, UserType.NURSEPARTNER, locale);
                } else orders.setRefundAmount(orders.getAmount());

                //status update for service state
                if (!orders.getStatus().equals(StatusFullName.Completed)) {
                    state.setState(State.Cancelled);
                    state.setCancelAt(LocalDateTime.now(ZoneId.of(zone)));
                    state.setCancelBy(CancelBy.Patient);
                    state.setCancelMessage(request.getMessage());

                    orders.setStatus(StatusFullName.Cancelled);
                    orders.setIsTransfered(IsTransfered.PENDING);

                    nurseServiceStateRepository.save(state);
                    nurseDemandOrdersRepository.save(orders);

                    //messages
                    publicService.sendNurseOnDemandMsg(tempRequest, Constants.CANCEL_SMS_PATIENT_NOD, UserType.Patient, locale);
                    publicService.sendNurseOnDemandMsg(tempRequest, Constants.CANCEL_SMS_NURSE_NOD, UserType.NursePartner, locale);

                    return ResponseEntity.status(HttpStatus.OK).body(new Response(
                            Constants.SUCCESS_CODE,
                            Constants.SUCCESS_CODE,
                            messageSource.getMessage(Constants.REQUEST_CANCELLED_SUCCESSFULLY, null, locale)
                    ));

//                $log = [
//                'user_id' => $stateModel->patient_id,
//                        'lat_patient'=>$stateModel->lat_patient,
//                        'long_patient'=>$stateModel->long_patient,
//                        'search_id' => $stateModel->search_id,
//                        'cancel_by' => $stateModel->cancel_by,
//                        'cancel_at' => $stateModel->cancel_at,
//                        'status' => $stateModel->state
//                            ];
//
//                $this->HelperComponent()->socketLogs(($log));

                } else {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new Response(
                            Constants.BLANK_DATA_GIVEN_CODE,
                            Constants.BLANK_DATA_GIVEN_CODE,
                            messageSource.getMessage(Constants.UNABLE_NOD_CANCEL, null, locale)
                    ));
                }
            } else {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new Response(
                        Constants.BLANK_DATA_GIVEN_CODE,
                        Constants.BLANK_DATA_GIVEN_CODE,
                        messageSource.getMessage(Constants.UNABLE_NOD_CANCEL, null, locale)
                ));
            }
        } catch (Exception e) {
            e.printStackTrace();
            log.error("Error found in nod cancel by patient :{}", e);
            return null;
        }
    }

    public ResponseEntity<?> ptNurseTracker(Locale locale, CancelOrderRequest request) {
        if(request.getUser_id() == null || request.getSearch_id() == null){
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new Response(
                    Constants.NO_CONTENT_FOUNT_CODE,
                    Constants.NO_CONTENT_FOUNT_CODE,
                    messageSource.getMessage(Constants.BLANK_DATA_GIVEN_CODE, null, locale)
            ));
        }
        try{
            Map<String, Object> data = new HashMap<>();
            //find nurse from track location table
            NodTrackLocation track = nodTrackLocationRepository.findByPatientIdAndSearchId(request.getUser_id(), request.getSearch_id());
            data.put("location", track);

            //if nurse not found by tracking then we have to find from order table
            PartnerNurse nurse = nurseDemandOrdersRepository.findByTripId(request.getSearch_id());
            if(nurse != null) {
                 Integer ratingSum = nurseServiceStateRepository.findRatingSumByNurseId(nurse.getId());
                 NurseDataDto dto = new NurseDataDto();
                 dto.setId(nurse.getId());
                 dto.setName(nurse.getName());
                 dto.setContact_number(nurse.getContactNumber());
                 dto.setProfile_picture(nurse.getProfilePicture() == null || !nurse.getProfilePicture().isEmpty()
                         ? "" : baseUrl + "uploaded_file/NursePartner/" + nurse.getId() + "/" + nurse.getProfilePicture());
                 if(ratingSum != null){
                     Integer ratingCount = nurseServiceStateRepository.findRatingCountByNurseId(nurse.getId());
                     float rating = Math.round((((float) ratingSum / ratingCount) * 100.0f) / 100.0f);
                     dto.setRating(rating);
                 }else dto.setRating(0);

                 data.put("nurse_date", dto);

                 return ResponseEntity.status(HttpStatus.OK).body(new Response(
                         Constants.SUCCESS_CODE,
                         Constants.SUCCESS_CODE,
                         messageSource.getMessage(Constants.SUCCESS_MESSAGE, null, locale),
                         data
                 ));
             }
             //if we are not finding nurse from any method
            else{
                 return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new Response(
                         Constants.BLANK_DATA_GIVEN_CODE,
                         Constants.BLANK_DATA_GIVEN_CODE,
                         messageSource.getMessage(Constants.BLANK_DATA_GIVEN, null, locale),
                         new ArrayList<>()
                 ));
             }

        }catch (Exception e){
            e.printStackTrace();
            log.error("Errof in ptNurseTracker api : {}",e);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new Response(
                    Constants.NO_CONTENT_FOUNT_CODE,
                    Constants.NO_CONTENT_FOUNT_CODE,
                    messageSource.getMessage(Constants.SOMETHING_WENT_WRONG, null, locale)
            ));
        }
    }

    public ResponseEntity<?> ndReviewRating(Locale locale, NurseReviewRatingRequest request) {
        if (request.getUser_id() == null || request.getOrder_id() == null
                || request.getComment() == null || request.getComment().isEmpty()
                || request.getRating() == null || request.getRating().isEmpty()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new Response(
                    Constants.NO_CONTENT_FOUNT_CODE,
                    Constants.NO_CONTENT_FOUNT_CODE,
                    messageSource.getMessage(Constants.BLANK_DATA_GIVEN_CODE, null, locale)
            ));
        }
        try {
            NurseServiceState state = nurseServiceStateRepository.findByOrderId(request.getOrder_id());
            if (state != null) {
                state.setNRating(request.getRating());
                state.setNRemark(request.getComment());

                nurseServiceStateRepository.save(state);

                return ResponseEntity.status(HttpStatus.OK).body(new Response(
                        Constants.SUCCESS_CODE,
                        Constants.SUCCESS_CODE,
                        messageSource.getMessage(Constants.RATING_ADDED, null, locale),
                        new ArrayList<>()
                ));
            } else {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new Response(
                        Constants.BLANK_DATA_GIVEN_CODE,
                        Constants.BLANK_DATA_GIVEN_CODE,
                        messageSource.getMessage(Constants.NO_CONTENT_FOUNT, null, locale),
                        new ArrayList<>()
                ));
            }
        } catch (Exception e) {
            e.printStackTrace();
            log.error("Error found in ndReviewRating api :{}", e);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new Response(
                    Constants.NO_CONTENT_FOUNT_CODE,
                    Constants.NO_CONTENT_FOUNT_CODE,
                    messageSource.getMessage(Constants.SOMETHING_WENT_WRONG, null, locale)
            ));
        }
    }

    public ResponseEntity<?> ptConfirmedHistory(Locale locale, String userId, String tripId, String date, Integer page) {
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new Response(
                    Constants.NO_CONTENT_FOUNT_CODE,
                    Constants.NO_CONTENT_FOUNT_CODE,
                    messageSource.getMessage(Constants.BLANK_DATA_GIVEN_CODE, null, locale)
            ));
        }
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            StringBuilder sb = new StringBuilder("SELECT u FROM NurseDemandOrders u WHERE u.patientId.userId = " + userId);
            if (tripId != null && !tripId.isEmpty()) {
                sb.append(" AND u.tripId = '" + tripId + "'");
            }
            if (date != null && !date.isEmpty()) {
                LocalDate localDate = LocalDate.parse(date);
                String sd = formatter.format(localDate.atStartOfDay());
                String ed = formatter.format(localDate.atTime(23, 59, 59));
                sb.append(" AND u.createdAt BETWEEN '" + sd + "' AND '" + ed + "'");
            }
            sb.append(" ORDER BY u.id DESC");
            Query main = entityManager.createQuery(sb.toString(), NurseDemandOrders.class);

            List<NurseDemandOrders> nurseDemandOrdersList = main.getResultList();
            long count = nurseDemandOrdersList.size();
            main.setFirstResult(page == null ? 0 : page * 10);
            main.setMaxResults(10);
            nurseDemandOrdersList = main.getResultList();

            if (nurseDemandOrdersList.isEmpty()) {
                return ResponseEntity.status(HttpStatus.OK).body(new Response(
                        Constants.SUCCESS_CODE,
                        Constants.SUCCESS_CODE,
                        messageSource.getMessage(Constants.ORDER_NOT_FOUND, null, locale),
                        new ArrayList<>()
                ));
            }

            List<Map<String, Object>> list = new ArrayList<>();
            for (NurseDemandOrders orders : nurseDemandOrdersList) {
                Map<String, Object> dto = new HashMap<>();

                dto.put("order_id", orders.getId());
                dto.put("nurse_name", orders.getNurseId().getName());
                String pic = orders.getNurseId().getProfilePicture() == null || !orders.getNurseId().getProfilePicture().isEmpty()
                        ? "" : baseUrl + "uploaded_file/NursePartner/" + orders.getNurseId().getId() + "/" + orders.getNurseId().getProfilePicture();
                dto.put("nurse_picture", pic);
                dto.put("trip_id", orders.getTripId());
                dto.put("payment_status", orders.getPaymentStatus());
                dto.put("order_status", orders.getStatus().name());
                String amountTotal = null;
                if (orders.getCurrency().equalsIgnoreCase("USD")) {
                    amountTotal = orders.getCurrency() + " " + orders.getAmount();
                } else amountTotal = orders.getCurrency() + " " + orders.getSlshAmount();
                dto.put("amount_total", amountTotal);

                String cancelMessage = null;
                String nRating = null;
                String nRemark = null;
                NurseServiceState state = nurseServiceStateRepository.findByOrderId(orders.getId());
                if (state != null) {
                    cancelMessage = state.getCancelMessage();
                    nRating = state.getNRating();
                    nRemark = state.getNRemark();
                }
                dto.put("cancel_message", cancelMessage);
                dto.put("rating", nRating);
                dto.put("review", nRemark);
                dto.put("order_date", formatter.format(orders.getCreatedAt()));

                list.add(dto);
            }
            Map<String, Object> data = new HashMap<>();
            data.put("status", Constants.SUCCESS_CODE);
            data.put("message", messageSource.getMessage(Constants.ORDERS_FETCH_SUCCESSFULLY, null, locale));
            data.put("data", list);
            data.put("count", count);
            return ResponseEntity.status(HttpStatus.OK).body(data);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("Error found in pt-confirmed-history api :{}", e);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new Response(
                    Constants.NO_CONTENT_FOUNT_CODE,
                    Constants.NO_CONTENT_FOUNT_CODE,
                    messageSource.getMessage(Constants.SOMETHING_WENT_WRONG, null, locale)
            ));
        }
    }

    public ResponseEntity<?> checkNotification(Locale locale, Integer userId) {
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new Response(
                    Constants.NO_CONTENT_FOUNT_CODE,
                    Constants.NO_CONTENT_FOUNT_CODE,
                    messageSource.getMessage(Constants.BLANK_DATA_GIVEN, null, locale),
                    new ArrayList<>()
            ));
        }
        try {
            LocalDateTime sd = LocalDateTime.now(ZoneId.of(zone)).minusDays(1);
            NurseServiceState state = nurseServiceStateRepository.findDataByDate(userId, State.Completed, sd, LocalDateTime.now(ZoneId.of(zone)));
            if (state != null) {
                PartnerNurse nurse = partnerNurseRepository.findById(state.getNurseId()).orElse(null);

                String message = messageSource.getMessage(Constants.RATING_DETAIL_TEXT_NOD, null, locale);
                message = message.replace("{{nurse_name}}", nurse != null ? nurse.getName() : "");

                Map<String, Object> nurseDetail = new HashMap<>();
                nurseDetail.put("id", nurse.getId());
                nurseDetail.put("name", nurse.getName());

                Map<String, Object> dto = creatingDto(state);
                dto.put("nurseDetail", nurseDetail);

                Map<String, Object> response = new HashMap<>();
                response.put("rating_detail_text_nod", message);
                response.put("type", "nod_rating");
                response.put("order_detail", dto);

                return ResponseEntity.status(HttpStatus.OK).body(new Response(
                        Constants.SUCCESS_CODE,
                        Constants.SUCCESS_CODE,
                        messageSource.getMessage(Constants.SUCCESS_MESSAGE, null, locale),
                        response
                ));
            }
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new Response(
                    Constants.BLANK_DATA_GIVEN_CODE,
                    Constants.BLANK_DATA_GIVEN_CODE,
                    messageSource.getMessage(Constants.BLANK_DATA_GIVEN, null, locale)
            ));
        } catch (Exception e) {
            e.printStackTrace();
            log.error("Error found in check- notification api :{}", e);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new Response(
                    Constants.NO_CONTENT_FOUNT_CODE,
                    Constants.NO_CONTENT_FOUNT_CODE,
                    messageSource.getMessage(Constants.SOMETHING_WENT_WRONG, null, locale)
            ));
        }
    }

    private Map<String, Object> creatingDto(NurseServiceState state) {
        Map<String, Object> dto = new HashMap<>();
        dto.put("id", state.getId());
        dto.put("order_id", state.getOrderId());
        dto.put("patient_id", state.getPatientId());
        dto.put("nurse_id", state.getNurseId());
        dto.put("lat_patient", state.getLatPatient());
        dto.put("long_patient", state.getLongPatient());
        dto.put("lat_nurse", state.getLatNurse());
        dto.put("long_nurse", state.getLongNurse());
        dto.put("state", state.getState().name());
        dto.put("distance", state.getDistance());
        dto.put("search_id", state.getSearchId());
        dto.put("p_remark", state.getPRemark());
        dto.put("p_rating", state.getPRating());
        dto.put("rating_notified_to_patient", state.getRatingNotifiedToPatient());
        dto.put("n_remark", state.getNRemark());
        dto.put("n_rating", state.getNRating());
        dto.put("cancel_message", state.getCancelMessage());
        dto.put("cancel_by", state.getCancelBy().name());
        dto.put("device_env", state.getDeviceEnv().name());
        dto.put("confirm_ack", state.getConfirmAck());
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        dto.put("cancel_at", formatter.format(state.getCancelAt()));
        dto.put("arrived_at", formatter.format(state.getArrivedAt()));
        dto.put("created_at", formatter.format(state.getCreatedAt()));
        dto.put("updated_at", formatter.format(state.getUpdatedAt()));
        dto.put("completed_at", formatter.format(state.getCompletedAt()));

        return dto;
    }

    public ResponseEntity<?> updateNotifiedFlag(Locale locale, NotificationFlagRequest request) {
        if (request.getUser_id() == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new Response(
                    Constants.NO_CONTENT_FOUNT_CODE,
                    Constants.NO_CONTENT_FOUNT_CODE,
                    messageSource.getMessage(Constants.BLANK_DATA_GIVEN, null, locale),
                    new ArrayList<>()
            ));
        }
        if(request.getId() != null){
            NurseServiceState state = nurseServiceStateRepository.findById(request.getId()).orElse(null);
            if(state != null){
                state.setRatingNotifiedToPatient(1);
                nurseServiceStateRepository.save(state);

                return ResponseEntity.status(HttpStatus.OK).body(new Response(
                        Constants.SUCCESS_CODE,
                        Constants.SUCCESS_CODE,
                        messageSource.getMessage(Constants.SUCCESS_MESSAGE, null, locale)
                ));
            }

        }
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new Response(
                Constants.NO_CONTENT_CODE,
                Constants.NO_CONTENT_CODE,
                messageSource.getMessage(Constants.SOMETHING_WENT_WRONG, null, locale)
        ));
    }
}
