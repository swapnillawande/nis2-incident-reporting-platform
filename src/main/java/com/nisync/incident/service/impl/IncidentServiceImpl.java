package com.nisync.incident.service.impl;

import com.nisync.common.exception.ResourceNotFoundException;
import com.nisync.audit.service.AuditLogService;
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
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class IncidentServiceImpl implements IncidentService {

    private static final Logger logger = LoggerFactory.getLogger(IncidentServiceImpl.class);

    @Autowired
    private IncidentRepository incidentRepository;

    @Autowired
    private AuditLogService auditLogService;

    @Override
    public IncidentResponseDto createIncident(CreateIncidentRequestDto request, String reportedByEmail) {
        logger.info("Creating incident. title: {}, reportedBy: {}", request.getTitle(), reportedByEmail);

        Incident incident = new Incident();
        incident.setTitle(request.getTitle());
        incident.setDescription(request.getDescription());
        incident.setSeverity(request.getSeverity());
        incident.setStatus(IncidentStatus.OPEN);
        incident.setReportedByEmail(reportedByEmail);
        incident.setAssignedToEmail(normalizeEmail(request.getAssignedToEmail()));
        incident.setDueAt(request.getDueAt());

        Incident savedIncident = incidentRepository.save(incident);

        auditLogService.record(
                "INCIDENT_CREATED",
                "INCIDENT",
                savedIncident.getId(),
                reportedByEmail,
                "Incident created: " + savedIncident.getTitle()
        );

        logger.info("Incident created successfully. incidentId: {}", savedIncident.getId());

        return IncidentMapperDto.toResponse(savedIncident);
    }

    @Override
    public List<IncidentResponseDto> getIncidents(
            IncidentStatus status,
            IncidentSeverity severity,
            String assignedToEmail,
            String query) {
        logger.info(
                "Fetching incidents. status: {}, severity: {}, assignedToEmail: {}, query: {}",
                status,
                severity,
                assignedToEmail,
                query
        );

        return incidentRepository.findAll(
                        buildIncidentSpecification(status, severity, assignedToEmail, query),
                        Sort.by(Sort.Direction.DESC, "createdAt")
                )
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
    public IncidentResponseDto updateIncidentById(Long incidentId, UpdateIncidentRequestDto request, String actorEmail) {
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

        if (request.getAssignedToEmail() != null) {
            incident.setAssignedToEmail(normalizeEmail(request.getAssignedToEmail()));
        }

        if (Boolean.TRUE.equals(request.getClearDueAt())) {
            incident.setDueAt(null);
        } else if (request.getDueAt() != null) {
            incident.setDueAt(request.getDueAt());
        }

        Incident savedIncident = incidentRepository.save(incident);

        auditLogService.record(
                "INCIDENT_UPDATED",
                "INCIDENT",
                savedIncident.getId(),
                actorEmail,
                "Incident updated: " + savedIncident.getTitle()
        );

        logger.info("Incident updated successfully. incidentId: {}", savedIncident.getId());

        return IncidentMapperDto.toResponse(savedIncident);
    }

    @Override
    public IncidentResponseDto deleteIncidentById(Long incidentId, String actorEmail) {
        logger.info("Deleting incident by id: {}", incidentId);

        Incident incident = findIncidentOrThrow(incidentId);
        IncidentResponseDto response = IncidentMapperDto.toResponse(incident);

        incidentRepository.delete(incident);

        auditLogService.record(
                "INCIDENT_DELETED",
                "INCIDENT",
                incidentId,
                actorEmail,
                "Incident deleted: " + incident.getTitle()
        );

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

    private String normalizeEmail(String email) {
        if (email == null || email.isBlank()) {
            return null;
        }

        return email.trim();
    }

    private Specification<Incident> buildIncidentSpecification(
            IncidentStatus status,
            IncidentSeverity severity,
            String assignedToEmail,
            String query) {

        return (root, criteriaQuery, criteriaBuilder) -> {
            var predicate = criteriaBuilder.conjunction();

            if (status != null) {
                predicate = criteriaBuilder.and(predicate, criteriaBuilder.equal(root.get("status"), status));
            }

            if (severity != null) {
                predicate = criteriaBuilder.and(predicate, criteriaBuilder.equal(root.get("severity"), severity));
            }

            if (assignedToEmail != null && !assignedToEmail.isBlank()) {
                predicate = criteriaBuilder.and(
                        predicate,
                        criteriaBuilder.equal(
                                criteriaBuilder.lower(root.get("assignedToEmail")),
                                assignedToEmail.trim().toLowerCase()
                        )
                );
            }

            if (query != null && !query.isBlank()) {
                String searchTerm = "%" + query.trim().toLowerCase() + "%";
                var titlePredicate = criteriaBuilder.like(criteriaBuilder.lower(root.get("title")), searchTerm);
                var descriptionPredicate = criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("description")),
                        searchTerm
                );
                var assignedToPredicate = criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("assignedToEmail")),
                        searchTerm
                );
                predicate = criteriaBuilder.and(
                        predicate,
                        criteriaBuilder.or(titlePredicate, descriptionPredicate, assignedToPredicate)
                );
            }

            return predicate;
        };
    }
}
