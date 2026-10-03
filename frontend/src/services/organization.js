import api from './api';

export const organizationService = {
  // Get organization statistics
  getStats: async () => {
    const response = await api.get('/organizations/stats');
    return response.data;
  },

  // Get organization details
  getOrganization: async () => {
    const response = await api.get('/organizations/me');
    return response.data;
  },

  // Update organization
  updateOrganization: async (data) => {
    const response = await api.put('/organizations/me', data);
    return response.data;
  },

  // Get devices with statistics
  getDevices: async () => {
    const response = await api.get('/organizations/devices');
    return response.data;
  },

  // Generate device enrollment token
  generateEnrollmentToken: async () => {
    const response = await api.post('/organizations/devices/token');
    return response.data;
  },

  // Get students with statistics
  getStudents: async () => {
    const response = await api.get('/organizations/students');
    return response.data;
  },

  // Upload students CSV
  uploadStudents: async (csvData) => {
    const response = await api.post('/organizations/students/upload', csvData);
    return response.data;
  },
};
