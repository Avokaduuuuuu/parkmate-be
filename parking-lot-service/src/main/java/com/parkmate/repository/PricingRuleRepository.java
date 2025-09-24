package com.parkmate.repository;

import com.parkmate.entity.PricingRuleEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface PricingRuleRepository extends JpaRepository<PricingRuleEntity, UUID> {
}
