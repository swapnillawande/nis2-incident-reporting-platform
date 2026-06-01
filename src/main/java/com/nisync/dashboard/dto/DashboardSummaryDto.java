package com.nisync.dashboard.dto;

import java.util.List;

public class DashboardSummaryDto {

    private long totalUsers;
    private long activeUsers;
    private long inactiveUsers;
    private long suspendedUsers;
    private long totalAuditLogs;
    private long adminUsers;
    private long securityAnalystUsers;
    private long complianceOfficerUsers;
    private long auditorUsers;
    private long totalIncidents;
    private long openIncidents;
    private long inProgressIncidents;
    private long resolvedIncidents;
    private long closedIncidents;
    private long lowSeverityIncidents;
    private long mediumSeverityIncidents;
    private long highSeverityIncidents;
    private long criticalSeverityIncidents;
    private long overdueIncidents;
    private long dueSoonIncidents;
    private long unscheduledActiveIncidents;
    private long assignedActiveIncidents;
    private long unassignedActiveIncidents;
    private List<DashboardTrendPointDto> incidentTrend;
    private List<DashboardTrendPointDto> auditTrend;

    public DashboardSummaryDto() {
    }

    public DashboardSummaryDto(
            long totalUsers,
            long activeUsers,
            long inactiveUsers,
            long suspendedUsers,
            long totalAuditLogs,
            long adminUsers,
            long securityAnalystUsers,
            long complianceOfficerUsers,
            long auditorUsers,
            long totalIncidents,
            long openIncidents,
            long inProgressIncidents,
            long resolvedIncidents,
            long closedIncidents,
            long lowSeverityIncidents,
            long mediumSeverityIncidents,
            long highSeverityIncidents,
            long criticalSeverityIncidents,
            long overdueIncidents,
            long dueSoonIncidents,
            long unscheduledActiveIncidents,
            long assignedActiveIncidents,
            long unassignedActiveIncidents,
            List<DashboardTrendPointDto> incidentTrend,
            List<DashboardTrendPointDto> auditTrend) {
        this.totalUsers = totalUsers;
        this.activeUsers = activeUsers;
        this.inactiveUsers = inactiveUsers;
        this.suspendedUsers = suspendedUsers;
        this.totalAuditLogs = totalAuditLogs;
        this.adminUsers = adminUsers;
        this.securityAnalystUsers = securityAnalystUsers;
        this.complianceOfficerUsers = complianceOfficerUsers;
        this.auditorUsers = auditorUsers;
        this.totalIncidents = totalIncidents;
        this.openIncidents = openIncidents;
        this.inProgressIncidents = inProgressIncidents;
        this.resolvedIncidents = resolvedIncidents;
        this.closedIncidents = closedIncidents;
        this.lowSeverityIncidents = lowSeverityIncidents;
        this.mediumSeverityIncidents = mediumSeverityIncidents;
        this.highSeverityIncidents = highSeverityIncidents;
        this.criticalSeverityIncidents = criticalSeverityIncidents;
        this.overdueIncidents = overdueIncidents;
        this.dueSoonIncidents = dueSoonIncidents;
        this.unscheduledActiveIncidents = unscheduledActiveIncidents;
        this.assignedActiveIncidents = assignedActiveIncidents;
        this.unassignedActiveIncidents = unassignedActiveIncidents;
        this.incidentTrend = incidentTrend;
        this.auditTrend = auditTrend;
    }

    public long getTotalUsers() {
        return totalUsers;
    }

    public void setTotalUsers(long totalUsers) {
        this.totalUsers = totalUsers;
    }

    public long getActiveUsers() {
        return activeUsers;
    }

    public void setActiveUsers(long activeUsers) {
        this.activeUsers = activeUsers;
    }

    public long getInactiveUsers() {
        return inactiveUsers;
    }

    public void setInactiveUsers(long inactiveUsers) {
        this.inactiveUsers = inactiveUsers;
    }

    public long getSuspendedUsers() {
        return suspendedUsers;
    }

    public void setSuspendedUsers(long suspendedUsers) {
        this.suspendedUsers = suspendedUsers;
    }

    public long getTotalAuditLogs() {
        return totalAuditLogs;
    }

    public void setTotalAuditLogs(long totalAuditLogs) {
        this.totalAuditLogs = totalAuditLogs;
    }

    public long getAdminUsers() {
        return adminUsers;
    }

    public void setAdminUsers(long adminUsers) {
        this.adminUsers = adminUsers;
    }

    public long getSecurityAnalystUsers() {
        return securityAnalystUsers;
    }

    public void setSecurityAnalystUsers(long securityAnalystUsers) {
        this.securityAnalystUsers = securityAnalystUsers;
    }

    public long getComplianceOfficerUsers() {
        return complianceOfficerUsers;
    }

    public void setComplianceOfficerUsers(long complianceOfficerUsers) {
        this.complianceOfficerUsers = complianceOfficerUsers;
    }

    public long getAuditorUsers() {
        return auditorUsers;
    }

    public void setAuditorUsers(long auditorUsers) {
        this.auditorUsers = auditorUsers;
    }

    public long getTotalIncidents() {
        return totalIncidents;
    }

    public void setTotalIncidents(long totalIncidents) {
        this.totalIncidents = totalIncidents;
    }

    public long getOpenIncidents() {
        return openIncidents;
    }

    public void setOpenIncidents(long openIncidents) {
        this.openIncidents = openIncidents;
    }

    public long getInProgressIncidents() {
        return inProgressIncidents;
    }

    public void setInProgressIncidents(long inProgressIncidents) {
        this.inProgressIncidents = inProgressIncidents;
    }

    public long getResolvedIncidents() {
        return resolvedIncidents;
    }

    public void setResolvedIncidents(long resolvedIncidents) {
        this.resolvedIncidents = resolvedIncidents;
    }

    public long getClosedIncidents() {
        return closedIncidents;
    }

    public void setClosedIncidents(long closedIncidents) {
        this.closedIncidents = closedIncidents;
    }

    public long getLowSeverityIncidents() {
        return lowSeverityIncidents;
    }

    public void setLowSeverityIncidents(long lowSeverityIncidents) {
        this.lowSeverityIncidents = lowSeverityIncidents;
    }

    public long getMediumSeverityIncidents() {
        return mediumSeverityIncidents;
    }

    public void setMediumSeverityIncidents(long mediumSeverityIncidents) {
        this.mediumSeverityIncidents = mediumSeverityIncidents;
    }

    public long getHighSeverityIncidents() {
        return highSeverityIncidents;
    }

    public void setHighSeverityIncidents(long highSeverityIncidents) {
        this.highSeverityIncidents = highSeverityIncidents;
    }

    public long getCriticalSeverityIncidents() {
        return criticalSeverityIncidents;
    }

    public void setCriticalSeverityIncidents(long criticalSeverityIncidents) {
        this.criticalSeverityIncidents = criticalSeverityIncidents;
    }

    public long getOverdueIncidents() {
        return overdueIncidents;
    }

    public void setOverdueIncidents(long overdueIncidents) {
        this.overdueIncidents = overdueIncidents;
    }

    public long getDueSoonIncidents() {
        return dueSoonIncidents;
    }

    public void setDueSoonIncidents(long dueSoonIncidents) {
        this.dueSoonIncidents = dueSoonIncidents;
    }

    public long getUnscheduledActiveIncidents() {
        return unscheduledActiveIncidents;
    }

    public void setUnscheduledActiveIncidents(long unscheduledActiveIncidents) {
        this.unscheduledActiveIncidents = unscheduledActiveIncidents;
    }

    public long getAssignedActiveIncidents() {
        return assignedActiveIncidents;
    }

    public void setAssignedActiveIncidents(long assignedActiveIncidents) {
        this.assignedActiveIncidents = assignedActiveIncidents;
    }

    public long getUnassignedActiveIncidents() {
        return unassignedActiveIncidents;
    }

    public void setUnassignedActiveIncidents(long unassignedActiveIncidents) {
        this.unassignedActiveIncidents = unassignedActiveIncidents;
    }

    public List<DashboardTrendPointDto> getIncidentTrend() {
        return incidentTrend;
    }

    public void setIncidentTrend(List<DashboardTrendPointDto> incidentTrend) {
        this.incidentTrend = incidentTrend;
    }

    public List<DashboardTrendPointDto> getAuditTrend() {
        return auditTrend;
    }

    public void setAuditTrend(List<DashboardTrendPointDto> auditTrend) {
        this.auditTrend = auditTrend;
    }
}
