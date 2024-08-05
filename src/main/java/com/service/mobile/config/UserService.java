package com.service.mobile.config;

import com.service.mobile.dto.*;
import com.service.mobile.dto.enums.UserType;
import com.service.mobile.dto.request.JwtRequest;
import com.service.mobile.dto.response.*;
import com.service.mobile.model.UserOTP;
import com.service.mobile.repository.OtpRepository;
import com.service.mobile.repository.UsersRepository;
import com.service.mobile.model.Users;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Random;

@Service
@Log4j2
public class UserService {
    @Value("${app.otp.expiry.minutes}")
    public Integer OTP_VALIDITY_TIME;


    @Autowired
    private OtpRepository otpRepository;

    @Autowired
    UsersRepository repository;

    @Autowired
    AuthConfig authConfig;

    @Autowired
    UsersDetailsService usersDetailsService;

    /*public Response createUserOld(UserDto request){
        Response response = new Response();
        try {
            Users users = new Users(request);
            Roles roles = rolesRepository.findByName(request.getRoleName()).orElse(null);
            if(roles!=null){
                users.setRoles(roles);
                users.setPassword(authConfig.passwordEncoder().encode(request.getPassword()));
                users = repository.save(users);
                response.setData(users);
            }else{
                throw new Exception("Role not found");
            }
        }catch (Exception e){
            log.error(e.getMessage());
            response = new Response(e);
        }
        return  response;
    } */
    
    public Response createUser(UserDto userRegister){
        Response responseDto = new Response();
        if(!userRegister.isTermsAndCondition()) {
            responseDto.setMessage(Constants.PLEASE_TICK_THE_CHECKBOX);
            responseDto.setCode(Constants.PLEASE_TICK_THE_CHECKBOX_CODE);
            responseDto.setStatus(Constants.FAIL);
        }
        else {
            Users existUser = repository.findByContactNumber(userRegister.getContactNumber()).orElse(null);
            if (existUser != null) {
                responseDto.setMessage(Constants.USER_ALREADY_EXISTS);
                responseDto.setCode(Constants.USER_ALREADY_EXISTS_CODE);
                responseDto.setStatus(Constants.FAIL);
            }
            else {
                Users user = new Users();
                user.setFirstName(userRegister.getFullName());
                user.setLastName("");
                user.setType(UserType.Patient);
                user.setContactNumber(userRegister.getContactNumber());
                repository.save(user);  //save the user then generate the otp

                responseDto = generateOtp(userRegister.getContactNumber(), Constants.Registration);
            }
        }
        return responseDto;
    }

    public Response generateOtp(String contactNumber, String apiGatewayEnum) {
        Response responseDto = new Response();
        LoginResponse loginResponse = new LoginResponse();
        RegistrationResponse registrationResponse= new RegistrationResponse();

        Users user=repository.findByContactNumber(contactNumber).orElse(null);
        if(user == null) {
            loginResponse.setIsRegistered(Constants.FALSE);
            // responseDto.setData(loginResponse);
            responseDto.setStatus(Constants.FAIL);
            responseDto.setCode(Constants.USER_NOT_FOUND_CODE);
            responseDto.setMessage(Constants.USER_NOT_FOUND);
        }

        //if user is not exists, for login credentials

        else {
            //user exists
            int otp  = saveOtp(user, apiGatewayEnum);
            if(apiGatewayEnum == Constants.Registration) {
                registrationResponse.setIsRegistered(Constants.FALSE);
                registrationResponse.setMobileNumber(contactNumber);
                registrationResponse.setFullName(user.getFirstName());
                responseDto.setData(registrationResponse);
            }
            else {
                loginResponse.setIsRegistered(Constants.TRUE);
                loginResponse.setContactNumber(user.getContactNumber());
                loginResponse.setFullName(user.getFirstName()+" "+user.getLastName());
                responseDto.setData(loginResponse);
            }
            responseDto.setMessage(Constants.OTP_SEND_SUCCESSFUL);
        }
        return responseDto;
    }

    public Response generateOtp(UserDto request) {
        Response responseDto = new Response();
        OtpResponse response = new OtpResponse();
        Users user=repository.findByContactNumber(request.getContactNumber()).orElse(null);

        if(user == null) {
            responseDto.setStatus(Constants.FAIL);
            responseDto.setCode(Constants.USER_NOT_FOUND_CODE);
            responseDto.setMessage(Constants.USER_NOT_FOUND);
        }
        else {
            int otp  = saveOtp(user, Constants.Login); //NOTE : resend is from register or login
            response.setOtp(String.valueOf(otp));
            responseDto.setMessage(Constants.OTP_SENT_MESSAGE);
            responseDto.setData(response);
        }
        return responseDto;
    }

    public int saveOtp(Users user, String apiGatewayEnum) {

        UserOTP userOTP = new UserOTP();
        userOTP.setUserId(user.getUserId());

        //generate otp of 6 digit in integer
        Random random = new Random();
        int randomNumber = 123456; //random.nextInt(900000) + 100000; //this will use for integration
        //NOTE: sms integration
        System.out.println(randomNumber);
        //sendMessageToUsers(String.valueOf(randomNumber));


        userOTP.setOtp(authConfig.passwordEncoder().encode(String.valueOf(randomNumber)));
        userOTP.setExpiredAt(LocalDateTime.now().plusMinutes(OTP_VALIDITY_TIME));
        userOTP.setStatus("1");
        userOTP.setIsFrom(apiGatewayEnum);
        userOTP.setType(Constants.PATIENT); //we can change for other parameters

        otpRepository.save(userOTP); //save the generated otp
        //also save otp to user
        user.setOtp(randomNumber);
        user.setOtpTime(new Timestamp(System.currentTimeMillis()));
        repository.save(user);
        return randomNumber;
    }
    
    public String generateToken(String username) {
        Users users = repository.findByContactNumber(username).orElse(null);
        if(users!=null){
            return authConfig.GenerateToken(username);
        }else{
            throw new UsernameNotFoundException("User not found");
        }
    }

    public void validateToken(String token) throws Exception {
        if(!authConfig.validateToken(token)){
            throw new Exception("Invalid token");
        }
    }

    public Response verifyOtp(JwtRequest request) {
        Response responseDto = new Response();
        if(request.getContactNumber().isEmpty() || request.getOtp().isEmpty() || request.getIsRegistered().isEmpty()) {
            responseDto.setStatus(Constants.FAIL);
            responseDto.setCode(Constants.FIELD_MISSING_CODE);
            responseDto.setMessage(Constants.FIELD_MISSING);
        } else {
            Users pUser = repository.findByContactNumber(request.getContactNumber()).orElse(null);

            if (pUser == null) {
                responseDto.setStatus(Constants.FAIL);
                responseDto.setCode(Constants.USER_NOT_FOUND_CODE);
                responseDto.setMessage(Constants.USER_NOT_FOUND);
            }else{
                UserOTP userOTP = otpRepository.findFirstByUserIdOrderByIdDesc(pUser.getUserId());
                //check otp time is expire or not
                if (!LocalDateTime.now().isBefore(userOTP.getExpiredAt())) {
                    responseDto.setStatus(Constants.FAIL);
                    responseDto.setCode(Constants.OTP_EXPIRES_CODE);
                    responseDto.setMessage(Constants.OTP_EXPIRES);
                } else {
                    // if register, then verify the otp
                    if (request.getIsRegistered().equalsIgnoreCase(Constants.FALSE) && authConfig.passwordEncoder().matches(request.getOtp(), userOTP.getOtp())) {

                        JwtRequest responseRegistration = new JwtRequest();
                        responseRegistration.setIsRegistered(Constants.FALSE); //save
                        responseRegistration.setUserId(userOTP.getUserId());

                        responseRegistration.setHasApp(Constants.TRUE); //NOTE : need to ask
                        responseRegistration.setDataBundleOffer(Constants.TRUE); //NOTE : need to ask

                        responseDto.setMessage(Constants.USER_REGISTERED);
//                    responseDto.setResponseStatus(APIGatewayEnum.ResponseStatus.SUCCESS);
//                    responseDto.setStatusCode(SUCCESS);
                        responseDto.setData(responseRegistration);
                    }else if (authConfig.passwordEncoder().matches(request.getOtp(), userOTP.getOtp()) && request.getIsRegistered().equalsIgnoreCase(Constants.TRUE)) {
                        //if login then generate token , first verify the otp

                        String token = generateToken(request.getContactNumber());
                        JwtResponse jwtResponse = JwtResponse.builder()
                                .isRegistered(Constants.TRUE)
                                .userId(pUser.getUserId())
                                .authKey(token)
                                .userType(Constants.PATIENT)
                                .userPhoto(null)
                                .firstName(pUser.getFirstName())
                                .lastName(pUser.getLastName())
                                .fullName(pUser.getFirstName() + " " + pUser.getLastName())
                                .email(pUser.getEmail())
                                .contactNumber(pUser.getContactNumber())
                                .build();

                        responseDto.setMessage(Constants.USER_LOGGED_IN);
//                    responseDto.setResponseStatus(APIGatewayEnum.ResponseStatus.SUCCESS);
//                    responseDto.setStatusCode(SUCCESS);
                        responseDto.setData(jwtResponse);
                    } else {
                        responseDto.setStatus(Constants.FAIL);
                        responseDto.setCode(Constants.INVALID_OTP_CODE);
                        responseDto.setMessage(Constants.INVALID_OTP);
                    }
                }
            }
        }
        return responseDto;
    }
}
