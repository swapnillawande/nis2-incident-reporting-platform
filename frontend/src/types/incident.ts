export type IncidentSeverity = "LOW" | "MEDIUM" | "HIGH" | "CRITICAL";

export type IncidentStatus = "OPEN" | "IN_PROGRESS" | "RESOLVED" | "CLOSED";

export interface IncidentResponse {
  id: number;
  title: string;
  description: string;
  severity: IncidentSeverity;
  status: IncidentStatus;
  reportedByEmail: string;
  createdAt: string;
  updatedAt: string;
}

export interface CreateIncidentRequest {
  title: string;
  description: string;
  severity: IncidentSeverity;
}

export interface UpdateIncidentRequest {
  title?: string;
  description?: string;
  severity?: IncidentSeverity;
  status?: IncidentStatus;
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
