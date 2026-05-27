package com.dormitory.service;

import com.dormitory.entity.AuditLog;
import com.dormitory.repository.AuditLogRepository;
import org.springframework.stereotype.Service;

@Service
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;

    public AuditLogService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    public AuditLog record(AuditLog auditLog) {
        return auditLogRepository.save(auditLog);
    }
}
