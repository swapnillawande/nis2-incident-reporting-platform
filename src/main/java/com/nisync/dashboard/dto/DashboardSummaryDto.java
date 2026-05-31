package com.nisync.dashboard.dto;

public class DashboardSummaryDto {

    private long totalUsers;
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
            long totalIncidents,
            long openIncidents,
            long inProgressIncidents,
            long resolvedIncidents,
            long closedIncidents,
            long overdueIncidents,
            long dueSoonIncidents,
            long unscheduledActiveIncidents) {
        this.totalUsers = totalUsers;
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
