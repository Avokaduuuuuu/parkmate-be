package com.parkmate.session;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SessionRepository extends JpaRepository<SessionEntity, Long> {
    Optional<SessionEntity> findByCardUUID(String cardUUID);
}
