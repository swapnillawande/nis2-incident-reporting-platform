package com.nisync.incident.note.dto;

import com.nisync.incident.note.entity.IncidentNote;

public class IncidentNoteMapperDto {

    private IncidentNoteMapperDto() {
    }

    public static IncidentNoteResponseDto toResponse(IncidentNote incidentNote) {
        IncidentNoteResponseDto response = new IncidentNoteResponseDto();

        response.setId(incidentNote.getId());
        response.setIncidentId(incidentNote.getIncident().getId());
        response.setNote(incidentNote.getNote());
        response.setCreatedByEmail(incidentNote.getCreatedByEmail());
        response.setCreatedAt(incidentNote.getCreatedAt());

        return response;
    }
}
