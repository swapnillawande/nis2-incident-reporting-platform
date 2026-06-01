package com.nisync.incident.dto;

import com.nisync.incident.enums.IncidentStatus;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public class BulkIncidentStatusUpdateRequestDto {

    @NotEmpty(message = "At least one incident id is required")
    private List<Long> incidentIds;

    @NotNull(message = "Status is required")
    private IncidentStatus status;

    public List<Long> getIncidentIds() {
        return incidentIds;
    }

    public void setIncidentIds(List<Long> incidentIds) {
        this.incidentIds = incidentIds;
    }

    public IncidentStatus getStatus() {
        return status;
    }

    public void setStatus(IncidentStatus status) {
        this.status = status;
    }
}
