package com.nisync.incident.service;

import com.nisync.common.response.PagedResponseDto;
import com.nisync.incident.dto.CreateIncidentRequestDto;
import com.nisync.incident.dto.IncidentResponseDto;
import com.nisync.incident.dto.UpdateIncidentRequestDto;
import com.nisync.incident.enums.IncidentSeverity;
import com.nisync.incident.enums.IncidentStatus;

import java.util.List;
import java.time.LocalDateTime;

public interface IncidentService {

    IncidentResponseDto createIncident(CreateIncidentRequestDto request, String reportedByEmail);

    PagedResponseDto<IncidentResponseDto> getIncidents(
            IncidentStatus status,
            IncidentSeverity severity,
            String assignedToEmail,
            String query,
            LocalDateTime createdFrom,
            LocalDateTime createdTo,
            LocalDateTime dueFrom,
            LocalDateTime dueTo,
            int page,
            int size,
            String sortBy,
            String sortDir);

    String exportIncidentsCsv(
            IncidentStatus status,
            IncidentSeverity severity,
            String assignedToEmail,
            String query,
            LocalDateTime createdFrom,
            LocalDateTime createdTo,
            LocalDateTime dueFrom,
            LocalDateTime dueTo,
            String actorEmail);

    IncidentResponseDto getIncidentById(Long incidentId);

    IncidentResponseDto updateIncidentById(Long incidentId, UpdateIncidentRequestDto request, String actorEmail);

    IncidentResponseDto assignIncident(Long incidentId, String assignedToEmail, String actorEmail);

    IncidentResponseDto unassignIncident(Long incidentId, String actorEmail);

    List<IncidentResponseDto> bulkAssignIncidents(List<Long> incidentIds, String assignedToEmail, String actorEmail);

    List<IncidentResponseDto> bulkUnassignIncidents(List<Long> incidentIds, String actorEmail);

    List<IncidentResponseDto> bulkUpdateStatus(List<Long> incidentIds, IncidentStatus status, String actorEmail);

    IncidentResponseDto deleteIncidentById(Long incidentId, String actorEmail);
}
