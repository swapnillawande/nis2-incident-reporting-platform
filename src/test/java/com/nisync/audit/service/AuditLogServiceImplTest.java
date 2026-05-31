package com.nisync.audit.service;

import com.nisync.audit.dto.AuditLogResponseDto;
import com.nisync.audit.entity.AuditLog;
import com.nisync.audit.repository.AuditLogRepository;
import com.nisync.audit.service.impl.AuditLogServiceImpl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AuditLogServiceImplTest {

    private AuditLogRepository auditLogRepository;
    private AuditLogServiceImpl auditLogService;

    @BeforeEach
    void setUp() {
        auditLogRepository = mock(AuditLogRepository.class);
        auditLogService = new AuditLogServiceImpl();

        ReflectionTestUtils.setField(auditLogService, "auditLogRepository", auditLogRepository);
    }

    @Test
    void shouldRecordAuditLogSuccessfully() {
        when(auditLogRepository.save(any(AuditLog.class))).thenAnswer(invocation -> {
            AuditLog auditLog = invocation.getArgument(0);
            auditLog.setId(1L);
            auditLog.setCreatedAt(LocalDateTime.now());
            return auditLog;
        });

        AuditLogResponseDto response = auditLogService.record(
                "INCIDENT_CREATED",
                "INCIDENT",
                10L,
                "analyst@nis2.com",
                "Incident created"
        );

        assertEquals(1L, response.getId());
        assertEquals("INCIDENT_CREATED", response.getAction());
        assertEquals("INCIDENT", response.getResourceType());
        assertEquals("10", response.getResourceId());
        assertEquals("analyst@nis2.com", response.getActorEmail());
    }

    @Test
    void shouldReturnRecentAuditLogs() {
        AuditLog auditLog = new AuditLog();
        auditLog.setId(1L);
        auditLog.setAction("USER_LOGIN");
        auditLog.setResourceType("USER");
        auditLog.setResourceId("5");
        auditLog.setActorEmail("admin@nis2.com");
        auditLog.setDetails("User login");
        auditLog.setCreatedAt(LocalDateTime.now());

        when(auditLogRepository.findTop100ByOrderByCreatedAtDesc()).thenReturn(List.of(auditLog));

        List<AuditLogResponseDto> response = auditLogService.getRecentAuditLogs();

        assertEquals(1, response.size());
        assertEquals("USER_LOGIN", response.get(0).getAction());
        assertEquals("admin@nis2.com", response.get(0).getActorEmail());
    }
}
