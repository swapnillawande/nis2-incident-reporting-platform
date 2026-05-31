import axios from "axios";
import { getAuthHeader } from "./userApi";
import type { AuditLogResponse } from "../types/audit";

const API_BASE_URL =
  import.meta.env.VITE_API_BASE_URL ?? "http://localhost:8080/api/v1";

export const getAuditLogs = async (): Promise<AuditLogResponse[]> => {
  const response = await axios.get(`${API_BASE_URL}/audit-logs`, {
    headers: getAuthHeader(),
  });

  return response.data;
};
