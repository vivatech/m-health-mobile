package com.service.mobile.service;

import com.service.mobile.dto.request.UpdatePictureRequest;
import com.service.mobile.repository.UsersRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public class UsersService {

    @Autowired
    private UsersRepository usersRepository;

    @Value("${app.base.url}")
    private String baseUrl;

    public ResponseEntity<?> updateProfilePicture(UpdatePictureRequest request) {
//        Users users = usersRepository.findById(request.getUser_id()).orElse(null);
//        if(users!=null){
//            users.setSce
//
//            return ResponseEntity.status(HttpStatus.OK).body(new Response(
//                    Constants.SUCCESS_CODE,
//                    Constants.SUCCESS_CODE,
//                    messageSource.getMessage(Constants.SUCCESS_MESSAGE,null,locale),
//                    profile
//            ));
//
//        }else{
//            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new Response(
//                    Constants.UNAUTHORIZED_CODE,
//                    Constants.UNAUTHORIZED_CODE,
//                    messageSource.getMessage(Constants.UNAUTHORIZED_MSG,null,locale)
//            ));
//        }
        return null;
    }
}
