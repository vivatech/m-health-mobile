package com.service.mobile.service;

import com.service.mobile.config.Constants;
import com.service.mobile.dto.Status;
import com.service.mobile.dto.enums.YesNo;
import com.service.mobile.dto.request.UpdateFullNameRequest;
import com.service.mobile.dto.response.ActiveHealthTipsPackageResponse;
import com.service.mobile.dto.response.ActivitiesResponse;
import com.service.mobile.dto.response.Response;
import com.service.mobile.dto.response.UpdateFullnameResponse;
import com.service.mobile.model.*;
import com.service.mobile.repository.*;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.security.SecureRandom;
import java.util.Locale;

@Service
@Slf4j
public class PatientService {

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

    @Value("${app.development.environment}")
    private Boolean isDevelopmentEnvironment;


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
}
