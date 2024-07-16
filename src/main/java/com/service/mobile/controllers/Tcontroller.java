package com.service.mobile.controllers;

import com.service.mobile.config.UserService;
import com.service.mobile.config.Utility;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/test/check")
public class Tcontroller {
    @Autowired
    UserService userService;

    @Autowired
    Utility utility;

    @GetMapping("/login-user")
    public Object checkValidation(){
        return utility.getLoginUser();
    }
}
