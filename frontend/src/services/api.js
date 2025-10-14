import axios from "axios";

const API_BASE_URL =
  import.meta.env.VITE_API_BASE_URL || "http://localhost:8080";

// Create axios instance
const api = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    "Content-Type": "application/json",
  },
});

// Request interceptor - Add JWT token to requests
api.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem("token");
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

// Response interceptor - Handle token expiration
api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      // Only redirect if we're not already on the login/register pages
      const currentPath = window.location.pathname;
      if (
        currentPath !== "/login" &&
        currentPath !== "/register" &&
        currentPath !== "/verify"
      ) {
        localStorage.removeItem("token");
        localStorage.removeItem("user");
        window.location.href = "/login";
      }
    }
    return Promise.reject(error);
  }
);

// Auth API
export const authAPI = {
  register: (userData) => api.post("/api/v1/auth/register", userData),
  login: (credentials) => api.post("/api/v1/auth/login", credentials),
  logout: () => api.post("/api/v1/auth/logout"),
  getCurrentUser: () => api.get("/api/v1/auth/me"),
  forgotPassword: (email) =>
    api.post("/api/v1/auth/forgot-password", { email }),
  resetPassword: (token, newPassword) =>
    api.post("/api/v1/auth/reset-password", { token, newPassword }),
  resendVerification: (email) =>
    api.post("/api/v1/auth/resend-verification", { email }),
  verifyEmail: (token) => api.get(`/api/v1/auth/verify-email?token=${token}`),
};

// Chat API
export const chatAPI = {
  // Send message (non-streaming)
  sendMessage: (message, sessionId = null) => {
    const payload = { message };
    if (sessionId) {
      return api.post(`/api/v1/chat/sessions/${sessionId}`, payload);
    }
    return api.post("/api/v1/chat", payload);
  },

  // Send message (streaming) - Returns EventSource URL
  getStreamUrl: (sessionId = null) => {
    const token = localStorage.getItem("token");
    const baseUrl = sessionId
      ? `${API_BASE_URL}/api/v1/chat/sessions/${sessionId}/stream`
      : `${API_BASE_URL}/api/v1/chat/stream`;
    return `${baseUrl}?token=${token}`;
  },
};

// Session API
export const sessionAPI = {
  getSessions: () => api.get("/api/v1/sessions"),
  getSession: (sessionId) => api.get(`/api/v1/sessions/${sessionId}`),
  deleteSession: (sessionId) => api.delete(`/api/v1/sessions/${sessionId}`),
  updateSession: (sessionId, data) =>
    api.put(`/api/v1/sessions/${sessionId}`, data),
};

// Message API
export const messageAPI = {
  getMessages: (sessionId) => api.get(`/api/v1/sessions/${sessionId}/messages`),
  getMessage: (messageId) => api.get(`/api/v1/messages/${messageId}`),
  editMessage: (messageId, content, regenerateResponse = false) =>
    api.put(`/api/v1/messages/${messageId}`, { content, regenerateResponse }),
  deleteMessage: (messageId) => api.delete(`/api/v1/messages/${messageId}`),
  regenerateResponse: (sessionId) =>
    api.post(`/api/v1/sessions/${sessionId}/regenerate`),
};

// Profile API
export const profileAPI = {
  getProfile: () => api.get("/api/v1/user/profile"),
  updateProfile: (data) => api.put("/api/v1/user/profile", data),
  changePassword: (data) =>
    api.post("/api/v1/user/profile/change-password", data),
  deactivateAccount: () => api.post("/api/v1/user/profile/deactivate"),
  reactivateAccount: () => api.post("/api/v1/user/profile/reactivate"),
};

export default api;
