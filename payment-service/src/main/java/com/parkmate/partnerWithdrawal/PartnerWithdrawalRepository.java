package com.parkmate.partnerWithdrawal;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.UUID;

public interface PartnerWithdrawalRepository extends JpaRepository<PartnerWithdrawal, UUID>, JpaSpecificationExecutor<PartnerWithdrawal> {
}
