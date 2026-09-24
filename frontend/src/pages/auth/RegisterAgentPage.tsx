import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { authApi } from '../../api/authApi';
import { Button } from '../../components/ui/Button';
import { Input } from '../../components/ui/Input';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '../../components/ui/Card';
import { Badge } from '../../components/ui/Badge';
import { Headphones, ShieldAlert, CheckCircle2, ShieldCheck, KeyRound } from 'lucide-react';

export const RegisterAgentPage: React.FC = () => {
  const [fullName, setFullName] = useState('');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [agentId, setAgentId] = useState('');
  const [error, setError] = useState('');
  const [fieldErrors, setFieldErrors] = useState<{ [key: string]: string }>({});
  const [isLoading, setIsLoading] = useState(false);

  const navigate = useNavigate();

  const validateForm = (): boolean => {
    const errs: { [key: string]: string } = {};

    if (!fullName.trim()) {
      errs.fullName = 'Full Name is required.';
    }

    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    if (!email.trim()) {
      errs.email = 'Email Address is required.';
    } else if (!emailRegex.test(email.trim())) {
      errs.email = 'Please enter a valid email format (e.g., agent@supportdesk.com).';
    }

    if (!password) {
      errs.password = 'Password is required.';
    } else if (password.length < 6) {
      errs.password = 'Password must be at least 6 characters.';
    }

    // Agent ID validation
    const trimmedId = agentId.trim();
    if (!trimmedId) {
      errs.agentId = 'Agent ID is mandatory.';
    } else {
      // Must be an integer
      const parsedId = Number(trimmedId);
      if (!Number.isInteger(parsedId) || !/^\d+$/.test(trimmedId)) {
        errs.agentId = 'Incorrect Agent ID. Please enter a valid authorized Agent ID.';
      } else if (parsedId < 123 || parsedId > 127) {
        errs.agentId = 'Incorrect Agent ID. Please enter a valid authorized Agent ID.';
      }
    }

    setFieldErrors(errs);
    return Object.keys(errs).length === 0;
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!validateForm()) {
      return;
    }

    try {
      setIsLoading(true);
      setError('');
      await authApi.registerAgent({
        fullName: fullName.trim(),
        email: email.trim().toLowerCase(),
        password,
        agentId: parseInt(agentId.trim(), 10),
      });

      // Redirect to login page with successful registration message
      navigate('/login', {
        state: {
          registeredMessage: 'Service agent account registered successfully! Please sign in with your email and password.',
        },
        replace: true,
      });
    } catch (err: any) {
      const respData = err.response?.data;
      if (respData?.validationErrors) {
        setFieldErrors(respData.validationErrors);
      }
      const msg = respData?.message || 'Agent registration failed. Please verify your credentials.';
      setError(msg);
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className="min-h-screen bg-slate-50 flex flex-col justify-center py-12 sm:px-6 lg:px-8 dark:bg-slate-950">
      <div className="sm:mx-auto sm:w-full sm:max-w-md text-center mb-6">
        <div className="mx-auto flex h-12 w-12 items-center justify-center rounded-xl bg-emerald-600 text-white shadow-md shadow-emerald-500/20 mb-3">
          <Headphones className="h-6 w-6" />
        </div>
        <h2 className="text-2xl font-bold tracking-tight text-slate-900 dark:text-slate-100">
          SupportDesk
        </h2>
        <p className="text-sm text-slate-500 dark:text-slate-400 mt-1">
          Service Agent Staff Portal Registration
        </p>
      </div>

      <div className="sm:mx-auto sm:w-full sm:max-w-md px-4 sm:px-0">
        <Card className="shadow-lg border-emerald-100 dark:border-emerald-950">
          <CardHeader className="space-y-1">
            <div className="flex items-center justify-between">
              <div className="flex items-center gap-2">
                <ShieldCheck className="h-5 w-5 text-emerald-600" />
                <CardTitle className="text-xl">Register as Service Agent</CardTitle>
              </div>
              <Badge variant="success">Staff Portal</Badge>
            </div>
            <CardDescription>
              Authorized service agents only. Requires a verified organization Agent ID.
            </CardDescription>
          </CardHeader>
          <CardContent>
            {error && (
              <div className="mb-4 rounded-lg bg-rose-50 p-3 text-sm text-rose-700 border border-rose-200 flex items-start gap-2.5 dark:bg-rose-950/30 dark:border-rose-900 dark:text-rose-300">
                <ShieldAlert className="h-5 w-5 shrink-0 mt-0.5 text-rose-600" />
                <span>{error}</span>
              </div>
            )}

            <form onSubmit={handleSubmit} className="space-y-4" noValidate>
              <div>
                <Input
                  label="Full Name"
                  type="text"
                  id="fullName"
                  placeholder="Sarah Agent"
                  value={fullName}
                  onChange={(e) => {
                    setFullName(e.target.value);
                    if (fieldErrors.fullName) setFieldErrors({ ...fieldErrors, fullName: '' });
                  }}
                  error={fieldErrors.fullName}
                  required
                />
              </div>

              <div>
                <Input
                  label="Organization Email Address"
                  type="email"
                  id="email"
                  placeholder="agent@supportdesk.com"
                  value={email}
                  onChange={(e) => {
                    setEmail(e.target.value);
                    if (fieldErrors.email) setFieldErrors({ ...fieldErrors, email: '' });
                  }}
                  error={fieldErrors.email}
                  required
                />
              </div>

              <div>
                <Input
                  label="Authorized Agent ID"
                  type="text"
                  id="agentId"
                  placeholder="Enter your Agent ID"
                  value={agentId}
                  onChange={(e) => {
                    setAgentId(e.target.value);
                    if (fieldErrors.agentId) setFieldErrors({ ...fieldErrors, agentId: '' });
                  }}
                  error={fieldErrors.agentId}
                  required
                />
              </div>

              <div>
                <Input
                  label="Password (min 6 characters)"
                  type="password"
                  id="password"
                  placeholder="••••••••"
                  value={password}
                  onChange={(e) => {
                    setPassword(e.target.value);
                    if (fieldErrors.password) setFieldErrors({ ...fieldErrors, password: '' });
                  }}
                  error={fieldErrors.password}
                  required
                />
              </div>

              <div className="space-y-1.5 pt-1 text-xs text-slate-500">
                <div className="flex items-center gap-1.5">
                  <KeyRound className="h-3.5 w-3.5 text-emerald-600" />
                  <span>Verified Agent ID required for support staff role</span>
                </div>
                <div className="flex items-center gap-1.5">
                  <CheckCircle2 className="h-3.5 w-3.5 text-emerald-600" />
                  <span>Role assigned: <strong>SUPPORT_AGENT</strong></span>
                </div>
              </div>

              <Button type="submit" className="w-full bg-emerald-600 hover:bg-emerald-700 text-white" isLoading={isLoading}>
                Complete Agent Registration
              </Button>
            </form>

            <div className="mt-6 pt-4 border-t border-slate-100 dark:border-slate-800 space-y-2 text-center text-sm">
              <div>
                Not support staff?{' '}
                <Link to="/register/customer" className="font-semibold text-blue-600 hover:text-blue-500">
                  Register as Customer
                </Link>
              </div>
              <div className="text-slate-500 dark:text-slate-400">
                Already registered?{' '}
                <Link to="/login" className="font-semibold text-emerald-600 hover:text-emerald-500">
                  Sign in
                </Link>
              </div>
            </div>
          </CardContent>
        </Card>
      </div>
    </div>
  );
};
