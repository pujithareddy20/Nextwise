import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { ticketApi } from '../../api/ticketApi';
import {
  TicketCategory,
  TicketPriority,
  AiClassifyTicketResponse,
} from '../../types/ticket.types';
import { Button } from '../../components/ui/Button';
import { Input } from '../../components/ui/Input';
import { Textarea } from '../../components/ui/Textarea';
import { Select } from '../../components/ui/Select';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '../../components/ui/Card';
import { TicketCategoryBadge } from '../../components/tickets/TicketCategoryBadge';
import { TicketPriorityBadge } from '../../components/tickets/TicketPriorityBadge';
import {
  ArrowLeft,
  Send,
  ShieldAlert,
  Sparkles,
  Check,
  X,
  AlertCircle,
  Loader2,
} from 'lucide-react';

export const CreateTicketPage: React.FC = () => {
  const navigate = useNavigate();

  const [title, setTitle] = useState('');
  const [description, setDescription] = useState('');
  const [category, setCategory] = useState<TicketCategory>('GENERAL');
  const [priority, setPriority] = useState<TicketPriority>('MEDIUM');
  const [error, setError] = useState('');
  const [isLoading, setIsLoading] = useState(false);

  // AI Classification state
  const [isAiLoading, setIsAiLoading] = useState(false);
  const [aiSuggestion, setAiSuggestion] = useState<AiClassifyTicketResponse | null>(null);
  const [aiError, setAiError] = useState('');
  const [appliedSuccess, setAppliedSuccess] = useState(false);

  const canSuggestAi = title.trim().length >= 5 && description.trim().length >= 10;

  const handleSuggestAi = async () => {
    if (!canSuggestAi || isAiLoading) return;

    try {
      setIsAiLoading(true);
      setAiError('');
      setAppliedSuccess(false);

      const suggestion = await ticketApi.suggestClassification({
        title: title.trim(),
        description: description.trim(),
      });

      setAiSuggestion(suggestion);
    } catch (err: any) {
      const msg =
        err.response?.data?.message ||
        'AI classification service is currently unavailable. You may choose your category and priority manually.';
      setAiError(msg);
    } finally {
      setIsAiLoading(false);
    }
  };

  const handleApplySuggestion = () => {
    if (!aiSuggestion) return;
    setCategory(aiSuggestion.category);
    setPriority(aiSuggestion.priority);
    setAppliedSuccess(true);
    setTimeout(() => setAppliedSuccess(false), 3000);
  };

  const handleDismissSuggestion = () => {
    setAiSuggestion(null);
    setAiError('');
    setAppliedSuccess(false);
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!title.trim() || !description.trim()) {
      setError('Please provide both a title and description for your ticket.');
      return;
    }

    if (title.trim().length < 5) {
      setError('Ticket title must be at least 5 characters long.');
      return;
    }

    if (description.trim().length < 10) {
      setError('Ticket description must be at least 10 characters long.');
      return;
    }

    try {
      setIsLoading(true);
      setError('');
      const created = await ticketApi.createTicket({
        title: title.trim(),
        description: description.trim(),
        category,
        priority,
      });

      navigate(`/tickets/${created.id}`);
    } catch (err: any) {
      const msg = err.response?.data?.message || 'Failed to create ticket. Please check your inputs.';
      setError(msg);
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className="max-w-3xl mx-auto space-y-6">
      <Button
        variant="ghost"
        size="sm"
        onClick={() => navigate(-1)}
        className="text-slate-600 hover:text-slate-900 mb-2"
      >
        <ArrowLeft className="h-4 w-4 mr-1.5" />
        Back
      </Button>

      <Card className="shadow-sm">
        <CardHeader>
          <CardTitle className="text-xl">Create a New Support Ticket</CardTitle>
          <CardDescription>
            Describe your issue with clarity so our engineering and support specialists can help promptly.
          </CardDescription>
        </CardHeader>
        <CardContent>
          {error && (
            <div className="mb-5 rounded-lg bg-rose-50 p-3 text-sm text-rose-700 border border-rose-200 flex items-start gap-2.5 dark:bg-rose-950/30 dark:border-rose-900 dark:text-rose-300">
              <ShieldAlert className="h-5 w-5 shrink-0 mt-0.5 text-rose-600" />
              <span>{error}</span>
            </div>
          )}

          <form onSubmit={handleSubmit} className="space-y-5">
            <Input
              label="Subject / Title"
              id="title"
              placeholder="e.g. Cannot generate export reports in CSV format"
              value={title}
              onChange={(e) => setTitle(e.target.value)}
              required
            />

            <Textarea
              label="Detailed Description"
              id="description"
              rows={6}
              placeholder="Please provide steps to reproduce the issue, what you expected vs what occurred..."
              value={description}
              onChange={(e) => setDescription(e.target.value)}
              required
            />

            {/* AI Assistant Section */}
            <div className="space-y-3 pt-2">
              <div className="flex items-center justify-between flex-wrap gap-2">
                <div>
                  <span className="text-sm font-semibold text-slate-800 dark:text-slate-200">
                    Category & Priority
                  </span>
                  <p className="text-xs text-slate-500 dark:text-slate-400">
                    Choose manually or let AI recommend classifications based on your issue details.
                  </p>
                </div>
                <Button
                  type="button"
                  variant="outline"
                  size="sm"
                  onClick={handleSuggestAi}
                  disabled={!canSuggestAi || isAiLoading}
                  className="border-indigo-300 text-indigo-700 hover:bg-indigo-50 dark:border-indigo-700 dark:text-indigo-300 dark:hover:bg-indigo-950/50 shadow-sm"
                  title={
                    canSuggestAi
                      ? 'Analyze ticket with AI to suggest category and priority'
                      : 'Enter at least 5 characters in Title and 10 characters in Description to enable AI suggestion'
                  }
                >
                  {isAiLoading ? (
                    <>
                      <Loader2 className="h-3.5 w-3.5 mr-1.5 animate-spin text-indigo-600" />
                      Analyzing ticket...
                    </>
                  ) : (
                    <>
                      <Sparkles className="h-3.5 w-3.5 mr-1.5 text-indigo-600 dark:text-indigo-400" />
                      Suggest with AI
                    </>
                  )}
                </Button>
              </div>

              {/* AI Loading State */}
              {isAiLoading && (
                <div className="rounded-lg border border-indigo-100 bg-indigo-50/70 p-3.5 dark:border-indigo-900/40 dark:bg-indigo-950/20 flex items-center gap-2.5 text-indigo-700 dark:text-indigo-300 text-sm animate-pulse">
                  <Loader2 className="h-4 w-4 animate-spin text-indigo-600 dark:text-indigo-400 shrink-0" />
                  <span>Analyzing ticket title and description with AI...</span>
                </div>
              )}

              {/* AI Error / Notice State */}
              {aiError && (
                <div className="rounded-lg border border-amber-200 bg-amber-50/90 p-3.5 dark:border-amber-900/40 dark:bg-amber-950/25 text-amber-800 dark:text-amber-300 text-sm flex items-start justify-between gap-3">
                  <div className="flex items-start gap-2.5">
                    <AlertCircle className="h-4 w-4 text-amber-600 dark:text-amber-400 shrink-0 mt-0.5" />
                    <div>
                      <span className="font-semibold text-xs uppercase tracking-wider text-amber-900 dark:text-amber-200">
                        AI Notice
                      </span>
                      <p className="text-xs text-amber-700 dark:text-amber-300 mt-0.5">{aiError}</p>
                    </div>
                  </div>
                  <button
                    type="button"
                    onClick={() => setAiError('')}
                    className="text-amber-600 hover:text-amber-800 dark:text-amber-400 dark:hover:text-amber-200 p-0.5"
                    title="Dismiss"
                  >
                    <X className="h-3.5 w-3.5" />
                  </button>
                </div>
              )}

              {/* AI Suggestions Card */}
              {aiSuggestion && (
                <div className="rounded-xl border border-indigo-200 bg-gradient-to-br from-indigo-50/80 via-white to-slate-50 p-4 shadow-sm dark:border-indigo-900/50 dark:from-indigo-950/30 dark:via-slate-900 dark:to-slate-900/60 transition-all">
                  <div className="flex items-center justify-between pb-2.5 border-b border-indigo-100 dark:border-indigo-900/40">
                    <div className="flex items-center gap-2 text-indigo-950 dark:text-indigo-200 font-semibold text-sm">
                      <Sparkles className="h-4 w-4 text-indigo-600 dark:text-indigo-400" />
                      <span>AI Suggestions</span>
                    </div>
                    <button
                      type="button"
                      onClick={handleDismissSuggestion}
                      className="text-slate-400 hover:text-slate-600 dark:hover:text-slate-200 p-1"
                      title="Dismiss suggestion"
                    >
                      <X className="h-4 w-4" />
                    </button>
                  </div>

                  <div className="mt-3 grid grid-cols-1 sm:grid-cols-2 gap-3">
                    <div className="flex items-center gap-2">
                      <span className="text-xs text-slate-500 dark:text-slate-400 font-medium">
                        Suggested Category:
                      </span>
                      <TicketCategoryBadge category={aiSuggestion.category} />
                    </div>
                    <div className="flex items-center gap-2">
                      <span className="text-xs text-slate-500 dark:text-slate-400 font-medium">
                        Suggested Priority:
                      </span>
                      <TicketPriorityBadge priority={aiSuggestion.priority} />
                    </div>
                  </div>

                  {aiSuggestion.reason && (
                    <div className="mt-3 text-xs text-slate-700 dark:text-slate-300 bg-white/80 dark:bg-slate-800/70 p-2.5 rounded-lg border border-indigo-100/70 dark:border-indigo-900/30 leading-relaxed">
                      <span className="font-semibold text-indigo-900 dark:text-indigo-300">Reason: </span>
                      {aiSuggestion.reason}
                    </div>
                  )}

                  <div className="mt-3 flex items-center justify-between gap-3 pt-1">
                    <span className="text-[11px] text-slate-400">
                      Suggestions are optional. You can review or edit anytime.
                    </span>
                    <Button
                      type="button"
                      size="sm"
                      onClick={handleApplySuggestion}
                      className={
                        appliedSuccess
                          ? 'bg-emerald-600 hover:bg-emerald-700 text-white'
                          : 'bg-indigo-600 hover:bg-indigo-700 text-white'
                      }
                    >
                      {appliedSuccess ? (
                        <>
                          <Check className="h-3.5 w-3.5 mr-1" />
                          Applied to fields!
                        </>
                      ) : (
                        <>
                          <Check className="h-3.5 w-3.5 mr-1" />
                          Apply Suggestions
                        </>
                      )}
                    </Button>
                  </div>
                </div>
              )}

              {/* Category and Priority Select Dropdowns */}
              <div className="grid grid-cols-1 sm:grid-cols-2 gap-4 pt-1">
                <Select
                  label="Category"
                  id="category"
                  value={category}
                  onChange={(e) => setCategory(e.target.value as TicketCategory)}
                >
                  <option value="GENERAL">General Inquiries</option>
                  <option value="TECHNICAL">Technical & Bugs</option>
                  <option value="BILLING">Billing & Invoices</option>
                  <option value="ACCOUNT">Account Management</option>
                  <option value="FEATURE_REQUEST">Feature Request</option>
                </Select>

                <Select
                  label="Urgency / Priority"
                  id="priority"
                  value={priority}
                  onChange={(e) => setPriority(e.target.value as TicketPriority)}
                >
                  <option value="LOW">Low — Minor question or cosmetic issue</option>
                  <option value="MEDIUM">Medium — Standard support request</option>
                  <option value="HIGH">High — Functionality blocked / urgent</option>
                  <option value="URGENT">Urgent — Complete service outage</option>
                </Select>
              </div>
            </div>

            <div className="flex items-center justify-end gap-3 pt-3 border-t border-slate-100 dark:border-slate-800">
              <Button
                type="button"
                variant="outline"
                onClick={() => navigate('/tickets')}
              >
                Cancel
              </Button>
              <Button type="submit" isLoading={isLoading}>
                <Send className="h-4 w-4 mr-1.5" />
                Submit Ticket
              </Button>
            </div>
          </form>
        </CardContent>
      </Card>
    </div>
  );
};
