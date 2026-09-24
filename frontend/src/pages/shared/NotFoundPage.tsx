import React from 'react';
import { useNavigate } from 'react-router-dom';
import { Button } from '../../components/ui/Button';
import { FileQuestion, ArrowLeft } from 'lucide-react';

export const NotFoundPage: React.FC = () => {
  const navigate = useNavigate();

  return (
    <div className="min-h-screen bg-slate-50 flex items-center justify-center p-6 dark:bg-slate-950">
      <div className="max-w-md w-full text-center space-y-4">
        <div className="mx-auto flex h-16 w-16 items-center justify-center rounded-2xl bg-slate-100 text-slate-500 dark:bg-slate-900">
          <FileQuestion className="h-8 w-8" />
        </div>
        <h1 className="text-3xl font-bold text-slate-900 dark:text-slate-100">404</h1>
        <p className="text-base text-slate-600 dark:text-slate-400">
          The page or support ticket you are looking for does not exist.
        </p>
        <Button onClick={() => navigate('/')}>
          <ArrowLeft className="h-4 w-4 mr-1.5" />
          Back to Home
        </Button>
      </div>
    </div>
  );
};
