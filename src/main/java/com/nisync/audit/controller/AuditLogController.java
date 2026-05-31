package com.nisync.audit.controller;

import com.nisync.audit.dto.AuditLogResponseDto;
import com.nisync.audit.service.AuditLogService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("audit-logs")
public class AuditLogController {

    @Autowired
    private AuditLogService auditLogService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public List<AuditLogResponseDto> getRecentAuditLogs(
            @RequestParam(name = "action", required = false) String action,
            @RequestParam(name = "resourceType", required = false) String resourceType,
            @RequestParam(name = "q", required = false) String query) {

        return auditLogService.getRecentAuditLogs(action, resourceType, query);
    }
}
