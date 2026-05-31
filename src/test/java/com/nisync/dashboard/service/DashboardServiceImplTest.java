package com.nisync.dashboard.service;

import com.nisync.dashboard.dto.DashboardSummaryDto;
import com.nisync.dashboard.service.impl.DashboardServiceImpl;
import com.nisync.incident.enums.IncidentStatus;
import com.nisync.incident.repository.IncidentRepository;
import com.nisync.user.repository.UserRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
        when(incidentRepository.count()).thenReturn(10L);
        when(incidentRepository.countByStatus(IncidentStatus.OPEN)).thenReturn(3L);
        when(incidentRepository.countByStatus(IncidentStatus.IN_PROGRESS)).thenReturn(2L);
        when(incidentRepository.countByStatus(IncidentStatus.RESOLVED)).thenReturn(4L);
        when(incidentRepository.countByStatus(IncidentStatus.CLOSED)).thenReturn(1L);

        DashboardSummaryDto response = dashboardService.getSummary();

        assertEquals(4L, response.getTotalUsers());
        assertEquals(10L, response.getTotalIncidents());
        assertEquals(3L, response.getOpenIncidents());
        assertEquals(2L, response.getInProgressIncidents());
        assertEquals(4L, response.getResolvedIncidents());
        assertEquals(1L, response.getClosedIncidents());
    }
}
