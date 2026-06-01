package com.nisync.incident.controller;

import com.nisync.common.response.PagedResponseDto;
import com.nisync.incident.dto.AssignIncidentRequestDto;
import com.nisync.incident.dto.BulkIncidentStatusUpdateRequestDto;
import com.nisync.incident.dto.CreateIncidentRequestDto;
import com.nisync.incident.dto.IncidentResponseDto;
import com.nisync.incident.dto.UpdateIncidentRequestDto;
import com.nisync.incident.enums.IncidentSeverity;
import com.nisync.incident.enums.IncidentStatus;
import com.nisync.incident.note.dto.CreateIncidentNoteRequestDto;
import com.nisync.incident.note.dto.IncidentNoteResponseDto;
import com.nisync.incident.note.service.IncidentNoteService;
import com.nisync.incident.service.IncidentService;
import com.nisync.incident.timeline.dto.IncidentTimelineItemDto;
import com.nisync.incident.timeline.service.IncidentTimelineService;

import jakarta.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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
import java.time.LocalDateTime;

@RestController
@RequestMapping("incidents")
public class IncidentController {

    private static final Logger logger = LoggerFactory.getLogger(IncidentController.class);

    @Autowired
    private IncidentService incidentService;

    @Autowired
    private IncidentNoteService incidentNoteService;

    @Autowired
    private IncidentTimelineService incidentTimelineService;

    @PostMapping
    public IncidentResponseDto createIncident(
            @Valid @RequestBody CreateIncidentRequestDto request,
            Authentication authentication) {

        logger.info("POST /incidents called by {}", authentication.getName());

        return incidentService.createIncident(request, authentication.getName());
    }

    @GetMapping
    public PagedResponseDto<IncidentResponseDto> getIncidents(
            @RequestParam(name = "status", required = false) IncidentStatus status,
            @RequestParam(name = "severity", required = false) IncidentSeverity severity,
            @RequestParam(name = "assignedToEmail", required = false) String assignedToEmail,
            @RequestParam(name = "q", required = false) String query,
            @RequestParam(name = "createdFrom", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime createdFrom,
            @RequestParam(name = "createdTo", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime createdTo,
            @RequestParam(name = "dueFrom", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dueFrom,
            @RequestParam(name = "dueTo", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dueTo,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size,
            @RequestParam(name = "sortBy", defaultValue = "createdAt") String sortBy,
            @RequestParam(name = "sortDir", defaultValue = "desc") String sortDir) {

        logger.info(
                "GET /incidents called. status: {}, severity: {}, assignedToEmail: {}, query: {}, createdFrom: {}, createdTo: {}, dueFrom: {}, dueTo: {}, page: {}, size: {}, sortBy: {}, sortDir: {}",
                status,
                severity,
                assignedToEmail,
                query,
                createdFrom,
                createdTo,
                dueFrom,
                dueTo,
                page,
                size,
                sortBy,
                sortDir
        );

        return incidentService.getIncidents(
                status,
                severity,
                assignedToEmail,
                query,
                createdFrom,
                createdTo,
                dueFrom,
                dueTo,
                page,
                size,
                sortBy,
                sortDir
        );
    }

    @GetMapping("/export")
    public ResponseEntity<String> exportIncidents(
            @RequestParam(name = "status", required = false) IncidentStatus status,
            @RequestParam(name = "severity", required = false) IncidentSeverity severity,
            @RequestParam(name = "assignedToEmail", required = false) String assignedToEmail,
            @RequestParam(name = "q", required = false) String query,
            @RequestParam(name = "createdFrom", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime createdFrom,
            @RequestParam(name = "createdTo", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime createdTo,
            @RequestParam(name = "dueFrom", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dueFrom,
            @RequestParam(name = "dueTo", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dueTo,
            Authentication authentication) {

        logger.info(
                "GET /incidents/export called by {}. status: {}, severity: {}, assignedToEmail: {}, query: {}, createdFrom: {}, createdTo: {}, dueFrom: {}, dueTo: {}",
                authentication.getName(),
                status,
                severity,
                assignedToEmail,
                query,
                createdFrom,
                createdTo,
                dueFrom,
                dueTo
        );

        String csv = incidentService.exportIncidentsCsv(
                status,
                severity,
                assignedToEmail,
                query,
                createdFrom,
                createdTo,
                dueFrom,
                dueTo,
                authentication.getName()
        );

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=incidents-export.csv")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(csv);
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

    @PutMapping("/{incidentId}/assignment")
    public IncidentResponseDto assignIncident(
            @PathVariable("incidentId") Long incidentId,
            @Valid @RequestBody AssignIncidentRequestDto request,
            Authentication authentication) {

        logger.info(
                "PUT /incidents/{}/assignment called by {}. assignedToEmail: {}",
                incidentId,
                authentication.getName(),
                request.getAssignedToEmail()
        );

        return incidentService.assignIncident(
                incidentId,
                request.getAssignedToEmail(),
                authentication.getName()
        );
    }

    @PutMapping("/{incidentId}/assignment/me")
    public IncidentResponseDto assignIncidentToMe(
            @PathVariable("incidentId") Long incidentId,
            Authentication authentication) {

        logger.info("PUT /incidents/{}/assignment/me called by {}", incidentId, authentication.getName());

        return incidentService.assignIncident(
                incidentId,
                authentication.getName(),
                authentication.getName()
        );
    }

    @DeleteMapping("/{incidentId}/assignment")
    public IncidentResponseDto unassignIncident(
            @PathVariable("incidentId") Long incidentId,
            Authentication authentication) {

        logger.info("DELETE /incidents/{}/assignment called by {}", incidentId, authentication.getName());

        return incidentService.unassignIncident(incidentId, authentication.getName());
    }

    @PutMapping("/bulk-status")
    public List<IncidentResponseDto> bulkUpdateIncidentStatus(
            @Valid @RequestBody BulkIncidentStatusUpdateRequestDto request,
            Authentication authentication) {

        logger.info(
                "PUT /incidents/bulk-status called by {}. count: {}, status: {}",
                authentication.getName(),
                request.getIncidentIds().size(),
                request.getStatus()
        );

        return incidentService.bulkUpdateStatus(
                request.getIncidentIds(),
                request.getStatus(),
                authentication.getName()
        );
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

    @GetMapping("/{incidentId}/timeline")
    public List<IncidentTimelineItemDto> getIncidentTimeline(@PathVariable("incidentId") Long incidentId) {
        logger.info("GET /incidents/{}/timeline called", incidentId);

        return incidentTimelineService.getTimelineByIncidentId(incidentId);
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
