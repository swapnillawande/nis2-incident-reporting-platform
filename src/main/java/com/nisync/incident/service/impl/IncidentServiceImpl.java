package com.nisync.incident.service.impl;

import com.nisync.common.exception.ResourceNotFoundException;
import com.nisync.common.response.PagedResponseDto;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

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
    public PagedResponseDto<IncidentResponseDto> getIncidents(
            IncidentStatus status,
            IncidentSeverity severity,
            String assignedToEmail,
            String query,
            LocalDateTime createdFrom,
            LocalDateTime createdTo,
            LocalDateTime dueFrom,
            LocalDateTime dueTo,
            int page,
            int size,
            String sortBy,
            String sortDir) {
        logger.info(
                "Fetching incidents. status: {}, severity: {}, assignedToEmail: {}, query: {}, createdFrom: {}, createdTo: {}, dueFrom: {}, dueTo: {}, page: {}, size: {}, sortBy: {}, sortDir: {}",
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

        Page<IncidentResponseDto> incidents = incidentRepository.findAll(
                        buildIncidentSpecification(
                                status,
                                severity,
                                assignedToEmail,
                                query,
                                createdFrom,
                                createdTo,
                                dueFrom,
                                dueTo
                        ),
                        PageRequest.of(normalizePage(page), normalizeSize(size), buildSort(sortBy, sortDir))
                )
                .map(IncidentMapperDto::toResponse);

        return PagedResponseDto.fromPage(incidents);
    }

    @Override
    public String exportIncidentsCsv(
            IncidentStatus status,
            IncidentSeverity severity,
            String assignedToEmail,
            String query,
            LocalDateTime createdFrom,
            LocalDateTime createdTo,
            LocalDateTime dueFrom,
            LocalDateTime dueTo,
            String actorEmail) {

        logger.info(
                "Exporting incidents CSV. status: {}, severity: {}, assignedToEmail: {}, query: {}, createdFrom: {}, createdTo: {}, dueFrom: {}, dueTo: {}, actor: {}",
                status,
                severity,
                assignedToEmail,
                query,
                createdFrom,
                createdTo,
                dueFrom,
                dueTo,
                actorEmail
        );

        List<Incident> incidents = incidentRepository.findAll(
                buildIncidentSpecification(status, severity, assignedToEmail, query, createdFrom, createdTo, dueFrom, dueTo),
                Sort.by(Sort.Direction.DESC, "createdAt")
        );

        auditLogService.record(
                "INCIDENTS_EXPORTED",
                "INCIDENT",
                null,
                actorEmail,
                "Incidents exported to CSV. Count: " + incidents.size()
        );

        return buildIncidentCsv(incidents);
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
    public IncidentResponseDto assignIncident(Long incidentId, String assignedToEmail, String actorEmail) {
        String normalizedAssignedToEmail = normalizeEmail(assignedToEmail);

        logger.info(
                "Assigning incident by id: {} to {} by {}",
                incidentId,
                normalizedAssignedToEmail,
                actorEmail
        );

        Incident incident = findIncidentOrThrow(incidentId);
        incident.setAssignedToEmail(normalizedAssignedToEmail);

        Incident savedIncident = incidentRepository.save(incident);

        auditLogService.record(
                "INCIDENT_ASSIGNED",
                "INCIDENT",
                savedIncident.getId(),
                actorEmail,
                "Incident assigned to " + normalizedAssignedToEmail + ": " + savedIncident.getTitle()
        );

        return IncidentMapperDto.toResponse(savedIncident);
    }

    @Override
    public IncidentResponseDto unassignIncident(Long incidentId, String actorEmail) {
        logger.info("Unassigning incident by id: {} by {}", incidentId, actorEmail);

        Incident incident = findIncidentOrThrow(incidentId);
        incident.setAssignedToEmail(null);

        Incident savedIncident = incidentRepository.save(incident);

        auditLogService.record(
                "INCIDENT_UNASSIGNED",
                "INCIDENT",
                savedIncident.getId(),
                actorEmail,
                "Incident unassigned: " + savedIncident.getTitle()
        );

        return IncidentMapperDto.toResponse(savedIncident);
    }

    @Override
    public List<IncidentResponseDto> bulkAssignIncidents(
            List<Long> incidentIds,
            String assignedToEmail,
            String actorEmail) {

        String normalizedAssignedToEmail = normalizeEmail(assignedToEmail);
        logger.info(
                "Bulk assigning incidents. count: {}, assignedToEmail: {}, actor: {}",
                incidentIds.size(),
                normalizedAssignedToEmail,
                actorEmail
        );

        List<Incident> incidents = findDistinctIncidentsOrThrow(incidentIds, "Bulk assignment");
        incidents.forEach(incident -> incident.setAssignedToEmail(normalizedAssignedToEmail));

        List<Incident> savedIncidents = saveIncidents(incidents);

        auditLogService.record(
                "INCIDENTS_BULK_ASSIGNED",
                "INCIDENT",
                null,
                actorEmail,
                "Incidents assigned to " + normalizedAssignedToEmail + ". Count: " + savedIncidents.size()
        );

        return savedIncidents.stream()
                .map(IncidentMapperDto::toResponse)
                .toList();
    }

    @Override
    public List<IncidentResponseDto> bulkUnassignIncidents(List<Long> incidentIds, String actorEmail) {
        logger.info("Bulk unassigning incidents. count: {}, actor: {}", incidentIds.size(), actorEmail);

        List<Incident> incidents = findDistinctIncidentsOrThrow(incidentIds, "Bulk unassignment");
        incidents.forEach(incident -> incident.setAssignedToEmail(null));

        List<Incident> savedIncidents = saveIncidents(incidents);

        auditLogService.record(
                "INCIDENTS_BULK_UNASSIGNED",
                "INCIDENT",
                null,
                actorEmail,
                "Incidents unassigned. Count: " + savedIncidents.size()
        );

        return savedIncidents.stream()
                .map(IncidentMapperDto::toResponse)
                .toList();
    }

    @Override
    public List<IncidentResponseDto> bulkUpdateStatus(
            List<Long> incidentIds,
            IncidentStatus status,
            String actorEmail) {

        logger.info("Bulk updating incident status. count: {}, status: {}, actor: {}", incidentIds.size(), status, actorEmail);

        List<Incident> incidents = findDistinctIncidentsOrThrow(incidentIds, "Bulk status update");

        incidents.forEach(incident -> incident.setStatus(status));

        List<Incident> savedIncidents = saveIncidents(incidents);

        auditLogService.record(
                "INCIDENTS_BULK_STATUS_UPDATED",
                "INCIDENT",
                null,
                actorEmail,
                "Incidents status updated to " + status + ". Count: " + savedIncidents.size()
        );

        logger.info("Bulk incident status update completed. count: {}, status: {}", savedIncidents.size(), status);

        return savedIncidents.stream()
                .map(IncidentMapperDto::toResponse)
                .toList();
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

    private List<Incident> findDistinctIncidentsOrThrow(List<Long> incidentIds, String operationName) {
        List<Long> distinctIncidentIds = incidentIds.stream()
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        List<Incident> incidents = incidentRepository.findAllById(distinctIncidentIds);
        Set<Long> foundIncidentIds = incidents.stream()
                .map(Incident::getId)
                .collect(Collectors.toCollection(HashSet::new));
        List<Long> missingIncidentIds = distinctIncidentIds.stream()
                .filter(incidentId -> !foundIncidentIds.contains(incidentId))
                .toList();

        if (!missingIncidentIds.isEmpty()) {
            logger.warn("{} failed. Missing incident ids: {}", operationName, missingIncidentIds);
            throw new ResourceNotFoundException("Incidents not found with ids: " + missingIncidentIds);
        }

        return incidents;
    }

    private List<Incident> saveIncidents(List<Incident> incidents) {
        List<Incident> savedIncidents = new ArrayList<>();
        incidentRepository.saveAll(incidents).forEach(savedIncidents::add);

        return savedIncidents;
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
            String query,
            LocalDateTime createdFrom,
            LocalDateTime createdTo,
            LocalDateTime dueFrom,
            LocalDateTime dueTo) {

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

            if (createdFrom != null) {
                predicate = criteriaBuilder.and(
                        predicate,
                        criteriaBuilder.greaterThanOrEqualTo(root.get("createdAt"), createdFrom)
                );
            }

            if (createdTo != null) {
                predicate = criteriaBuilder.and(
                        predicate,
                        criteriaBuilder.lessThanOrEqualTo(root.get("createdAt"), createdTo)
                );
            }

            if (dueFrom != null) {
                predicate = criteriaBuilder.and(
                        predicate,
                        criteriaBuilder.greaterThanOrEqualTo(root.get("dueAt"), dueFrom)
                );
            }

            if (dueTo != null) {
                predicate = criteriaBuilder.and(
                        predicate,
                        criteriaBuilder.lessThanOrEqualTo(root.get("dueAt"), dueTo)
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

    private int normalizePage(int page) {
        return Math.max(page, 0);
    }

    private int normalizeSize(int size) {
        if (size < 1) {
            return 10;
        }

        return Math.min(size, 100);
    }

    private Sort buildSort(String sortBy, String sortDir) {
        String sortProperty = switch (normalizeSortKey(sortBy)) {
            case "title" -> "title";
            case "severity" -> "severity";
            case "status" -> "status";
            case "reportedByEmail" -> "reportedByEmail";
            case "assignedToEmail" -> "assignedToEmail";
            case "dueAt" -> "dueAt";
            default -> "createdAt";
        };
        Sort.Direction direction = "asc".equalsIgnoreCase(sortDir) ? Sort.Direction.ASC : Sort.Direction.DESC;

        return Sort.by(direction, sortProperty);
    }

    private String normalizeSortKey(String sortBy) {
        return sortBy == null ? "" : sortBy.trim();
    }

    private String buildIncidentCsv(List<Incident> incidents) {
        StringBuilder csv = new StringBuilder();
        csv.append("ID,Title,Description,Severity,Status,Reported By,Assigned To,SLA Due,Created At,Updated At\n");

        incidents.forEach(incident -> csv.append(toCsvRow(Arrays.asList(
                incident.getId(),
                incident.getTitle(),
                incident.getDescription(),
                incident.getSeverity(),
                incident.getStatus(),
                incident.getReportedByEmail(),
                incident.getAssignedToEmail(),
                incident.getDueAt(),
                incident.getCreatedAt(),
                incident.getUpdatedAt()
        ))).append("\n"));

        return csv.toString();
    }

    private String toCsvRow(List<Object> values) {
        return values.stream()
                .map(this::escapeCsvValue)
                .collect(Collectors.joining(","));
    }

    private String escapeCsvValue(Object value) {
        String text = Objects.toString(formatCsvValue(value), "");
        boolean needsQuotes = text.contains(",") || text.contains("\"") || text.contains("\n") || text.contains("\r");

        if (!needsQuotes) {
            return text;
        }

        return "\"" + text.replace("\"", "\"\"") + "\"";
    }

    private Object formatCsvValue(Object value) {
        if (value instanceof LocalDateTime dateTime) {
            return dateTime.toString();
        }

        return value;
    }
}
