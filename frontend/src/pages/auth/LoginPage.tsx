import React, { useState } from 'react';
import { Link, useNavigate, useLocation } from 'react-router-dom';
import { useAuth, isCustomerRole } from '../../context/AuthContext';
import { authApi } from '../../api/authApi';
import { Button } from '../../components/ui/Button';
import { Input } from '../../components/ui/Input';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '../../components/ui/Card';
import { Headphones, ShieldAlert, UserCheck, CheckCircle2 } from 'lucide-react';

export const LoginPage: React.FC = () => {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const [isLoading, setIsLoading] = useState(false);

  const { login } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();

  const from = location.state?.from?.pathname || null;
  const registeredMessage = location.state?.registeredMessage || null;

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!email.trim() || !password.trim()) {
      setError('Please fill in both email and password.');
      return;
    }

    try {
      setIsLoading(true);
      setError('');
      const data = await authApi.login({ email: email.trim(), password });
      login(data.accessToken, data.user);

      if (from) {
        navigate(from, { replace: true });
      } else if (isCustomerRole(data.user.role)) {
        navigate('/dashboard', { replace: true });
      } else {
        navigate('/agent/dashboard', { replace: true });
      }
    } catch (err: any) {
      const msg = err.response?.data?.message || 'Invalid email or password. Please verify your credentials.';
      setError(msg);
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className="min-h-screen bg-slate-50 flex flex-col justify-center py-12 sm:px-6 lg:px-8 dark:bg-slate-950">
      <div className="sm:mx-auto sm:w-full sm:max-w-md text-center mb-6">
        <div className="mx-auto flex h-12 w-12 items-center justify-center rounded-xl bg-blue-600 text-white shadow-md shadow-blue-500/20 mb-3">
          <Headphones className="h-6 w-6" />
        </div>
        <h2 className="text-2xl font-bold tracking-tight text-slate-900 dark:text-slate-100">
          SupportDesk
        </h2>
        <p className="text-sm text-slate-500 dark:text-slate-400 mt-1">
          Customer Support &amp; Issue Ticketing System
        </p>
      </div>

      <div className="sm:mx-auto sm:w-full sm:max-w-md px-4 sm:px-0 space-y-4">
        <Card className="shadow-lg">
          <CardHeader className="space-y-1">
            <CardTitle className="text-xl">Sign in to your account</CardTitle>
            <CardDescription>
              Enter your email and password to access your account
            </CardDescription>
          </CardHeader>
          <CardContent>
            {registeredMessage && !error && (
              <div className="mb-4 rounded-lg bg-emerald-50 p-3 text-sm text-emerald-800 border border-emerald-200 flex items-start gap-2.5 dark:bg-emerald-950/30 dark:border-emerald-900 dark:text-emerald-300">
                <CheckCircle2 className="h-5 w-5 shrink-0 mt-0.5 text-emerald-600" />
                <span>{registeredMessage}</span>
              </div>
            )}

            {error && (
              <div className="mb-4 rounded-lg bg-rose-50 p-3 text-sm text-rose-700 border border-rose-200 flex items-start gap-2.5 dark:bg-rose-950/30 dark:border-rose-900 dark:text-rose-300">
                <ShieldAlert className="h-5 w-5 shrink-0 mt-0.5 text-rose-600" />
                <span>{error}</span>
              </div>
            )}

            <form onSubmit={handleSubmit} className="space-y-4">
              <Input
                label="Email Address"
                type="email"
                id="email"
                placeholder="name@example.com"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                autoComplete="email"
                required
              />

              <Input
                label="Password"
                type="password"
                id="password"
                placeholder="••••••••"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                autoComplete="current-password"
                required
              />

              <Button type="submit" className="w-full" isLoading={isLoading}>
                Sign In
              </Button>
            </form>

            {/* Registration Options: Customer & Service Agent */}
            <div className="mt-6 pt-4 border-t border-slate-100 dark:border-slate-800 space-y-3">
              <p className="text-center text-xs font-medium text-slate-500 uppercase tracking-wider">
                Don't have an account? Select an option:
              </p>
              <div className="grid grid-cols-2 gap-2">
                <Link
                  to="/register/customer"
                  className="flex items-center justify-center gap-1.5 px-3 py-2 text-xs font-semibold text-blue-700 bg-blue-50 border border-blue-200 rounded-lg hover:bg-blue-100 dark:bg-blue-950/40 dark:border-blue-900 dark:text-blue-300 transition-colors text-center"
                >
                  <UserCheck className="h-3.5 w-3.5" />
                  <span>Register as Customer</span>
                </Link>

                <Link
                  to="/register/agent"
                  className="flex items-center justify-center gap-1.5 px-3 py-2 text-xs font-semibold text-emerald-700 bg-emerald-50 border border-emerald-200 rounded-lg hover:bg-emerald-100 dark:bg-emerald-950/40 dark:border-emerald-900 dark:text-emerald-300 transition-colors text-center"
                >
                  <Headphones className="h-3.5 w-3.5" />
                  <span>Register as Service Agent</span>
                </Link>
              </div>
            </div>
          </CardContent>
        </Card>
      </div>
    </div>
  );
};
