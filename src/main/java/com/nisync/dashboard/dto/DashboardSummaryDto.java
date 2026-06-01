package com.nisync.dashboard.dto;

public class DashboardSummaryDto {

    private long totalUsers;
    private long activeUsers;
    private long inactiveUsers;
    private long suspendedUsers;
    private long adminUsers;
    private long securityAnalystUsers;
    private long complianceOfficerUsers;
    private long auditorUsers;
    private long totalIncidents;
    private long openIncidents;
    private long inProgressIncidents;
    private long resolvedIncidents;
    private long closedIncidents;
    private long overdueIncidents;
    private long dueSoonIncidents;
    private long unscheduledActiveIncidents;

    public DashboardSummaryDto() {
    }

    public DashboardSummaryDto(
            long totalUsers,
            long activeUsers,
            long inactiveUsers,
            long suspendedUsers,
            long adminUsers,
            long securityAnalystUsers,
            long complianceOfficerUsers,
            long auditorUsers,
            long totalIncidents,
            long openIncidents,
            long inProgressIncidents,
            long resolvedIncidents,
            long closedIncidents,
            long overdueIncidents,
            long dueSoonIncidents,
            long unscheduledActiveIncidents) {
        this.totalUsers = totalUsers;
        this.activeUsers = activeUsers;
        this.inactiveUsers = inactiveUsers;
        this.suspendedUsers = suspendedUsers;
        this.adminUsers = adminUsers;
        this.securityAnalystUsers = securityAnalystUsers;
        this.complianceOfficerUsers = complianceOfficerUsers;
        this.auditorUsers = auditorUsers;
        this.totalIncidents = totalIncidents;
        this.openIncidents = openIncidents;
        this.inProgressIncidents = inProgressIncidents;
        this.resolvedIncidents = resolvedIncidents;
        this.closedIncidents = closedIncidents;
        this.overdueIncidents = overdueIncidents;
        this.dueSoonIncidents = dueSoonIncidents;
        this.unscheduledActiveIncidents = unscheduledActiveIncidents;
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
}
