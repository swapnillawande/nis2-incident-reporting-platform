import axios from "axios";
import type {
  AuthResponse,
  LoginRequest,
  RegisterRequest,
  UserResponse,
} from "../types/user";

const API_BASE_URL = "http://localhost:8080/api/v1";

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