package com.parkmate.subscription;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Map;

public interface SubscriptionRepository extends JpaRepository<SubscriptionEntity, Long>, JpaSpecificationExecutor<SubscriptionEntity> {

}
