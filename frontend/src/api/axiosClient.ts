import axios from 'axios';

export const axiosClient = axios.create({
  baseURL: '',
  headers: {
    'Content-Type': 'application/json',
  },
});

axiosClient.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('supportdesk_token');
    if (token && config.headers) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => Promise.reject(error)
);

axiosClient.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      const isAuthPage =
        window.location.pathname.startsWith('/login') ||
        window.location.pathname.startsWith('/register');

      if (!isAuthPage) {
        localStorage.removeItem('supportdesk_token');
        localStorage.removeItem('supportdesk_user');
        window.location.href = '/login';
      }
    }
    return Promise.reject(error);
  }
);
