import axiosClient from "./axiosClient";
import { MOCK_ROUTES, MOCK_SCHEDULES, MOCK_TICKETS } from "./mockData";

const USE_MOCK = true;

export const passengerApi = {
  // ── Routes ────────────────────────────────────────────────────────────────
  getRoutes: () => {
    if (USE_MOCK) return Promise.resolve({ data: MOCK_ROUTES });
    return axiosClient.get("/api/passenger/routes");
  },

  getRoute: (routeId) => {
    if (USE_MOCK) {
      const route = MOCK_ROUTES.find((r) => r.id === routeId || r.routeId === routeId);
      return Promise.resolve({ data: route });
    }
    return axiosClient.get(`/api/passenger/routes/${routeId}`);
  },

  getRouteStops: (routeId) => {
    if (USE_MOCK) {
      const route = MOCK_ROUTES.find((r) => r.id === routeId || r.routeId === routeId);
      return Promise.resolve({ data: route?.stops || [] });
    }
    return axiosClient.get(`/api/passenger/routes/${routeId}/stops`);
  },

  searchRoute: (params) => {
    if (USE_MOCK) {
      const query = params.query?.toLowerCase() || "";
      const filtered = MOCK_ROUTES.filter(
        (r) =>
          r.name.toLowerCase().includes(query) ||
          r.origin.toLowerCase().includes(query) ||
          r.destination.toLowerCase().includes(query)
      );
      return Promise.resolve({ data: filtered });
    }
    return axiosClient.post("/api/passenger/search-route", params);
  },

  // ── Schedules ─────────────────────────────────────────────────────────────
  getSchedules: () => {
    if (USE_MOCK) return Promise.resolve({ data: MOCK_SCHEDULES });
    return axiosClient.get("/api/passenger/schedules");
  },

  getSchedule: (scheduleId) => {
    if (USE_MOCK) {
      const schedule = MOCK_SCHEDULES.find((s) => s.id === scheduleId || s.scheduleId === scheduleId);
      return Promise.resolve({ data: schedule });
    }
    return axiosClient.get(`/api/passenger/schedules/${scheduleId}`);
  },

  getSchedulesByRoute: (routeId) => {
    if (USE_MOCK) {
      const filtered = MOCK_SCHEDULES.filter((s) => s.routeId === routeId);
      return Promise.resolve({ data: filtered });
    }
    return axiosClient.get(`/api/passenger/routes/${routeId}/schedules`);
  },

  // ── Fare estimate ─────────────────────────────────────────────────────────
  getFareParams: () => axiosClient.get("/api/passenger/fare-estimate"),

  calculateFare: (params) => {
    if (USE_MOCK) return Promise.resolve({ data: { fare: 25.0 } });
    return axiosClient.post("/api/passenger/fare-estimate", params);
  },

  // ── Tickets ───────────────────────────────────────────────────────────────
  getTickets: () => {
    if (USE_MOCK) return Promise.resolve({ data: MOCK_TICKETS });
    return axiosClient.get("/api/passenger/tickets");
  },

  getTicket: (ticketId) => {
    if (USE_MOCK) {
      const ticket = MOCK_TICKETS.find((t) => t.id === ticketId || t.ticketId === ticketId);
      return Promise.resolve({ data: ticket });
    }
    return axiosClient.get(`/api/passenger/tickets/${ticketId}`);
  },

  bookTicket: (payload) => {
    if (USE_MOCK) {
      const newTicket = {
        id: Math.floor(Math.random() * 1000),
        ticketCode: "TKT-NEW-MOCK",
        ...payload,
        status: "BOOKED",
        purchaseDate: new Date().toISOString(),
      };
      return Promise.resolve({ data: newTicket });
    }
    return axiosClient.post("/api/passenger/tickets/book", payload);
  },

  payTicket: (ticketId, paymentPayload) => {
    if (USE_MOCK) return Promise.resolve({ data: { status: "PAID" } });
    return axiosClient.post(`/api/passenger/tickets/${ticketId}/pay`, paymentPayload);
  },

  // ── Ticket validation ─────────────────────────────────────────────────────
  validateTicket: (code) => {
    if (USE_MOCK) return Promise.resolve({ data: { valid: true, ticket: MOCK_TICKETS[0] } });
    return axiosClient.post(`/api/passenger/tickets/${code}/validate`);
  },
};
