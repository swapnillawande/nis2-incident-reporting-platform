package com.nisync.incident.repository;

import com.nisync.incident.entity.Incident;
import com.nisync.incident.enums.IncidentStatus;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface IncidentRepository extends JpaRepository<Incident, Long>, JpaSpecificationExecutor<Incident> {

    long countByStatus(IncidentStatus status);
}
