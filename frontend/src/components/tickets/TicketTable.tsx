import React from 'react';
import { useNavigate } from 'react-router-dom';
import { TicketListItem } from '../../types/ticket.types';
import { TicketStatusBadge } from './TicketStatusBadge';
import { TicketPriorityBadge } from './TicketPriorityBadge';
import { TicketCategoryBadge } from './TicketCategoryBadge';
import { MessageSquare, Calendar, User as UserIcon } from 'lucide-react';

interface TicketTableProps {
  tickets: TicketListItem[];
  isAgent?: boolean;
}

export const TicketTable: React.FC<TicketTableProps> = ({ tickets, isAgent = false }) => {
  const navigate = useNavigate();

  const formatDate = (dateStr: string) => {
    try {
      const date = new Date(dateStr);
      return date.toLocaleDateString('en-US', {
        month: 'short',
        day: 'numeric',
        year: 'numeric',
      });
    } catch {
      return dateStr;
    }
  };

  return (
    <div className="overflow-hidden rounded-xl border border-slate-200/80 bg-white shadow-sm dark:border-slate-800 dark:bg-slate-900">
      <div className="overflow-x-auto">
        <table className="w-full text-left text-sm">
          <thead className="bg-slate-50 border-b border-slate-200 text-xs font-semibold uppercase tracking-wider text-slate-500 dark:bg-slate-800/60 dark:border-slate-700 dark:text-slate-400">
            <tr>
              <th scope="col" className="px-5 py-3.5">
                Ticket
              </th>
              <th scope="col" className="px-5 py-3.5">
                Category
              </th>
              {isAgent && (
                <th scope="col" className="px-5 py-3.5">
                  Customer
                </th>
              )}
              <th scope="col" className="px-5 py-3.5">
                Assignee
              </th>
              <th scope="col" className="px-5 py-3.5">
                Priority
              </th>
              <th scope="col" className="px-5 py-3.5">
                Status
              </th>
              <th scope="col" className="px-5 py-3.5">
                Created
              </th>
            </tr>
          </thead>
          <tbody className="divide-y divide-slate-100 dark:divide-slate-800">
            {tickets.map((ticket) => (
              <tr
                key={ticket.id}
                onClick={() => navigate(`/tickets/${ticket.id}`)}
                className="group cursor-pointer hover:bg-slate-50/80 transition-colors dark:hover:bg-slate-800/40"
              >
                <td className="px-5 py-4">
                  <div className="flex items-start gap-2.5">
                    <span className="font-mono text-xs font-semibold text-blue-600 bg-blue-50 px-2 py-0.5 rounded dark:bg-blue-950/60 dark:text-blue-400 shrink-0">
                      {ticket.ticketNumber}
                    </span>
                    <div>
                      <div className="font-medium text-slate-900 group-hover:text-blue-600 transition-colors dark:text-slate-100 dark:group-hover:text-blue-400 line-clamp-1">
                        {ticket.title}
                      </div>
                      {ticket.messageCount > 0 && (
                        <div className="flex items-center gap-1 text-xs text-slate-400 mt-1">
                          <MessageSquare className="h-3 w-3" />
                          <span>{ticket.messageCount} {ticket.messageCount === 1 ? 'reply' : 'replies'}</span>
                        </div>
                      )}
                    </div>
                  </div>
                </td>

                <td className="px-5 py-4 whitespace-nowrap">
                  <TicketCategoryBadge category={ticket.category} />
                </td>

                {isAgent && (
                  <td className="px-5 py-4 whitespace-nowrap">
                    <div className="flex items-center gap-2">
                      <div className="h-6 w-6 rounded-full bg-slate-100 flex items-center justify-center text-slate-600 dark:bg-slate-800 dark:text-slate-300">
                        <UserIcon className="h-3.5 w-3.5" />
                      </div>
                      <span className="text-xs font-medium text-slate-700 dark:text-slate-300">
                        {ticket.customerName || 'Customer'}
                      </span>
                    </div>
                  </td>
                )}

                <td className="px-5 py-4 whitespace-nowrap text-xs">
                  {ticket.assignedAgentName ? (
                    <span className="text-slate-700 font-medium dark:text-slate-300">
                      {ticket.assignedAgentName}
                    </span>
                  ) : (
                    <span className="text-amber-600 font-medium italic dark:text-amber-400">
                      Unassigned
                    </span>
                  )}
                </td>

                <td className="px-5 py-4 whitespace-nowrap">
                  <TicketPriorityBadge priority={ticket.priority} />
                </td>

                <td className="px-5 py-4 whitespace-nowrap">
                  <TicketStatusBadge status={ticket.status} />
                </td>

                <td className="px-5 py-4 whitespace-nowrap text-xs text-slate-500 dark:text-slate-400">
                  <div className="flex items-center gap-1.5">
                    <Calendar className="h-3.5 w-3.5" />
                    <span>{formatDate(ticket.createdAt)}</span>
                  </div>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
};
