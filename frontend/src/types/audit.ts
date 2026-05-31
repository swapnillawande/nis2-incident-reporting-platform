export interface AuditLogResponse {
  id: number;
  action: string;
  resourceType: string;
  resourceId: string | null;
  actorEmail: string;
  details: string | null;
  createdAt: string;
}
