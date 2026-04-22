import axiosClient from "./axiosClient";
import { MOCK_USER, MOCK_DRIVER } from "./mockData";

const USE_MOCK = true;

// A helper to create a fake JWT payload
function createMockJwt(user) {
  const payload = {
    sub: user.email,
    firstName: user.firstName,
    lastName: user.lastName,
    role: user.role,
    userId: user.id,
    busId: user.busId,
    exp: 4110220800,
  };
  const header = btoa(JSON.stringify({ alg: "HS256", typ: "JWT" }));
  const body = btoa(JSON.stringify(payload)).replace(/=/g, "");
  return `${header}.${body}.mock-signature`;
}

export const authApi = {
  login: (email, password) => {
    if (USE_MOCK) {
      const isDriver = email.toLowerCase().includes("driver");
      const user = isDriver ? MOCK_DRIVER : MOCK_USER;
      const token = createMockJwt(user);

      return Promise.resolve({
        data: {
          token: token,
          accessToken: token,
          refreshToken: "mock-refresh-token",
          user: user,
        },
      });
    }
    return axiosClient.post("/api/auth/login", { email, password });
  },

  // Payload matches SignupDto exactly:
  // firstName, lastName, email, phoneNumber, password, confirmPassword
  signup: ({
    firstName,
    lastName,
    email,
    phoneNumber,
    password,
    confirmPassword,
  }) => {
    if (USE_MOCK) {
      const isDriver = email.toLowerCase().includes("driver");
      const baseUser = isDriver ? MOCK_DRIVER : MOCK_USER;
      const user = { ...baseUser, firstName, lastName, email, phoneNumber };
      const token = createMockJwt(user);

      return Promise.resolve({
        data: {
          token: token,
          accessToken: token,
          refreshToken: "mock-refresh-token",
          user: user,
        },
      });
    }
    return axiosClient.post("/api/auth/signup", {
      firstName,
      lastName,
      email,
      phoneNumber,
      password,
      confirmPassword,
    });
  },
  refresh: (refreshToken) => {
    if (USE_MOCK) {
      return Promise.resolve({
        data: {
          accessToken: "mock-access-token",
          refreshToken: "mock-refresh-token",
        },
      });
    }
    return axiosClient.post("/api/auth/refresh", { refreshToken });
  },
};
