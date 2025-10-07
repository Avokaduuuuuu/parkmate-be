package com.parkmate.pricing_rule;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface PricingRuleRepository extends JpaRepository<PricingRuleEntity, Long> {
}
