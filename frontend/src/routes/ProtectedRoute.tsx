import React from 'react';
import { Navigate, useLocation } from 'react-router-dom';
import { useAuth, isCustomerRole, isAgentRole } from '../context/AuthContext';
import { Role } from '../types/auth.types';
import { LoadingSpinner } from '../components/common/LoadingSpinner';

interface ProtectedRouteProps {
  children: React.ReactElement;
  allowedRoles?: Role[];
}

export const ProtectedRoute: React.FC<ProtectedRouteProps> = ({
  children,
  allowedRoles,
}) => {
  const { isAuthenticated, isLoading, user } = useAuth();
  const location = useLocation();

  if (isLoading) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-slate-50">
        <LoadingSpinner text="Checking authentication..." />
      </div>
    );
  }

  if (!isAuthenticated || !user) {
    return <Navigate to="/login" state={{ from: location }} replace />;
  }

  if (allowedRoles && allowedRoles.length > 0) {
    const hasPermission = allowedRoles.some((allowedRole) => {
      if (allowedRole === 'ROLE_CUSTOMER' || allowedRole === 'CUSTOMER') {
        return isCustomerRole(user.role);
      }
      if (
        allowedRole === 'ROLE_AGENT' ||
        allowedRole === 'AGENT' ||
        allowedRole === 'ROLE_SUPPORT_AGENT' ||
        allowedRole === 'SUPPORT_AGENT'
      ) {
        return isAgentRole(user.role);
      }
      return user.role === allowedRole;
    });

    if (!hasPermission) {
      if (isCustomerRole(user.role)) {
        return <Navigate to="/dashboard" replace />;
      }
      return <Navigate to="/agent/dashboard" replace />;
    }
  }

  return children;
};
