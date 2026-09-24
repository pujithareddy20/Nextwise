export type Role =
  | 'ROLE_CUSTOMER'
  | 'CUSTOMER'
  | 'ROLE_AGENT'
  | 'AGENT'
  | 'ROLE_SUPPORT_AGENT'
  | 'SUPPORT_AGENT'
  | 'ROLE_ADMIN'
  | 'ADMIN';

export interface User {
  id: number;
  fullName: string;
  email: string;
  role: Role;
  contactNumber?: string;
  agentId?: number;
}

export interface AuthResponse {
  accessToken: string;
  tokenType: string;
  user: User;
}

export interface LoginRequest {
  email: string;
  password: string;
}

export interface RegisterRequest {
  fullName: string;
  email: string;
  password: string;
}

export interface CustomerRegisterRequest {
  fullName: string;
  email: string;
  password: string;
  contactNumber: string;
}

export interface AgentRegisterRequest {
  fullName: string;
  email: string;
  password: string;
  agentId: number;
}

