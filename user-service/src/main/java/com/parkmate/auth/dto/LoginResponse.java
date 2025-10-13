package com.parkmate.auth.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.parkmate.partner.dto.PartnerResponse;
import com.parkmate.user.dto.UserResponse;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class LoginResponse {

    AuthResponse authResponse;
    UserResponse userResponse;
    PartnerResponse partnerResponse;

}
