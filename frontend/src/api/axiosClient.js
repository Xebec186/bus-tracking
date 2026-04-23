import axios from "axios";
import { API_BASE_URL } from "../constants";
import {
  forceLogout,
  getAccessToken,
  refreshAccessToken,
  saveTokens,
} from "./authSession";

const axiosClient = axios.create({
  baseURL: API_BASE_URL,
  timeout: 15000,
  headers: {
    "Content-Type": "application/json",
    Accept: "application/json",
  },
});

axiosClient.interceptors.request.use(
  async (config) => {
    const token = await getAccessToken();
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => Promise.reject(error),
);

let isRefreshing = false;
let refreshQueue = [];

function resolveQueuedRequests(error, token = null) {
  refreshQueue.forEach(({ resolve, reject }) => {
    if (error) {
      reject(error);
    } else {
      resolve(token);
    }
  });
  refreshQueue = [];
}

axiosClient.interceptors.response.use(
  (response) => response,
  async (error) => {
    const originalRequest = error.config || {};
    const status = error.response?.status;
    const isAuthEndpoint =
      typeof originalRequest.url === "string" &&
      originalRequest.url.startsWith("/api/auth/");

    if (status !== 401 || originalRequest._retry || isAuthEndpoint) {
      return Promise.reject(error);
    }

    if (isRefreshing) {
      return new Promise((resolve, reject) => {
        refreshQueue.push({ resolve, reject });
      }).then((token) => {
        originalRequest.headers.Authorization = `Bearer ${token}`;
        return axiosClient(originalRequest);
      });
    }

    originalRequest._retry = true;
    isRefreshing = true;

    try {
      const tokenResponse = await refreshAccessToken();
      const newToken = tokenResponse?.token || tokenResponse?.accessToken;
      const newRefreshToken = tokenResponse?.refreshToken;
      if (!newToken) {
        throw new Error("Refresh response missing access token");
      }

      await saveTokens({ token: newToken, refreshToken: newRefreshToken });
      resolveQueuedRequests(null, newToken);
      originalRequest.headers.Authorization = `Bearer ${newToken}`;
      return axiosClient(originalRequest);
    } catch (refreshError) {
      resolveQueuedRequests(refreshError, null);
      await forceLogout();
      return Promise.reject(refreshError);
    } finally {
      isRefreshing = false;
    }
  },
);

export default axiosClient;
