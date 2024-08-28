package com.service.mobile.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VerifyOtpResponse {
    private String is_registered;
    private String user_id;
    private String auth_key;
    private String user_type;
    private String user_photo;
    private String first_name;
    private String last_name;
    private String fullName;
    private String dob;
    private String residence_address;
    private String email;
    private String contact_number;
    private String signling_server;
    private String verify_token;
    private String turn_username;
    private String turn_password;
    private String turn_server;
    private String sturn_server;
    private String data_bundle_offer;
    private String has_app;
    private String data_bundle_offer_message;
    private boolean new_registration_with_more_fields;

}
