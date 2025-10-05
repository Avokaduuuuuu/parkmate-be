package com.parkmate.mobileDevice;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;

public interface MobileDeviceRepository extends JpaRepository<MobileDevice, Long>, QuerydslPredicateExecutor<MobileDevice> {
}
