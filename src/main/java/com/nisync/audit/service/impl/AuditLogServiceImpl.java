package com.nisync.audit.service.impl;

import com.nisync.audit.dto.AuditLogMapperDto;
import com.nisync.audit.dto.AuditLogResponseDto;
import com.nisync.audit.entity.AuditLog;
import com.nisync.audit.repository.AuditLogRepository;
import com.nisync.audit.service.AuditLogService;
import com.nisync.common.response.PagedResponseDto;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class AuditLogServiceImpl implements AuditLogService {

    @Autowired
    private AuditLogRepository auditLogRepository;

    @Override
    public AuditLogResponseDto record(
            String action,
            String resourceType,
            Object resourceId,
            String actorEmail,
            String details) {

        AuditLog auditLog = new AuditLog();
        auditLog.setAction(action);
        auditLog.setResourceType(resourceType);
        auditLog.setResourceId(resourceId == null ? null : String.valueOf(resourceId));
        auditLog.setActorEmail(actorEmail == null || actorEmail.isBlank() ? "system" : actorEmail);
        auditLog.setDetails(details);

        return AuditLogMapperDto.toResponse(auditLogRepository.save(auditLog));
    }

    @Override
    public PagedResponseDto<AuditLogResponseDto> getRecentAuditLogs(
            String action,
            String resourceType,
            String query,
            int page,
            int size,
            String sortBy,
            String sortDir) {
        Page<AuditLogResponseDto> auditLogs = auditLogRepository.findAll(
                buildAuditLogSpecification(action, resourceType, query),
                        PageRequest.of(normalizePage(page), normalizeSize(size), buildSort(sortBy, sortDir))
                )
                .map(AuditLogMapperDto::toResponse);

        return PagedResponseDto.fromPage(auditLogs);
    }

    @Override
    public String exportAuditLogsCsv(String action, String resourceType, String query) {
        List<AuditLog> auditLogs = auditLogRepository.findAll(
                buildAuditLogSpecification(action, resourceType, query),
                Sort.by(Sort.Direction.DESC, "createdAt")
        );

        return buildAuditLogsCsv(auditLogs);
    }

    private Specification<AuditLog> buildAuditLogSpecification(String action, String resourceType, String query) {
        return (root, criteriaQuery, criteriaBuilder) -> {
            var predicate = criteriaBuilder.conjunction();

            if (action != null && !action.isBlank()) {
                predicate = criteriaBuilder.and(
                        predicate,
                        criteriaBuilder.equal(root.get("action"), action.trim())
                );
            }

            if (resourceType != null && !resourceType.isBlank()) {
                predicate = criteriaBuilder.and(
                        predicate,
                        criteriaBuilder.equal(root.get("resourceType"), resourceType.trim())
                );
            }

            if (query != null && !query.isBlank()) {
                String searchTerm = "%" + query.trim().toLowerCase() + "%";
                var actorPredicate = criteriaBuilder.like(criteriaBuilder.lower(root.get("actorEmail")), searchTerm);
                var detailsPredicate = criteriaBuilder.like(criteriaBuilder.lower(root.get("details")), searchTerm);
                var resourceIdPredicate = criteriaBuilder.like(criteriaBuilder.lower(root.get("resourceId")), searchTerm);
                predicate = criteriaBuilder.and(
                        predicate,
                        criteriaBuilder.or(actorPredicate, detailsPredicate, resourceIdPredicate)
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
            case "action" -> "action";
            case "resourceType" -> "resourceType";
            case "actorEmail" -> "actorEmail";
            default -> "createdAt";
        };
        Sort.Direction direction = "asc".equalsIgnoreCase(sortDir) ? Sort.Direction.ASC : Sort.Direction.DESC;

        return Sort.by(direction, sortProperty);
    }

    private String normalizeSortKey(String sortBy) {
        return sortBy == null ? "" : sortBy.trim();
    }

    private String buildAuditLogsCsv(List<AuditLog> auditLogs) {
        StringBuilder csv = new StringBuilder();
        csv.append("ID,Action,Resource Type,Resource ID,Actor Email,Details,Created At\n");

        auditLogs.forEach(auditLog -> csv.append(toCsvRow(Arrays.asList(
                auditLog.getId(),
                auditLog.getAction(),
                auditLog.getResourceType(),
                auditLog.getResourceId(),
                auditLog.getActorEmail(),
                auditLog.getDetails(),
                auditLog.getCreatedAt()
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
