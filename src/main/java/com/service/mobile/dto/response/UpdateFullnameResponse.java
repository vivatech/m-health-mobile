package com.service.mobile.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class UpdateFullnameResponse {
    private String user_id;
    private String auth_key;
    private String user_type;
    private String user_photo;
    private String fullName;
    private String first_name;
    private String last_name;
    private String email;
    private String contact_number;
    private String signling_server;
    private String verify_token;
    private String turn_username;
    private String turn_password;

    public UpdateFullnameResponse(String userId, String authKey, String userType, String userPhoto, String fullName, String firstName, String lastName, String email, String contactNumber, String signalingServer, String verifyToken, String turnUsername, String turnPassword) {
        this.user_id = userId;
        this.auth_key = authKey;
        this.user_type = userType;
        this.user_photo = userPhoto;
        this.fullName = fullName;
        this.first_name = firstName;
        this.last_name = lastName;
        this.email = email;
        this.contact_number = contactNumber;
        this.signling_server = signalingServer;
        this.verify_token = verifyToken;
        this.turn_username = turnUsername;
        this.turn_password = turnPassword;
    }
}
