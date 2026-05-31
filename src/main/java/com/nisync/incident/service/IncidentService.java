package com.nisync.incident.service;

import com.nisync.incident.dto.CreateIncidentRequestDto;
import com.nisync.incident.dto.IncidentResponseDto;
import com.nisync.incident.dto.UpdateIncidentRequestDto;
import com.nisync.incident.enums.IncidentSeverity;
import com.nisync.incident.enums.IncidentStatus;

import java.util.List;

public interface IncidentService {

    IncidentResponseDto createIncident(CreateIncidentRequestDto request, String reportedByEmail);

    List<IncidentResponseDto> getIncidents(
            IncidentStatus status,
            IncidentSeverity severity,
            String assignedToEmail,
            String query);

    IncidentResponseDto getIncidentById(Long incidentId);

    IncidentResponseDto updateIncidentById(Long incidentId, UpdateIncidentRequestDto request, String actorEmail);

    IncidentResponseDto deleteIncidentById(Long incidentId, String actorEmail);
}
