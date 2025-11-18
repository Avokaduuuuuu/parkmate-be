package com.parkmate.partner;

import com.parkmate.common.dto.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/internal/partners")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Partner Internal", description = "Internal APIs for partner operations")
public class PartnerInternalController {

    private final PartnerRepository partnerRepository;

    /**
     * Get partner ID by account ID
     * Used by other services to resolve partnerId from accountId in JWT token
     */
    @GetMapping("/account/{accountId}")
    public ApiResponse<Long> getPartnerIdByAccountId(@PathVariable Long accountId) {
        log.info("Getting partnerId for accountId={}", accountId);

        Long partnerId = partnerRepository.findPartnerIdByAccountId(accountId);

        if (partnerId == null) {
            log.warn("No partner found for accountId={}", accountId);
            return ApiResponse.success(null);
        }

        log.info("Found partnerId={} for accountId={}", partnerId, accountId);
        return ApiResponse.success(partnerId);
    }
}
