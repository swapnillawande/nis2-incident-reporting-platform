export type IncidentSeverity = "LOW" | "MEDIUM" | "HIGH" | "CRITICAL";

export type IncidentStatus = "OPEN" | "IN_PROGRESS" | "RESOLVED" | "CLOSED";

export type IncidentAssignmentState = "ASSIGNED" | "UNASSIGNED";

export type IncidentDueState = "OVERDUE" | "DUE_SOON" | "NO_SLA";

export interface IncidentResponse {
  id: number;
  title: string;
  description: string;
  severity: IncidentSeverity;
  status: IncidentStatus;
  reportedByEmail: string;
  assignedToEmail: string | null;
  dueAt: string | null;
  createdAt: string;
  updatedAt: string;
}

export interface CreateIncidentRequest {
  title: string;
  description: string;
  severity: IncidentSeverity;
  assignedToEmail?: string;
  dueAt?: string;
}

export interface UpdateIncidentRequest {
  title?: string;
  description?: string;
  severity?: IncidentSeverity;
  status?: IncidentStatus;
  assignedToEmail?: string;
  dueAt?: string;
  clearDueAt?: boolean;
}

export interface BulkIncidentStatusUpdateRequest {
  incidentIds: number[];
  status: IncidentStatus;
}

export interface BulkIncidentAssignmentRequest {
  incidentIds: number[];
  assignedToEmail?: string;
}

export interface IncidentNote {
  id: number;
  incidentId: number;
  note: string;
  createdByEmail: string;
  createdAt: string;
}

export interface CreateIncidentNoteRequest {
  note: string;
}

export type IncidentTimelineItemType = "NOTE" | "AUDIT";

export interface IncidentTimelineItem {
  type: IncidentTimelineItemType;
  id: number;
  action: string | null;
  actorEmail: string;
  details: string | null;
  note: string | null;
  createdAt: string;
}
