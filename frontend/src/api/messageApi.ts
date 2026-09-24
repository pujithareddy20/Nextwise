import { axiosClient } from './axiosClient';
import { ApiResponse } from '../types/api.types';
import { TicketMessage } from '../types/ticket.types';

export const messageApi = {
  getMessages: async (ticketId: number): Promise<TicketMessage[]> => {
    const res = await axiosClient.get<ApiResponse<TicketMessage[]>>(`/api/tickets/${ticketId}/messages`);
    return res.data.data;
  },

  sendMessage: async (ticketId: number, message: string, internalNote = false): Promise<TicketMessage> => {
    const res = await axiosClient.post<ApiResponse<TicketMessage>>(`/api/tickets/${ticketId}/messages`, {
      message,
      internalNote,
    });
    return res.data.data;
  },
};
