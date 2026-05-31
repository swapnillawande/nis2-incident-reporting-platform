package com.nisync.incident.repository;

import com.nisync.incident.entity.Incident;

import org.springframework.data.jpa.repository.JpaRepository;

public interface IncidentRepository extends JpaRepository<Incident, Long> {
}
