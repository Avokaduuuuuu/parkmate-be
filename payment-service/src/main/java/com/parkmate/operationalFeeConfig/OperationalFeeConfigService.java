package com.parkmate.operationalFeeConfig;

import com.parkmate.operationalFeeConfig.dto.req.OperationalFeeConfigCreateRequest;
import com.parkmate.operationalFeeConfig.dto.req.OperationalFeeConfigUpdateRequest;
import com.parkmate.operationalFeeConfig.dto.resp.OperationalFeeConfigResponse;
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
