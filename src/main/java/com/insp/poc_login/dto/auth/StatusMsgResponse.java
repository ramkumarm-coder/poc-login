package com.insp.poc_login.dto.auth;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class StatusMsgResponse {
    private String status;
    private String message;
}
