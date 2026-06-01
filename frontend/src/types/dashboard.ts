export interface DashboardSummary {
  totalUsers: number;
  activeUsers: number;
  inactiveUsers: number;
  suspendedUsers: number;
  adminUsers: number;
  securityAnalystUsers: number;
  complianceOfficerUsers: number;
  auditorUsers: number;
  totalIncidents: number;
  openIncidents: number;
  inProgressIncidents: number;
  resolvedIncidents: number;
  closedIncidents: number;
  lowSeverityIncidents: number;
  mediumSeverityIncidents: number;
  highSeverityIncidents: number;
  criticalSeverityIncidents: number;
  overdueIncidents: number;
  dueSoonIncidents: number;
  unscheduledActiveIncidents: number;
  assignedActiveIncidents: number;
  unassignedActiveIncidents: number;
}
