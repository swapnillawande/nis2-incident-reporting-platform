import axios from "axios";
import { getAuthHeader } from "./userApi";
import type {
  BulkIncidentStatusUpdateRequest,
  CreateIncidentRequest,
  CreateIncidentNoteRequest,
  IncidentNote,
  IncidentResponse,
  IncidentSeverity,
  IncidentStatus,
  IncidentTimelineItem,
  UpdateIncidentRequest,
} from "../types/incident";
import type { PagedResponse, PaginationParams } from "../types/pagination";

const API_BASE_URL =
  import.meta.env.VITE_API_BASE_URL ?? "http://localhost:8080/api/v1";

export interface IncidentFilters extends PaginationParams {
  status?: IncidentStatus | "";
  severity?: IncidentSeverity | "";
  assignedToEmail?: string;
  query?: string;
  createdFrom?: string;
  createdTo?: string;
  dueFrom?: string;
  dueTo?: string;
}

const cleanCreateIncidentPayload = (
  data: CreateIncidentRequest
): CreateIncidentRequest => ({
  ...data,
  assignedToEmail: data.assignedToEmail?.trim() || undefined,
  dueAt: data.dueAt || undefined,
});

const cleanUpdateIncidentPayload = (
  data: UpdateIncidentRequest
): UpdateIncidentRequest => {
  const payload: UpdateIncidentRequest = {
    ...data,
    assignedToEmail: data.assignedToEmail?.trim() ?? data.assignedToEmail,
  };

  if (data.dueAt === "") {
    delete payload.dueAt;
    payload.clearDueAt = true;
  }

  return payload;
};

export const getAllIncidents = async (
  filters: IncidentFilters = {}
): Promise<PagedResponse<IncidentResponse>> => {
  const response = await axios.get(`${API_BASE_URL}/incidents`, {
    headers: getAuthHeader(),
    params: {
      status: filters.status || undefined,
      severity: filters.severity || undefined,
      assignedToEmail: filters.assignedToEmail?.trim() || undefined,
      q: filters.query?.trim() || undefined,
      createdFrom: filters.createdFrom || undefined,
      createdTo: filters.createdTo || undefined,
      dueFrom: filters.dueFrom || undefined,
      dueTo: filters.dueTo || undefined,
      page: filters.page,
      size: filters.size,
      sortBy: filters.sortBy,
      sortDir: filters.sortDir,
    },
  });

  return response.data;
};

export const exportIncidentsCsv = async (
  filters: IncidentFilters = {}
): Promise<Blob> => {
  const response = await axios.get(`${API_BASE_URL}/incidents/export`, {
    headers: getAuthHeader(),
    params: {
      status: filters.status || undefined,
      severity: filters.severity || undefined,
      assignedToEmail: filters.assignedToEmail?.trim() || undefined,
      q: filters.query?.trim() || undefined,
      createdFrom: filters.createdFrom || undefined,
      createdTo: filters.createdTo || undefined,
      dueFrom: filters.dueFrom || undefined,
      dueTo: filters.dueTo || undefined,
    },
    responseType: "blob",
  });

  return response.data;
};

export const createIncident = async (
  data: CreateIncidentRequest
): Promise<IncidentResponse> => {
  const response = await axios.post(`${API_BASE_URL}/incidents`, cleanCreateIncidentPayload(data), {
    headers: getAuthHeader(),
  });

  return response.data;
};

export const updateIncident = async (
  incidentId: number,
  data: UpdateIncidentRequest
): Promise<IncidentResponse> => {
  const response = await axios.put(
    `${API_BASE_URL}/incidents/${incidentId}`,
    cleanUpdateIncidentPayload(data),
    {
      headers: getAuthHeader(),
    }
  );

  return response.data;
};

export const bulkUpdateIncidentStatus = async (
  data: BulkIncidentStatusUpdateRequest
): Promise<IncidentResponse[]> => {
  const response = await axios.put(`${API_BASE_URL}/incidents/bulk-status`, data, {
    headers: getAuthHeader(),
  });

  return response.data;
};

export const deleteIncident = async (
  incidentId: number
): Promise<IncidentResponse> => {
  const response = await axios.delete(`${API_BASE_URL}/incidents/${incidentId}`, {
    headers: getAuthHeader(),
  });

  return response.data;
};

export const getIncidentNotes = async (
  incidentId: number
): Promise<IncidentNote[]> => {
  const response = await axios.get(`${API_BASE_URL}/incidents/${incidentId}/notes`, {
    headers: getAuthHeader(),
  });

  return response.data;
};

export const getIncidentTimeline = async (
  incidentId: number
): Promise<IncidentTimelineItem[]> => {
  const response = await axios.get(`${API_BASE_URL}/incidents/${incidentId}/timeline`, {
    headers: getAuthHeader(),
  });

  return response.data;
};

export const addIncidentNote = async (
  incidentId: number,
  data: CreateIncidentNoteRequest
): Promise<IncidentNote> => {
  const response = await axios.post(
    `${API_BASE_URL}/incidents/${incidentId}/notes`,
    data,
    {
      headers: getAuthHeader(),
    }
  );

  return response.data;
};
