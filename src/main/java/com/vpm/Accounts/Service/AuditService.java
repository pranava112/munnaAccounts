package com.vpm.Accounts.Service;

import java.time.LocalDateTime;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.vpm.Accounts.Entity.AuditLog;
import com.vpm.Accounts.Repository.AuditLogRepository;
import com.vpm.Accounts.security.TenantContext;

@Service
public class AuditService {
    private final AuditLogRepository repository;

    public AuditService(AuditLogRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public AuditLog record(String action, String entityType, Long entityId, String beforeState, String afterState) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        AuditLog log = new AuditLog();
        log.setTenantId(TenantContext.require());
        log.setOccurredAt(LocalDateTime.now());
        log.setActor(authentication == null ? "system" : authentication.getName());
        log.setAction(action);
        log.setEntityType(entityType);
        log.setEntityId(entityId);
        log.setBeforeState(beforeState);
        log.setAfterState(afterState);
        return repository.save(log);
    }
}
