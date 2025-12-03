import axios from "axios";

const API_BASE_URL =
  import.meta.env.VITE_API_BASE_URL || "http://localhost:8080";

// Create admin axios instance
const adminApi = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    "Content-Type": "application/json",
  },
});

// Request interceptor - Add admin JWT token to requests
adminApi.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem("adminToken");
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
adminApi.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      const currentPath = window.location.pathname;
      if (currentPath.startsWith("/admin") && currentPath !== "/admin/login") {
        localStorage.removeItem("adminToken");
        localStorage.removeItem("admin");
        window.location.href = "/admin/login";
      }
    }
    return Promise.reject(error);
  }
);

// Admin Auth API
// Note: Admin login uses the same endpoint as user login, but with userType: "admin"
export const adminAuthAPI = {
  login: (credentials) =>
    adminApi.post("/api/v1/auth/login", { ...credentials, userType: "admin" }),
  logout: () => adminApi.post("/api/v1/auth/logout"),
  getCurrentAdmin: () => adminApi.get("/api/v1/admin/profile"),
};

// Admin Profile API
export const adminProfileAPI = {
  getProfile: () => adminApi.get("/api/v1/admin/profile"),
  getAdminProfile: (adminId) =>
    adminApi.get(`/api/v1/admin/profile/${adminId}`),
  updateProfile: (data) => adminApi.put("/api/v1/admin/profile", data),
  changePassword: (data) =>
    adminApi.post("/api/v1/admin/profile/change-password", data),
  deactivate: (adminId) =>
    adminApi.post(`/api/v1/admin/profile/${adminId}/deactivate`),
  reactivate: (adminId) =>
    adminApi.post(`/api/v1/admin/profile/${adminId}/reactivate`),
};

// Admin User Management API
export const adminUserAPI = {
  getAllUsers: (params = {}) => {
    const {
      page = 0,
      size = 10,
      sortBy = "createdAt",
      sortDirection = "desc",
    } = params;
    return adminApi.get("/api/v1/admin/users", {
      params: { page, size, sortBy, sortDirection },
    });
  },
  searchUsers: (q, page = 0, size = 10) =>
    adminApi.get("/api/v1/admin/users/search", { params: { q, page, size } }),
  getUserById: (userId) => adminApi.get(`/api/v1/admin/users/${userId}`),
  createUser: (data) => adminApi.post("/api/v1/admin/users", data),
  updateUser: (userId, data) =>
    adminApi.put(`/api/v1/admin/users/${userId}`, data),
  deleteUser: (userId) => adminApi.delete(`/api/v1/admin/users/${userId}`),
  unlockUser: (userId) => adminApi.post(`/api/v1/admin/users/${userId}/unlock`),
  activateUser: (userId) =>
    adminApi.post(`/api/v1/admin/users/${userId}/activate`),
  deactivateUser: (userId) =>
    adminApi.post(`/api/v1/admin/users/${userId}/deactivate`),
  verifyEmail: (userId) =>
    adminApi.post(`/api/v1/admin/users/${userId}/verify-email`),
  resetUserPassword: (userId, newPassword) =>
    adminApi.post(`/api/v1/admin/users/${userId}/reset-password`, {
      newPassword,
    }),
};

// Admin Session Management API
export const adminSessionAPI = {
  getAllSessions: (params = {}) => {
    const {
      page = 0,
      size = 10,
      sortBy = "createdAt",
      sortDirection = "desc",
      userId,
      status,
      isFlagged,
      isPublic,
    } = params;
    return adminApi.get("/api/v1/admin/sessions", {
      params: {
        page,
        size,
        sortBy,
        sortDirection,
        ...(userId && { userId }),
        ...(status && { status }),
        ...(isFlagged !== undefined && { isFlagged }),
        ...(isPublic !== undefined && { isPublic }),
      },
    });
  },
  getSession: (sessionId) =>
    adminApi.get(`/api/v1/admin/sessions/${sessionId}`),
  deleteSession: (sessionId) =>
    adminApi.delete(`/api/v1/admin/sessions/${sessionId}`),
  archiveSession: (sessionId) =>
    adminApi.post(`/api/v1/admin/sessions/${sessionId}/archive`),
  flagSession: (sessionId, flagType, reason) =>
    adminApi.post(`/api/v1/admin/sessions/${sessionId}/flag`, {
      flagType,
      reason,
    }),
  unflagSession: (sessionId) =>
    adminApi.post(`/api/v1/admin/sessions/${sessionId}/unflag`),
  togglePublic: (sessionId) =>
    adminApi.post(`/api/v1/admin/sessions/${sessionId}/toggle-public`),
};

// Admin Message Management API
export const adminMessageAPI = {
  getAllMessages: (params = {}) => {
    const {
      page = 0,
      size = 10,
      sortBy = "timestamp",
      sortDirection = "desc",
      sessionId,
      userId,
      role,
      isFlagged,
    } = params;
    return adminApi.get("/api/v1/admin/messages", {
      params: {
        page,
        size,
        sortBy,
        sortDirection,
        ...(sessionId && { sessionId }),
        ...(userId && { userId }),
        ...(role && { role }),
        ...(isFlagged !== undefined && { isFlagged }),
      },
    });
  },
  getMessage: (messageId) =>
    adminApi.get(`/api/v1/admin/messages/${messageId}`),
  getMessagesBySession: (sessionId, params = {}) => {
    const {
      page = 0,
      size = 10,
      sortBy = "timestamp",
      sortDirection = "asc",
    } = params;
    return adminApi.get(`/api/v1/admin/messages/session/${sessionId}`, {
      params: { page, size, sortBy, sortDirection },
    });
  },
  deleteMessage: (messageId) =>
    adminApi.delete(`/api/v1/admin/messages/${messageId}`),
  flagMessage: (messageId, flagType, reason) =>
    adminApi.post(`/api/v1/admin/messages/${messageId}/flag`, {
      flagType,
      reason,
    }),
  unflagMessage: (messageId) =>
    adminApi.post(`/api/v1/admin/messages/${messageId}/unflag`),
};

// Admin Management API (for managing other admins)
export const adminManagementAPI = {
  getAllAdmins: (params = {}) => {
    const {
      page = 0,
      size = 10,
      sortBy = "createdAt",
      sortDirection = "desc",
    } = params;
    return adminApi.get("/api/v1/admin/admins", {
      params: { page, size, sortBy, sortDirection },
    });
  },
  getAdmin: (adminId) => adminApi.get(`/api/v1/admin/admins/${adminId}`),
  createAdmin: (data) => adminApi.post("/api/v1/admin/admins", data),
  updateAdmin: (adminId, data) =>
    adminApi.put(`/api/v1/admin/admins/${adminId}`, data),
  deleteAdmin: (adminId) => adminApi.delete(`/api/v1/admin/admins/${adminId}`),
  activateAdmin: (adminId) =>
    adminApi.post(`/api/v1/admin/admins/${adminId}/activate`),
  deactivateAdmin: (adminId) =>
    adminApi.post(`/api/v1/admin/admins/${adminId}/deactivate`),
  resetAdminPassword: (adminId, newPassword) =>
    adminApi.post(`/api/v1/admin/admins/${adminId}/reset-password`, {
      newPassword,
    }),
  unlockAdmin: (adminId) =>
    adminApi.post(`/api/v1/admin/admins/${adminId}/unlock`),
};

// Activity Log API (Level 0 only)
export const adminActivityLogAPI = {
  getAllLogs: (params = {}) => {
    const {
      page = 0,
      size = 10,
      sortBy = "createdAt",
      sortDirection = "desc",
      adminId,
      action,
      resourceType,
      startDate,
      endDate,
    } = params;
    return adminApi.get("/api/v1/admin/activity-logs", {
      params: {
        page,
        size,
        sortBy,
        sortDirection,
        ...(adminId && { adminId }),
        ...(action && { action }),
        ...(resourceType && { resourceType }),
        ...(startDate && { startDate }),
        ...(endDate && { endDate }),
      },
    });
  },
  getLog: (logId) => adminApi.get(`/api/v1/admin/activity-logs/${logId}`),
};

// Token Management API (Level 0 only)
export const adminTokenAPI = {
  getPasswordResetTokens: (params = {}) => {
    const { page = 0, size = 10, userType, includeExpired = false } = params;
    return adminApi.get("/api/v1/admin/tokens/password-reset", {
      params: { page, size, ...(userType && { userType }), includeExpired },
    });
  },
  getPasswordResetToken: (tokenId) =>
    adminApi.get(`/api/v1/admin/tokens/password-reset/${tokenId}`),
  deletePasswordResetToken: (tokenId) =>
    adminApi.delete(`/api/v1/admin/tokens/password-reset/${tokenId}`),
  invalidatePasswordResetToken: (tokenId) =>
    adminApi.post(`/api/v1/admin/tokens/password-reset/${tokenId}/invalidate`),
  getVerificationTokens: (params = {}) => {
    const { page = 0, size = 10, includeExpired = false } = params;
    return adminApi.get("/api/v1/admin/tokens/verification", {
      params: { page, size, includeExpired },
    });
  },
  getVerificationToken: (tokenId) =>
    adminApi.get(`/api/v1/admin/tokens/verification/${tokenId}`),
  deleteVerificationToken: (tokenId) =>
    adminApi.delete(`/api/v1/admin/tokens/verification/${tokenId}`),
  invalidateVerificationToken: (tokenId) =>
    adminApi.post(`/api/v1/admin/tokens/verification/${tokenId}/invalidate`),
};

export default adminApi;
