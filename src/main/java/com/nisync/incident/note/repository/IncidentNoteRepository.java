package com.nisync.incident.note.repository;

import com.nisync.incident.note.entity.IncidentNote;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface IncidentNoteRepository extends JpaRepository<IncidentNote, Long> {

    List<IncidentNote> findByIncidentIdOrderByCreatedAtDesc(Long incidentId);
}
