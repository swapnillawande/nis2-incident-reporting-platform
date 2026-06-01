package com.nisync.incident.timeline.service.impl;

import com.nisync.audit.entity.AuditLog;
import com.nisync.audit.repository.AuditLogRepository;
import com.nisync.common.exception.ResourceNotFoundException;
import com.nisync.incident.note.entity.IncidentNote;
import com.nisync.incident.note.repository.IncidentNoteRepository;
import com.nisync.incident.repository.IncidentRepository;
import com.nisync.incident.timeline.dto.IncidentTimelineItemDto;
import com.nisync.incident.timeline.service.IncidentTimelineService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

@Service
public class IncidentTimelineServiceImpl implements IncidentTimelineService {

    private static final String INCIDENT_RESOURCE_TYPE = "INCIDENT";
    private static final String NOTE_TYPE = "NOTE";
    private static final String AUDIT_TYPE = "AUDIT";

    @Autowired
    private IncidentRepository incidentRepository;

    @Autowired
    private IncidentNoteRepository incidentNoteRepository;

    @Autowired
    private AuditLogRepository auditLogRepository;

    @Override
    public List<IncidentTimelineItemDto> getTimelineByIncidentId(Long incidentId) {
        if (!incidentRepository.existsById(incidentId)) {
            throw new ResourceNotFoundException("Incident not found with id: " + incidentId);
        }

        List<IncidentTimelineItemDto> noteItems = incidentNoteRepository
                .findByIncidentIdOrderByCreatedAtDesc(incidentId)
                .stream()
                .map(this::toNoteItem)
                .toList();

        List<IncidentTimelineItemDto> auditItems = auditLogRepository
                .findByResourceTypeAndResourceIdOrderByCreatedAtDesc(
                        INCIDENT_RESOURCE_TYPE,
                        String.valueOf(incidentId)
                )
                .stream()
                .map(this::toAuditItem)
                .toList();

        return Stream.concat(noteItems.stream(), auditItems.stream())
                .sorted(Comparator.comparing(IncidentTimelineItemDto::getCreatedAt).reversed())
                .toList();
    }

    private IncidentTimelineItemDto toNoteItem(IncidentNote note) {
        IncidentTimelineItemDto item = new IncidentTimelineItemDto();
        item.setType(NOTE_TYPE);
        item.setId(note.getId());
        item.setActorEmail(note.getCreatedByEmail());
        item.setNote(note.getNote());
        item.setCreatedAt(note.getCreatedAt());

        return item;
    }

    private IncidentTimelineItemDto toAuditItem(AuditLog auditLog) {
        IncidentTimelineItemDto item = new IncidentTimelineItemDto();
        item.setType(AUDIT_TYPE);
        item.setId(auditLog.getId());
        item.setAction(auditLog.getAction());
        item.setActorEmail(auditLog.getActorEmail());
        item.setDetails(auditLog.getDetails());
        item.setCreatedAt(auditLog.getCreatedAt());

        return item;
    }
}
