import { axiosClient } from './axiosClient';
import { ApiResponse, Page } from '../types/api.types';
import {
  CreateTicketRequest,
  TicketCategory,
  TicketDetail,
  TicketListItem,
  TicketPriority,
  TicketStatus,
  AiClassifyTicketRequest,
  AiClassifyTicketResponse,
} from '../types/ticket.types';

export interface TicketQueryParams {
  search?: string;
  status?: TicketStatus;
  priority?: TicketPriority;
  category?: TicketCategory;
  assignedAgentId?: number;
  unassigned?: boolean;
  page?: number;
  size?: number;
  sortBy?: string;
  sortDir?: 'asc' | 'desc';
}

export const ticketApi = {
  getTickets: async (params?: TicketQueryParams): Promise<Page<TicketListItem>> => {
    const res = await axiosClient.get<ApiResponse<Page<TicketListItem>>>('/api/tickets', {
      params,
    });
    return res.data.data;
  },

  getTicketById: async (id: number): Promise<TicketDetail> => {
    const res = await axiosClient.get<ApiResponse<TicketDetail>>(`/api/tickets/${id}`);
    return res.data.data;
  },

  createTicket: async (data: CreateTicketRequest): Promise<TicketDetail> => {
    const res = await axiosClient.post<ApiResponse<TicketDetail>>('/api/tickets', data);
    return res.data.data;
  },

  updateStatus: async (id: number, status: TicketStatus): Promise<TicketDetail> => {
    const res = await axiosClient.patch<ApiResponse<TicketDetail>>(`/api/tickets/${id}/status`, {
      status,
    });
    return res.data.data;
  },

  updatePriority: async (id: number, priority: TicketPriority): Promise<TicketDetail> => {
    const res = await axiosClient.patch<ApiResponse<TicketDetail>>(`/api/tickets/${id}/priority`, {
      priority,
    });
    return res.data.data;
  },

  assignTicket: async (id: number, agentId?: number): Promise<TicketDetail> => {
    const res = await axiosClient.patch<ApiResponse<TicketDetail>>(`/api/tickets/${id}/assign`, {
      agentId,
    });
    return res.data.data;
  },

  suggestClassification: async (data: AiClassifyTicketRequest): Promise<AiClassifyTicketResponse> => {
    const res = await axiosClient.post<ApiResponse<AiClassifyTicketResponse>>(
      '/api/ai/suggest-ticket-classification',
      data
    );
    return res.data.data;
  },
};
