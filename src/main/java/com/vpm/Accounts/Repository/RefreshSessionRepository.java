package com.vpm.Accounts.Repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import com.vpm.Accounts.Entity.RefreshSession;

public interface RefreshSessionRepository extends JpaRepository<RefreshSession, Long> {
    Optional<RefreshSession> findByTokenHash(String tokenHash);
}
