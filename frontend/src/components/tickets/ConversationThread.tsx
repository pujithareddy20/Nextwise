import React from 'react';
import { TicketMessage } from '../../types/ticket.types';
import { Badge } from '../ui/Badge';
import { User, Headphones, Lock, Clock } from 'lucide-react';
import { cn } from '../../lib/utils';
import { isAgentRole } from '../../context/AuthContext';

interface ConversationThreadProps {
  messages: TicketMessage[];
  currentUserId?: number;
}

export const ConversationThread: React.FC<ConversationThreadProps> = ({
  messages,
  currentUserId,
}) => {
  const formatTime = (dateStr: string) => {
    try {
      const date = new Date(dateStr);
      return date.toLocaleString('en-US', {
        month: 'short',
        day: 'numeric',
        hour: '2-digit',
        minute: '2-digit',
      });
    } catch {
      return dateStr;
    }
  };

  if (!messages || messages.length === 0) {
    return (
      <div className="py-8 text-center text-sm text-slate-400">
        No conversation messages yet. Be the first to reply!
      </div>
    );
  }

  return (
    <div className="space-y-4">
      {messages.map((msg) => {
        const isMe = currentUserId === msg.sender?.id;
        const isAgent = isAgentRole(msg.sender?.role);
        const isNote = msg.internalNote;

        return (
          <div
            key={msg.id}
            className={cn(
              'rounded-xl p-4 transition-all border',
              isNote
                ? 'bg-amber-50/80 border-amber-200 dark:bg-amber-950/20 dark:border-amber-900/50'
                : isAgent
                ? 'bg-blue-50/50 border-blue-100 dark:bg-blue-950/20 dark:border-blue-900/50'
                : 'bg-white border-slate-200/80 dark:bg-slate-900 dark:border-slate-800'
            )}
          >
            {/* Message Header */}
            <div className="flex items-center justify-between gap-3 mb-2.5">
              <div className="flex items-center gap-2.5">
                <div
                  className={cn(
                    'h-7 w-7 rounded-full flex items-center justify-center font-bold text-xs',
                    isNote
                      ? 'bg-amber-100 text-amber-800 dark:bg-amber-900 dark:text-amber-200'
                      : isAgent
                      ? 'bg-blue-100 text-blue-800 dark:bg-blue-900 dark:text-blue-200'
                      : 'bg-slate-100 text-slate-700 dark:bg-slate-800 dark:text-slate-200'
                  )}
                >
                  {isAgent ? <Headphones className="h-3.5 w-3.5" /> : <User className="h-3.5 w-3.5" />}
                </div>

                <div className="flex items-center gap-2">
                  <span className="text-sm font-semibold text-slate-900 dark:text-slate-100">
                    {msg.sender?.fullName || 'User'}
                    {isMe && ' (You)'}
                  </span>
                  {isNote ? (
                    <Badge variant="warning" className="flex items-center gap-1 text-[10px]">
                      <Lock className="h-2.5 w-2.5" />
                      Internal Note
                    </Badge>
                  ) : isAgent ? (
                    <Badge variant="info" className="text-[10px]">
                      Support Agent
                    </Badge>
                  ) : (
                    <Badge variant="secondary" className="text-[10px]">
                      Customer
                    </Badge>
                  )}
                </div>
              </div>

              <div className="flex items-center gap-1 text-xs text-slate-400">
                <Clock className="h-3 w-3" />
                <span>{formatTime(msg.createdAt)}</span>
              </div>
            </div>

            {/* Message Body */}
            <div className="text-sm text-slate-700 dark:text-slate-300 leading-relaxed whitespace-pre-wrap pl-9">
              {msg.message}
            </div>
          </div>
        );
      })}
    </div>
  );
};
