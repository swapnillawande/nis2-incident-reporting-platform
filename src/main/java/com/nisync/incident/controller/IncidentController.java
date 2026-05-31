package com.nisync.incident.controller;

import com.nisync.incident.dto.CreateIncidentRequestDto;
import com.nisync.incident.dto.IncidentResponseDto;
import com.nisync.incident.dto.UpdateIncidentRequestDto;
import com.nisync.incident.enums.IncidentSeverity;
import com.nisync.incident.enums.IncidentStatus;
import com.nisync.incident.note.dto.CreateIncidentNoteRequestDto;
import com.nisync.incident.note.dto.IncidentNoteResponseDto;
import com.nisync.incident.note.service.IncidentNoteService;
import com.nisync.incident.service.IncidentService;

import jakarta.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("incidents")
public class IncidentController {

    private static final Logger logger = LoggerFactory.getLogger(IncidentController.class);

    @Autowired
    private IncidentService incidentService;

    @Autowired
    private IncidentNoteService incidentNoteService;

    @PostMapping
    public IncidentResponseDto createIncident(
            @Valid @RequestBody CreateIncidentRequestDto request,
            Authentication authentication) {

        logger.info("POST /incidents called by {}", authentication.getName());

        return incidentService.createIncident(request, authentication.getName());
    }

    @GetMapping
    public List<IncidentResponseDto> getIncidents(
            @RequestParam(name = "status", required = false) IncidentStatus status,
            @RequestParam(name = "severity", required = false) IncidentSeverity severity,
            @RequestParam(name = "q", required = false) String query) {

        logger.info("GET /incidents called. status: {}, severity: {}, query: {}", status, severity, query);

        return incidentService.getIncidents(status, severity, query);
    }

    @GetMapping("/{incidentId}")
    public IncidentResponseDto getIncidentById(@PathVariable("incidentId") Long incidentId) {
        logger.info("GET /incidents/{} called", incidentId);

        return incidentService.getIncidentById(incidentId);
    }

    @PutMapping("/{incidentId}")
    public IncidentResponseDto updateIncidentById(
            @PathVariable("incidentId") Long incidentId,
            @Valid @RequestBody UpdateIncidentRequestDto request,
            Authentication authentication) {

        logger.info("PUT /incidents/{} called by {}", incidentId, authentication.getName());

        return incidentService.updateIncidentById(incidentId, request, authentication.getName());
    }

    @DeleteMapping("/{incidentId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SECURITY_ANALYST')")
    public IncidentResponseDto deleteIncidentById(
            @PathVariable("incidentId") Long incidentId,
            Authentication authentication) {
        logger.info("DELETE /incidents/{} called by {}", incidentId, authentication.getName());

        return incidentService.deleteIncidentById(incidentId, authentication.getName());
    }

    @GetMapping("/{incidentId}/notes")
    public List<IncidentNoteResponseDto> getIncidentNotes(@PathVariable("incidentId") Long incidentId) {
        logger.info("GET /incidents/{}/notes called", incidentId);

        return incidentNoteService.getNotesByIncidentId(incidentId);
    }

    @PostMapping("/{incidentId}/notes")
    public IncidentNoteResponseDto addIncidentNote(
            @PathVariable("incidentId") Long incidentId,
            @Valid @RequestBody CreateIncidentNoteRequestDto request,
            Authentication authentication) {

        logger.info("POST /incidents/{}/notes called by {}", incidentId, authentication.getName());

        return incidentNoteService.addNote(incidentId, request, authentication.getName());
    }
}
