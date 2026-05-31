package com.nisync.audit.service.impl;

import com.nisync.audit.dto.AuditLogMapperDto;
import com.nisync.audit.dto.AuditLogResponseDto;
import com.nisync.audit.entity.AuditLog;
import com.nisync.audit.repository.AuditLogRepository;
import com.nisync.audit.service.AuditLogService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;

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
    public List<AuditLogResponseDto> getRecentAuditLogs(String action, String resourceType, String query) {
        return auditLogRepository.findAll(
                buildAuditLogSpecification(action, resourceType, query),
                        PageRequest.of(0, 100, Sort.by(Sort.Direction.DESC, "createdAt"))
                )
                .stream()
                .map(AuditLogMapperDto::toResponse)
                .toList();
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
}
