package com.service.mobile.service;

import com.service.mobile.config.Constants;
import com.service.mobile.dto.dto.*;
import com.service.mobile.dto.enums.*;
import com.service.mobile.dto.enums.State;
import com.service.mobile.dto.request.GetNurseLocationInfoRequest;
import com.service.mobile.dto.request.LogsNurseNotFoundRequest;
import com.service.mobile.dto.request.NodAckRequest;
import com.service.mobile.dto.request.SendNurseOnDemandMsgRequest;
import com.service.mobile.dto.response.GetNurseLocationInfoResponse;
import com.service.mobile.dto.response.Response;
import com.service.mobile.model.*;
import com.service.mobile.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Service
public class NursePartnerService {

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

    public ResponseEntity<?> ptOnlineNurses(Locale locale, Integer userId) {

        List<AvailableNursesMapDto> map = publicService.availableNursesMap();
        List<AvailableNursesMapDto> result = new ArrayList<>();
        List<String> contactNumber = new ArrayList<>();
        List<String> contactNumbersLocal = new ArrayList<>();
        for(AvailableNursesMapDto dto:map){
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
                messageSource.getMessage(Constants.SUCCESS_MESSAGE,null,locale),
                result
        ));
    }

    public ResponseEntity<?> nurseService(Locale locale) {
        List<NurseService> services = nurseServiceRepository.findByStatus("A");
        if (services != null && !services.isEmpty()) {
            String currency = "₹"; // Retrieve the currency symbol from application properties or other configuration
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

                serve.setService_price(currency + " " + serv.getTotalServicePrice());

                if (serv.getServiceImage() != null && !serv.getServiceImage().isEmpty()) {
                    serve.setService_image(baseUrl + "/uploaded_file/nurse_services/" + serv.getId() + "/" + serv.getServiceImage());
                } else {
                    serve.setService_image(baseUrl + "/uploaded_file/noimagefound.png");
                }

                data.add(serve);
            }

            return ResponseEntity.status(HttpStatus.OK).body(new Response(
                    Constants.SUCCESS_CODE,
                    Constants.SUCCESS_CODE,
                    messageSource.getMessage(Constants.NURSE_SERVICE_FETCHED,null,locale),
                    data
            ));
        } else {
            return ResponseEntity.status(HttpStatus.NO_CONTENT).body(new Response(
                    Constants.BLANK_DATA_GIVEN_CODE,
                    Constants.BLANK_DATA_GIVEN_CODE,
                    messageSource.getMessage(Constants.BLANK_DATA_GIVEN,null,locale)
            ));
        }
    }

    public ResponseEntity<?> logsNurseNotFound(Locale locale, LogsNurseNotFoundRequest request) {
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
            publicService.sendNurseOnDemandMsg(tempRequest, "NURSE_NOT_FOUND_PATIENT_NOD", UserType.Patient,locale);
        }

        if (userId!=null && userId!=0 &&
            state!=null &&!state.isEmpty() &&
            reason!=null && !reason.isEmpty() &&
            latPatient!=null && !latPatient.isEmpty() &&
            longPatient!=null && !longPatient.isEmpty() &&
            searchId!=null && !searchId.isEmpty()) {
            Long transactionCount = walletTransactionRepository.countByPatientId(userId);
            OrderType orderType = (transactionCount > 0) ? OrderType.ONE : OrderType.ZERO;

            NodLog nodLog = new NodLog();
            nodLog.setUserId(userId);
            nodLog.setSearchId(searchId);
            nodLog.setLat(latPatient);
            nodLog.setLng(longPatient);
            nodLog.setOrderType(orderType);
            nodLog.setReason(reason);
            nodLog.setChannel(Channel.Mobile);
            nodLog.setStatus("Failed");
            nodLogRepository.save(nodLog);

            SendNurseOnDemandMsgRequest tempRequest = new SendNurseOnDemandMsgRequest();
            tempRequest.setPatient_id(userId);
            tempRequest.setNurse_id(0);
            tempRequest.setId(0);
            tempRequest.setStatus(reason);
            publicService.sendNurseOnDemandMsg(tempRequest, "AGENT_NOTIFICATION_FOR_FAILED_NOD", UserType.Patient,locale);

            return ResponseEntity.status(HttpStatus.OK).body(new Response(
                    Constants.SUCCESS_CODE,
                    Constants.SUCCESS_CODE,
                    messageSource.getMessage(Constants.SUCCESS_MESSAGE,null,locale)
            ));
        } else {
            return ResponseEntity.status(HttpStatus.NO_CONTENT).body(new Response(
                    Constants.BLANK_DATA_GIVEN_CODE,
                    Constants.BLANK_DATA_GIVEN_CODE,
                    messageSource.getMessage(Constants.REQUEST_PARAM_MISSING,null,locale)
            ));
        }
    }

    public ResponseEntity<?> getNurseLocationInfo(Locale locale, GetNurseLocationInfoRequest request) {
        if(request.getP_latutude()!=null && request.getP_latutude()!=0 &&
            request.getP_longitude()!=null && request.getP_longitude()!=0 &&
            request.getN_latutude()!=null && request.getN_latutude()!=0 &&
            request.getN_longitude()!=null && request.getN_longitude()!=0 &&
            request.getService_id()!=null && !request.getService_id().isEmpty() &&
            request.getNurse_mobile()!=null && !request.getNurse_mobile().isEmpty() &&
            request.getSearch_id()!=null && request.getSearch_id()!=0 &&
            request.getUser_id()!=null && request.getUser_id()!=0 &&
            request.getDistance()!=null && request.getDistance()!=0
        ){
            GetNurseLocationInfoResponse response = new GetNurseLocationInfoResponse();
            Float servicePrice = 0.0f;
            List<NamePriceDto> serviceArr = new ArrayList<>();

            List<PartnerNurse> nurses = partnerNurseRepository.findByContactNumberIdDesc(request.getNurse_mobile());
            PartnerNurse nurse = null;
            for(PartnerNurse n:nurses){nurse = n;}
            GlobalConfiguration ondemand_rate = globalConfigurationRepository.findByKey("ONDEMAND_RATE");
            GlobalConfiguration min_distance_fee = globalConfigurationRepository.findByKey("ONDEMAND_MIN_DISTANCE_FEE");
            GlobalConfiguration sls_rate = globalConfigurationRepository.findByKey("WAAFI_PAYMENT_RATE");

            String[] serviceIds = request.getService_id().split(",");
            List<Integer> serviceIntIds = new ArrayList<>();
            for(String s:serviceIds){serviceIntIds.add(Integer.parseInt(s));}
            List<NurseService> service = nurseServiceRepository.findByIdsAndStatus(serviceIntIds,"A");

            if(!service.isEmpty()){
                for(NurseService n:service){
                    servicePrice = servicePrice + n.getTotalServicePrice();
                    NamePriceDto temp = new NamePriceDto();
                    temp.setName(n.getSeviceName());
                    temp.setPrice(currencySymbol + " "+n.getTotalServicePrice());
                    serviceArr.add(temp);
                }
                Float distanceFees = request.getDistance() * Float.valueOf(ondemand_rate.getValue());
                if(Float.valueOf(min_distance_fee.getValue()) > distanceFees){
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

            IdNameMobileDto nurseData = new IdNameMobileDto();
            nurseData.setId(nurse.getId());
            nurseData.setMobile(nurse.getContactNumber());
            nurseData.setName(nurse.getName());
            response.setNurse(nurseData);

            NurseServiceState nurseServiceState = new NurseServiceState();
            nurseServiceState.setPatientId(request.getUser_id());
            nurseServiceState.setNurseId(nurse.getId());
            nurseServiceState.setLatPatient(request.getP_latutude().toString());
            nurseServiceState.setLongPatient(request.getN_longitude().toString());
            nurseServiceState.setLatNurse(request.getN_latutude().toString());
            nurseServiceState.setLongNurse(request.getN_longitude().toString());
            nurseServiceState.setState(State.PROCESSING);
            nurseServiceState.setDistance(request.getDistance().toString());
            nurseServiceState.setSearchId(request.getSearch_id().toString());
            nurseServiceState = nurseServiceStateRepository.save(nurseServiceState);
            response.setState_id(nurseServiceState.getId());

            return ResponseEntity.status(HttpStatus.OK).body(new Response(
                    Constants.SUCCESS_CODE,
                    Constants.SUCCESS_CODE,
                    messageSource.getMessage(Constants.RECORD_FETCHED,null,locale),
                    response
            ));

        }else{
            return ResponseEntity.status(HttpStatus.NO_CONTENT).body(new Response(
                    Constants.BLANK_DATA_GIVEN_CODE,
                    Constants.BLANK_DATA_GIVEN_CODE,
                    messageSource.getMessage(Constants.BLANK_DATA_GIVEN,null,locale)
            ));
        }
    }

    //NOTE-TODO make this api
    public ResponseEntity<?> processPayment(Locale locale, ProcessPaymentRequest request) {
        return null;
    }


    public ResponseEntity<?> nodack(Locale locale, NodAckRequest request) {
        publicService.confirmAck(request);
        return ResponseEntity.status(HttpStatus.OK).body(new Response(
                Constants.SUCCESS_CODE,
                Constants.SUCCESS_CODE,
                messageSource.getMessage(Constants.SUCCESS_MESSAGE,null,locale)
        ));
    }

    public ResponseEntity<?> ndPatientOrderDetail(Locale locale, Integer userId) {
        Users patient = usersRepository.findById(userId).orElse(null);
        if(userId!=null){
            List<NurseDemandOrders> orders = nurseDemandOrdersRepository.findByPatientNurseNotNullPaymentStatusStatus(
                    patient.getUserId(), PaymentStatus.Completed,StatusFullName.Inprogress
            );
            List<NdPatientOrderDetailDto> response = new ArrayList<>();
            if(!orders.isEmpty()){
                for(NurseDemandOrders n:orders){
                    NdPatientOrderDetailDto dto = new NdPatientOrderDetailDto();

                    dto.setOrder_id(n.getId());
                    dto.setTrip_id(n.getTripId());
                    dto.setNurse_name(n.getNurseId().getName());
                    dto.setNurse_contact(n.getNurseId().getContactNumber());
                    dto.setDate(n.getCreatedAt());


                    NurseServiceState state = null;
                    List<NurseServiceState> states = nurseServiceStateRepository.findByOrderId(n.getId());
                    for(NurseServiceState s:states){state = s;}
                    if(state!=null){dto.setState(state.getState());}

                    if(n.getCurrency().equalsIgnoreCase("USD")){
                        dto.setOrder_amount(n.getCurrency() + " "+n.getAmount());
                    }else{
                        dto.setOrder_amount(n.getCurrency() + " "+n.getSlshAmount());
                    }

                    List<NurseServiceOrder> nurseServiceOrders = nurseServiceOrderRepository.findByOrderId(n.getId());
                    String serviceType = "";
                    if(!nurseServiceOrders.isEmpty()){
                        List<Integer> ids = new ArrayList<>();
                        for(NurseServiceOrder nso:nurseServiceOrders){ids.add(nso.getId().getServiceId());}

                        List<NurseService> nurseServices = nurseServiceRepository.findByIds(ids);
                        for(NurseService nso:nurseServices){serviceType = serviceType + "," + nso.getSeviceName();}

                    }
                    dto.setService_type(serviceType);
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
}
