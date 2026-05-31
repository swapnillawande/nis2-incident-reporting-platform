package com.nisync.incident.service;

import com.nisync.incident.dto.CreateIncidentRequestDto;
import com.nisync.incident.dto.IncidentResponseDto;
import com.nisync.incident.dto.UpdateIncidentRequestDto;

import java.util.List;

public interface IncidentService {

    IncidentResponseDto createIncident(CreateIncidentRequestDto request, String reportedByEmail);

    List<IncidentResponseDto> getAllIncidents();

    IncidentResponseDto getIncidentById(Long incidentId);

    IncidentResponseDto updateIncidentById(Long incidentId, UpdateIncidentRequestDto request);

    IncidentResponseDto deleteIncidentById(Long incidentId);
}
