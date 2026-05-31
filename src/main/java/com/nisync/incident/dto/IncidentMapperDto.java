package com.nisync.incident.dto;

import com.nisync.incident.entity.Incident;

public class IncidentMapperDto {

    private IncidentMapperDto() {
    }

    public static IncidentResponseDto toResponse(Incident incident) {
        IncidentResponseDto response = new IncidentResponseDto();

        response.setId(incident.getId());
        response.setTitle(incident.getTitle());
        response.setDescription(incident.getDescription());
        response.setSeverity(incident.getSeverity());
        response.setStatus(incident.getStatus());
        response.setReportedByEmail(incident.getReportedByEmail());
        response.setAssignedToEmail(incident.getAssignedToEmail());
        response.setDueAt(incident.getDueAt());
        response.setCreatedAt(incident.getCreatedAt());
        response.setUpdatedAt(incident.getUpdatedAt());

        return response;
    }
}
