import axiosClient from './axiosClient';
import { MOCK_BUS_LOCATIONS } from './mockData';

const USE_MOCK = true;

export const trackingApi = {
  getBuses: () => {
    if (USE_MOCK) return Promise.resolve({ data: MOCK_BUS_LOCATIONS });
    return axiosClient.get('/api/tracking/buses');
  },

  getRouteTracking: (routeId) => {
    // Return mock coordinates if routeId is provided
    if (USE_MOCK) {
      return Promise.resolve({ 
        data: { 
          coordinates: [
            { latitude: 5.6037, longitude: -0.1870 },
            { latitude: 5.6050, longitude: -0.1885 },
            { latitude: 5.6080, longitude: -0.1900 },
          ] 
        } 
      });
    }
    return axiosClient.get(`/api/tracking/routes/${routeId}`);
  },
};
