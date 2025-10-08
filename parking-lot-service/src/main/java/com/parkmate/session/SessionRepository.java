package com.parkmate.session;

import com.parkmate.session.enums.SessionStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SessionRepository extends JpaRepository<SessionEntity, Long> {
    Optional<SessionEntity> findByCardUUIDAndStatus(String cardUUID, SessionStatus status);
    Optional<SessionEntity> findByCardUUID(String cardUUID);
}
