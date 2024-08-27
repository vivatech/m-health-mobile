package com.service.mobile.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class JwtResponse {
    private String isRegistered; //boolean type 'YES' or 'NO'
    private int userId;
    private String authKey;
    private String userType; //either doctor or patient
    private String userPhoto; //null
    private String firstName;
    private String lastName;
    private String fullName;
    private String email;
    private String contactNumber;
    private String signLingServer;
    private String verifyToken;
    private String turnUsername;
    private String turnPassword;
    private String turnServer;
    private String dataBundleOffer;
    private String hasApp;
    private String dataBundleOfferMessage;
}
