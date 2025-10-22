package com.parkmate.pricing_rule.dto.req;

import com.parkmate.session.enums.SyncStatus;

import java.util.List;

public record SyncedPricingRulesUpdateRequest(
        List<Long> ruleIds,
        SyncStatus status
) {
}
