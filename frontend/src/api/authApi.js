import axiosClient from "./axiosClient";

export const authApi = {
  login: (email, password) => axiosClient.post("/api/auth/login", { email, password }),

  // Payload matches SignupDto exactly:
  // firstName, lastName, email, phoneNumber, password, confirmPassword
  signup: ({
    firstName,
    lastName,
    email,
    phoneNumber,
    password,
    confirmPassword,
  }) =>
    axiosClient.post("/api/auth/signup", {
      firstName,
      lastName,
      email,
      phoneNumber,
      password,
      confirmPassword,
    }),
  refresh: (refreshToken) => axiosClient.post("/api/auth/refresh", { refreshToken }),
};
