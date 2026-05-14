import axios from 'axios';
import { useAuthStore } from '../stores/auth';

export const api = axios.create({ baseURL: import.meta.env.VITE_API_URL ?? '/api/v1' });

api.interceptors.request.use((config) => {
  const token = useAuthStore.getState().accessToken;
  if (token) config.headers.Authorization = `Bearer ${token}`;
  return config;
});

api.interceptors.response.use(r => r, async (error) => {
  const state = useAuthStore.getState();
  if (error.response?.status === 401 && state.refreshToken) {
    const res = await axios.post(`${api.defaults.baseURL}/auth/refresh`, { refreshToken: state.refreshToken });
    state.setSession(res.data.data);
    error.config.headers.Authorization = `Bearer ${res.data.data.accessToken}`;
    return api.request(error.config);
  }
  return Promise.reject(error);
});
