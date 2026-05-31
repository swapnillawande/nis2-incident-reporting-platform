import axios from "axios";
import { getAuthHeader } from "./userApi";
import type { DashboardSummary } from "../types/dashboard";

const API_BASE_URL =
  import.meta.env.VITE_API_BASE_URL ?? "http://localhost:8080/api/v1";

export const getDashboardSummary = async (): Promise<DashboardSummary> => {
  const response = await axios.get(`${API_BASE_URL}/dashboard/summary`, {
    headers: getAuthHeader(),
  });

  return response.data;
};
