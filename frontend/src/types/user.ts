export type RoleName =
  | "ADMIN"
  | "SECURITY_ANALYST"
  | "COMPLIANCE_OFFICER"
  | "AUDITOR";

export interface RegisterRequest {
  fullName: string;
  email: string;
  password: string;
  role: RoleName;
}

export interface LoginRequest {
  email: string;
  password: string;
}

export interface UserResponse {
  id: number;
  fullName: string;
  email: string;
  status: UserStatus;
  roles: RoleName[];
  createdAt: string;
  updatedAt: string;
}

export type UserStatus = "ACTIVE" | "INACTIVE" | "SUSPENDED";

export interface UpdateUserRequest {
  fullName: string;
  email: string;
  status: UserStatus;
  roles: RoleName[];
}

export interface AuthResponse {
  token: string | null;
  userId: number;
  fullName: string;
  email: string;
  roles: RoleName[];
}
