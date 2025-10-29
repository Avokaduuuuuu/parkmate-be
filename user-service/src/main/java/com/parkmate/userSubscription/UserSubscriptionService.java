package com.parkmate.userSubscription;

import com.parkmate.userSubscription.dto.CreateUserSubscriptionRequest;
import com.parkmate.userSubscription.dto.UpdateUserSubscriptionRequest;
import com.parkmate.userSubscription.dto.UserSubscriptionResponse;
import com.parkmate.userSubscription.dto.UserSubscriptionSearchCriteria;
import org.springframework.data.domain.Page;

public interface UserSubscriptionService {

    UserSubscriptionResponse create(CreateUserSubscriptionRequest request, String accountIdHeader);

    UserSubscriptionResponse findById(Long id);

    Page<UserSubscriptionResponse> findAll(int page, int size, String sortBy, String sortOrder, String accountIdHeader, UserSubscriptionSearchCriteria searchCriteria);

    UserSubscriptionResponse update(Long id, UpdateUserSubscriptionRequest request);

    void delete(Long id);

}
