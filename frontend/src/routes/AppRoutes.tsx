import React from 'react';
import { Routes, Route, Navigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { LoginPage } from '../pages/auth/LoginPage';
import { RegisterCustomerPage } from '../pages/auth/RegisterCustomerPage';
import { RegisterAgentPage } from '../pages/auth/RegisterAgentPage';
import { CustomerDashboardPage } from '../pages/customer/CustomerDashboardPage';
import { CreateTicketPage } from '../pages/customer/CreateTicketPage';
import { MyTicketsPage } from '../pages/customer/MyTicketsPage';
import { AgentDashboardPage } from '../pages/agent/AgentDashboardPage';
import { AllTicketsPage } from '../pages/agent/AllTicketsPage';
import { TicketDetailsPage } from '../pages/shared/TicketDetailsPage';
import { NotFoundPage } from '../pages/shared/NotFoundPage';
import { UnauthorizedPage } from '../pages/shared/UnauthorizedPage';
import { ProtectedRoute } from './ProtectedRoute';
import { DashboardLayout } from '../components/layout/DashboardLayout';
import { LoadingSpinner } from '../components/common/LoadingSpinner';

const HomeRedirect: React.FC = () => {
  const { isAuthenticated, isCustomer, isAgent, isLoading } = useAuth();

  if (isLoading) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-slate-50">
        <LoadingSpinner text="Loading SupportDesk..." />
      </div>
    );
  }

  if (!isAuthenticated) {
    return <Navigate to="/login" replace />;
  }

  if (isCustomer) {
    return <Navigate to="/dashboard" replace />;
  }

  if (isAgent) {
    return <Navigate to="/agent/dashboard" replace />;
  }

  return <Navigate to="/login" replace />;
};

export const AppRoutes: React.FC = () => {
  return (
    <Routes>
      {/* Public Authentication Routes */}
      <Route path="/login" element={<LoginPage />} />
      <Route path="/register" element={<Navigate to="/register/customer" replace />} />
      <Route path="/register/customer" element={<RegisterCustomerPage />} />
      <Route path="/register/agent" element={<RegisterAgentPage />} />
      <Route path="/" element={<HomeRedirect />} />

      {/* Protected Dashboard Layout */}
      <Route
        element={
          <ProtectedRoute>
            <DashboardLayout />
          </ProtectedRoute>
        }
      >
        {/* Customer-only Routes */}
        <Route
          path="/dashboard"
          element={
            <ProtectedRoute allowedRoles={['ROLE_CUSTOMER']}>
              <CustomerDashboardPage />
            </ProtectedRoute>
          }
        />
        <Route
          path="/tickets"
          element={
            <ProtectedRoute allowedRoles={['ROLE_CUSTOMER']}>
              <MyTicketsPage />
            </ProtectedRoute>
          }
        />
        <Route
          path="/tickets/new"
          element={
            <ProtectedRoute allowedRoles={['ROLE_CUSTOMER', 'ROLE_AGENT', 'ROLE_SUPPORT_AGENT']}>
              <CreateTicketPage />
            </ProtectedRoute>
          }
        />

        {/* Support Agent Routes */}
        <Route
          path="/agent/dashboard"
          element={
            <ProtectedRoute allowedRoles={['ROLE_AGENT', 'ROLE_SUPPORT_AGENT']}>
              <AgentDashboardPage />
            </ProtectedRoute>
          }
        />
        <Route
          path="/agent/tickets"
          element={
            <ProtectedRoute allowedRoles={['ROLE_AGENT', 'ROLE_SUPPORT_AGENT']}>
              <AllTicketsPage />
            </ProtectedRoute>
          }
        />

        {/* Shared Authenticated Routes */}
        <Route path="/tickets/:id" element={<TicketDetailsPage />} />
      </Route>

      {/* Error & Fallback Routes */}
      <Route path="/unauthorized" element={<UnauthorizedPage />} />
      <Route path="*" element={<NotFoundPage />} />
    </Routes>
  );
};
