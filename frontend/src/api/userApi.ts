import axios from "axios";
import type {
  AuthResponse,
  CreateUserRequest,
  LoginRequest,
  RegisterRequest,
  UpdateUserRequest,
  UserResponse,
  RoleName,
  UserStatus,
} from "../types/user";
import type { PagedResponse, PaginationParams } from "../types/pagination";

const API_BASE_URL =
  import.meta.env.VITE_API_BASE_URL ?? "http://localhost:8080/api/v1";

export const getAuthHeader = () => {
  const userData = localStorage.getItem("user");

  if (!userData) {
    return {};
  }

  const user = JSON.parse(userData);

  return {
    Authorization: `Bearer ${user.token}`,
  };
};

export const registerUser = async (
  data: RegisterRequest
): Promise<UserResponse> => {
  const response = await axios.post(`${API_BASE_URL}/users/register`, data);
  return response.data;
};

export const createUser = async (
  data: CreateUserRequest
): Promise<UserResponse> => {
  const response = await axios.post(`${API_BASE_URL}/users`, data, {
    headers: getAuthHeader(),
  });

  return response.data;
};

export const loginUser = async (
  data: LoginRequest
): Promise<AuthResponse> => {
  const response = await axios.post(`${API_BASE_URL}/users/login`, data);
  return response.data;
};

export const getCurrentUser = async (): Promise<UserResponse> => {
  const response = await axios.get(`${API_BASE_URL}/users/me`, {
    headers: getAuthHeader(),
  });

  return response.data;
};

export interface UserFilters extends PaginationParams {
  status?: UserStatus | "";
  role?: RoleName | "";
  query?: string;
}

export const getAllUsers = async (
  filters: UserFilters = {}
): Promise<PagedResponse<UserResponse>> => {
  const response = await axios.get(`${API_BASE_URL}/users`, {
    headers: getAuthHeader(),
    params: {
      status: filters.status || undefined,
      role: filters.role || undefined,
      q: filters.query?.trim() || undefined,
      page: filters.page,
      size: filters.size,
    },
  });

  return response.data;
};

export const exportUsersCsv = async (
  filters: UserFilters = {}
): Promise<Blob> => {
  const response = await axios.get(`${API_BASE_URL}/users/export`, {
    headers: getAuthHeader(),
    params: {
      status: filters.status || undefined,
      role: filters.role || undefined,
      q: filters.query?.trim() || undefined,
    },
    responseType: "blob",
  });

  return response.data;
};

export const updateUser = async (
  userId: number,
  data: UpdateUserRequest
): Promise<UserResponse> => {
  const response = await axios.put(`${API_BASE_URL}/users/${userId}`, data, {
    headers: getAuthHeader(),
  });

  return response.data;
};

export const deleteUser = async (userId: number): Promise<UserResponse> => {
  const response = await axios.delete(`${API_BASE_URL}/users/${userId}`, {
    headers: getAuthHeader(),
  });

  return response.data;
};
