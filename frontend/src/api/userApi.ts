import { axiosClient } from './axiosClient';
import { ApiResponse } from '../types/api.types';
import { User } from '../types/auth.types';

export const userApi = {
  getSupportAgents: async (): Promise<User[]> => {
    const res = await axiosClient.get<ApiResponse<User[]>>('/api/users/agents');
    return res.data.data;
  },
};
