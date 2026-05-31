package com.nisync.audit.service;

import com.nisync.audit.dto.AuditLogResponseDto;

import java.util.List;

public interface AuditLogService {

    AuditLogResponseDto record(String action, String resourceType, Object resourceId, String actorEmail, String details);

    List<AuditLogResponseDto> getRecentAuditLogs(String action, String resourceType, String query);
}
