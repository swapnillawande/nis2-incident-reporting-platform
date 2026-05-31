package com.nisync.incident.service;

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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
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

        when(incidentRepository.findAll(anyIncidentSpecification(), anyCreatedAtDescSort()))
                .thenReturn(List.of(firstIncident, secondIncident));

        List<IncidentResponseDto> response = incidentService.getIncidents(null, null, null, null);

        assertEquals(2, response.size());
        assertEquals("First Incident", response.get(0).getTitle());
        assertEquals("Second Incident", response.get(1).getTitle());
    }

    @Test
    void shouldFilterIncidentsByStatusAndSeveritySuccessfully() {
        Incident incident = buildIncident(1L, "Filtered Incident");
        incident.setStatus(IncidentStatus.IN_PROGRESS);
        incident.setSeverity(IncidentSeverity.HIGH);

        when(incidentRepository.findAll(anyIncidentSpecification(), anyCreatedAtDescSort())).thenReturn(List.of(incident));

        List<IncidentResponseDto> response = incidentService.getIncidents(
                IncidentStatus.IN_PROGRESS,
                IncidentSeverity.HIGH,
                "analyst@nis2.com",
                "filtered"
        );

        assertEquals(1, response.size());
        assertEquals(IncidentStatus.IN_PROGRESS, response.get(0).getStatus());
        assertEquals(IncidentSeverity.HIGH, response.get(0).getSeverity());
        assertEquals("analyst@nis2.com", response.get(0).getAssignedToEmail());
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
    void shouldDeleteIncidentSuccessfully() {
        Incident incident = buildIncident(1L, "Delete Incident");

        when(incidentRepository.findById(1L)).thenReturn(Optional.of(incident));

        IncidentResponseDto response = incidentService.deleteIncidentById(1L, "admin@nis2.com");

        assertEquals(1L, response.getId());
        verify(incidentRepository).delete(incident);
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
}
