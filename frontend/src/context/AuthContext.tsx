import React, { createContext, useContext, useEffect, useState } from 'react';
import { User } from '../types/auth.types';
import { authApi } from '../api/authApi';

export const isCustomerRole = (role?: string | null): boolean => {
  if (!role) return false;
  const normalized = role.toUpperCase();
  return normalized === 'ROLE_CUSTOMER' || normalized === 'CUSTOMER';
};

export const isAgentRole = (role?: string | null): boolean => {
  if (!role) return false;
  const normalized = role.toUpperCase();
  return (
    normalized === 'ROLE_AGENT' ||
    normalized === 'AGENT' ||
    normalized === 'ROLE_SUPPORT_AGENT' ||
    normalized === 'SUPPORT_AGENT' ||
    normalized === 'ROLE_ADMIN' ||
    normalized === 'ADMIN'
  );
};

export const isAdminRole = (role?: string | null): boolean => {
  if (!role) return false;
  const normalized = role.toUpperCase();
  return normalized === 'ROLE_ADMIN' || normalized === 'ADMIN';
};

interface AuthContextType {
  user: User | null;
  token: string | null;
  isLoading: boolean;
  isAuthenticated: boolean;
  isCustomer: boolean;
  isAgent: boolean;
  isAdmin: boolean;
  login: (token: string, user: User) => void;
  logout: () => void;
  refreshUser: () => Promise<void>;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export const AuthProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const [user, setUser] = useState<User | null>(() => {
    const savedUser = localStorage.getItem('supportdesk_user');
    return savedUser ? JSON.parse(savedUser) : null;
  });
  const [token, setToken] = useState<string | null>(() => {
    return localStorage.getItem('supportdesk_token');
  });
  const [isLoading, setIsLoading] = useState<boolean>(true);

  useEffect(() => {
    const initAuth = async () => {
      const storedToken = localStorage.getItem('supportdesk_token');
      if (storedToken) {
        try {
          const freshUser = await authApi.getMe();
          setUser(freshUser);
          localStorage.setItem('supportdesk_user', JSON.stringify(freshUser));
        } catch {
          localStorage.removeItem('supportdesk_token');
          localStorage.removeItem('supportdesk_user');
          setUser(null);
          setToken(null);
        }
      }
      setIsLoading(false);
    };

    initAuth();
  }, []);

  const login = (newToken: string, newUser: User) => {
    localStorage.setItem('supportdesk_token', newToken);
    localStorage.setItem('supportdesk_user', JSON.stringify(newUser));
    setToken(newToken);
    setUser(newUser);
  };

  const logout = () => {
    localStorage.removeItem('supportdesk_token');
    localStorage.removeItem('supportdesk_user');
    setToken(null);
    setUser(null);
  };

  const refreshUser = async () => {
    try {
      const freshUser = await authApi.getMe();
      setUser(freshUser);
      localStorage.setItem('supportdesk_user', JSON.stringify(freshUser));
    } catch {
      logout();
    }
  };

  const isAuthenticated = !!token && !!user;
  const isCustomer = isCustomerRole(user?.role);
  const isAgent = isAgentRole(user?.role);
  const isAdmin = isAdminRole(user?.role);

  return (
    <AuthContext.Provider
      value={{
        user,
        token,
        isLoading,
        isAuthenticated,
        isCustomer,
        isAgent,
        isAdmin,
        login,
        logout,
        refreshUser,
      }}
    >
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = () => {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
};
