package com.nisync.incident.controller;

import com.nisync.incident.dto.CreateIncidentRequestDto;
import com.nisync.incident.dto.IncidentResponseDto;
import com.nisync.incident.dto.UpdateIncidentRequestDto;
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
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("incidents")
public class IncidentController {

    private static final Logger logger = LoggerFactory.getLogger(IncidentController.class);

    @Autowired
    private IncidentService incidentService;

    @PostMapping
    public IncidentResponseDto createIncident(
            @Valid @RequestBody CreateIncidentRequestDto request,
            Authentication authentication) {

        logger.info("POST /incidents called by {}", authentication.getName());

        return incidentService.createIncident(request, authentication.getName());
    }

    @GetMapping
    public List<IncidentResponseDto> getAllIncidents() {
        logger.info("GET /incidents called");

        return incidentService.getAllIncidents();
    }

    @GetMapping("/{incidentId}")
    public IncidentResponseDto getIncidentById(@PathVariable("incidentId") Long incidentId) {
        logger.info("GET /incidents/{} called", incidentId);

        return incidentService.getIncidentById(incidentId);
    }

    @PutMapping("/{incidentId}")
    public IncidentResponseDto updateIncidentById(
            @PathVariable("incidentId") Long incidentId,
            @Valid @RequestBody UpdateIncidentRequestDto request) {

        logger.info("PUT /incidents/{} called", incidentId);

        return incidentService.updateIncidentById(incidentId, request);
    }

    @DeleteMapping("/{incidentId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SECURITY_ANALYST')")
    public IncidentResponseDto deleteIncidentById(@PathVariable("incidentId") Long incidentId) {
        logger.info("DELETE /incidents/{} called", incidentId);

        return incidentService.deleteIncidentById(incidentId);
    }
}
