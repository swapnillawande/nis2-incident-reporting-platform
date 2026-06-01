package com.nisync.incident.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;

public class BulkIncidentAssignmentRequestDto {

    @NotEmpty(message = "At least one incident id is required")
    private List<Long> incidentIds;

    @Email(message = "Assigned user email must be valid")
    @Size(max = 150, message = "Assigned user email must be at most 150 characters")
    private String assignedToEmail;

    public List<Long> getIncidentIds() {
        return incidentIds;
    }

    public void setIncidentIds(List<Long> incidentIds) {
        this.incidentIds = incidentIds;
    }

    public String getAssignedToEmail() {
        return assignedToEmail;
    }

    public void setAssignedToEmail(String assignedToEmail) {
        this.assignedToEmail = assignedToEmail;
    }
}
