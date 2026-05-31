package com.nisync.incident.note.service.impl;

import com.nisync.common.exception.ResourceNotFoundException;
import com.nisync.incident.entity.Incident;
import com.nisync.incident.note.dto.CreateIncidentNoteRequestDto;
import com.nisync.incident.note.dto.IncidentNoteMapperDto;
import com.nisync.incident.note.dto.IncidentNoteResponseDto;
import com.nisync.incident.note.entity.IncidentNote;
import com.nisync.incident.note.repository.IncidentNoteRepository;
import com.nisync.incident.note.service.IncidentNoteService;
import com.nisync.incident.repository.IncidentRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class IncidentNoteServiceImpl implements IncidentNoteService {

    @Autowired
    private IncidentRepository incidentRepository;

    @Autowired
    private IncidentNoteRepository incidentNoteRepository;

    @Override
    public IncidentNoteResponseDto addNote(
            Long incidentId,
            CreateIncidentNoteRequestDto request,
            String createdByEmail) {

        Incident incident = findIncidentOrThrow(incidentId);

        IncidentNote incidentNote = new IncidentNote();
        incidentNote.setIncident(incident);
        incidentNote.setNote(request.getNote());
        incidentNote.setCreatedByEmail(createdByEmail);

        return IncidentNoteMapperDto.toResponse(incidentNoteRepository.save(incidentNote));
    }

    @Override
    public List<IncidentNoteResponseDto> getNotesByIncidentId(Long incidentId) {
        findIncidentOrThrow(incidentId);

        return incidentNoteRepository.findByIncidentIdOrderByCreatedAtDesc(incidentId)
                .stream()
                .map(IncidentNoteMapperDto::toResponse)
                .toList();
    }

    private Incident findIncidentOrThrow(Long incidentId) {
        return incidentRepository.findById(incidentId)
                .orElseThrow(() -> new ResourceNotFoundException("Incident not found with id: " + incidentId));
    }
}
