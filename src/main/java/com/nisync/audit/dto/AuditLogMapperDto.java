package com.nisync.audit.dto;

import com.nisync.audit.entity.AuditLog;

public final class AuditLogMapperDto {

    private AuditLogMapperDto() {
    }

    public static AuditLogResponseDto toResponse(AuditLog auditLog) {
        AuditLogResponseDto response = new AuditLogResponseDto();
        response.setId(auditLog.getId());
        response.setAction(auditLog.getAction());
        response.setResourceType(auditLog.getResourceType());
        response.setResourceId(auditLog.getResourceId());
        response.setActorEmail(auditLog.getActorEmail());
        response.setDetails(auditLog.getDetails());
        response.setCreatedAt(auditLog.getCreatedAt());
        return response;
    }
}
