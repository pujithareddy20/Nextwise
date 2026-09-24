import React from 'react';
import { Link, useLocation } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';
import {
  LayoutDashboard,
  Ticket,
  PlusCircle,
  Inbox,
  CheckCircle,
  ShieldCheck,
} from 'lucide-react';
import { cn } from '../../lib/utils';

export const Sidebar: React.FC = () => {
  const { isCustomer, isAgent } = useAuth();
  const location = useLocation();

  const customerLinks = [
    { to: '/dashboard', label: 'Dashboard', icon: LayoutDashboard },
    { to: '/tickets', label: 'My Tickets', icon: Ticket },
    { to: '/tickets/new', label: 'Create Ticket', icon: PlusCircle },
  ];

  const agentLinks = [
    { to: '/agent/dashboard', label: 'Agent Overview', icon: LayoutDashboard },
    { to: '/agent/tickets', label: 'All Tickets', icon: Inbox },
    { to: '/agent/tickets?status=OPEN', label: 'Open Queue', icon: Ticket },
    { to: '/agent/tickets?status=RESOLVED', label: 'Resolved Tickets', icon: CheckCircle },
  ];

  const links = isCustomer ? customerLinks : agentLinks;

  const isLinkActive = (linkTo: string) => {
    const [targetPath, targetSearch = ''] = linkTo.split('?');
    const targetParams = new URLSearchParams(targetSearch);
    const currentParams = new URLSearchParams(location.search);

    // If pathnames do not match, it is not active
    if (location.pathname !== targetPath) {
      return false;
    }

    // Exact matching for agent ticket queues with query parameters:
    if (targetPath === '/agent/tickets') {
      const targetStatus = targetParams.get('status');
      const currentStatus = currentParams.get('status');

      if (targetStatus) {
        // "Open Queue" (status=OPEN) or "Resolved Tickets" (status=RESOLVED)
        return currentStatus === targetStatus;
      } else {
        // "All Tickets" is active only when no specific status filter is active
        return !currentStatus;
      }
    }

    // Exact matching for customer /tickets vs /tickets/new
    if (targetPath === '/tickets') {
      return location.pathname === '/tickets';
    }

    // For any other link with query parameters
    if (targetSearch) {
      for (const [key, value] of targetParams.entries()) {
        if (currentParams.get(key) !== value) {
          return false;
        }
      }
      return true;
    }

    // Default exact pathname matching
    return location.pathname === targetPath;
  };

  return (
    <aside className="w-64 border-r border-slate-200 bg-white/70 backdrop-blur p-4 flex flex-col justify-between hidden md:flex shrink-0 dark:border-slate-800 dark:bg-slate-900/70">
      <div className="space-y-6">
        <div className="px-3 py-1">
          <p className="text-xs font-semibold uppercase tracking-wider text-slate-400">
            {isCustomer ? 'Customer Portal' : 'Support Desk'}
          </p>
        </div>

        <nav className="space-y-1">
          {links.map((link) => {
            const Icon = link.icon;
            const active = isLinkActive(link.to);
            return (
              <Link
                key={link.to}
                to={link.to}
                className={cn(
                  'flex items-center gap-3 px-3 py-2.5 rounded-lg text-sm font-medium transition-all duration-150',
                  active
                    ? 'bg-blue-50 text-blue-700 font-semibold dark:bg-blue-950/60 dark:text-blue-300'
                    : 'text-slate-600 hover:bg-slate-100 hover:text-slate-900 dark:text-slate-400 dark:hover:bg-slate-800 dark:hover:text-slate-200'
                )}
              >
                <Icon className="h-4 w-4" />
                <span>{link.label}</span>
              </Link>
            );
          })}
        </nav>
      </div>

      <div className="rounded-xl border border-slate-200 bg-slate-50 p-3.5 dark:border-slate-800 dark:bg-slate-800/40">
        <div className="flex items-center gap-2 mb-1.5 text-xs font-semibold text-slate-700 dark:text-slate-300">
          <ShieldCheck className="h-4 w-4 text-emerald-600" />
          <span>RBAC Protected</span>
        </div>
        <p className="text-xs text-slate-500 dark:text-slate-400">
          {isCustomer
            ? 'Your tickets are private and only visible to you and assigned agents.'
            : 'You have full triage access to assign and resolve support tickets.'}
        </p>
      </div>
    </aside>
  );
};
