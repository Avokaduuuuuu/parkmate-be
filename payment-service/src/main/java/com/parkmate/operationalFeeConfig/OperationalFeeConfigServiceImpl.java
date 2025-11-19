package com.parkmate.operationalFeeConfig;

import com.parkmate.exception.AppException;
import com.parkmate.exception.ErrorCode;
import com.parkmate.operationalFeeConfig.dto.req.OperationalFeeConfigCreateRequest;
import com.parkmate.operationalFeeConfig.dto.req.OperationalFeeConfigUpdateRequest;
import com.parkmate.operationalFeeConfig.dto.resp.OperationalFeeConfigResponse;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Transactional
public class OperationalFeeConfigServiceImpl implements OperationalFeeConfigService{
    private final OperationalFeeConfigRepository operationalFeeConfigRepository;

    @Override
    public OperationalFeeConfigResponse findById(Long id) {
        return OperationalFeeConfigMapper.INSTANCE.toResponse(
                operationalFeeConfigRepository.findById(id)
                        .orElseThrow(() -> new AppException(ErrorCode.OPERATIONAL_FEE_CONFIG_NOT_FOUND, "Operational fee config with id " + id + " not found"))
        );
    }

    @Override
    public Page<OperationalFeeConfigResponse> findAll(int page, int size, String sortBy, String sortOrder, OperationalFeeConfigFilterParams filterParams) {
        Sort sort = Sort.by(Sort.Direction.fromString(sortOrder), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<OperationalFeeConfigEntity> operationalFeeConfigEntities = operationalFeeConfigRepository.findAll(filterParams.getSpecification(),pageable);
        return operationalFeeConfigEntities.map(OperationalFeeConfigMapper.INSTANCE::toResponse);
    }

    @Override
    public OperationalFeeConfigResponse addOperationalFeeConfig(OperationalFeeConfigCreateRequest request) {
        OperationalFeeConfigEntity operationalFeeConfigEntity = OperationalFeeConfigEntity.builder()
                .pricePerSqm(request.pricePerSqm())
                .billingPeriodMonths(request.billingPeriodMonths())
                .description(request.description())
                .validFrom(request.validFrom())
                .validUntil(request.validUntil())
                .isActive(false)
                .build();
        return OperationalFeeConfigMapper.INSTANCE.toResponse(operationalFeeConfigRepository.save(operationalFeeConfigEntity));
    }

    @Override
    public OperationalFeeConfigResponse updateOperationalFeeConfig(Long id, OperationalFeeConfigUpdateRequest request) {
        OperationalFeeConfigEntity operationalFeeConfigEntity = operationalFeeConfigRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.OPERATIONAL_FEE_CONFIG_NOT_FOUND, id + " not found"));

        if (request.pricePerSqm() != null) operationalFeeConfigEntity.setPricePerSqm(request.pricePerSqm());
        if (request.billingPeriodMonths() != null) operationalFeeConfigEntity.setBillingPeriodMonths(request.billingPeriodMonths());
        if (request.description() != null) operationalFeeConfigEntity.setDescription(request.description());
        if (request.validFrom() != null) operationalFeeConfigEntity.setValidFrom(request.validFrom());
        if (request.validUntil() != null) operationalFeeConfigEntity.setValidUntil(request.validUntil());
        return null;
    }

    @Override
    public OperationalFeeConfigResponse deleteOperationalFeeConfig(Long id) {
        OperationalFeeConfigEntity operationalFeeConfigEntity = operationalFeeConfigRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.OPERATIONAL_FEE_CONFIG_NOT_FOUND, id + " not found"));
        operationalFeeConfigEntity.setIsActive(false);
        return OperationalFeeConfigMapper.INSTANCE.toResponse(operationalFeeConfigEntity);
    }


}
