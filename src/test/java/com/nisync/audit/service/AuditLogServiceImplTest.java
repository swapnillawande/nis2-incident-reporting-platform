package com.nisync.audit.service;

import com.nisync.audit.dto.AuditLogResponseDto;
import com.nisync.audit.entity.AuditLog;
import com.nisync.audit.repository.AuditLogRepository;
import com.nisync.audit.service.impl.AuditLogServiceImpl;
import com.nisync.common.response.PagedResponseDto;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
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

        when(auditLogRepository.findAll(anyAuditLogSpecification(), anyCreatedAtDescPageable()))
                .thenReturn(new PageImpl<>(List.of(auditLog)));

        PagedResponseDto<AuditLogResponseDto> response = auditLogService.getRecentAuditLogs(
                null,
                null,
                null,
                null,
                null,
                0,
                10,
                "createdAt",
                "desc"
        );

        assertEquals(1, response.getContent().size());
        assertEquals("USER_LOGIN", response.getContent().get(0).getAction());
        assertEquals("admin@nis2.com", response.getContent().get(0).getActorEmail());
    }

    @Test
    void shouldReturnFilteredAuditLogs() {
        AuditLog auditLog = new AuditLog();
        auditLog.setId(2L);
        auditLog.setAction("INCIDENT_UPDATED");
        auditLog.setResourceType("INCIDENT");
        auditLog.setResourceId("9");
        auditLog.setActorEmail("analyst@nis2.com");
        auditLog.setDetails("Incident updated");
        auditLog.setCreatedAt(LocalDateTime.now());

        when(auditLogRepository.findAll(anyAuditLogSpecification(), anyActionAscPageable()))
                .thenReturn(new PageImpl<>(List.of(auditLog)));

        PagedResponseDto<AuditLogResponseDto> response = auditLogService.getRecentAuditLogs(
                "INCIDENT_UPDATED",
                "INCIDENT",
                "analyst",
                LocalDateTime.of(2026, 1, 1, 0, 0),
                LocalDateTime.of(2026, 12, 31, 23, 59),
                0,
                10,
                "action",
                "asc"
        );

        assertEquals(1, response.getContent().size());
        assertEquals("INCIDENT_UPDATED", response.getContent().get(0).getAction());
        assertEquals("INCIDENT", response.getContent().get(0).getResourceType());
    }

    @Test
    void shouldExportAuditLogsCsvSuccessfully() {
        AuditLog auditLog = new AuditLog();
        auditLog.setId(3L);
        auditLog.setAction("INCIDENTS_EXPORTED");
        auditLog.setResourceType("INCIDENT");
        auditLog.setResourceId("12");
        auditLog.setActorEmail("admin@nis2.com");
        auditLog.setDetails("Exported, with \"quotes\"");
        auditLog.setCreatedAt(LocalDateTime.of(2026, 6, 1, 8, 30));

        when(auditLogRepository.findAll(anyAuditLogSpecification(), anyCreatedAtDescSort()))
                .thenReturn(List.of(auditLog));

        String csv = auditLogService.exportAuditLogsCsv("INCIDENTS_EXPORTED", "INCIDENT", "exported");
        String[] lines = csv.split("\\n");

        assertEquals("ID,Action,Resource Type,Resource ID,Actor Email,Details,Created At", lines[0]);
        assertTrue(lines[1].contains("\"Exported, with \"\"quotes\"\"\""));
        assertTrue(lines[1].contains("2026-06-01T08:30"));
    }

    private Specification<AuditLog> anyAuditLogSpecification() {
        return any();
    }

    private Sort anyCreatedAtDescSort() {
        return argThat(sort -> sort.getOrderFor("createdAt") != null
                && Sort.Direction.DESC.equals(sort.getOrderFor("createdAt").getDirection()));
    }

    private Pageable anyCreatedAtDescPageable() {
        return argThat(pageable -> pageable.getSort().getOrderFor("createdAt") != null
                && Sort.Direction.DESC.equals(pageable.getSort().getOrderFor("createdAt").getDirection()));
    }

    private Pageable anyActionAscPageable() {
        return argThat(pageable -> pageable.getSort().getOrderFor("action") != null
                && Sort.Direction.ASC.equals(pageable.getSort().getOrderFor("action").getDirection()));
    }
}
