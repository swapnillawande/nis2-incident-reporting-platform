package com.nisync.incident.service.impl;

import com.nisync.common.exception.ResourceNotFoundException;
import com.nisync.incident.dto.CreateIncidentRequestDto;
import com.nisync.incident.dto.IncidentMapperDto;
import com.nisync.incident.dto.IncidentResponseDto;
import com.nisync.incident.dto.UpdateIncidentRequestDto;
import com.nisync.incident.entity.Incident;
import com.nisync.incident.enums.IncidentSeverity;
import com.nisync.incident.enums.IncidentStatus;
import com.nisync.incident.repository.IncidentRepository;
import com.nisync.incident.service.IncidentService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class IncidentServiceImpl implements IncidentService {

    private static final Logger logger = LoggerFactory.getLogger(IncidentServiceImpl.class);

    @Autowired
    private IncidentRepository incidentRepository;

    @Override
    public IncidentResponseDto createIncident(CreateIncidentRequestDto request, String reportedByEmail) {
        logger.info("Creating incident. title: {}, reportedBy: {}", request.getTitle(), reportedByEmail);

        Incident incident = new Incident();
        incident.setTitle(request.getTitle());
        incident.setDescription(request.getDescription());
        incident.setSeverity(request.getSeverity());
        incident.setStatus(IncidentStatus.OPEN);
        incident.setReportedByEmail(reportedByEmail);

        Incident savedIncident = incidentRepository.save(incident);

        logger.info("Incident created successfully. incidentId: {}", savedIncident.getId());

        return IncidentMapperDto.toResponse(savedIncident);
    }

    @Override
    public List<IncidentResponseDto> getIncidents(IncidentStatus status, IncidentSeverity severity) {
        logger.info("Fetching incidents. status: {}, severity: {}", status, severity);

        return findIncidents(status, severity)
                .stream()
                .map(IncidentMapperDto::toResponse)
                .toList();
    }

    @Override
    public IncidentResponseDto getIncidentById(Long incidentId) {
        logger.info("Fetching incident by id: {}", incidentId);

        Incident incident = findIncidentOrThrow(incidentId);

        return IncidentMapperDto.toResponse(incident);
    }

    @Override
    public IncidentResponseDto updateIncidentById(Long incidentId, UpdateIncidentRequestDto request) {
        logger.info("Updating incident by id: {}", incidentId);

        Incident incident = findIncidentOrThrow(incidentId);

        if (request.getTitle() != null) {
            incident.setTitle(request.getTitle());
        }

        if (request.getDescription() != null) {
            incident.setDescription(request.getDescription());
        }

        if (request.getSeverity() != null) {
            incident.setSeverity(request.getSeverity());
        }

        if (request.getStatus() != null) {
            incident.setStatus(request.getStatus());
        }

        Incident savedIncident = incidentRepository.save(incident);

        logger.info("Incident updated successfully. incidentId: {}", savedIncident.getId());

        return IncidentMapperDto.toResponse(savedIncident);
    }

    @Override
    public IncidentResponseDto deleteIncidentById(Long incidentId) {
        logger.info("Deleting incident by id: {}", incidentId);

        Incident incident = findIncidentOrThrow(incidentId);
        IncidentResponseDto response = IncidentMapperDto.toResponse(incident);

        incidentRepository.delete(incident);

        logger.info("Incident deleted successfully. incidentId: {}", incidentId);

        return response;
    }

    private Incident findIncidentOrThrow(Long incidentId) {
        return incidentRepository.findById(incidentId)
                .orElseThrow(() -> {
                    logger.warn("Incident not found with id: {}", incidentId);
                    return new ResourceNotFoundException("Incident not found with id: " + incidentId);
                });
    }

    private List<Incident> findIncidents(IncidentStatus status, IncidentSeverity severity) {
        if (status != null && severity != null) {
            return incidentRepository.findByStatusAndSeverity(status, severity);
        }

        if (status != null) {
            return incidentRepository.findByStatus(status);
        }

        if (severity != null) {
            return incidentRepository.findBySeverity(severity);
        }

        return incidentRepository.findAll();
    }
}
