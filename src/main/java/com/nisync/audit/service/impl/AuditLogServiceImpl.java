package com.nisync.audit.service.impl;

import com.nisync.audit.dto.AuditLogMapperDto;
import com.nisync.audit.dto.AuditLogResponseDto;
import com.nisync.audit.entity.AuditLog;
import com.nisync.audit.repository.AuditLogRepository;
import com.nisync.audit.service.AuditLogService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AuditLogServiceImpl implements AuditLogService {

    @Autowired
    private AuditLogRepository auditLogRepository;

    @Override
    public AuditLogResponseDto record(
            String action,
            String resourceType,
            Object resourceId,
            String actorEmail,
            String details) {

        AuditLog auditLog = new AuditLog();
        auditLog.setAction(action);
        auditLog.setResourceType(resourceType);
        auditLog.setResourceId(resourceId == null ? null : String.valueOf(resourceId));
        auditLog.setActorEmail(actorEmail == null || actorEmail.isBlank() ? "system" : actorEmail);
        auditLog.setDetails(details);

        return AuditLogMapperDto.toResponse(auditLogRepository.save(auditLog));
    }

    @Override
    public List<AuditLogResponseDto> getRecentAuditLogs() {
        return auditLogRepository.findTop100ByOrderByCreatedAtDesc()
                .stream()
                .map(AuditLogMapperDto::toResponse)
                .toList();
    }
}
