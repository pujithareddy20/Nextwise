import React from 'react';
import { Badge } from '../ui/Badge';
import { TicketStatus } from '../../types/ticket.types';

interface TicketStatusBadgeProps {
  status: TicketStatus;
}

export const TicketStatusBadge: React.FC<TicketStatusBadgeProps> = ({ status }) => {
  const configs: Record<TicketStatus, { label: string; variant: 'info' | 'warning' | 'success' | 'secondary' }> = {
    OPEN: { label: 'Open', variant: 'info' },
    IN_PROGRESS: { label: 'In Progress', variant: 'warning' },
    RESOLVED: { label: 'Resolved', variant: 'success' },
    CLOSED: { label: 'Closed', variant: 'secondary' },
  };

  const config = configs[status] || { label: status, variant: 'secondary' };

  return <Badge variant={config.variant}>{config.label}</Badge>;
};
