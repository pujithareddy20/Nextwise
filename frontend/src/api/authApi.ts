import { axiosClient } from './axiosClient';
import { ApiResponse } from '../types/api.types';
import {
  AuthResponse,
  LoginRequest,
  RegisterRequest,
  CustomerRegisterRequest,
  AgentRegisterRequest,
  User,
} from '../types/auth.types';

export const authApi = {
  login: async (credentials: LoginRequest): Promise<AuthResponse> => {
    const res = await axiosClient.post<ApiResponse<AuthResponse>>('/api/auth/login', credentials);
    return res.data.data;
  },

  register: async (data: RegisterRequest): Promise<AuthResponse> => {
    const res = await axiosClient.post<ApiResponse<AuthResponse>>('/api/auth/register', data);
    return res.data.data;
  },

  registerCustomer: async (data: CustomerRegisterRequest): Promise<AuthResponse> => {
    const res = await axiosClient.post<ApiResponse<AuthResponse>>('/api/auth/register/customer', data);
    return res.data.data;
  },

  registerAgent: async (data: AgentRegisterRequest): Promise<AuthResponse> => {
    const res = await axiosClient.post<ApiResponse<AuthResponse>>('/api/auth/register/agent', data);
    return res.data.data;
  },

  getMe: async (): Promise<User> => {
    const res = await axiosClient.get<ApiResponse<User>>('/api/auth/me');
    return res.data.data;
  },
};
