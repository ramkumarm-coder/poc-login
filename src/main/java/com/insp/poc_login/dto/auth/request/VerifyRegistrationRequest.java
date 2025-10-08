package com.insp.poc_login.dto.auth.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class VerifyRegistrationRequest {
    private String email;
    private String otp;
}
