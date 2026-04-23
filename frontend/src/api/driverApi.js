import axiosClient from "./axiosClient";

export const driverApi = {
  // Fetch all trips for the driver's bus
  getTripsByBus: (busId) => axiosClient.get(`/api/trips/bus/${busId}`),
  getTripsByDriver: (driverId) => axiosClient.get(`/api/trips/driver/${driverId}`),

  // Fetch a single trip's detail
  getTrip: (tripId) => axiosClient.get(`/api/trips/${tripId}`),

  // Update the trip's status field
  updateTripStatus: (tripId, status) =>
    axiosClient.put(`/api/trips/${tripId}/status`, { status }),

  // Mark departure event
  depart: (tripId) => axiosClient.post(`/api/trips/${tripId}/depart`),

  // Mark arrival event
  arrive: (tripId) => axiosClient.post(`/api/trips/${tripId}/arrive`),

  validateTicket: (code) => axiosClient.post(`/api/driver/tickets/${code}/validate`),
};
