package com.nisync.incident.timeline.service;

import com.nisync.audit.entity.AuditLog;
import com.nisync.audit.repository.AuditLogRepository;
import com.nisync.common.exception.ResourceNotFoundException;
import com.nisync.incident.entity.Incident;
import com.nisync.incident.enums.IncidentSeverity;
import com.nisync.incident.enums.IncidentStatus;
import com.nisync.incident.note.entity.IncidentNote;
import com.nisync.incident.note.repository.IncidentNoteRepository;
import com.nisync.incident.repository.IncidentRepository;
import com.nisync.incident.timeline.dto.IncidentTimelineItemDto;
import com.nisync.incident.timeline.service.impl.IncidentTimelineServiceImpl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class IncidentTimelineServiceImplTest {

    private IncidentRepository incidentRepository;
    private IncidentNoteRepository incidentNoteRepository;
    private AuditLogRepository auditLogRepository;
    private IncidentTimelineServiceImpl incidentTimelineService;

    @BeforeEach
    void setUp() {
        incidentRepository = mock(IncidentRepository.class);
        incidentNoteRepository = mock(IncidentNoteRepository.class);
        auditLogRepository = mock(AuditLogRepository.class);
        incidentTimelineService = new IncidentTimelineServiceImpl();

        ReflectionTestUtils.setField(incidentTimelineService, "incidentRepository", incidentRepository);
        ReflectionTestUtils.setField(incidentTimelineService, "incidentNoteRepository", incidentNoteRepository);
        ReflectionTestUtils.setField(incidentTimelineService, "auditLogRepository", auditLogRepository);
    }

    @Test
    void shouldReturnMergedTimelineNewestFirst() {
        Incident incident = buildIncident(1L);
        IncidentNote olderNote = buildNote(20L, incident, LocalDateTime.of(2026, 5, 30, 10, 0));
        AuditLog newestAuditLog = buildAuditLog(30L, LocalDateTime.of(2026, 5, 30, 12, 0));

        when(incidentRepository.existsById(1L)).thenReturn(true);
        when(incidentNoteRepository.findByIncidentIdOrderByCreatedAtDesc(1L)).thenReturn(List.of(olderNote));
        when(auditLogRepository.findByResourceTypeAndResourceIdOrderByCreatedAtDesc("INCIDENT", "1"))
                .thenReturn(List.of(newestAuditLog));

        List<IncidentTimelineItemDto> response = incidentTimelineService.getTimelineByIncidentId(1L);

        assertEquals(2, response.size());
        assertEquals("AUDIT", response.get(0).getType());
        assertEquals("INCIDENT_UPDATED", response.get(0).getAction());
        assertEquals("NOTE", response.get(1).getType());
        assertEquals("Containment started.", response.get(1).getNote());
    }

    @Test
    void shouldThrowWhenIncidentDoesNotExist() {
        when(incidentRepository.existsById(99L)).thenReturn(false);

        assertThrows(
                ResourceNotFoundException.class,
                () -> incidentTimelineService.getTimelineByIncidentId(99L)
        );
    }

    private Incident buildIncident(Long id) {
        Incident incident = new Incident();
        incident.setId(id);
        incident.setTitle("Suspicious activity");
        incident.setDescription("Suspicious activity detected on workstation.");
        incident.setSeverity(IncidentSeverity.HIGH);
        incident.setStatus(IncidentStatus.OPEN);
        incident.setReportedByEmail("admin@nis2.com");
        incident.setCreatedAt(LocalDateTime.now());
        incident.setUpdatedAt(LocalDateTime.now());

        return incident;
    }

    private IncidentNote buildNote(Long id, Incident incident, LocalDateTime createdAt) {
        IncidentNote note = new IncidentNote();
        note.setId(id);
        note.setIncident(incident);
        note.setNote("Containment started.");
        note.setCreatedByEmail("analyst@nis2.com");
        note.setCreatedAt(createdAt);

        return note;
    }

    private AuditLog buildAuditLog(Long id, LocalDateTime createdAt) {
        AuditLog auditLog = new AuditLog();
        auditLog.setId(id);
        auditLog.setAction("INCIDENT_UPDATED");
        auditLog.setResourceType("INCIDENT");
        auditLog.setResourceId("1");
        auditLog.setActorEmail("admin@nis2.com");
        auditLog.setDetails("Incident updated");
        auditLog.setCreatedAt(createdAt);

        return auditLog;
    }
}
