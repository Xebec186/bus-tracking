import axiosClient from "./axiosClient";

export const trackingApi = {
  getBuses: () => axiosClient.get("/api/tracking/buses"),

  getRouteTracking: (routeId) => axiosClient.get(`/api/tracking/routes/${routeId}`),

  getRoutePath: (routeId) => axiosClient.get(`/api/tracking/routes/${routeId}/path`),
};
