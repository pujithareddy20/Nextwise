import React from 'react';
import { Badge } from '../ui/Badge';
import { TicketCategory } from '../../types/ticket.types';

interface TicketCategoryBadgeProps {
  category: TicketCategory;
}

export const TicketCategoryBadge: React.FC<TicketCategoryBadgeProps> = ({ category }) => {
  const labels: Record<TicketCategory, string> = {
    TECHNICAL: 'Technical',
    BILLING: 'Billing',
    ACCOUNT: 'Account',
    GENERAL: 'General',
    FEATURE_REQUEST: 'Feature Request',
  };

  return <Badge variant="outline">{labels[category] || category}</Badge>;
};
