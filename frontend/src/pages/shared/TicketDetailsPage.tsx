import React, { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';
import { ticketApi } from '../../api/ticketApi';
import { messageApi } from '../../api/messageApi';
import { userApi } from '../../api/userApi';
import {
  TicketDetail,
  TicketMessage,
  TicketPriority,
  TicketStatus,
} from '../../types/ticket.types';
import { User } from '../../types/auth.types';
import { TicketStatusBadge } from '../../components/tickets/TicketStatusBadge';
import { TicketPriorityBadge } from '../../components/tickets/TicketPriorityBadge';
import { TicketCategoryBadge } from '../../components/tickets/TicketCategoryBadge';
import { ConversationThread } from '../../components/tickets/ConversationThread';
import { ReplyBox } from '../../components/tickets/ReplyBox';
import { LoadingSpinner } from '../../components/common/LoadingSpinner';
import { Button } from '../../components/ui/Button';
import { Select } from '../../components/ui/Select';
import { Card, CardContent, CardHeader, CardTitle } from '../../components/ui/Card';
import {
  ArrowLeft,
  Calendar,
  User as UserIcon,
  Headphones,
  ShieldAlert,
  UserCheck,
  CheckCircle2,
  XCircle,
  Loader2,
} from 'lucide-react';

export const TicketDetailsPage: React.FC = () => {
  const { id } = useParams<{ id: string }>();
  const ticketId = parseInt(id || '', 10);
  const navigate = useNavigate();
  const { user, isAgent, isCustomer } = useAuth();

  const [ticket, setTicket] = useState<TicketDetail | null>(null);
  const [messages, setMessages] = useState<TicketMessage[]>([]);
  const [agents, setAgents] = useState<User[]>([]);
  const [agentsLoading, setAgentsLoading] = useState(false);
  const [agentsError, setAgentsError] = useState('');
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState('');
  const [actionLoading, setActionLoading] = useState(false);

  const fetchTicketAndMessages = async () => {
    if (isNaN(ticketId)) {
      setError('Invalid ticket ID');
      setIsLoading(false);
      return;
    }

    try {
      setIsLoading(true);
      setError('');
      const [ticketData, messagesData] = await Promise.all([
        ticketApi.getTicketById(ticketId),
        messageApi.getMessages(ticketId),
      ]);
      setTicket(ticketData);
      setMessages(messagesData);

      if (isAgent) {
        try {
          setAgentsLoading(true);
          setAgentsError('');
          const agentsData = await userApi.getSupportAgents();
          setAgents(agentsData || []);
        } catch (agentErr) {
          console.error('Failed to load support agents', agentErr);
          setAgentsError('Failed to load registered service agents.');
        } finally {
          setAgentsLoading(false);
        }
      }
    } catch (err: any) {
      console.error('Failed to load ticket details', err);
      if (err.response?.status === 403) {
        setError('Access Denied: You do not have permission to view this ticket.');
      } else if (err.response?.status === 404) {
        setError('Ticket not found.');
      } else {
        setError('Failed to load ticket details. Please try again.');
      }
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    fetchTicketAndMessages();
  }, [ticketId]);

  const handleStatusChange = async (newStatus: TicketStatus) => {
    if (!ticket) return;
    try {
      setActionLoading(true);
      const updated = await ticketApi.updateStatus(ticket.id, newStatus);
      setTicket(updated);
    } catch (err: any) {
      alert(err.response?.data?.message || 'Failed to update status');
    } finally {
      setActionLoading(false);
    }
  };

  const handlePriorityChange = async (newPriority: TicketPriority) => {
    if (!ticket) return;
    try {
      setActionLoading(true);
      const updated = await ticketApi.updatePriority(ticket.id, newPriority);
      setTicket(updated);
    } catch (err: any) {
      alert(err.response?.data?.message || 'Failed to update priority');
    } finally {
      setActionLoading(false);
    }
  };

  const handleAssignAgent = async (agentId?: number) => {
    if (!ticket) return;
    try {
      setActionLoading(true);
      const updated = await ticketApi.assignTicket(ticket.id, agentId);
      setTicket(updated);
    } catch (err: any) {
      alert(err.response?.data?.message || 'Failed to assign ticket');
    } finally {
      setActionLoading(false);
    }
  };

  const handleSendMessage = async (msgText: string, isInternalNote: boolean) => {
    if (!ticket) return;
    const newMsg = await messageApi.sendMessage(ticket.id, msgText, isInternalNote);
    setMessages((prev) => [...prev, newMsg]);

    // If customer replied to resolved ticket, reload ticket to get updated IN_PROGRESS status
    if (isCustomer && ticket.status === 'RESOLVED') {
      const refreshed = await ticketApi.getTicketById(ticket.id);
      setTicket(refreshed);
    }
  };

  const formatDate = (dateStr?: string | null) => {
    if (!dateStr) return 'N/A';
    try {
      return new Date(dateStr).toLocaleString('en-US', {
        month: 'short',
        day: 'numeric',
        year: 'numeric',
        hour: '2-digit',
        minute: '2-digit',
      });
    } catch {
      return dateStr;
    }
  };

  if (isLoading) {
    return <LoadingSpinner text="Loading ticket details..." className="min-h-[50vh]" />;
  }

  if (error || !ticket) {
    return (
      <div className="max-w-2xl mx-auto my-12 text-center space-y-4">
        <div className="mx-auto flex h-14 w-14 items-center justify-center rounded-full bg-rose-100 text-rose-600">
          <ShieldAlert className="h-7 w-7" />
        </div>
        <h2 className="text-xl font-bold text-slate-900 dark:text-slate-100">
          {error || 'Ticket Unavailable'}
        </h2>
        <p className="text-sm text-slate-500">
          If you believe this is an error, please verify your login permissions or contact support.
        </p>
        <Button onClick={() => navigate(isCustomer ? '/tickets' : '/agent/tickets')}>
          <ArrowLeft className="h-4 w-4 mr-1.5" />
          Return to Tickets
        </Button>
      </div>
    );
  }

  return (
    <div className="max-w-6xl mx-auto space-y-6">
      {/* Back button */}
      <Button
        variant="ghost"
        size="sm"
        onClick={() => navigate(isCustomer ? '/tickets' : '/agent/tickets')}
        className="text-slate-600 hover:text-slate-900 -ml-2"
      >
        <ArrowLeft className="h-4 w-4 mr-1.5" />
        {isCustomer ? 'Back to My Tickets' : 'Back to Ticket Board'}
      </Button>

      {/* Main Grid: Ticket Details + Conversation & Side Panel */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        {/* Left 2 Cols: Ticket Header, Description & Conversation Feed */}
        <div className="lg:col-span-2 space-y-6">
          <Card>
            <CardHeader className="p-6 pb-4 border-b border-slate-100 dark:border-slate-800">
              <div className="flex flex-wrap items-center justify-between gap-3 mb-2">
                <span className="font-mono text-xs font-bold text-blue-600 bg-blue-50 px-2.5 py-1 rounded dark:bg-blue-950 dark:text-blue-300">
                  {ticket.ticketNumber}
                </span>

                <div className="flex items-center gap-2">
                  <TicketCategoryBadge category={ticket.category} />
                  <TicketPriorityBadge priority={ticket.priority} />
                  <TicketStatusBadge status={ticket.status} />
                </div>
              </div>

              <CardTitle className="text-xl font-bold text-slate-900 dark:text-slate-100 mt-1">
                {ticket.title}
              </CardTitle>

              <div className="flex flex-wrap items-center gap-4 text-xs text-slate-400 mt-2">
                <div className="flex items-center gap-1.5">
                  <Calendar className="h-3.5 w-3.5" />
                  <span>Created {formatDate(ticket.createdAt)}</span>
                </div>
                {ticket.resolvedAt && (
                  <div className="flex items-center gap-1.5 text-emerald-600 dark:text-emerald-400">
                    <CheckCircle2 className="h-3.5 w-3.5" />
                    <span>Resolved {formatDate(ticket.resolvedAt)}</span>
                  </div>
                )}
              </div>
            </CardHeader>

            <CardContent className="p-6">
              <h4 className="text-xs font-semibold uppercase tracking-wider text-slate-400 mb-2">
                Initial Issue Description
              </h4>
              <div className="text-sm text-slate-700 dark:text-slate-300 leading-relaxed whitespace-pre-wrap bg-slate-50/60 p-4 rounded-xl border border-slate-100 dark:bg-slate-900/50 dark:border-slate-800">
                {ticket.description}
              </div>
            </CardContent>
          </Card>

          {/* Conversation & Replies */}
          <Card>
            <CardHeader className="p-6 pb-4 border-b border-slate-100 dark:border-slate-800">
              <CardTitle className="text-base font-semibold">
                Conversation Thread ({messages.length})
              </CardTitle>
            </CardHeader>
            <CardContent className="p-6 space-y-6">
              <ConversationThread messages={messages} currentUserId={user?.id} />

              <div className="pt-4 border-t border-slate-100 dark:border-slate-800">
                <h4 className="text-sm font-semibold text-slate-800 dark:text-slate-200 mb-3">
                  Post a Response
                </h4>
                <ReplyBox onSendMessage={handleSendMessage} isAgent={isAgent} />
              </div>
            </CardContent>
          </Card>
        </div>

        {/* Right 1 Col: Management / Metadata Sidebar */}
        <div className="space-y-6">
          {/* Ticket Ownership & Assignment Card */}
          <Card>
            <CardHeader className="p-5 pb-3 border-b border-slate-100 dark:border-slate-800">
              <CardTitle className="text-sm font-semibold uppercase tracking-wider text-slate-500">
                Assignment &amp; Ownership
              </CardTitle>
            </CardHeader>
            <CardContent className="p-5 space-y-4 text-sm">
              <div>
                <p className="text-xs text-slate-400 mb-1">Customer</p>
                <div className="flex items-center gap-2 font-medium text-slate-800 dark:text-slate-200">
                  <div className="h-7 w-7 rounded-full bg-slate-100 flex items-center justify-center text-slate-600 dark:bg-slate-800">
                    <UserIcon className="h-4 w-4" />
                  </div>
                  <div>
                    <div>{ticket.customer?.fullName}</div>
                    <div className="text-xs text-slate-400 font-normal">{ticket.customer?.email}</div>
                  </div>
                </div>
              </div>

              <div className="pt-2 border-t border-slate-100 dark:border-slate-800">
                <p className="text-xs text-slate-400 mb-1">Assigned Support Agent</p>
                {ticket.assignedAgent ? (
                  <div className="flex items-center gap-2 font-medium text-slate-800 dark:text-slate-200">
                    <div className="h-7 w-7 rounded-full bg-blue-100 flex items-center justify-center text-blue-700 dark:bg-blue-900/50">
                      <Headphones className="h-4 w-4" />
                    </div>
                    <div>
                      <div>{ticket.assignedAgent.fullName}</div>
                      <div className="text-xs text-slate-400 font-normal">{ticket.assignedAgent.email}</div>
                    </div>
                  </div>
                ) : (
                  <p className="text-amber-600 italic font-medium">Unassigned</p>
                )}
              </div>

              {/* Agent Assignment Actions */}
              {isAgent && (
                <div className="pt-3 border-t border-slate-100 dark:border-slate-800 space-y-3">
                  <Button
                    variant="outline"
                    size="sm"
                    className="w-full"
                    onClick={() => handleAssignAgent(user?.id)}
                    isLoading={actionLoading}
                    disabled={ticket.assignedAgent?.id === user?.id}
                  >
                    <UserCheck className="h-4 w-4 mr-1.5 text-blue-600" />
                    {ticket.assignedAgent?.id === user?.id ? 'Assigned to You' : 'Take Ownership'}
                  </Button>

                  <div className="space-y-1.5 pt-1">
                    <label className="text-sm font-medium text-slate-700 dark:text-slate-300">
                      Reassign to Service Agent
                    </label>
                    {agentsLoading ? (
                      <div className="flex items-center gap-2 py-2 px-3 text-xs text-slate-500 bg-slate-50 rounded-lg dark:bg-slate-900 border border-slate-200 dark:border-slate-800">
                        <Loader2 className="h-3.5 w-3.5 animate-spin text-blue-600" />
                        <span>Loading service agents...</span>
                      </div>
                    ) : agentsError ? (
                      <div className="text-xs text-rose-600 dark:text-rose-400 bg-rose-50 dark:bg-rose-950/30 p-2.5 rounded-lg border border-rose-200 dark:border-rose-900">
                        {agentsError}
                      </div>
                    ) : agents.length === 0 ? (
                      <div className="text-xs text-slate-500 italic bg-slate-50 dark:bg-slate-900 p-2.5 rounded-lg border border-slate-200 dark:border-slate-800">
                        No service agents available.
                      </div>
                    ) : (
                      <Select
                        value={ticket.assignedAgent?.id ? String(ticket.assignedAgent.id) : ''}
                        onChange={(e) => {
                          const val = e.target.value;
                          if (val) handleAssignAgent(parseInt(val, 10));
                        }}
                        disabled={actionLoading}
                      >
                        <option value="">Select Agent...</option>
                        {agents.map((ag) => (
                          <option key={ag.id} value={String(ag.id)}>
                            {ag.fullName} ({ag.email})
                          </option>
                        ))}
                      </Select>
                    )}
                  </div>
                </div>
              )}
            </CardContent>
          </Card>

          {/* Ticket Lifecycle & Status Card */}
          <Card>
            <CardHeader className="p-5 pb-3 border-b border-slate-100 dark:border-slate-800">
              <CardTitle className="text-sm font-semibold uppercase tracking-wider text-slate-500">
                Ticket Controls
              </CardTitle>
            </CardHeader>
            <CardContent className="p-5 space-y-4">
              {/* Agent Status and Priority Selectors */}
              {isAgent && (
                <>
                  <Select
                    label="Status Lifecycle"
                    value={ticket.status}
                    onChange={(e) => handleStatusChange(e.target.value as TicketStatus)}
                    disabled={actionLoading}
                  >
                    <option value="OPEN">Open</option>
                    <option value="IN_PROGRESS">In Progress</option>
                    <option value="RESOLVED">Resolved</option>
                    <option value="CLOSED">Closed</option>
                  </Select>

                  <Select
                    label="Priority Level"
                    value={ticket.priority}
                    onChange={(e) => handlePriorityChange(e.target.value as TicketPriority)}
                    disabled={actionLoading}
                  >
                    <option value="LOW">Low</option>
                    <option value="MEDIUM">Medium</option>
                    <option value="HIGH">High</option>
                    <option value="URGENT">Urgent</option>
                  </Select>
                </>
              )}

              {/* Customer Actions: Close Resolved Ticket */}
              {isCustomer && (
                <div className="space-y-3">
                  <div className="flex items-center justify-between text-xs text-slate-500">
                    <span>Current Status:</span>
                    <TicketStatusBadge status={ticket.status} />
                  </div>

                  {ticket.status === 'RESOLVED' && (
                    <div className="pt-2">
                      <p className="text-xs text-slate-600 mb-2">
                        Has your issue been solved? You can confirm and close this ticket.
                      </p>
                      <Button
                        variant="secondary"
                        size="sm"
                        className="w-full text-slate-700 bg-slate-200 hover:bg-slate-300"
                        onClick={() => handleStatusChange('CLOSED')}
                        isLoading={actionLoading}
                      >
                        <XCircle className="h-4 w-4 mr-1.5" />
                        Close Ticket
                      </Button>
                    </div>
                  )}

                  {ticket.status === 'CLOSED' && (
                    <p className="text-xs text-slate-400 italic text-center">
                      This ticket is closed. Post a reply below if you need to reopen it.
                    </p>
                  )}
                </div>
              )}
            </CardContent>
          </Card>
        </div>
      </div>
    </div>
  );
};
