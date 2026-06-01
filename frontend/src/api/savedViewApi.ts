import axios from "axios";
import { getAuthHeader } from "./userApi";
import type {
  CreateSavedViewRequest,
  SavedViewResponse,
  SavedViewType,
} from "../types/savedView";

const API_BASE_URL =
  import.meta.env.VITE_API_BASE_URL ?? "http://localhost:8080/api/v1";

export const getSavedViews = async (
  viewType: SavedViewType
): Promise<SavedViewResponse[]> => {
  const response = await axios.get(`${API_BASE_URL}/saved-views`, {
    headers: getAuthHeader(),
    params: { viewType },
  });

  return response.data;
};

export const createSavedView = async (
  data: CreateSavedViewRequest
): Promise<SavedViewResponse> => {
  const response = await axios.post(`${API_BASE_URL}/saved-views`, data, {
    headers: getAuthHeader(),
  });

  return response.data;
};

export const deleteSavedView = async (
  savedViewId: number
): Promise<SavedViewResponse> => {
  const response = await axios.delete(`${API_BASE_URL}/saved-views/${savedViewId}`, {
    headers: getAuthHeader(),
  });

  return response.data;
};
