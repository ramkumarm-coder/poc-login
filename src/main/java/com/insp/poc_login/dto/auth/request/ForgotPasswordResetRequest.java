package com.insp.poc_login.dto.auth.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ForgotPasswordResetRequest {
    private String email;
    private String password;
    private String confirmPassword;
}
