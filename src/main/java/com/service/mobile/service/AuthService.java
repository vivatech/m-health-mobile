package com.service.mobile.service;

import com.service.mobile.config.AuthConfig;
import com.service.mobile.config.Constants;
import com.service.mobile.config.Utility;
import com.service.mobile.dto.enums.YesNo;
import com.service.mobile.dto.request.MobileReleaseRequest;
import com.service.mobile.dto.request.VerifyOtpRequest;
import com.service.mobile.dto.response.LoginResponse;
import com.service.mobile.dto.response.Response;
import com.service.mobile.dto.response.VerifyOtpResponse;
import com.service.mobile.model.UserOTP;
import com.service.mobile.model.Users;
import com.service.mobile.repository.UserOTPRepository;
import com.service.mobile.repository.UsersRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import static com.service.mobile.config.Constants.*;

@Service
public class AuthService {
    @Value("${app.otp.expiry.minutes}")
    private Long expiryTime;
    @Autowired
    private UsersRepository usersRepository;
    @Autowired
    PasswordEncoder encoder;
    @Autowired
    private MessageSource messageSource;
    @Autowired
    private UserOTPRepository userOTPRepository;
    @Autowired
    private AuthConfig authConfig;
    @Autowired
    private LanguageService languageService;
    @Autowired
    private Utility utility;

    public Response actionLogin(MobileReleaseRequest request, Locale locale) {
        Response response;
        if(request.getContact_number() == null || request.getContact_number().isEmpty()){
            response = new Response(
                    Constants.NO_RECORD_FOUND_CODE,
                    Constants.BLANK_DATA_GIVEN_CODE,
                    messageSource.getMessage(Constants.BLANK_DATA_GIVEN,null,locale));
        }
        else{
            Users users = usersRepository.findByContactNumber(request.getContact_number()).orElse(null);
            if(users != null){
                if(users.getIsSuspended() == 1){
                    response = new Response(
                            Constants.NO_RECORD_FOUND_CODE,
                            Constants.NO_RECORD_FOUND_CODE,
                            messageSource.getMessage(Constants.USER_SUSPENDED,null,locale));
                }
                else {
                    int otp = 123456; //TODO : SMS integration

                    //save otp into user otp table
                    saveOtpIntoUserOtpTableAndUsersTable(users, otp);

                    LoginResponse ress = setLoginResponse();

                    Map<String, Object> responseData = new HashMap<>();
                    responseData.put("is_registered", YesNo.Yes.toString());
                    responseData.put("userData", ress);

                    response = new Response(
                            Constants.SUCCESS_CODE,
                            Constants.SUCCESS_CODE,
                            messageSource.getMessage(Constants.USER_LOGIN_IS_SUCCESS, null, locale),
                            responseData
                    );
                }
            }
            else response = new Response(
                        Constants.NO_RECORD_FOUND_CODE,
                        Constants.NO_RECORD_FOUND_CODE,
                        messageSource.getMessage(Constants.MOBILE_USER_NOT_FOUND,null,locale));
        }
        return response;
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

    private LoginResponse setLoginResponse() {
        LoginResponse response = new LoginResponse();
        response.setIs_registered(YesNo.Yes.toString());
        return response;
    }

    public Response actionVerifyOtp(VerifyOtpRequest request, Locale locale) {
        Response responseData ;
        LocaleContextHolder.setLocale(locale);

        try {
            if (request.getContact_number() == null || request.getContact_number().isEmpty()
                    || request.getOtp() == null || request.getOtp().isEmpty()
                    || request.getIs_registered() == null || request.getIs_registered().isEmpty()
                    || request.getDevice_token() == null || request.getDevice_token().isEmpty()) {
                responseData = new Response(
                        Constants.NO_RECORD_FOUND_CODE,
                        Constants.BLANK_DATA_GIVEN_CODE,
                        messageSource.getMessage(Constants.BLANK_DATA_GIVEN, null, locale));
            } else {
                Users users = usersRepository.findByContactNumber(request.getContact_number()).orElse(null);
                if (users != null) {
                    if (users.getIsSuspended() == 1 || users.getAttemptCounter() == 10) {
                        responseData = new Response(
                                Constants.OTP_EXPIRES_CODE,
                                Constants.BLANK_DATA_GIVEN_CODE,
                                messageSource.getMessage(Constants.USER_SUSPENDED, null, locale)
                        );
                    } else {
                        UserOTP otp = userOTPRepository.findByUserIdAndIsFrom(users.getUserId());
                        if (otp != null) {
                            //check for expiry
                            if (LocalDateTime.now().isAfter(otp.getExpiredAt())) {
                                users.setAttemptCounter((short) (users.getAttemptCounter() + 1));
                                responseData = new Response(
                                        Constants.OTP_EXPIRES_CODE,
                                        Constants.BLANK_DATA_GIVEN_CODE,
                                        messageSource.getMessage(OTP_EXPIRES, null, locale) + ", " + languageService.gettingMessages(ATTEMPT_REACH, 10 - users.getAttemptCounter())
                                );
                            }
                            //check otp matching
                            else if (!utility.md5Hash(request.getOtp()).equals(otp.getOtp())) {
                                users.setAttemptCounter((short) (users.getAttemptCounter() + 1));
                                responseData = new Response(
                                        Constants.OTP_EXPIRES_CODE,
                                        Constants.BLANK_DATA_GIVEN_CODE,
                                        messageSource.getMessage(OTP_NOT_MATCHED, null, locale) + ", " + languageService.gettingMessages(ATTEMPT_REACH, 10 - users.getAttemptCounter())
                                );
                            } else {
                                //save Active state in otp table
                                otp.setStatus(STATUS_ACTIVE);
                                userOTPRepository.save(otp);

                                //reset Attempt counter
                                users.setAttemptCounter((short) 0);

                                String token = authConfig.GenerateToken(users.getContactNumber());
                                VerifyOtpResponse response = saveResponse(users, token);

                                responseData = new Response(
                                        Constants.SUCCESS_CODE,
                                        messageSource.getMessage(Constants.OTP_VERIFIED_SUCCESSFULLY, null, locale),
                                        Constants.SUCCESS_CODE,
                                        response
                                );
                            }
                        } else {
                            responseData = new Response(
                                    Constants.NO_RECORD_FOUND_CODE,
                                    Constants.BLANK_DATA_GIVEN_CODE,
                                    messageSource.getMessage(Constants.INVALID_OTP, null, locale)
                            );
                        }
                    }
                } else responseData = new Response(
                        Constants.USER_NOT_FOUND_CODE,
                        Constants.BLANK_DATA_GIVEN_CODE,
                        messageSource.getMessage(Constants.USER_NOT_FOUND, null, locale)
                );
            }
        }
        catch (Exception e){
            e.printStackTrace();
            responseData = new Response(
                    Constants.BLANK_DATA_GIVEN_CODE,
                    Constants.BLANK_DATA_GIVEN_CODE,
                    messageSource.getMessage(Constants.EXCEPTION, null, locale), e.getMessage()
            );
        }
        return responseData;
    }

    private VerifyOtpResponse saveResponse(Users users, String token) {
        VerifyOtpResponse response = new VerifyOtpResponse();
        response.setIs_registered(YesNo.Yes.toString());
        response.setUser_id(String.valueOf(users.getUserId()));
        response.setAuth_key(token);
        response.setUser_type(Constants.PATIENT);
        response.setUser_photo(users.getProfilePicture() == null || users.getProfilePicture().isEmpty() ? null : Constants.FILE_PATH + Constants.USER_PROFILE_PATH + users.getUserId() + "/" + users.getProfilePicture());

        String fName = users.getFirstName() == null || users.getFirstName().isEmpty() ? null : users.getFirstName();
        String lName = users.getLastName() == null || users.getLastName().isEmpty() ? null : users.getLastName();

        response.setFirst_name(fName);
        response.setLast_name(lName);
        response.setFullName(fName + (lName == null ? "" : " " + lName));
        response.setDob(users.getDob() == null ? null : users.getDob().toString());
        response.setResidence_address(users.getResidenceAddress() == null || !users.getResidenceAddress().isEmpty() ? null : users.getResidenceAddress());
        response.setEmail(users.getEmail() == null || users.getEmail().isEmpty() ? null : users.getEmail());
        response.setContact_number(users.getContactNumber());
        response.setSignling_server(SIGNALING_SERVER);
        response.setVerify_token(VERIFY_TOKEN);
        response.setTurn_username(TURN_USER_NAME);
        response.setTurn_server(TURN_SERVER);
        response.setTurn_password(TURN_PASSWORD);
        response.setSturn_server(S_TURN_SERVER);
        response.setData_bundle_offer(DATA_BUNDLE_OFFER);
        response.setData_bundle_offer_message(DATA_BUNDLE_OFFER_MESSAGE);
        response.setHas_app(users.getHasApp());
        response.setNew_registration_with_more_fields(NEW_REGISTRATION_WITH_MORE_FIELDS);

        return response;
    }
}