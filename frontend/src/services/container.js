import api from './api';

export const containerService = {
  // Create container
  createContainer: async (containerData) => {
    const response = await api.post('/containers', containerData);
    return response.data;
  },

  // Get user's containers
  getContainers: async () => {
    const response = await api.get('/containers');
    return response.data;
  },

  // Get container by ID
  getContainer: async (id) => {
    const response = await api.get(`/containers/${id}`);
    return response.data;
  },

  // Stop container
  stopContainer: async (id) => {
    const response = await api.post(`/containers/${id}/stop`);
    return response.data;
  },

  // Restart container
  restartContainer: async (id) => {
    const response = await api.post(`/containers/${id}/restart`);
    return response.data;
  },

  // Delete container
  deleteContainer: async (id) => {
    const response = await api.delete(`/containers/${id}`);
    return response.data;
  },
};
