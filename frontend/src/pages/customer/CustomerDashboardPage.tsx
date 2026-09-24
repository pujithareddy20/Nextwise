import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';
import { dashboardApi } from '../../api/dashboardApi';
import { ticketApi } from '../../api/ticketApi';
import { DashboardStats, TicketListItem } from '../../types/ticket.types';
import { StatCard } from '../../components/common/StatCard';
import { TicketTable } from '../../components/tickets/TicketTable';
import { LoadingSpinner } from '../../components/common/LoadingSpinner';
import { EmptyState } from '../../components/common/EmptyState';
import { Button } from '../../components/ui/Button';
import { Card, CardContent, CardHeader, CardTitle } from '../../components/ui/Card';
import {
  Ticket,
  Clock,
  CheckCircle2,
  AlertCircle,
  PlusCircle,
  ArrowRight,
} from 'lucide-react';

export const CustomerDashboardPage: React.FC = () => {
  const { user } = useAuth();
  const navigate = useNavigate();

  const [stats, setStats] = useState<DashboardStats | null>(null);
  const [recentTickets, setRecentTickets] = useState<TicketListItem[]>([]);
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    const fetchData = async () => {
      try {
        setIsLoading(true);
        const [statsData, ticketsData] = await Promise.all([
          dashboardApi.getStats(),
          ticketApi.getTickets({ size: 5, sortBy: 'createdAt', sortDir: 'desc' }),
        ]);
        setStats(statsData);
        setRecentTickets(ticketsData.content);
      } catch (err) {
        console.error('Failed to load customer dashboard data', err);
      } finally {
        setIsLoading(false);
      }
    };

    fetchData();
  }, []);

  if (isLoading) {
    return <LoadingSpinner text="Loading dashboard metrics..." className="min-h-[50vh]" />;
  }

  return (
    <div className="space-y-6">
      {/* Top Banner */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 bg-white p-6 rounded-xl border border-slate-200/80 shadow-sm dark:bg-slate-900 dark:border-slate-800">
        <div>
          <h1 className="text-2xl font-bold tracking-tight text-slate-900 dark:text-slate-100">
            Welcome back, {user?.fullName}!
          </h1>
          <p className="text-sm text-slate-500 dark:text-slate-400 mt-1">
            Track your open support requests and converse with our technical support team.
          </p>
        </div>

        <Button onClick={() => navigate('/tickets/new')} className="shrink-0">
          <PlusCircle className="h-4 w-4 mr-1.5" />
          Create Support Ticket
        </Button>
      </div>

      {/* Dynamic Statistics Cards */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
        <StatCard
          title="Total Tickets"
          value={stats?.totalTickets ?? 0}
          icon={Ticket}
          color="blue"
          subtitle="All tickets submitted by you"
          onClick={() => navigate('/tickets')}
        />
        <StatCard
          title="Open Tickets"
          value={stats?.openTickets ?? 0}
          icon={Clock}
          color="indigo"
          subtitle="Waiting for agent response"
          onClick={() => navigate('/tickets?status=OPEN')}
        />
        <StatCard
          title="In Progress"
          value={stats?.inProgressTickets ?? 0}
          icon={AlertCircle}
          color="amber"
          subtitle="Under active investigation"
          onClick={() => navigate('/tickets?status=IN_PROGRESS')}
        />
        <StatCard
          title="Resolved"
          value={stats?.resolvedTickets ?? 0}
          icon={CheckCircle2}
          color="emerald"
          subtitle="Issues successfully solved"
          onClick={() => navigate('/tickets?status=RESOLVED')}
        />
      </div>

      {/* Recent Tickets Section */}
      <Card>
        <CardHeader className="flex flex-row items-center justify-between p-5 pb-3">
          <CardTitle className="text-base font-semibold">Your Recent Tickets</CardTitle>
          <Button
            variant="ghost"
            size="sm"
            onClick={() => navigate('/tickets')}
            className="text-xs text-blue-600 hover:text-blue-700"
          >
            <span>View all tickets</span>
            <ArrowRight className="h-3.5 w-3.5 ml-1" />
          </Button>
        </CardHeader>
        <CardContent className="p-0">
          {recentTickets.length === 0 ? (
            <div className="p-6">
              <EmptyState
                title="No support tickets yet"
                description="Need assistance? Submit your first support request and an agent will be assigned to help."
                actionLabel="Create Ticket"
                onAction={() => navigate('/tickets/new')}
              />
            </div>
          ) : (
            <TicketTable tickets={recentTickets} />
          )}
        </CardContent>
      </Card>
    </div>
  );
};
