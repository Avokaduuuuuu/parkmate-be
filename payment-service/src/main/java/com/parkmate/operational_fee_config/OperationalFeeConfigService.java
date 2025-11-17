package com.parkmate.operational_fee_config;

import com.parkmate.operational_fee_config.dto.req.OperationalFeeConfigCreateRequest;
import com.parkmate.operational_fee_config.dto.req.OperationalFeeConfigUpdateRequest;
import com.parkmate.operational_fee_config.dto.resp.OperationalFeeConfigResponse;
import org.springframework.data.domain.Page;

public interface OperationalFeeConfigService {
    OperationalFeeConfigResponse findById(Long id);
    Page<OperationalFeeConfigResponse> findAll(
            int page,
            int size,
            String sortBy,
            String sortOrder,
            OperationalFeeConfigFilterParams filterParams
    );
    OperationalFeeConfigResponse addOperationalFeeConfig(OperationalFeeConfigCreateRequest request);
    OperationalFeeConfigResponse updateOperationalFeeConfig(Long id,OperationalFeeConfigUpdateRequest request);
    OperationalFeeConfigResponse deleteOperationalFeeConfig(Long id);
}
