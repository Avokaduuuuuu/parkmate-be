package com.parkmate.operational_fee_config;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface OperationalFeeConfigRepository extends JpaRepository<OperationalFeeConfigEntity, Long>, JpaSpecificationExecutor<OperationalFeeConfigEntity> {
}
