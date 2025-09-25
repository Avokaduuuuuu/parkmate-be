package com.parkmate.mobileDevice;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;

import java.util.UUID;

public interface MobileDeviceRepository extends JpaRepository<MobileDevice, UUID>, QuerydslPredicateExecutor<MobileDevice> {
}
