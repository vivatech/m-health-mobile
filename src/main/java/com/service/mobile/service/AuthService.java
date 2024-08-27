package com.service.mobile.service;

import com.service.mobile.config.AuthConfig;
import com.service.mobile.config.Constants;
import com.service.mobile.dto.enums.YesNo;
import com.service.mobile.dto.request.MobileReleaseRequest;
import com.service.mobile.dto.request.VerifyOtpRequest;
import com.service.mobile.dto.response.JwtResponse;
import com.service.mobile.dto.response.Response;
import com.service.mobile.model.UserOTP;
import com.service.mobile.model.Users;
import com.service.mobile.repository.UserOTPRepository;
import com.service.mobile.repository.UsersRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Locale;

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

    public ResponseEntity<?> actionLogin(MobileReleaseRequest request, Locale locale) {
        if(request.getContact_number() == null || request.getContact_number().isEmpty()){
            return ResponseEntity.status(HttpStatus.NO_CONTENT).body(new Response(
                    Constants.NO_RECORD_FOUND_CODE,
                    Constants.BLANK_DATA_GIVEN_CODE,
                    messageSource.getMessage(Constants.BLANK_DATA_GIVEN,null,locale)));
        }
        else{
            Users users = usersRepository.findByContactNumber(request.getContact_number()).orElse(null);
            if(users != null){
                int otp = 123456; //TODO : SMS integration

                UserOTP otps = new UserOTP();
                otps.setOtp(encoder.encode(String.valueOf(otp)));
                otps.setIsFrom(Constants.Login);
                otps.setUserId(users.getUserId());
                otps.setExpiredAt(LocalDateTime.now().plusMinutes(expiryTime));
                otps.setType(Constants.Mobile);

                userOTPRepository.save(otps);

                Response res = new Response();

                res = new Response(
                        Constants.SUCCESS_CODE,
                        Constants.SUCCESS_CODE,
                        messageSource.getMessage(Constants.USER_LOGIN_IS_SUCCESS,null,locale)
                );
                return ResponseEntity.ok(res);
            }
            else return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new Response(
                        Constants.NO_RECORD_FOUND_CODE,
                        Constants.NO_RECORD_FOUND_CODE,
                        messageSource.getMessage(Constants.MOBILE_USER_NOT_FOUND,null,locale)));
        }
    }

    public ResponseEntity<?> actionVerifyOtp(VerifyOtpRequest request, Locale locale) {
        if(request.getContact_number() == null || request.getContact_number().isEmpty()
        || request.getOtp() == null || request.getOtp().isEmpty()
        || request.getIs_registered() == null || request.getIs_registered().isEmpty()
        || request.getDevice_token() == null || request.getDevice_token().isEmpty()){
            return ResponseEntity.status(HttpStatus.NO_CONTENT).body(new Response(
                    Constants.NO_RECORD_FOUND_CODE,
                    Constants.BLANK_DATA_GIVEN_CODE,
                    messageSource.getMessage(Constants.BLANK_DATA_GIVEN,null,locale)));
        }
        else{
            Users users = usersRepository.findByContactNumber(request.getContact_number()).orElse(null);
            if(users != null){
                UserOTP otp = userOTPRepository.findByUserIdAndIsFrom(users.getUserId());
                if(otp != null){
                    if(LocalDateTime.now().isBefore(otp.getExpiredAt())){
                        if(encoder.matches(request.getOtp(), otp.getOtp())){
                            String token = authConfig.GenerateToken(users.getContactNumber());
                            JwtResponse response = new JwtResponse();
                            response.setAuthKey(token);
                            response.setIsRegistered(YesNo.Yes.toString());
                            response.setUserId(users.getUserId());
                            response.setUserType(Constants.PATIENT);
                            response.setUserPhoto(users.getProfilePicture() == null || users.getProfilePicture().isEmpty() ? null : Constants.USER_PROFILE_PATH + users.getUserId() +"/"+ users.getProfilePicture());

                            String firstName = users.getFirstName() == null || users.getFirstName().isEmpty() ? " " : users.getFirstName().trim();
                            response.setFirstName(firstName);
                            String lastName = users.getLastName() == null || users.getLastName().isEmpty() ? " " : users.getLastName().trim();
                            response.setLastName(lastName);
                            response.setFullName(firstName + " " + lastName);
                            response.setContactNumber(users.getContactNumber());
                            response.setEmail(users.getEmail() == null || users.getEmail().isEmpty() ? null : users.getEmail());

                            Response responses = new Response(
                                    Constants.SUCCESS_CODE,
                                    messageSource.getMessage(Constants.OTP_VERIFIED_SUCCESSFULLY,null,locale),
                                    Constants.SUCCESS_CODE,
                                    response
                            );
                            return ResponseEntity.ok(responses);

                        }else return ResponseEntity.status(HttpStatus.NO_CONTENT).body(new Response(
                                Constants.NO_RECORD_FOUND_CODE,
                                Constants.BLANK_DATA_GIVEN_CODE,
                                messageSource.getMessage(Constants.INVALID_OTP,null,locale)
                        ));
                    }
                    else return ResponseEntity.status(HttpStatus.NO_CONTENT).body(new Response(
                            Constants.OTP_EXPIRES_CODE,
                            Constants.BLANK_DATA_GIVEN_CODE,
                            messageSource.getMessage(Constants.OTP_EXPIRES,null,locale)
                    ));
                }
                else return ResponseEntity.status(HttpStatus.NO_CONTENT).body(new Response(
                        Constants.NO_RECORD_FOUND_CODE,
                        Constants.BLANK_DATA_GIVEN_CODE,
                        messageSource.getMessage(Constants.NO_RECORD_FOUND,null,locale)
                ));
            }
            return ResponseEntity.status(HttpStatus.NO_CONTENT).body(new Response(
                    Constants.USER_NOT_FOUND_CODE,
                    Constants.BLANK_DATA_GIVEN_CODE,
                    messageSource.getMessage(Constants.USER_NOT_FOUND,null,locale)
            ));
        }
    }
}
