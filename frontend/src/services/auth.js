import api from './api';

export const authService = {
  // Register organization
  registerOrganization: async (data) => {
    const response = await api.post('/organizations/register', data);
    return response.data;
  },

  // Login
  login: async (username, password) => {
    const response = await api.post('/auth/login', { username, password });
    if (response.data.success) {
      const loginData = response.data.data;
      const token = loginData.token;
      const user = {
        id: loginData.userId,
        username: loginData.username,
        email: loginData.email,
        role: loginData.role,
        fullName: loginData.fullName || loginData.username, // Fallback to username if fullName not present
      };
      localStorage.setItem('token', token);
      localStorage.setItem('user', JSON.stringify(user));
      return { success: true, data: { token, user } };
    }
    return response.data;
  },

  // First-time login for students
  firstTimeLogin: async (studentId, email, password) => {
    const response = await api.post('/auth/first-login', {
      studentId,
      email,
      password,
    });
    if (response.data.success) {
      const { token, user } = response.data.data;
      localStorage.setItem('token', token);
      localStorage.setItem('user', JSON.stringify(user));
    }
    return response.data;
  },

  // Get current user
  getCurrentUser: async () => {
    const response = await api.get('/auth/me');
    return response.data;
  },

  // Logout
  logout: () => {
    localStorage.removeItem('token');
    localStorage.removeItem('user');
  },

  // Check if user is authenticated
  isAuthenticated: () => {
    return !!localStorage.getItem('token');
  },

  // Get user from localStorage
  getUser: () => {
    const user = localStorage.getItem('user');
    return user ? JSON.parse(user) : null;
  },
};
