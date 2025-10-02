package com.parkmate.auth.dto;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class EmailVerificationResponse {

    private boolean isSuccess;

    private String message;

}
