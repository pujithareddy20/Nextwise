import { User } from './auth.types';

export type TicketStatus = 'OPEN' | 'IN_PROGRESS' | 'RESOLVED' | 'CLOSED';
export type TicketPriority = 'LOW' | 'MEDIUM' | 'HIGH' | 'URGENT';
export type TicketCategory = 'TECHNICAL' | 'BILLING' | 'ACCOUNT' | 'GENERAL' | 'FEATURE_REQUEST';

export interface TicketListItem {
  id: number;
  ticketNumber: string;
  title: string;
  status: TicketStatus;
  priority: TicketPriority;
  category: TicketCategory;
  customerId: number;
  customerName: string;
  customerEmail: string;
  assignedAgentId: number | null;
  assignedAgentName: string | null;
  createdAt: string;
  updatedAt: string;
  messageCount: number;
}

export interface TicketDetail {
  id: number;
  ticketNumber: string;
  title: string;
  description: string;
  status: TicketStatus;
  priority: TicketPriority;
  category: TicketCategory;
  customer: User;
  assignedAgent: User | null;
  createdAt: string;
  updatedAt: string;
  resolvedAt: string | null;
  messageCount: number;
}

export interface TicketMessage {
  id: number;
  ticketId: number;
  sender: User;
  message: string;
  internalNote: boolean;
  createdAt: string;
}

export interface CreateTicketRequest {
  title: string;
  description: string;
  category: TicketCategory;
  priority: TicketPriority;
}

export interface AiClassifyTicketRequest {
  title: string;
  description: string;
}

export interface AiClassifyTicketResponse {
  category: TicketCategory;
  priority: TicketPriority;
  reason: string;
}

export interface DashboardStats {
  totalTickets: number;
  openTickets: number;
  inProgressTickets: number;
  resolvedTickets: number;
  closedTickets: number;
  highPriorityTickets: number;
  urgentTickets?: number;
}
