package com.nisync.incident.service;

import com.nisync.common.response.PagedResponseDto;
import com.nisync.common.exception.ResourceNotFoundException;
import com.nisync.audit.service.AuditLogService;
import com.nisync.incident.dto.CreateIncidentRequestDto;
import com.nisync.incident.dto.IncidentResponseDto;
import com.nisync.incident.dto.UpdateIncidentRequestDto;
import com.nisync.incident.entity.Incident;
import com.nisync.incident.enums.IncidentSeverity;
import com.nisync.incident.enums.IncidentStatus;
import com.nisync.incident.repository.IncidentRepository;
import com.nisync.incident.service.impl.IncidentServiceImpl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class IncidentServiceImplTest {

    private IncidentRepository incidentRepository;
    private AuditLogService auditLogService;
    private IncidentServiceImpl incidentService;

    @BeforeEach
    void setUp() {
        incidentRepository = mock(IncidentRepository.class);
        auditLogService = mock(AuditLogService.class);
        incidentService = new IncidentServiceImpl();

        ReflectionTestUtils.setField(incidentService, "incidentRepository", incidentRepository);
        ReflectionTestUtils.setField(incidentService, "auditLogService", auditLogService);
    }

    @Test
    void shouldCreateIncidentSuccessfully() {
        CreateIncidentRequestDto request = new CreateIncidentRequestDto();
        request.setTitle("Suspicious login attempts");
        request.setDescription("Multiple failed login attempts detected from one IP address.");
        request.setSeverity(IncidentSeverity.HIGH);
        request.setAssignedToEmail(" analyst@nis2.com ");
        request.setDueAt(LocalDateTime.of(2026, 6, 1, 12, 0));

        when(incidentRepository.save(any(Incident.class))).thenAnswer(invocation -> {
            Incident incident = invocation.getArgument(0);
            incident.setId(1L);
            incident.setCreatedAt(LocalDateTime.now());
            incident.setUpdatedAt(LocalDateTime.now());
            return incident;
        });

        IncidentResponseDto response = incidentService.createIncident(request, "admin@nis2.com");

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("Suspicious login attempts", response.getTitle());
        assertEquals(IncidentSeverity.HIGH, response.getSeverity());
        assertEquals(IncidentStatus.OPEN, response.getStatus());
        assertEquals("admin@nis2.com", response.getReportedByEmail());
        assertEquals("analyst@nis2.com", response.getAssignedToEmail());
        assertEquals(LocalDateTime.of(2026, 6, 1, 12, 0), response.getDueAt());
    }

    @Test
    void shouldGetAllIncidentsSuccessfully() {
        Incident firstIncident = buildIncident(1L, "First Incident");
        Incident secondIncident = buildIncident(2L, "Second Incident");

        when(incidentRepository.findAll(anyIncidentSpecification(), anyCreatedAtDescPageable()))
                .thenReturn(new PageImpl<>(List.of(firstIncident, secondIncident)));

        PagedResponseDto<IncidentResponseDto> response = incidentService.getIncidents(
                null,
                null,
                null,
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

        assertEquals(2, response.getContent().size());
        assertEquals("First Incident", response.getContent().get(0).getTitle());
        assertEquals("Second Incident", response.getContent().get(1).getTitle());
    }

    @Test
    void shouldFilterIncidentsByStatusAndSeveritySuccessfully() {
        Incident incident = buildIncident(1L, "Filtered Incident");
        incident.setStatus(IncidentStatus.IN_PROGRESS);
        incident.setSeverity(IncidentSeverity.HIGH);

        when(incidentRepository.findAll(anyIncidentSpecification(), anySeverityAscPageable()))
                .thenReturn(new PageImpl<>(List.of(incident)));

        PagedResponseDto<IncidentResponseDto> response = incidentService.getIncidents(
                IncidentStatus.IN_PROGRESS,
                IncidentSeverity.HIGH,
                "analyst@nis2.com",
                "filtered",
                LocalDateTime.of(2026, 5, 1, 0, 0),
                LocalDateTime.of(2026, 6, 30, 23, 59),
                LocalDateTime.of(2026, 6, 1, 0, 0),
                LocalDateTime.of(2026, 6, 5, 23, 59),
                0,
                10,
                "severity",
                "asc"
        );

        assertEquals(1, response.getContent().size());
        assertEquals(IncidentStatus.IN_PROGRESS, response.getContent().get(0).getStatus());
        assertEquals(IncidentSeverity.HIGH, response.getContent().get(0).getSeverity());
        assertEquals("analyst@nis2.com", response.getContent().get(0).getAssignedToEmail());
    }

    @Test
    void shouldGetIncidentByIdSuccessfully() {
        Incident incident = buildIncident(1L, "Incident Detail");

        when(incidentRepository.findById(1L)).thenReturn(Optional.of(incident));

        IncidentResponseDto response = incidentService.getIncidentById(1L);

        assertEquals(1L, response.getId());
        assertEquals("Incident Detail", response.getTitle());
    }

    @Test
    void shouldThrowWhenIncidentDoesNotExist() {
        when(incidentRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> incidentService.getIncidentById(99L));
    }

    @Test
    void shouldUpdateIncidentSuccessfully() {
        Incident incident = buildIncident(1L, "Old Incident");
        UpdateIncidentRequestDto request = new UpdateIncidentRequestDto();
        request.setTitle("Updated Incident");
        request.setDescription("Updated incident description with enough detail.");
        request.setSeverity(IncidentSeverity.CRITICAL);
        request.setStatus(IncidentStatus.IN_PROGRESS);
        request.setAssignedToEmail("lead@nis2.com");
        request.setDueAt(LocalDateTime.of(2026, 6, 2, 18, 30));

        when(incidentRepository.findById(1L)).thenReturn(Optional.of(incident));
        when(incidentRepository.save(any(Incident.class))).thenAnswer(invocation -> invocation.getArgument(0));

        IncidentResponseDto response = incidentService.updateIncidentById(1L, request, "admin@nis2.com");

        assertEquals("Updated Incident", response.getTitle());
        assertEquals(IncidentSeverity.CRITICAL, response.getSeverity());
        assertEquals(IncidentStatus.IN_PROGRESS, response.getStatus());
        assertEquals("lead@nis2.com", response.getAssignedToEmail());
        assertEquals(LocalDateTime.of(2026, 6, 2, 18, 30), response.getDueAt());
    }

    @Test
    void shouldClearIncidentDueAtSuccessfully() {
        Incident incident = buildIncident(1L, "SLA Incident");
        UpdateIncidentRequestDto request = new UpdateIncidentRequestDto();
        request.setClearDueAt(true);

        when(incidentRepository.findById(1L)).thenReturn(Optional.of(incident));
        when(incidentRepository.save(any(Incident.class))).thenAnswer(invocation -> invocation.getArgument(0));

        IncidentResponseDto response = incidentService.updateIncidentById(1L, request, "admin@nis2.com");

        assertEquals("SLA Incident", response.getTitle());
        assertNull(response.getDueAt());
    }

    @Test
    void shouldBulkUpdateIncidentStatusSuccessfully() {
        Incident firstIncident = buildIncident(1L, "First Incident");
        Incident secondIncident = buildIncident(2L, "Second Incident");

        when(incidentRepository.findAllById(List.of(1L, 2L))).thenReturn(List.of(firstIncident, secondIncident));
        when(incidentRepository.saveAll(any())).thenAnswer(invocation -> invocation.getArgument(0));

        List<IncidentResponseDto> response = incidentService.bulkUpdateStatus(
                List.of(1L, 2L),
                IncidentStatus.RESOLVED,
                "admin@nis2.com"
        );

        assertEquals(2, response.size());
        assertEquals(IncidentStatus.RESOLVED, response.get(0).getStatus());
        assertEquals(IncidentStatus.RESOLVED, response.get(1).getStatus());
        verify(auditLogService).record(
                eq("INCIDENTS_BULK_STATUS_UPDATED"),
                eq("INCIDENT"),
                eq(null),
                eq("admin@nis2.com"),
                eq("Incidents status updated to RESOLVED. Count: 2")
        );
    }

    @Test
    void shouldThrowWhenBulkUpdateIncidentStatusHasMissingIds() {
        Incident incident = buildIncident(1L, "First Incident");

        when(incidentRepository.findAllById(List.of(1L, 2L))).thenReturn(List.of(incident));

        assertThrows(
                ResourceNotFoundException.class,
                () -> incidentService.bulkUpdateStatus(
                        List.of(1L, 2L),
                        IncidentStatus.RESOLVED,
                        "admin@nis2.com"
                )
        );
    }

    @Test
    void shouldDeleteIncidentSuccessfully() {
        Incident incident = buildIncident(1L, "Delete Incident");

        when(incidentRepository.findById(1L)).thenReturn(Optional.of(incident));

        IncidentResponseDto response = incidentService.deleteIncidentById(1L, "admin@nis2.com");

        assertEquals(1L, response.getId());
        verify(incidentRepository).delete(incident);
    }

    @Test
    void shouldExportIncidentsCsvSuccessfully() {
        Incident incident = buildIncident(1L, "CSV Incident");
        incident.setDescription("Description with comma, and \"quote\".");

        when(incidentRepository.findAll(anyIncidentSpecification(), anyCreatedAtDescSort()))
                .thenReturn(List.of(incident));

        String csv = incidentService.exportIncidentsCsv(
                IncidentStatus.OPEN,
                IncidentSeverity.MEDIUM,
                "analyst@nis2.com",
                "csv",
                "admin@nis2.com"
        );

        String[] lines = csv.split("\\n");

        assertEquals("ID,Title,Description,Severity,Status,Reported By,Assigned To,SLA Due,Created At,Updated At", lines[0]);
        assertTrue(lines[1].contains("\"Description with comma, and \"\"quote\"\".\""));
        verify(auditLogService).record(
                eq("INCIDENTS_EXPORTED"),
                eq("INCIDENT"),
                eq(null),
                eq("admin@nis2.com"),
                eq("Incidents exported to CSV. Count: 1")
        );
    }

    private Incident buildIncident(Long id, String title) {
        Incident incident = new Incident();
        incident.setId(id);
        incident.setTitle(title);
        incident.setDescription("Incident description with enough useful detail.");
        incident.setSeverity(IncidentSeverity.MEDIUM);
        incident.setStatus(IncidentStatus.OPEN);
        incident.setReportedByEmail("admin@nis2.com");
        incident.setAssignedToEmail("analyst@nis2.com");
        incident.setDueAt(LocalDateTime.of(2026, 6, 1, 9, 0));
        incident.setCreatedAt(LocalDateTime.now());
        incident.setUpdatedAt(LocalDateTime.now());

        return incident;
    }

    private Specification<Incident> anyIncidentSpecification() {
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

    private Pageable anySeverityAscPageable() {
        return argThat(pageable -> pageable.getSort().getOrderFor("severity") != null
                && Sort.Direction.ASC.equals(pageable.getSort().getOrderFor("severity").getDirection()));
    }
}
