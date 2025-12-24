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

  // Send message (streaming with Server-Sent Events)
  sendMessageStream: async (message, sessionId = null, onChunk) => {
    const token = localStorage.getItem("token");
    const url = sessionId
      ? `${API_BASE_URL}/api/v1/chat/sessions/${sessionId}/stream`
      : `${API_BASE_URL}/api/v1/chat/stream`;

    console.log("Starting stream to:", url);

    return new Promise(async (resolve, reject) => {
      try {
        const response = await fetch(url, {
          method: "POST",
          headers: {
            "Content-Type": "application/json",
            Authorization: `Bearer ${token}`,
          },
          body: JSON.stringify({ message }),
        });

        console.log("Stream response status:", response.status);

        if (!response.ok) {
          throw new Error(`HTTP error! status: ${response.status}`);
        }

        const reader = response.body.getReader();
        const decoder = new TextDecoder();
        let buffer = "";
        let metadata = {};

        while (true) {
          const { value, done } = await reader.read();
          if (done) {
            console.log("Stream complete");
            break;
          }

          buffer += decoder.decode(value, { stream: true });
          const lines = buffer.split("\n");
          buffer = lines.pop() || "";

          for (const line of lines) {
            if (!line.trim()) continue;

            // SSE format: "event: message" or "data: content"
            if (line.startsWith("event:")) {
              // Skip event type line
              continue;
            } else if (line.startsWith("data:")) {
              // Don't trim the actual content, only remove "data:" prefix
              const dataContent = line.substring(5);

              // Only trim if it looks like JSON (starts with { or [)
              const trimmedCheck = dataContent.trim();
              console.log("SSE data:", trimmedCheck);

              if (trimmedCheck === "[DONE]") {
                resolve(metadata);
                return;
              }

              try {
                // Try to parse as JSON first (for session metadata)
                const data = JSON.parse(trimmedCheck);
                if (data.sessionId) metadata.sessionId = data.sessionId;
                if (data.userMessageId)
                  metadata.userMessageId = data.userMessageId;
              } catch (e) {
                // If not JSON, it's plain text content chunk
                // Use original dataContent (not trimmed) to preserve spaces
                console.log("Chunk:", dataContent);
                onChunk(dataContent);
              }
            }
          }
        }

        resolve(metadata);
      } catch (error) {
        console.error("Streaming error:", error);
        reject(error);
      }
    });
  },

  // Send message (streaming) - Returns EventSource URL (legacy method)
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
  getSessions: (status = null) => {
    const url = status
      ? `/api/v1/sessions?status=${status}`
      : "/api/v1/sessions";
    return api.get(url);
  },
  getActiveSessions: () => api.get("/api/v1/sessions/active"),
  getSession: (sessionId) => api.get(`/api/v1/sessions/${sessionId}`),
  createSession: (title = "New Conversation") =>
    api.post("/api/v1/sessions", { title }),
  updateSession: (sessionId, data) =>
    api.put(`/api/v1/sessions/${sessionId}`, data),
  renameSession: (sessionId, title) =>
    api.put(`/api/v1/sessions/${sessionId}`, { title }),
  deleteSession: (sessionId) => api.delete(`/api/v1/sessions/${sessionId}`),
  archiveSession: (sessionId) =>
    api.post(`/api/v1/sessions/${sessionId}/archive`),
  pauseSession: (sessionId) => api.post(`/api/v1/sessions/${sessionId}/pause`),
  activateSession: (sessionId) =>
    api.post(`/api/v1/sessions/${sessionId}/activate`),
  searchSessions: (query) =>
    api.get("/api/v1/sessions/search", { params: { q: query } }),
  toggleVisibility: (sessionId, isPublic) =>
    api.patch(`/api/v1/sessions/${sessionId}/visibility`, { isPublic }),
  getPublicSessions: () => api.get("/api/v1/sessions/public"),
  copyPublicSession: (sessionId) =>
    api.post(`/api/v1/sessions/public/${sessionId}/copy`),
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

// Project API
export const projectAPI = {
  getProjects: (
    page = 0,
    size = 10,
    archived = null,
    sortBy = "createdAt",
    sortDirection = "desc"
  ) => {
    const params = { page, size, sortBy, sortDirection };
    if (archived !== null) params.archived = archived;
    return api.get("/api/v1/projects", { params });
  },
  getProject: (projectId) => api.get(`/api/v1/projects/${projectId}`),
  createProject: (data) => api.post("/api/v1/projects", data),
  updateProject: (projectId, data) =>
    api.put(`/api/v1/projects/${projectId}`, data),
  deleteProject: (projectId) => api.delete(`/api/v1/projects/${projectId}`),
  archiveProject: (projectId) =>
    api.post(`/api/v1/projects/${projectId}/archive`),
  unarchiveProject: (projectId) =>
    api.post(`/api/v1/projects/${projectId}/unarchive`),
  addSession: (projectId, sessionId) =>
    api.post(`/api/v1/projects/${projectId}/sessions/${sessionId}`),
  removeSession: (projectId, sessionId) =>
    api.delete(`/api/v1/projects/${projectId}/sessions/${sessionId}`),
  searchProjects: (query) =>
    api.get("/api/v1/projects/search", { params: { q: query } }),
};

export default api;
