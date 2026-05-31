package com.nisync.incident.repository;

import com.nisync.incident.entity.Incident;
import com.nisync.incident.enums.IncidentStatus;

import org.springframework.data.jpa.repository.JpaRepository;

import com.nisync.incident.enums.IncidentSeverity;

import java.util.List;

public interface IncidentRepository extends JpaRepository<Incident, Long> {

    long countByStatus(IncidentStatus status);

    List<Incident> findByStatus(IncidentStatus status);

    List<Incident> findBySeverity(IncidentSeverity severity);

    List<Incident> findByStatusAndSeverity(IncidentStatus status, IncidentSeverity severity);
}
