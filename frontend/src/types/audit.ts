export interface AuditLogResponse {
  id: number;
  action: string;
  resourceType: string;
  resourceId: string | null;
  actorEmail: string;
  details: string | null;
  createdAt: string;
}

export interface AuditLogSummary {
  totalLogs: number;
  uniqueActors: number;
  latestActivityAt: string | null;
  actionCounts: Record<string, number>;
  resourceTypeCounts: Record<string, number>;
}
