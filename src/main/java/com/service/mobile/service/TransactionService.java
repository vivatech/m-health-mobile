package com.service.mobile.service;

import com.service.mobile.config.Constants;
import com.service.mobile.dto.enums.OrderStatus;
import com.service.mobile.dto.request.MyTransactionsRequest;
import com.service.mobile.dto.response.MyTransactionsResponse;
import com.service.mobile.dto.response.Response;
import com.service.mobile.model.*;
import com.service.mobile.repository.*;
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

    public ResponseEntity<?> myTransactions(Locale locale, MyTransactionsRequest request) {
        Pageable pageable = PageRequest.of(request.getPage(),10);
        List<WalletTransaction> transactions = new ArrayList<>();
        List<MyTransactionsResponse> responses = new ArrayList<>();
        Long total = 0L;
        if(request.getCreated_date()!=null){
            if(request.getType()!=null && !request.getType().isEmpty() &&
                    !request.getType().equalsIgnoreCase("all")){
                Page<WalletTransaction> page = walletTransactionRepository.
                        findByPatientIdIsDebitCreditServiceTypeCreatedAt(
                                request.getUser_id(),"debit",request.getCreated_date(),request.getType(),pageable
                        );
                transactions = page.getContent();
                total = page.getTotalElements();
            }else{
                Page<WalletTransaction> page = walletTransactionRepository.
                        findByPatientIdIsDebitCreditCreatedAt(
                                request.getUser_id(),"debit",request.getCreated_date(),pageable
                        );
                transactions = page.getContent();
                total = page.getTotalElements();
            }
        }
        else{
            if(request.getType()!=null && !request.getType().isEmpty() &&
                    !request.getType().equalsIgnoreCase("all")){
                Page<WalletTransaction> page = walletTransactionRepository.
                        findByPatientIdServiceType(
                                request.getUser_id(),"debit",request.getType(),pageable
                        );
                transactions = page.getContent();
                total = page.getTotalElements();
            }else{
                Page<WalletTransaction> page = walletTransactionRepository.
                        findByPatientIdIsDebit(
                                request.getUser_id(),"debit",pageable);
                transactions = page.getContent();
                total = page.getTotalElements();
            }
        }

        if(!transactions.isEmpty()){
            for(WalletTransaction wt:transactions){
                String title = "";
                Integer case_id = null;
                String orderStatus = null;
                Orders orderDetail = ordersRepository.findById(wt.getOrderId()).orElse(null);
                HealthTipOrders healthTipOrders = healthTipOrdersRepository.findById(wt.getOrderId()).orElse(null);
                LabOrders labOrders = labOrdersRepository.findById(wt.getOrderId()).orElse(null);
                if(wt.getServiceType().equalsIgnoreCase("consultation")){

                    if(orderDetail!=null && orderDetail.getPackageId()!=null && orderDetail.getHealthtipPackageId()!=null){
                        if(orderDetail.getStatus()==OrderStatus.Cancelled && wt.getIsDebitCredit().equalsIgnoreCase("credit")){
                            title = messageSource.getMessage(Constants.CONSULT_REQUEST_REJECT,null,locale);
                        }else if(wt.getIsDebitCredit().equalsIgnoreCase("credit") && wt.getOrderId()==null){
                            title = messageSource.getMessage(Constants.MONEY_ADDED_WALLET,null,locale);
                        }else{
                            if(orderDetail.getStatus() == OrderStatus.Cancelled && wt.getIsDebitCredit().equalsIgnoreCase("DEBIT")){
                                title = messageSource.getMessage(Constants.BOOKED_CONSULT,null,locale);
                            }else{
                                if(orderDetail.getStatus() != OrderStatus.Cancelled && wt.getIsDebitCredit().equalsIgnoreCase("DEBIT")){
                                    title = messageSource.getMessage(Constants.BOOKED_CONSULT,null,locale);
                                }
                            }
                        }
                    }else{
                        if(orderDetail.getStatus()==OrderStatus.Cancelled && wt.getIsDebitCredit().equalsIgnoreCase("credit")){
                            title = messageSource.getMessage(Constants.CONSULT_REQUEST_REJECT,null,locale);
                        }else{
                            if(orderDetail.getStatus() == OrderStatus.Cancelled && wt.getIsDebitCredit().equalsIgnoreCase("DEBIT")){
                                title = messageSource.getMessage(Constants.BOOKED_CONSULT,null,locale);
                            }else{
                                if(orderDetail.getStatus() != OrderStatus.Cancelled && wt.getIsDebitCredit().equalsIgnoreCase("DEBIT") &&
                                        orderDetail.getPackageId()!=null &&
                                    (orderDetail.getPackageId().getPackageName() !=null && !orderDetail.getPackageId().getPackageName().isEmpty())
                                ){
                                    title = messageSource.getMessage(Constants.PURCHASE_PACKAGE,null,locale);
                                }else if(orderDetail.getStatus() != OrderStatus.Cancelled && wt.getIsDebitCredit().equalsIgnoreCase("DEBIT") &&
                                        orderDetail.getHealthtipPackageId()!=null &&
                                        (orderDetail.getHealthtipPackageId().getPackageName() !=null && !orderDetail.getHealthtipPackageId().getPackageName().isEmpty())
                                ){
                                    title = messageSource.getMessage(Constants.PURCHASE_PACKAGE,null,locale);
                                }
                            }
                        }

                    }
                }
                else if(wt.getServiceType().equalsIgnoreCase("lab")){
                    if(labOrders!=null){
                        orderStatus = labOrders.getStatus().toString();
                    }
                    title = messageSource.getMessage(Constants.BOOKED_LAB_MSG,null,locale);
                }
                else if(wt.getServiceType().equalsIgnoreCase("load_wallet_balance")){
                    orderStatus = wt.getTransactionStatus();
                    title = publicService.getPaymentServiceType(locale).getLoad_wallet_balance();
                }
                else if(wt.getServiceType().equalsIgnoreCase("nurse_on_demand")){
                    if(orderDetail!=null){
                        orderStatus = orderDetail.getStatus().toString();
                    }
                    title = messageSource.getMessage(Constants.NURSE_ON_DEMAND,null,locale);
                }
                else{
                    if(healthTipOrders!=null){
                        orderStatus = healthTipOrders.getStatus().toString();
                    }
                    title = messageSource.getMessage(Constants.HEALTHTIP_PURCHASED,null,locale);
                }

                String currency = "";
                HealthTipPackageCategories packData = null;
                if(orderDetail!=null && wt.getServiceType().equalsIgnoreCase("consultation")){
                    currency = (orderDetail.getCurrency()!=null && !orderDetail.getCurrency().isEmpty())?
                            orderDetail.getCurrency() + " " + orderDetail.getCurrencyAmount() :
                            currencySymbolFdj + " " + orderDetail.getAmount();
                }
                else if(healthTipOrders!=null && wt.getServiceType().equalsIgnoreCase("healthtip")){
                    currency = (healthTipOrders.getCurrency()!=null && !healthTipOrders.getCurrency().isEmpty())?
                            healthTipOrders.getCurrency() + " " + healthTipOrders.getCurrencyAmount() :
                            currencySymbolFdj + " " + healthTipOrders.getAmount();
                    List<HealthTipPackageCategories> packDataList = healthTipPackageCategoriesRepository.findByPackageIds(healthTipOrders.getHealthTipPackage().getPackageId());
                    if(!packDataList.isEmpty()){packData = packDataList.get(0);}
                }
                else{
                    currency = currencySymbolFdj + " " + wt.getAmount();
                }
                String healthtips_package_name = "";
                if((locale.getLanguage().equalsIgnoreCase("en"))){
                    if(packData!=null && packData.getHealthTipCategoryMaster()!=null
                            && packData.getHealthTipCategoryMaster().getNameSl()!=null &&
                            !packData.getHealthTipCategoryMaster().getNameSl().isEmpty()){
                        healthtips_package_name = packData.getHealthTipCategoryMaster().getNameSl();
                    }
                }else{
                    if(packData!=null && packData.getHealthTipCategoryMaster()!=null
                            && packData.getHealthTipCategoryMaster().getName()!=null &&
                            !packData.getHealthTipCategoryMaster().getName().isEmpty()){
                        healthtips_package_name = packData.getHealthTipCategoryMaster().getName();
                    }
                }

                MyTransactionsResponse data = new MyTransactionsResponse();

                data.setTitle(title);
                data.setCase_id(case_id);
                data.setTransaction_id(wt.getTransactionId());
                data.setContact_number((wt.getPayerMobile()!=null && !wt.getPayerMobile().isEmpty())? Long.valueOf(wt.getPayerMobile().replace("+","")) : "-");
                data.setTransaction_type(wt.getIsDebitCredit().equalsIgnoreCase("DEBIT") ?
                                ("+"+currencySymbolFdj + " " + currency) : ("-" + wt.getAmount() + "-" + currency)
                        );
                data.setPackage_name(
                        (orderDetail!=null && orderDetail.getPackageId()!=null)?orderDetail.getPackageId().getPackageName():""
                );
                data.setHealthtips_package_name(healthtips_package_name);
                data.setDoctor_name(
                        (orderDetail!=null && orderDetail.getDoctorId()!=null)?orderDetail.getDoctorId().getFirstName() + " " + orderDetail.getDoctorId().getLastName():""
                );
                data.setCreated_at(wt.getCreatedAt());
                data.setStatus((orderStatus!=null && orderStatus.equalsIgnoreCase("Cancel"))?"Cancelled":orderStatus);
                data.setConsultation_type(
                        (orderDetail!=null && orderDetail.getCaseId().getConsultType()!=null)?
                                orderDetail.getCaseId().getConsultType():messageSource.getMessage(Constants.PAID_MSG,null,locale)
                );
                data.setAdded_type((orderDetail!=null && orderDetail.getCaseId()!=null)?orderDetail.getCaseId().getAddedType().toString():"");
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
