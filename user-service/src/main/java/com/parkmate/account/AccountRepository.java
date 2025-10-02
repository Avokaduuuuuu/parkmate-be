package com.parkmate.account;

import org.jspecify.annotations.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {

    boolean existsByEmail(String email);

    boolean existsByUsername(String username);

    boolean existsById(@NonNull Long id);

    Optional<Account> findAccountByEmail(String email);

    Optional<Account> findAccountByEmailVerificationToken(String token);
}
