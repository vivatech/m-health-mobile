package com.service.mobile.service;

import com.service.mobile.config.Constants;
import com.service.mobile.dto.enums.UserType;
import com.service.mobile.dto.response.HospitalListResponse;
import com.service.mobile.dto.response.Response;
import com.service.mobile.model.Users;
import com.service.mobile.repository.UsersRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Service
public class HospitalService {

    @Autowired
    private UsersRepository usersRepository;

    @Autowired
    private MessageSource messageSource;

    public ResponseEntity<?> getHospitalList(Locale locale) {
        List<Users> users = usersRepository.findByStatusAndTypeAndVerified("A", UserType.Hospital,"Yes");
        if(users.size()>0){

            List<HospitalListResponse> responses = new ArrayList<>();
            for(Users u:users){
                responses.add(new HospitalListResponse(u.getHospitalId()
                        ,u.getProfilePicture()
                        ,u.getClinicName()
                        ,u.getHospitalAddress()));
            }
            return ResponseEntity.status(HttpStatus.OK).body(new Response(
                    Constants.SUCCESS_CODE,
                    Constants.SUCCESS_CODE,
                    messageSource.getMessage(Constants.HOSPITAL_LIST_RETRIEVED,null,locale),
                    responses
            ));
        }else {
            return ResponseEntity.status(HttpStatus.OK).body(new Response(
                    Constants.SUCCESS_CODE,
                    Constants.SUCCESS_CODE,
                    messageSource.getMessage(Constants.NO_HOSPITAL_LIST_RETRIEVED,null,locale)
            ));
        }
    }
}
