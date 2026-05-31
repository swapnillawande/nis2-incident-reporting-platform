package com.nisync.incident.repository;

import com.nisync.incident.entity.Incident;
import com.nisync.incident.enums.IncidentStatus;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.time.LocalDateTime;
import java.util.Collection;

public interface IncidentRepository extends JpaRepository<Incident, Long>, JpaSpecificationExecutor<Incident> {

    long countByStatus(IncidentStatus status);

    long countByDueAtBeforeAndStatusIn(LocalDateTime dueAt, Collection<IncidentStatus> statuses);

    long countByDueAtBetweenAndStatusIn(
            LocalDateTime start,
            LocalDateTime end,
            Collection<IncidentStatus> statuses);

    long countByDueAtIsNullAndStatusIn(Collection<IncidentStatus> statuses);
}
