import {
  createContext,
  useContext,
  useState,
  useEffect,
  useCallback,
} from "react";
import { adminAuthAPI, adminProfileAPI, admin2FAAPI } from "../services/adminApi";

const AdminContext = createContext(null);

export const useAdmin = () => {
  const context = useContext(AdminContext);
  if (!context) {
    throw new Error("useAdmin must be used within an AdminProvider");
  }
  return context;
};

export const AdminProvider = ({ children }) => {
  const [admin, setAdmin] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  // Check for existing admin session on mount
  useEffect(() => {
    const initializeAdmin = async () => {
      const token = localStorage.getItem("adminToken");
      const storedAdmin = localStorage.getItem("admin");

      if (token && storedAdmin) {
        try {
          // Verify token is still valid by fetching profile
          const response = await adminProfileAPI.getProfile();
          setAdmin(response.data);
          localStorage.setItem("admin", JSON.stringify(response.data));
        } catch (err) {
          // Token is invalid, clear storage
          console.error("Admin session invalid:", err);
          localStorage.removeItem("adminToken");
          localStorage.removeItem("admin");
          setAdmin(null);
        }
      }
      setLoading(false);
    };

    initializeAdmin();
  }, []);

  const login = useCallback(async (credentials) => {
    setLoading(true);
    setError(null);

    try {
      const response = await adminAuthAPI.login(credentials);
      const data = response.data;

      // Check if 2FA is required FIRST (before checking success)
      if (data.requires2FA) {
        setLoading(false);
        return {
          success: false,
          requires2FA: true,
          username: data.user?.username || credentials.username,
          message: "2FA doğrulaması gerekiyor",
        };
      }

      // Check if login was successful
      if (!data.success) {
        setError(data.message || "Giriş başarısız");
        return { success: false, error: data.message };
      }

      // Backend returns: { success, accessToken, refreshToken, user: { id, username, email, level, ... } }
      const { accessToken, user } = data;

      // Verify this is actually an admin (has level property)
      if (user.userType !== "admin" && user.level === undefined) {
        setError("Bu hesap bir admin hesabı değil.");
        return { success: false, error: "Bu hesap bir admin hesabı değil." };
      }

      localStorage.setItem("adminToken", accessToken);
      localStorage.setItem("admin", JSON.stringify(user));
      setAdmin(user);

      return { success: true };
    } catch (err) {
      const message =
        err.response?.data?.message ||
        "Giriş başarısız. Lütfen bilgilerinizi kontrol edin.";
      setError(message);
      return { success: false, error: message };
    } finally {
      setLoading(false);
    }
  }, []);

  // Verify 2FA code and complete login
  const verify2FA = useCallback(async (username, code) => {
    setLoading(true);
    setError(null);

    try {
      const response = await admin2FAAPI.verifyLogin(username, code);
      const data = response.data;

      if (!data.success) {
        setError(data.message || "2FA doğrulaması başarısız");
        return { success: false, error: data.message };
      }

      const { accessToken, user } = data;

      localStorage.setItem("adminToken", accessToken);
      localStorage.setItem("admin", JSON.stringify(user));
      setAdmin(user);

      return { success: true };
    } catch (err) {
      const message =
        err.response?.data?.message ||
        "2FA doğrulaması başarısız. Lütfen kodu kontrol edin.";
      setError(message);
      return { success: false, error: message };
    } finally {
      setLoading(false);
    }
  }, []);

  const logout = useCallback(async () => {
    try {
      await adminAuthAPI.logout();
    } catch (err) {
      console.error("Logout error:", err);
    } finally {
      localStorage.removeItem("adminToken");
      localStorage.removeItem("admin");
      setAdmin(null);
    }
  }, []);

  const refreshAdmin = useCallback(async () => {
    try {
      const response = await adminProfileAPI.getProfile();
      setAdmin(response.data);
      localStorage.setItem("admin", JSON.stringify(response.data));
    } catch (err) {
      console.error("Failed to refresh admin data:", err);
    }
  }, []);

  // Helper function to check admin level
  const hasLevel = useCallback(
    (requiredLevel) => {
      if (!admin) return false;
      return admin.level <= requiredLevel; // Lower level = higher permissions
    },
    [admin]
  );

  // Check if admin can manage users
  const canManageUsers = useCallback(() => hasLevel(2), [hasLevel]);

  // Check if admin can manage other admins
  const canManageAdmins = useCallback(() => hasLevel(1), [hasLevel]);

  // Check if admin can access activity logs and tokens
  const canAccessSystemLogs = useCallback(() => hasLevel(0), [hasLevel]);

  // Check if admin can delete (hard delete)
  const canDelete = useCallback(() => hasLevel(1), [hasLevel]);

  const value = {
    admin,
    loading,
    error,
    isAuthenticated: !!admin,
    login,
    verify2FA,
    logout,
    refreshAdmin,
    hasLevel,
    canManageUsers,
    canManageAdmins,
    canAccessSystemLogs,
    canDelete,
    // Expose admin level info
    isSuperAdmin: admin?.level === 0,
    isAdmin: admin?.level === 1,
    isModerator: admin?.level === 2,
  };

  return (
    <AdminContext.Provider value={value}>{children}</AdminContext.Provider>
  );
};

export default AdminContext;
