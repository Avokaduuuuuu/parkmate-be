package com.parkmate.admin.dto;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PartnerRegistrationRejectRequest {

    String rejectionReason;

}
