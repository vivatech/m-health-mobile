package com.service.mobile.config;

import com.service.mobile.repository.UsersRepository;
import com.service.mobile.dto.CustomUserDetails;
import com.service.mobile.model.Users;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class UsersDetailsService implements UserDetailsService {

    @Autowired
    UsersRepository repository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return loadUserByEmail(username);
    }

    public UserDetails loadUserByEmail(String username) {
        Users users =  repository.findByContactNumber(username).orElse(null);
        if(users==null){
            throw new UsernameNotFoundException("could not found user..!!");
        }else{
            return new CustomUserDetails(users);
        }
    }
}
