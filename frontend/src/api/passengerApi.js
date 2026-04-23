import axiosClient from "./axiosClient";

export const passengerApi = {
  // Routes
  getRoutes: () => axiosClient.get("/api/passenger/routes"),
  getActiveRoutes: () => axiosClient.get("/api/passenger/routes/active"),
  getRoute: (id) => axiosClient.get(`/api/passenger/routes/${id}`),
  getRouteStops: (id) => axiosClient.get(`/api/passenger/routes/${id}/stops`),

  // Schedules
  getSchedulesByRoute: (routeId) =>
    axiosClient.get(`/api/passenger/routes/${routeId}/schedules`),

  // Booking
  bookTicket: (data) => axiosClient.post("/api/passenger/tickets/book", data),
  payTicket: (ticketId, data) =>
    axiosClient.post(`/api/passenger/tickets/${ticketId}/pay`, data),

  // My Data
  getMyTickets: () => axiosClient.get("/api/passenger/tickets"),
  getTicket: (id) => axiosClient.get(`/api/passenger/tickets/${id}`),

  // Fare
  getFareEstimate: (params) =>
    axiosClient.get("/api/passenger/fare-estimate", { params }),

  // Auth/Profile
  changePassword: (data) => axiosClient.post("/api/auth/change-password", data),
};
