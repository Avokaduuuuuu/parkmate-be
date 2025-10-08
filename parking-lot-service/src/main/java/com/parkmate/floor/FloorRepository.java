package com.parkmate.floor;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface FloorRepository extends JpaRepository<FloorEntity, Long>, JpaSpecificationExecutor<FloorEntity> {
    Long countAllBy();
}
