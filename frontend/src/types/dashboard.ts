export interface DashboardSummary {
  totalUsers: number;
  totalIncidents: number;
  openIncidents: number;
  inProgressIncidents: number;
  resolvedIncidents: number;
  closedIncidents: number;
  overdueIncidents: number;
  dueSoonIncidents: number;
  unscheduledActiveIncidents: number;
}
