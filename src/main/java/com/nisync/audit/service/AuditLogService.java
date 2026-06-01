package com.nisync.audit.service;

import com.nisync.audit.dto.AuditLogResponseDto;
import com.nisync.common.response.PagedResponseDto;

import java.util.List;
import java.time.LocalDateTime;

public interface AuditLogService {

    AuditLogResponseDto record(String action, String resourceType, Object resourceId, String actorEmail, String details);

    PagedResponseDto<AuditLogResponseDto> getRecentAuditLogs(
            String action,
            String resourceType,
            String query,
            LocalDateTime createdFrom,
            LocalDateTime createdTo,
            int page,
            int size,
            String sortBy,
            String sortDir);

    String exportAuditLogsCsv(String action, String resourceType, String query);
}
