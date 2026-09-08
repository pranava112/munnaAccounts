package com.vpm.Accounts.Repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.vpm.Accounts.Entity.AuditLog;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
}
