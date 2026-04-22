import axiosClient from './axiosClient';
import { MOCK_TRIPS } from './mockData';

const USE_MOCK = true;

export const driverApi = {
  // Fetch all trips for the driver's bus
  getTripsByBus: (busId) => {
    if (USE_MOCK) return Promise.resolve({ data: MOCK_TRIPS });
    return axiosClient.get(`/api/trips/bus/${busId}`);
  },

  // Fetch a single trip's detail
  getTrip: (tripId) => {
    if (USE_MOCK) {
      const trip = MOCK_TRIPS.find((t) => t.id === tripId || t.tripId === tripId);
      return Promise.resolve({ data: trip });
    }
    return axiosClient.get(`/api/trips/${tripId}`);
  },

  // Update the trip's status field
  updateTripStatus: (tripId, status) => {
    if (USE_MOCK) return Promise.resolve({ data: { status } });
    return axiosClient.put(`/api/trips/${tripId}/status`, { status });
  },

  // Mark departure event
  depart: (tripId) => {
    if (USE_MOCK) return Promise.resolve({ data: { status: 'DEPARTED' } });
    return axiosClient.post(`/api/trips/${tripId}/depart`);
  },

  // Mark arrival event
  arrive: (tripId) => {
    if (USE_MOCK) return Promise.resolve({ data: { status: 'ARRIVED' } });
    return axiosClient.post(`/api/trips/${tripId}/arrive`);
  },
};
