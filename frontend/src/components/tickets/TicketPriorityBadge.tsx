import React from 'react';
import { Badge } from '../ui/Badge';
import { TicketPriority } from '../../types/ticket.types';
import { AlertTriangle, AlertCircle, ArrowUp, ArrowDown } from 'lucide-react';

interface TicketPriorityBadgeProps {
  priority: TicketPriority;
}

export const TicketPriorityBadge: React.FC<TicketPriorityBadgeProps> = ({ priority }) => {
  const configs: Record<
    TicketPriority,
    { label: string; variant: 'secondary' | 'default' | 'warning' | 'destructive'; icon: React.ReactNode }
  > = {
    LOW: {
      label: 'Low',
      variant: 'secondary',
      icon: <ArrowDown className="h-3 w-3 mr-1 text-slate-500" />,
    },
    MEDIUM: {
      label: 'Medium',
      variant: 'default',
      icon: <ArrowUp className="h-3 w-3 mr-1 text-blue-500" />,
    },
    HIGH: {
      label: 'High',
      variant: 'warning',
      icon: <AlertTriangle className="h-3 w-3 mr-1 text-amber-500" />,
    },
    URGENT: {
      label: 'Urgent',
      variant: 'destructive',
      icon: <AlertCircle className="h-3 w-3 mr-1 text-rose-500" />,
    },
  };

  const config = configs[priority] || {
    label: priority,
    variant: 'secondary',
    icon: null,
  };

  return (
    <Badge variant={config.variant} className="flex items-center gap-0.5">
      {config.icon}
      {config.label}
    </Badge>
  );
};
