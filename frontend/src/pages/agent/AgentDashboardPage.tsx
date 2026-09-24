import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';
import { dashboardApi } from '../../api/dashboardApi';
import { ticketApi } from '../../api/ticketApi';
import { DashboardStats, TicketListItem } from '../../types/ticket.types';
import { StatCard } from '../../components/common/StatCard';
import { TicketTable } from '../../components/tickets/TicketTable';
import { LoadingSpinner } from '../../components/common/LoadingSpinner';
import { Card, CardContent, CardHeader, CardTitle } from '../../components/ui/Card';
import { Button } from '../../components/ui/Button';
import {
  Inbox,
  Clock,
  CheckCircle2,
  AlertCircle,
  AlertTriangle,
  ArrowRight,
} from 'lucide-react';

export const AgentDashboardPage: React.FC = () => {
  const { user } = useAuth();
  const navigate = useNavigate();

  const [stats, setStats] = useState<DashboardStats | null>(null);
  const [urgentTickets, setUrgentTickets] = useState<TicketListItem[]>([]);
  const [unassignedTickets, setUnassignedTickets] = useState<TicketListItem[]>([]);
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    const fetchData = async () => {
      try {
        setIsLoading(true);
        const [statsData, urgentData, unassignedData] = await Promise.all([
          dashboardApi.getStats(),
          ticketApi.getTickets({ priority: 'URGENT', size: 5, sortBy: 'createdAt', sortDir: 'desc' }),
          ticketApi.getTickets({ unassigned: true, size: 5, sortBy: 'createdAt', sortDir: 'desc' }),
        ]);

        setStats(statsData);
        setUrgentTickets(urgentData.content);
        setUnassignedTickets(unassignedData.content);
      } catch (err) {
        console.error('Failed to load agent dashboard', err);
      } finally {
        setIsLoading(false);
      }
    };

    fetchData();
  }, []);

  if (isLoading) {
    return <LoadingSpinner text="Loading agent triage overview..." className="min-h-[50vh]" />;
  }

  return (
    <div className="space-y-6">
      {/* Banner */}
      <div className="bg-white p-6 rounded-xl border border-slate-200/80 shadow-sm dark:bg-slate-900 dark:border-slate-800">
        <h1 className="text-2xl font-bold tracking-tight text-slate-900 dark:text-slate-100">
          Support Agent Operations Hub
        </h1>
        <p className="text-sm text-slate-500 dark:text-slate-400 mt-1">
          Welcome, {user?.fullName}. You are overseeing organization-wide ticket triage and resolution.
        </p>
      </div>

      {/* Dynamic System Stats */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-5 gap-4">
        <StatCard
          title="Total Tickets"
          value={stats?.totalTickets ?? 0}
          icon={Inbox}
          color="blue"
          subtitle="All platform tickets"
          onClick={() => navigate('/agent/tickets')}
        />
        <StatCard
          title="Open Queue"
          value={stats?.openTickets ?? 0}
          icon={Clock}
          color="indigo"
          subtitle="Awaiting initial response"
          onClick={() => navigate('/agent/tickets?status=OPEN')}
        />
        <StatCard
          title="In Progress"
          value={stats?.inProgressTickets ?? 0}
          icon={AlertCircle}
          color="amber"
          subtitle="Currently assigned & active"
          onClick={() => navigate('/agent/tickets?status=IN_PROGRESS')}
        />
        <StatCard
          title="Resolved"
          value={stats?.resolvedTickets ?? 0}
          icon={CheckCircle2}
          color="emerald"
          subtitle="Successfully closed tickets"
          onClick={() => navigate('/agent/tickets?status=RESOLVED')}
        />
        <StatCard
          title="URGENT"
          value={stats?.urgentTickets ?? stats?.highPriorityTickets ?? 0}
          icon={AlertTriangle}
          color="rose"
          subtitle="Urgent priority issues"
          onClick={() => navigate('/agent/tickets?priority=URGENT')}
        />
      </div>

      {/* Urgent & Unassigned Queues */}
      <div className="grid grid-cols-1 gap-6">
        {/* Unassigned Tickets */}
        <Card>
          <CardHeader className="flex flex-row items-center justify-between p-5 pb-3">
            <div>
              <CardTitle className="text-base font-semibold">Unassigned Tickets Awaiting Ownership</CardTitle>
              <p className="text-xs text-slate-500 mt-0.5">Tickets requiring assignment to an agent</p>
            </div>
            <Button
              variant="ghost"
              size="sm"
              onClick={() => navigate('/agent/tickets?unassigned=true')}
              className="text-xs text-blue-600 hover:text-blue-700"
            >
              <span>View all unassigned</span>
              <ArrowRight className="h-3.5 w-3.5 ml-1" />
            </Button>
          </CardHeader>
          <CardContent className="p-0">
            {unassignedTickets.length === 0 ? (
              <div className="p-6 text-center text-sm text-slate-500">
                Great job! There are currently no unassigned tickets in the queue.
              </div>
            ) : (
              <TicketTable tickets={unassignedTickets} isAgent={true} />
            )}
          </CardContent>
        </Card>

        {/* High Priority / Urgent Tickets */}
        <Card>
          <CardHeader className="flex flex-row items-center justify-between p-5 pb-3">
            <div>
              <CardTitle className="text-base font-semibold">Urgent Priority Tickets</CardTitle>
              <p className="text-xs text-slate-500 mt-0.5">Critical blockers requiring expedited handling</p>
            </div>
            <Button
              variant="ghost"
              size="sm"
              onClick={() => navigate('/agent/tickets?priority=URGENT')}
              className="text-xs text-rose-600 hover:text-rose-700"
            >
              <span>View all urgent</span>
              <ArrowRight className="h-3.5 w-3.5 ml-1" />
            </Button>
          </CardHeader>
          <CardContent className="p-0">
            {urgentTickets.length === 0 ? (
              <div className="p-6 text-center text-sm text-slate-500">
                No urgent priority tickets right now.
              </div>
            ) : (
              <TicketTable tickets={urgentTickets} isAgent={true} />
            )}
          </CardContent>
        </Card>
      </div>
    </div>
  );
};
