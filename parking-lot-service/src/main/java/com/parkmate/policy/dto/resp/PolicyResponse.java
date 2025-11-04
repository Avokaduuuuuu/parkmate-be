package com.parkmate.policy.dto.resp;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.parkmate.policy.enums.PolicyType;
import com.parkmate.session.enums.SyncStatus;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PolicyResponse {
    Long id;
    Long lotId;
    PolicyType policyType;
    Integer value;
    Double rate;
    Boolean isActive;
    SyncStatus syncStatus;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    LocalDateTime syncedAt;
}
