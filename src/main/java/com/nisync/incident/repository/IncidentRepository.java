package com.nisync.incident.repository;

import com.nisync.incident.entity.Incident;
import com.nisync.incident.enums.IncidentStatus;

import org.springframework.data.jpa.repository.JpaRepository;

public interface IncidentRepository extends JpaRepository<Incident, Long> {

    long countByStatus(IncidentStatus status);
}
