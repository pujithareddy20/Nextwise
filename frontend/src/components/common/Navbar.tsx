import React from 'react';
import { useAuth } from '../../context/AuthContext';
import { Badge } from '../ui/Badge';
import { Button } from '../ui/Button';
import { Headphones, LogOut, User as UserIcon } from 'lucide-react';
import { Link } from 'react-router-dom';

export const Navbar: React.FC = () => {
  const { user, logout, isCustomer, isAgent, isAdmin } = useAuth();

  const getRoleLabel = () => {
    if (isAdmin) return 'Administrator';
    if (isAgent) return 'Support Agent';
    if (isCustomer) return 'Customer';
    return 'User';
  };

  const getRoleBadgeVariant = () => {
    if (isAdmin) return 'destructive';
    if (isAgent) return 'success';
    if (isCustomer) return 'info';
    return 'default';
  };

  return (
    <header className="sticky top-0 z-30 flex h-16 w-full items-center justify-between border-b border-slate-200 bg-white/95 px-4 md:px-6 backdrop-blur dark:border-slate-800 dark:bg-slate-900/95">
      <div className="flex items-center gap-3">
        <Link to="/" className="flex items-center gap-2.5 font-bold text-slate-900 dark:text-white">
          <div className="flex h-9 w-9 items-center justify-center rounded-lg bg-blue-600 text-white shadow-sm shadow-blue-500/20">
            <Headphones className="h-5 w-5" />
          </div>
          <span className="text-lg tracking-tight">SupportDesk</span>
        </Link>
      </div>

      {user && (
        <div className="flex items-center gap-4">
          <div className="hidden sm:flex items-center gap-3 pl-3 pr-2 py-1.5 rounded-full bg-slate-50 border border-slate-200/80 dark:bg-slate-800/60 dark:border-slate-700">
            <div className="flex h-7 w-7 items-center justify-center rounded-full bg-blue-100 text-blue-700 font-semibold text-xs dark:bg-blue-900/50 dark:text-blue-300">
              <UserIcon className="h-3.5 w-3.5" />
            </div>
            <div className="flex items-center gap-2">
              <span className="text-xs font-medium text-slate-700 dark:text-slate-200">
                {user.fullName}
              </span>
              <Badge variant={getRoleBadgeVariant() as any}>
                {getRoleLabel()}
              </Badge>
            </div>
          </div>

          <Button
            variant="ghost"
            size="sm"
            onClick={logout}
            className="text-slate-500 hover:text-rose-600 hover:bg-rose-50 dark:hover:bg-rose-950/30"
            title="Log out"
          >
            <LogOut className="h-4 w-4 mr-1.5" />
            <span className="hidden sm:inline">Logout</span>
          </Button>
        </div>
      )}
    </header>
  );
};
