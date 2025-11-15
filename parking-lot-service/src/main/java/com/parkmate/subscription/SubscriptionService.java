package com.parkmate.subscription;

import com.parkmate.subscription.dto.req.SubscriptionCreateRequest;
import com.parkmate.subscription.dto.req.SubscriptionUpdateRequest;
import com.parkmate.subscription.dto.resp.SubscriptionResponse;
import org.springframework.data.domain.Page;

public interface SubscriptionService {
    Page<SubscriptionResponse> fetchAllSubscriptions(
            String userHeaderId,
            int page,
            int size,
            String sortBy,
            String sortOrder,
            SubscriptionFilterParams filterParams);

    SubscriptionResponse fetchSubscriptionById(Long id);

    SubscriptionResponse addSubscription(SubscriptionCreateRequest request);

    SubscriptionResponse updateSubscription(SubscriptionUpdateRequest request, Long id);

    SubscriptionResponse deleteSubscription(Long id);
}
