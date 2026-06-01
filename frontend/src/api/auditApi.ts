import axios from "axios";
import { getAuthHeader } from "./userApi";
import type { AuditLogResponse } from "../types/audit";

const API_BASE_URL =
  import.meta.env.VITE_API_BASE_URL ?? "http://localhost:8080/api/v1";

export interface AuditLogFilters {
  action?: string;
  resourceType?: string;
  query?: string;
}

export const getAuditLogs = async (
  filters: AuditLogFilters = {}
): Promise<AuditLogResponse[]> => {
  const response = await axios.get(`${API_BASE_URL}/audit-logs`, {
    headers: getAuthHeader(),
    params: {
      action: filters.action || undefined,
      resourceType: filters.resourceType || undefined,
      q: filters.query?.trim() || undefined,
    },
  });

  return response.data;
};

export const exportAuditLogsCsv = async (
  filters: AuditLogFilters = {}
): Promise<Blob> => {
  const response = await axios.get(`${API_BASE_URL}/audit-logs/export`, {
    headers: getAuthHeader(),
    params: {
      action: filters.action || undefined,
      resourceType: filters.resourceType || undefined,
      q: filters.query?.trim() || undefined,
    },
    responseType: "blob",
  });

  return response.data;
};
