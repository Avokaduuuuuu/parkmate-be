package com.parkmate.session;

import com.parkmate.session.enums.SessionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface SessionRepository extends JpaRepository<SessionEntity, Long>, JpaSpecificationExecutor<SessionEntity> {
    Optional<SessionEntity> findByCardUUIDAndStatus(String cardUUID, SessionStatus status);
    Optional<SessionEntity> findByCardUUID(String cardUUID);
}
