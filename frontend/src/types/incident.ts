export type IncidentSeverity = "LOW" | "MEDIUM" | "HIGH" | "CRITICAL";

export type IncidentStatus = "OPEN" | "IN_PROGRESS" | "RESOLVED" | "CLOSED";

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
