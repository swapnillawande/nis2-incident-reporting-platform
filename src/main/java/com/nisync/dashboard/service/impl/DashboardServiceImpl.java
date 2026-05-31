package com.nisync.dashboard.service.impl;

import com.nisync.dashboard.dto.DashboardSummaryDto;
import com.nisync.dashboard.service.DashboardService;
import com.nisync.incident.enums.IncidentStatus;
import com.nisync.incident.repository.IncidentRepository;
import com.nisync.user.repository.UserRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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

        return new DashboardSummaryDto(
                userRepository.count(),
                incidentRepository.count(),
                openIncidents,
                inProgressIncidents,
                resolvedIncidents,
                closedIncidents
        );
    }
}
