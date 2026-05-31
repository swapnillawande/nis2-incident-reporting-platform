package com.nisync.incident.note.service;

import com.nisync.incident.note.dto.CreateIncidentNoteRequestDto;
import com.nisync.incident.note.dto.IncidentNoteResponseDto;

import java.util.List;

public interface IncidentNoteService {

    IncidentNoteResponseDto addNote(Long incidentId, CreateIncidentNoteRequestDto request, String createdByEmail);

    List<IncidentNoteResponseDto> getNotesByIncidentId(Long incidentId);
}
