package com.nisync.incident.dto;

import com.nisync.incident.enums.IncidentSeverity;
import com.nisync.incident.enums.IncidentStatus;

import jakarta.validation.constraints.Size;

public class UpdateIncidentRequestDto {

    @Size(min = 3, max = 160, message = "Title must be between 3 and 160 characters")
    private String title;

    @Size(min = 10, max = 2000, message = "Description must be between 10 and 2000 characters")
    private String description;

    private IncidentSeverity severity;

    private IncidentStatus status;

    @Size(max = 150, message = "Assigned user email must be at most 150 characters")
    private String assignedToEmail;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public IncidentSeverity getSeverity() {
        return severity;
    }

    public void setSeverity(IncidentSeverity severity) {
        this.severity = severity;
    }

    public IncidentStatus getStatus() {
        return status;
    }

    public void setStatus(IncidentStatus status) {
        this.status = status;
    }

    public String getAssignedToEmail() {
        return assignedToEmail;
    }

    public void setAssignedToEmail(String assignedToEmail) {
        this.assignedToEmail = assignedToEmail;
    }
}
