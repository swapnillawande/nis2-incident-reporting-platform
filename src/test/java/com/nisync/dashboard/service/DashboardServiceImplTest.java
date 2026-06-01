package com.nisync.dashboard.service;

import com.nisync.dashboard.dto.DashboardSummaryDto;
import com.nisync.dashboard.service.impl.DashboardServiceImpl;
import com.nisync.incident.dto.IncidentResponseDto;
import com.nisync.incident.entity.Incident;
import com.nisync.incident.enums.IncidentSeverity;
import com.nisync.incident.enums.IncidentStatus;
import com.nisync.incident.repository.IncidentRepository;
import com.nisync.user.enums.RoleName;
import com.nisync.user.enums.UserStatus;
import com.nisync.user.repository.UserRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class DashboardServiceImplTest {

    private UserRepository userRepository;
    private IncidentRepository incidentRepository;
    private DashboardServiceImpl dashboardService;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        incidentRepository = mock(IncidentRepository.class);
        dashboardService = new DashboardServiceImpl();

        ReflectionTestUtils.setField(dashboardService, "userRepository", userRepository);
        ReflectionTestUtils.setField(dashboardService, "incidentRepository", incidentRepository);
    }

    @Test
    void shouldBuildDashboardSummarySuccessfully() {
        when(userRepository.count()).thenReturn(4L);
        when(userRepository.countByStatus(UserStatus.ACTIVE)).thenReturn(2L);
        when(userRepository.countByStatus(UserStatus.INACTIVE)).thenReturn(1L);
        when(userRepository.countByStatus(UserStatus.SUSPENDED)).thenReturn(1L);
        when(userRepository.countByRole(RoleName.ADMIN)).thenReturn(1L);
        when(userRepository.countByRole(RoleName.SECURITY_ANALYST)).thenReturn(2L);
        when(userRepository.countByRole(RoleName.COMPLIANCE_OFFICER)).thenReturn(1L);
        when(userRepository.countByRole(RoleName.AUDITOR)).thenReturn(3L);
        when(incidentRepository.count()).thenReturn(10L);
        when(incidentRepository.countByStatus(IncidentStatus.OPEN)).thenReturn(3L);
        when(incidentRepository.countByStatus(IncidentStatus.IN_PROGRESS)).thenReturn(2L);
        when(incidentRepository.countByStatus(IncidentStatus.RESOLVED)).thenReturn(4L);
        when(incidentRepository.countByStatus(IncidentStatus.CLOSED)).thenReturn(1L);
        when(incidentRepository.countBySeverity(IncidentSeverity.LOW)).thenReturn(1L);
        when(incidentRepository.countBySeverity(IncidentSeverity.MEDIUM)).thenReturn(2L);
        when(incidentRepository.countBySeverity(IncidentSeverity.HIGH)).thenReturn(3L);
        when(incidentRepository.countBySeverity(IncidentSeverity.CRITICAL)).thenReturn(4L);
        when(incidentRepository.countByDueAtBeforeAndStatusIn(
                any(LocalDateTime.class),
                any())).thenReturn(2L);
        when(incidentRepository.countByDueAtBetweenAndStatusIn(
                any(LocalDateTime.class),
                any(LocalDateTime.class),
                any())).thenReturn(1L);
        when(incidentRepository.countByDueAtIsNullAndStatusIn(any())).thenReturn(3L);
        when(incidentRepository.countByAssignedToEmailIsNotNullAndStatusIn(any())).thenReturn(4L);
        when(incidentRepository.countByAssignedToEmailIsNullAndStatusIn(any())).thenReturn(1L);

        DashboardSummaryDto response = dashboardService.getSummary();

        assertEquals(4L, response.getTotalUsers());
        assertEquals(2L, response.getActiveUsers());
        assertEquals(1L, response.getInactiveUsers());
        assertEquals(1L, response.getSuspendedUsers());
        assertEquals(1L, response.getAdminUsers());
        assertEquals(2L, response.getSecurityAnalystUsers());
        assertEquals(1L, response.getComplianceOfficerUsers());
        assertEquals(3L, response.getAuditorUsers());
        assertEquals(10L, response.getTotalIncidents());
        assertEquals(3L, response.getOpenIncidents());
        assertEquals(2L, response.getInProgressIncidents());
        assertEquals(4L, response.getResolvedIncidents());
        assertEquals(1L, response.getClosedIncidents());
        assertEquals(1L, response.getLowSeverityIncidents());
        assertEquals(2L, response.getMediumSeverityIncidents());
        assertEquals(3L, response.getHighSeverityIncidents());
        assertEquals(4L, response.getCriticalSeverityIncidents());
        assertEquals(2L, response.getOverdueIncidents());
        assertEquals(1L, response.getDueSoonIncidents());
        assertEquals(3L, response.getUnscheduledActiveIncidents());
        assertEquals(4L, response.getAssignedActiveIncidents());
        assertEquals(1L, response.getUnassignedActiveIncidents());
    }

    @Test
    void shouldGetRecentActiveIncidentsSuccessfully() {
        Incident incident = buildIncident(1L, "Suspicious login");

        when(incidentRepository.findTop5ByStatusInOrderByCreatedAtDesc(any())).thenReturn(List.of(incident));

        List<IncidentResponseDto> response = dashboardService.getRecentActiveIncidents();

        assertEquals(1, response.size());
        assertEquals(1L, response.get(0).getId());
        assertEquals("Suspicious login", response.get(0).getTitle());
        assertEquals(IncidentStatus.OPEN, response.get(0).getStatus());
    }

    private Incident buildIncident(Long id, String title) {
        Incident incident = new Incident();
        incident.setId(id);
        incident.setTitle(title);
        incident.setDescription("Incident description with enough detail.");
        incident.setSeverity(IncidentSeverity.HIGH);
        incident.setStatus(IncidentStatus.OPEN);
        incident.setReportedByEmail("admin@nis2.com");
        incident.setCreatedAt(LocalDateTime.now());
        incident.setUpdatedAt(LocalDateTime.now());

        return incident;
    }
}
