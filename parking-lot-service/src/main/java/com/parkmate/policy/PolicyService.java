package com.parkmate.policy;

import com.parkmate.policy.dto.req.PolicyUpdateRequest;
import com.parkmate.policy.dto.resp.PolicyResponse;

import java.util.List;

public interface PolicyService {
    List<PolicyResponse> pullPolicies(Long lotId);
    Integer syncPolicies(List<Long> policyIds);
    PolicyResponse updatePolicy(Long id, PolicyUpdateRequest policyUpdateRequest);
}
