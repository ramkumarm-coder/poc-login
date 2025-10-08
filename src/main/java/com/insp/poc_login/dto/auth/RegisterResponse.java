package com.insp.poc_login.dto.auth;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class RegisterResponse {
    private String email;
    private String status;
    private String message;

}
