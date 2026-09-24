import React from 'react';
import { Search, X, Filter } from 'lucide-react';
import { Input } from '../ui/Input';
import { Select } from '../ui/Select';
import { Button } from '../ui/Button';
import { TicketCategory, TicketPriority, TicketStatus } from '../../types/ticket.types';

interface TicketFilterBarProps {
  search: string;
  onSearchChange: (value: string) => void;
  status: TicketStatus | '';
  onStatusChange: (value: TicketStatus | '') => void;
  priority: TicketPriority | '';
  onPriorityChange: (value: TicketPriority | '') => void;
  category: TicketCategory | '';
  onCategoryChange: (value: TicketCategory | '') => void;
  onReset: () => void;
}

export const TicketFilterBar: React.FC<TicketFilterBarProps> = ({
  search,
  onSearchChange,
  status,
  onStatusChange,
  priority,
  onPriorityChange,
  category,
  onCategoryChange,
  onReset,
}) => {
  const hasActiveFilters = !!search || !!status || !!priority || !!category;

  return (
    <div className="bg-white p-4 rounded-xl border border-slate-200/80 shadow-sm space-y-3 dark:bg-slate-900 dark:border-slate-800">
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-5 gap-3">
        {/* Search Input */}
        <div className="relative sm:col-span-2 lg:col-span-2">
          <div className="relative">
            <Search className="absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-slate-400" />
            <input
              type="text"
              placeholder="Search by title or Ticket ID (e.g. TCK-1001)..."
              value={search}
              onChange={(e) => onSearchChange(e.target.value)}
              className="w-full h-10 pl-9 pr-3 rounded-lg border border-slate-300 bg-white text-sm placeholder:text-slate-400 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent dark:border-slate-700 dark:bg-slate-900 dark:text-slate-100"
            />
          </div>
        </div>

        {/* Status Filter */}
        <div>
          <Select
            value={status}
            onChange={(e) => onStatusChange(e.target.value as TicketStatus | '')}
          >
            <option value="">All Statuses</option>
            <option value="OPEN">Open</option>
            <option value="IN_PROGRESS">In Progress</option>
            <option value="RESOLVED">Resolved</option>
            <option value="CLOSED">Closed</option>
          </Select>
        </div>

        {/* Priority Filter */}
        <div>
          <Select
            value={priority}
            onChange={(e) => onPriorityChange(e.target.value as TicketPriority | '')}
          >
            <option value="">All Priorities</option>
            <option value="LOW">Low</option>
            <option value="MEDIUM">Medium</option>
            <option value="HIGH">High</option>
            <option value="URGENT">Urgent</option>
          </Select>
        </div>

        {/* Category Filter */}
        <div>
          <Select
            value={category}
            onChange={(e) => onCategoryChange(e.target.value as TicketCategory | '')}
          >
            <option value="">All Categories</option>
            <option value="TECHNICAL">Technical</option>
            <option value="BILLING">Billing</option>
            <option value="ACCOUNT">Account</option>
            <option value="GENERAL">General</option>
            <option value="FEATURE_REQUEST">Feature Request</option>
          </Select>
        </div>
      </div>

      {hasActiveFilters && (
        <div className="flex items-center justify-between pt-1 border-t border-slate-100 text-xs dark:border-slate-800">
          <div className="flex items-center gap-1.5 text-slate-500">
            <Filter className="h-3.5 w-3.5" />
            <span>Active filters applied</span>
          </div>
          <Button
            variant="ghost"
            size="sm"
            onClick={onReset}
            className="h-7 text-xs text-rose-600 hover:text-rose-700 hover:bg-rose-50 dark:hover:bg-rose-950/30"
          >
            <X className="h-3 w-3 mr-1" />
            Clear all filters
          </Button>
        </div>
      )}
    </div>
  );
};
