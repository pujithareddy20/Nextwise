import { axiosClient } from './axiosClient';
import { ApiResponse } from '../types/api.types';
import { DashboardStats } from '../types/ticket.types';

export const dashboardApi = {
  getStats: async (): Promise<DashboardStats> => {
    const res = await axiosClient.get<ApiResponse<DashboardStats>>('/api/dashboard/stats');
    return res.data.data;
  },
};
