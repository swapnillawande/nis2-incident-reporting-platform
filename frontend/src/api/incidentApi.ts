import axios from "axios";
import { getAuthHeader } from "./userApi";
import type {
  CreateIncidentRequest,
  IncidentResponse,
  UpdateIncidentRequest,
} from "../types/incident";

const API_BASE_URL =
  import.meta.env.VITE_API_BASE_URL ?? "http://localhost:8080/api/v1";

export const getAllIncidents = async (): Promise<IncidentResponse[]> => {
  const response = await axios.get(`${API_BASE_URL}/incidents`, {
    headers: getAuthHeader(),
  });

  return response.data;
};

export const createIncident = async (
  data: CreateIncidentRequest
): Promise<IncidentResponse> => {
  const response = await axios.post(`${API_BASE_URL}/incidents`, data, {
    headers: getAuthHeader(),
  });

  return response.data;
};

export const updateIncident = async (
  incidentId: number,
  data: UpdateIncidentRequest
): Promise<IncidentResponse> => {
  const response = await axios.put(`${API_BASE_URL}/incidents/${incidentId}`, data, {
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
