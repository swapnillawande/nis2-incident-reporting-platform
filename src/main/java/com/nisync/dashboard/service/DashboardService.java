package com.nisync.dashboard.service;

import com.nisync.dashboard.dto.DashboardSummaryDto;
import com.nisync.incident.dto.IncidentResponseDto;

import java.util.List;

public interface DashboardService {

    DashboardSummaryDto getSummary();

    List<IncidentResponseDto> getRecentActiveIncidents();
}
