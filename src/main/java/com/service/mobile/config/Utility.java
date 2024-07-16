package com.service.mobile.config;

import com.service.mobile.model.Users;
import com.service.mobile.repository.UsersRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Service
public class Utility {

    @Autowired
    UsersRepository usersRepository;

    public Users getLoginUser() {
        try{
            UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication()
                    .getPrincipal();
            String contactNumber =  userDetails.getUsername();
            return usersRepository.findByContactNumber(contactNumber).orElse(null);
        }catch (Exception e){
            return null;
        }
    }
}
