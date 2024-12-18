package com.service.mobile.service;

import com.service.mobile.config.Constants;
import com.service.mobile.dto.enums.OrderStatus;
import com.service.mobile.dto.request.MyTransactionsRequest;
import com.service.mobile.dto.response.MyTransactionsResponse;
import com.service.mobile.dto.response.Response;
import com.service.mobile.model.*;
import com.service.mobile.repository.*;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Service
public class TransactionService {

    @Autowired
    private WalletTransactionRepository walletTransactionRepository;

    @Autowired
    private MessageSource messageSource;

    @Autowired
    private PublicService publicService;

    @Autowired
    private LabOrdersRepository labOrdersRepository;

    @Autowired
    private OrdersRepository ordersRepository;

    @Autowired
    private HealthTipOrdersRepository healthTipOrdersRepository;

    @Autowired
    private HealthTipPackageCategoriesRepository healthTipPackageCategoriesRepository;

    @Value("${app.currency.symbol.fdj}")
    private String currencySymbolFdj;
    @PersistenceContext
    private EntityManager entityManager;
    @Autowired
    private NurseDemandOrdersRepository nurseDemandOrdersRepository;

    public ResponseEntity<?> myTransactions(Locale locale, MyTransactionsRequest request) {
        if(request.getUser_id() == null || request.getUser_id() == 0
            || request.getPage() == null ){
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new Response(
                    Constants.BLANK_DATA_GIVEN,
                    Constants.BLANK_DATA_GIVEN_CODE,
                    messageSource.getMessage(Constants.BLANK_DATA_GIVEN, null, locale)
            ));
        }
        StringBuilder sb = new StringBuilder("Select t From WalletTransaction t WHERE t.patientId.userId = :userId AND t.isDebitCredit = 'debit' ");
        if(request.getCreated_date() != null){
            sb.append("AND DATE(t.createdAt) = :createdAt ");
        }
        if(request.getType() != null && !request.getType().isEmpty()){
            sb.append("AND t.serviceType = :type ");
        }
        sb.append("ORDER BY t.id DESC");

        Query query = entityManager.createQuery(sb.toString(), WalletTransaction.class);

        query.setParameter("userId", request.getUser_id());
        if(request.getCreated_date() != null){
            query.setParameter("createdAt", request.getCreated_date());
        }
        if(request.getType() != null && !request.getType().isEmpty()){
            query.setParameter("type", request.getType());
        }
        List<WalletTransaction> transactions = query.getResultList();
        Long total = (long) transactions.size();
        // Apply pagination
        int page = request.getPage();
        int pageSize = 10;
        query.setFirstResult(page * pageSize);
        query.setMaxResults(pageSize);

        transactions = query.getResultList();

        List<MyTransactionsResponse> responses = new ArrayList<>();

        if(!transactions.isEmpty()){
            for(WalletTransaction wt:transactions){
                String title = "";
                Object case_id = "";
                String orderStatus = null;
                String currency = "";
                String healthTipPackageName = "";
                String packageName = "";
                String doctorName = "";
                String consultationType = "";
                String addedType = null;
                float amount = 0.0f;
                if(wt.getServiceType().equalsIgnoreCase("consultation")){
                    title = messageSource.getMessage(Constants.BOOKED_CONSULT,null,locale);
                    if(wt.getOrderId() != null) {
                        Orders orders = ordersRepository.findById(wt.getOrderId()).orElse(null);
                        if(orders != null){
                            case_id = orders.getCaseId().getCaseId();
                            orderStatus = orders.getStatus().name();
                            currency = orders.getCurrencyAmount() == null ? currencySymbolFdj + " " + orders.getAmount()
                                            : orders.getCurrency() + " " + orders.getCurrencyAmount();
                            amount = orders.getAmount();
                            doctorName = orders.getDoctorId().getFirstName() + " " + orders.getDoctorId().getLastName();
                            consultationType = orders.getCaseId().getConsultType() != null
                                    ? orders.getCaseId().getConsultType()
                                    : messageSource.getMessage(Constants.PAID_MSG,null,locale);
                            addedType = orders.getCaseId().getAddedType() != null
                                    ? orders.getCaseId().getAddedType().name()
                                    : "";
                        }
                    }else continue;
                }
                else if(wt.getServiceType().equalsIgnoreCase("healthtip")){
                    title = messageSource.getMessage(Constants.HEALTHTIP_PURCHASED,null,locale);
                    if(wt.getOrderId() != null){
                        HealthTipOrders orders = healthTipOrdersRepository.findById(wt.getOrderId()).orElse(null);
                        orderStatus = orders == null ? "" : orders.getStatus().name();
                        currency = orders == null ? "" :
                                (orders.getCurrencyAmount() == null ? currencySymbolFdj + " " + orders.getAmount()
                                        : orders.getCurrency() + " " + orders.getCurrencyAmount());
                        amount = orders.getAmount();
                        HealthTipPackageCategories categories = healthTipPackageCategoriesRepository.findByHealthTipPackage(orders.getHealthTipPackage().getPackageId());
                        healthTipPackageName = categories == null ? "" :
                                (locale.getLanguage().equalsIgnoreCase("en")
                                        ? categories.getHealthTipCategoryMaster().getName()
                                        : categories.getHealthTipCategoryMaster().getNameSl());
                        packageName = orders == null ? "" :
                                (locale.getLanguage().equals("en")
                                        ? orders.getHealthTipPackage().getPackageName()
                                        : orders.getHealthTipPackage().getPackageNameSl());
                    }else continue;
                }
                else if(wt.getServiceType().equalsIgnoreCase("nurse_on_demand")){
                    title = messageSource.getMessage(Constants.NURSE_ON_DEMAND,null,locale);
                    if(wt.getOrderId() != null){
                        NurseDemandOrders orders = nurseDemandOrdersRepository.findById(wt.getOrderId()).orElse(null);
                        orderStatus = orders == null ? "" : orders.getStatus().name();
                    }else continue;
                }
                else if(wt.getServiceType().equalsIgnoreCase("lab")){
                    title = messageSource.getMessage(Constants.BOOKED_LAB_MSG,null,locale);
                    if(wt.getOrderId() != null){
                        LabOrders orders = labOrdersRepository.findById(wt.getOrderId()).orElse(null);
                        orderStatus = orders == null ? "" : orders.getStatus().name();
                    }else continue;
                }
                else if(wt.getServiceType().equalsIgnoreCase("load_wallet_balance")){
                    title = publicService.getPaymentServiceType(locale).getLoad_wallet_balance();
                }
                else continue;

                MyTransactionsResponse data = new MyTransactionsResponse();

                data.setTitle(title);
                data.setCase_id(case_id);
                data.setTransaction_id(wt.getTransactionId());
                data.setContact_number((wt.getPayerMobile()!=null && !wt.getPayerMobile().isEmpty())? Long.valueOf(wt.getPayerMobile().replace("+","")) : "-");
                data.setTransaction_type(wt.getIsDebitCredit().equalsIgnoreCase("credit")
                        ? (locale.getLanguage().equalsIgnoreCase("en") ? "+USD " + amount : "+ " + currencySymbolFdj + " " + amount)
                        : "-" + currency);
                data.setPackage_name(packageName);
                data.setHealthtips_package_name(healthTipPackageName);
                data.setDoctor_name(doctorName);
                data.setCreated_at(wt.getCreatedAt());
                data.setStatus((orderStatus!=null && orderStatus.equalsIgnoreCase("Cancel"))?"Cancelled":orderStatus);
                data.setConsultation_type(consultationType);
                data.setAdded_type(addedType);
                data.setTotal_count(total);

                responses.add(data);

            }
            return ResponseEntity.status(HttpStatus.OK).body(new Response(
                    Constants.SUCCESS_CODE,
                    Constants.SUCCESS_CODE,
                    messageSource.getMessage(Constants.TRANSACTION_FETCH_SUCCESSFULLY,null,locale),
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
}
