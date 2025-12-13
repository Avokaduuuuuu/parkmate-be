package com.parkmate.user;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long>, QuerydslPredicateExecutor<User> {

    boolean existsByPhone(String phone);

    Optional<User> findByAccountId(Long id);

    @Query("SELECT u.id FROM User u WHERE u.account.id = :id")
    Long getUserIdByAccountId(Long id);

    @Query("SELECT " +
            "COALESCE(COUNT(u), 0)  as total," +
            "COALESCE(COUNT(CASE WHEN u.createdAt >= :from AND u.createdAt <= :to THEN 1 END), 0) as newThisPeriod " +
            "FROM User u")
    PlatformUserProjection getPlatformUserStatistic(
            @Param("from")LocalDateTime from, @Param("to") LocalDateTime to
    );
}
