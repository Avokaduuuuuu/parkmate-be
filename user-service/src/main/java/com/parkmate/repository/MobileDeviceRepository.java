package com.parkmate.repository;

import com.parkmate.entity.MobileDevice;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface MobileDeviceRepository extends JpaRepository<MobileDevice, UUID> {
}
