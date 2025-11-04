package com.parkmate.policy.dto.req;

import java.util.List;

public record SyncedPolicyUpdateRequest(
        List<Long> policyIds
        ) {
}
