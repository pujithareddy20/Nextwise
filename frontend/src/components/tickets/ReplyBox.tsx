import React, { useState } from 'react';
import { Button } from '../ui/Button';
import { Send, Lock } from 'lucide-react';

interface ReplyBoxProps {
  onSendMessage: (message: string, isInternalNote: boolean) => Promise<void>;
  isAgent?: boolean;
}

export const ReplyBox: React.FC<ReplyBoxProps> = ({ onSendMessage, isAgent = false }) => {
  const [message, setMessage] = useState('');
  const [isInternalNote, setIsInternalNote] = useState(false);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [error, setError] = useState('');

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!message.trim()) {
      setError('Please type a message before sending');
      return;
    }

    try {
      setIsSubmitting(true);
      setError('');
      await onSendMessage(message.trim(), isInternalNote);
      setMessage('');
      setIsInternalNote(false);
    } catch {
      setError('Failed to send message. Please try again.');
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <form onSubmit={handleSubmit} className="space-y-3">
      <div className="relative">
        <textarea
          rows={3}
          value={message}
          onChange={(e) => {
            setMessage(e.target.value);
            if (error) setError('');
          }}
          placeholder={
            isInternalNote
              ? 'Write an internal note (visible only to support agents)...'
              : 'Type your reply here...'
          }
          className={`w-full rounded-xl border p-3.5 text-sm placeholder:text-slate-400 focus:outline-none focus:ring-2 focus:border-transparent dark:bg-slate-900 dark:text-slate-100 ${
            isInternalNote
              ? 'border-amber-300 bg-amber-50/30 focus:ring-amber-500'
              : 'border-slate-300 bg-white focus:ring-blue-500 dark:border-slate-700'
          }`}
        />
        {error && <p className="text-xs text-rose-500 font-medium mt-1">{error}</p>}
      </div>

      <div className="flex items-center justify-between">
        {isAgent ? (
          <label className="flex items-center gap-2 cursor-pointer select-none text-xs text-slate-600 dark:text-slate-300">
            <input
              type="checkbox"
              checked={isInternalNote}
              onChange={(e) => setIsInternalNote(e.target.checked)}
              className="rounded border-slate-300 text-amber-600 focus:ring-amber-500 h-4 w-4"
            />
            <span className="flex items-center gap-1 font-medium">
              <Lock className="h-3 w-3 text-amber-600" />
              Internal Agent Note (Hidden from Customer)
            </span>
          </label>
        ) : (
          <div />
        )}

        <Button
          type="submit"
          size="sm"
          isLoading={isSubmitting}
          variant={isInternalNote ? 'secondary' : 'primary'}
          className={isInternalNote ? 'bg-amber-600 text-white hover:bg-amber-700' : ''}
        >
          <Send className="h-3.5 w-3.5 mr-1.5" />
          {isInternalNote ? 'Add Note' : 'Send Reply'}
        </Button>
      </div>
    </form>
  );
};
