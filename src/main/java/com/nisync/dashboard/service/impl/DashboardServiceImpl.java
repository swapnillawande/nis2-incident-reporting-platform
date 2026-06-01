package com.nisync.dashboard.service.impl;

import com.nisync.dashboard.dto.DashboardSummaryDto;
import com.nisync.dashboard.service.DashboardService;
import com.nisync.incident.dto.IncidentMapperDto;
import com.nisync.incident.dto.IncidentResponseDto;
import com.nisync.incident.enums.IncidentStatus;
import com.nisync.incident.repository.IncidentRepository;
import com.nisync.user.enums.RoleName;
import com.nisync.user.enums.UserStatus;
import com.nisync.user.repository.UserRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class DashboardServiceImpl implements DashboardService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private IncidentRepository incidentRepository;

    @Override
    public DashboardSummaryDto getSummary() {
        long openIncidents = incidentRepository.countByStatus(IncidentStatus.OPEN);
        long inProgressIncidents = incidentRepository.countByStatus(IncidentStatus.IN_PROGRESS);
        long resolvedIncidents = incidentRepository.countByStatus(IncidentStatus.RESOLVED);
        long closedIncidents = incidentRepository.countByStatus(IncidentStatus.CLOSED);
        LocalDateTime now = LocalDateTime.now();
        List<IncidentStatus> activeStatuses = getActiveStatuses();

        return new DashboardSummaryDto(
                userRepository.count(),
                userRepository.countByStatus(UserStatus.ACTIVE),
                userRepository.countByStatus(UserStatus.INACTIVE),
                userRepository.countByStatus(UserStatus.SUSPENDED),
                userRepository.countByRole(RoleName.ADMIN),
                userRepository.countByRole(RoleName.SECURITY_ANALYST),
                userRepository.countByRole(RoleName.COMPLIANCE_OFFICER),
                userRepository.countByRole(RoleName.AUDITOR),
                incidentRepository.count(),
                openIncidents,
                inProgressIncidents,
                resolvedIncidents,
                closedIncidents,
                incidentRepository.countByDueAtBeforeAndStatusIn(now, activeStatuses),
                incidentRepository.countByDueAtBetweenAndStatusIn(now, now.plusHours(24), activeStatuses),
                incidentRepository.countByDueAtIsNullAndStatusIn(activeStatuses)
        );
    }

    @Override
    public List<IncidentResponseDto> getRecentActiveIncidents() {
        return incidentRepository.findTop5ByStatusInOrderByCreatedAtDesc(getActiveStatuses())
                .stream()
                .map(IncidentMapperDto::toResponse)
                .toList();
    }

    private List<IncidentStatus> getActiveStatuses() {
        return List.of(
                IncidentStatus.OPEN,
                IncidentStatus.IN_PROGRESS
        );
    }
}
