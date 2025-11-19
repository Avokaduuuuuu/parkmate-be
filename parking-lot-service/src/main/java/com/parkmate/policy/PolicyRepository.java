package com.parkmate.policy;

import com.parkmate.session.enums.SyncStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PolicyRepository extends JpaRepository<PolicyEntity, Long> {

    List<PolicyEntity> findAllByParkingLotIdAndSyncStatus(Long lotId, SyncStatus syncStatus);

    @Modifying
    @Query("UPDATE PolicyEntity p SET p.syncStatus = 'SYNCED', p.syncedAt = CURRENT TIMESTAMP WHERE p.id IN :ids")
    int syncPolicies(@Param("ids") List<Long> ids);
}
