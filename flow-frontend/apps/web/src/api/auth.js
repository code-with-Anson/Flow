import api from './axios';

export const login = (username, password) => {
  return api.post('/auth/login', { username, password });
};

export const register = (username, password) => {
  return api.post('/user', { username, password });
};

export const getCurrentUser = () => {
  return api.get('/user/me');
};
