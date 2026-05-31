package com.nisync.incident.note.service;

import com.nisync.audit.service.AuditLogService;
import com.nisync.common.exception.ResourceNotFoundException;
import com.nisync.incident.entity.Incident;
import com.nisync.incident.enums.IncidentSeverity;
import com.nisync.incident.enums.IncidentStatus;
import com.nisync.incident.note.dto.CreateIncidentNoteRequestDto;
import com.nisync.incident.note.dto.IncidentNoteResponseDto;
import com.nisync.incident.note.entity.IncidentNote;
import com.nisync.incident.note.repository.IncidentNoteRepository;
import com.nisync.incident.note.service.impl.IncidentNoteServiceImpl;
import com.nisync.incident.repository.IncidentRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class IncidentNoteServiceImplTest {

    private IncidentRepository incidentRepository;
    private IncidentNoteRepository incidentNoteRepository;
    private AuditLogService auditLogService;
    private IncidentNoteServiceImpl incidentNoteService;

    @BeforeEach
    void setUp() {
        incidentRepository = mock(IncidentRepository.class);
        incidentNoteRepository = mock(IncidentNoteRepository.class);
        auditLogService = mock(AuditLogService.class);
        incidentNoteService = new IncidentNoteServiceImpl();

        ReflectionTestUtils.setField(incidentNoteService, "incidentRepository", incidentRepository);
        ReflectionTestUtils.setField(incidentNoteService, "incidentNoteRepository", incidentNoteRepository);
        ReflectionTestUtils.setField(incidentNoteService, "auditLogService", auditLogService);
    }

    @Test
    void shouldAddNoteSuccessfully() {
        Incident incident = buildIncident(1L);
        CreateIncidentNoteRequestDto request = new CreateIncidentNoteRequestDto();
        request.setNote("Initial triage completed.");

        when(incidentRepository.findById(1L)).thenReturn(Optional.of(incident));
        when(incidentNoteRepository.save(any(IncidentNote.class))).thenAnswer(invocation -> {
            IncidentNote note = invocation.getArgument(0);
            note.setId(10L);
            note.setCreatedAt(LocalDateTime.now());
            return note;
        });

        IncidentNoteResponseDto response = incidentNoteService.addNote(
                1L,
                request,
                "analyst@nis2.com"
        );

        assertEquals(10L, response.getId());
        assertEquals(1L, response.getIncidentId());
        assertEquals("Initial triage completed.", response.getNote());
        assertEquals("analyst@nis2.com", response.getCreatedByEmail());
    }

    @Test
    void shouldGetNotesByIncidentIdSuccessfully() {
        Incident incident = buildIncident(1L);
        IncidentNote note = buildNote(20L, incident);

        when(incidentRepository.findById(1L)).thenReturn(Optional.of(incident));
        when(incidentNoteRepository.findByIncidentIdOrderByCreatedAtDesc(1L)).thenReturn(List.of(note));

        List<IncidentNoteResponseDto> response = incidentNoteService.getNotesByIncidentId(1L);

        assertEquals(1, response.size());
        assertEquals(20L, response.get(0).getId());
        assertEquals("Containment started.", response.get(0).getNote());
    }

    @Test
    void shouldThrowWhenAddingNoteToMissingIncident() {
        CreateIncidentNoteRequestDto request = new CreateIncidentNoteRequestDto();
        request.setNote("This should fail.");

        when(incidentRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> incidentNoteService.addNote(99L, request, "analyst@nis2.com")
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

    private IncidentNote buildNote(Long id, Incident incident) {
        IncidentNote note = new IncidentNote();
        note.setId(id);
        note.setIncident(incident);
        note.setNote("Containment started.");
        note.setCreatedByEmail("analyst@nis2.com");
        note.setCreatedAt(LocalDateTime.now());

        return note;
    }
}
