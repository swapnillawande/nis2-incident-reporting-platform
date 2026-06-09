package com.nisync.audit.dto;

import java.time.LocalDateTime;
import java.util.Map;

public class AuditLogSummaryDto {

    private long totalLogs;
    private long uniqueActors;
    private LocalDateTime latestActivityAt;
    private Map<String, Long> actionCounts;
    private Map<String, Long> resourceTypeCounts;

    public AuditLogSummaryDto() {
    }

    public AuditLogSummaryDto(
            long totalLogs,
            long uniqueActors,
            LocalDateTime latestActivityAt,
            Map<String, Long> actionCounts,
            Map<String, Long> resourceTypeCounts) {
        this.totalLogs = totalLogs;
        this.uniqueActors = uniqueActors;
        this.latestActivityAt = latestActivityAt;
        this.actionCounts = actionCounts;
        this.resourceTypeCounts = resourceTypeCounts;
    }

    public long getTotalLogs() {
        return totalLogs;
    }

    public void setTotalLogs(long totalLogs) {
        this.totalLogs = totalLogs;
    }

    public long getUniqueActors() {
        return uniqueActors;
    }

    public void setUniqueActors(long uniqueActors) {
        this.uniqueActors = uniqueActors;
    }

    public LocalDateTime getLatestActivityAt() {
        return latestActivityAt;
    }

    public void setLatestActivityAt(LocalDateTime latestActivityAt) {
        this.latestActivityAt = latestActivityAt;
    }

    public Map<String, Long> getActionCounts() {
        return actionCounts;
    }

    public void setActionCounts(Map<String, Long> actionCounts) {
        this.actionCounts = actionCounts;
    }

    public Map<String, Long> getResourceTypeCounts() {
        return resourceTypeCounts;
    }

    public void setResourceTypeCounts(Map<String, Long> resourceTypeCounts) {
        this.resourceTypeCounts = resourceTypeCounts;
    }
}
