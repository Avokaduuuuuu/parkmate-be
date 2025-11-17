package com.parkmate.policy;

import com.parkmate.exception.AppException;
import com.parkmate.exception.ErrorCode;
import com.parkmate.policy.dto.req.PolicyUpdateRequest;
import com.parkmate.policy.dto.resp.PolicyResponse;
import com.parkmate.session.enums.SyncStatus;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class PolicyServiceImpl implements PolicyService {

    private final PolicyRepository policyRepository;

    @Override
    public List<PolicyResponse> pullPolicies(Long lotId) {
        return policyRepository.findAllByParkingLotIdAndSyncStatus(lotId, SyncStatus.PENDING)
                .stream()
                .map(PolicyMapper.INSTANCE::toResponse)
                .toList();
    }

    @Override
    public Integer syncPolicies(List<Long> policyIds) {
        return policyRepository.syncPolicies(policyIds);
    }

    @Override
    public PolicyResponse updatePolicy(Long id, PolicyUpdateRequest policyUpdateRequest) {
        PolicyEntity policyEntity = policyRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.POLICY_NOT_ENOUGH, "Policy with id " + id + " not found"));

        policyEntity.setValue(policyUpdateRequest.value());
        policyEntity.setSyncStatus(SyncStatus.PENDING);
        return PolicyMapper.INSTANCE.toResponse(policyRepository.save(policyEntity));
    }
}
