package com.parkmate.mobileDevice;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface MobileDeviceRepository extends JpaRepository<MobileDevice, Long>, QuerydslPredicateExecutor<MobileDevice> {

    @Query("SELECT md.pushToken FROM MobileDevice md WHERE md.user.id = :userId AND md.isActive = true AND md.pushToken IS NOT NULL")
    List<String> findActivePushTokensByUserId(@Param("userId") Long userId);

    Optional<MobileDevice> findByUser_IdAndDeviceId(Long userId, String deviceId);

    Optional<MobileDevice> findByPushToken(String pushToken);

    Optional<MobileDevice> findByDeviceId(String deviceId);
}
