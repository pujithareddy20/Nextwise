import React, { useEffect, useState } from 'react';
import { useSearchParams } from 'react-router-dom';
import { ticketApi } from '../../api/ticketApi';
import { TicketCategory, TicketListItem, TicketPriority, TicketStatus } from '../../types/ticket.types';
import { TicketFilterBar } from '../../components/tickets/TicketFilterBar';
import { TicketTable } from '../../components/tickets/TicketTable';
import { LoadingSpinner } from '../../components/common/LoadingSpinner';
import { EmptyState } from '../../components/common/EmptyState';
import { Button } from '../../components/ui/Button';
import { ChevronLeft, ChevronRight, Inbox } from 'lucide-react';

export const AllTicketsPage: React.FC = () => {
  const [searchParams, setSearchParams] = useSearchParams();

  const [tickets, setTickets] = useState<TicketListItem[]>([]);
  const [totalElements, setTotalElements] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [isLoading, setIsLoading] = useState(true);

  const search = searchParams.get('search') || '';
  const status = (searchParams.get('status') as TicketStatus) || '';
  const priority = (searchParams.get('priority') as TicketPriority) || '';
  const category = (searchParams.get('category') as TicketCategory) || '';
  const unassigned = searchParams.get('unassigned') === 'true';
  const page = parseInt(searchParams.get('page') || '0', 10);

  const fetchTickets = async () => {
    try {
      setIsLoading(true);
      const res = await ticketApi.getTickets({
        search: search || undefined,
        status: status || undefined,
        priority: priority || undefined,
        category: category || undefined,
        unassigned: unassigned ? true : undefined,
        page,
        size: 10,
        sortBy: 'createdAt',
        sortDir: 'desc',
      });
      setTickets(res.content);
      setTotalElements(res.totalElements);
      setTotalPages(res.totalPages);
    } catch (err) {
      console.error('Failed to load tickets', err);
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    fetchTickets();
  }, [search, status, priority, category, unassigned, page]);

  const updateParam = (key: string, value: string) => {
    const params = new URLSearchParams(searchParams);
    if (value) {
      params.set(key, value);
    } else {
      params.delete(key);
    }
    params.set('page', '0');
    setSearchParams(params);
  };

  const handlePageChange = (newPage: number) => {
    const params = new URLSearchParams(searchParams);
    params.set('page', newPage.toString());
    setSearchParams(params);
  };

  const handleReset = () => {
    setSearchParams(new URLSearchParams());
  };

  return (
    <div className="space-y-6">
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <h1 className="text-2xl font-bold tracking-tight text-slate-900 dark:text-slate-100">
            Support Ticket Management Board
          </h1>
          <p className="text-sm text-slate-500 dark:text-slate-400 mt-1">
            Browse, filter, assign, and manage all customer support tickets across the organization.
          </p>
        </div>

        <div className="flex items-center gap-2">
          <Button
            variant={unassigned ? 'primary' : 'outline'}
            size="sm"
            onClick={() => updateParam('unassigned', unassigned ? '' : 'true')}
          >
            <Inbox className="h-4 w-4 mr-1.5" />
            {unassigned ? 'Showing Unassigned' : 'Filter Unassigned Only'}
          </Button>
        </div>
      </div>

      <TicketFilterBar
        search={search}
        onSearchChange={(val) => updateParam('search', val)}
        status={status}
        onStatusChange={(val) => updateParam('status', val)}
        priority={priority}
        onPriorityChange={(val) => updateParam('priority', val)}
        category={category}
        onCategoryChange={(val) => updateParam('category', val)}
        onReset={handleReset}
      />

      {isLoading ? (
        <LoadingSpinner text="Fetching tickets..." className="min-h-[30vh]" />
      ) : tickets.length === 0 ? (
        <EmptyState
          title="No tickets found"
          description={
            search || status || priority || category || unassigned
              ? "No tickets match your active filter criteria. Try clearing some filters."
              : "No support tickets found in the system."
          }
          actionLabel="Clear Filters"
          onAction={handleReset}
        />
      ) : (
        <div className="space-y-4">
          <TicketTable tickets={tickets} isAgent={true} />

          {totalPages > 1 && (
            <div className="flex items-center justify-between px-2 text-sm text-slate-500">
              <div>
                Showing Page {page + 1} of {totalPages} ({totalElements} total tickets)
              </div>
              <div className="flex items-center gap-2">
                <Button
                  variant="outline"
                  size="sm"
                  disabled={page === 0}
                  onClick={() => handlePageChange(page - 1)}
                >
                  <ChevronLeft className="h-4 w-4 mr-1" />
                  Previous
                </Button>
                <Button
                  variant="outline"
                  size="sm"
                  disabled={page >= totalPages - 1}
                  onClick={() => handlePageChange(page + 1)}
                >
                  Next
                  <ChevronRight className="h-4 w-4 ml-1" />
                </Button>
              </div>
            </div>
          )}
        </div>
      )}
    </div>
  );
};
