import {
  BrowserRouter as Router,
  Routes,
  Route,
  Navigate,
} from "react-router-dom";
import { AuthProvider } from "./context/AuthContext";
import { ChatProvider } from "./context/ChatContext";
import { AdminProvider } from "./context/AdminContext";
import LoginPage from "./pages/LoginPage";
import RegisterPage from "./pages/RegisterPage";
import VerificationPendingPage from "./pages/VerificationPendingPage";
import EmailVerifyPage from "./pages/EmailVerifyPage";
import ForgotPasswordPage from "./pages/ForgotPasswordPage";
import ResetPasswordPage from "./pages/ResetPasswordPage";
import ProfilePage from "./pages/ProfilePage";
import ChatPage from "./pages/ChatPage";
import ProtectedRoute from "./components/auth/ProtectedRoute";
// Admin imports
import AdminLoginPage from "./pages/admin/AdminLoginPage";
import AdminLayout from "./components/admin/AdminLayout";
import AdminProtectedRoute from "./components/admin/AdminProtectedRoute";
import AdminDashboard from "./pages/admin/AdminDashboard";
import UserManagementPage from "./pages/admin/UserManagementPage";
import SessionManagementPage from "./pages/admin/SessionManagementPage";
import MessageManagementPage from "./pages/admin/MessageManagementPage";
import AdminManagementPage from "./pages/admin/AdminManagementPage";
import ActivityLogsPage from "./pages/admin/ActivityLogsPage";
import TokenManagementPage from "./pages/admin/TokenManagementPage";
import AdminProfilePage from "./pages/admin/AdminProfilePage";
import "./App.css";

function App() {
  return (
    <Router>
      <AuthProvider>
        <ChatProvider>
          <AdminProvider>
            <Routes>
              {/* User Routes */}
              <Route path="/login" element={<LoginPage />} />
              <Route path="/register" element={<RegisterPage />} />
              <Route
                path="/verification-pending"
                element={<VerificationPendingPage />}
              />
              <Route path="/verify" element={<EmailVerifyPage />} />
              <Route path="/forgot-password" element={<ForgotPasswordPage />} />
              <Route path="/reset-password" element={<ResetPasswordPage />} />
              <Route
                path="/profile"
                element={
                  <ProtectedRoute>
                    <ProfilePage />
                  </ProtectedRoute>
                }
              />
              <Route
                path="/chat"
                element={
                  <ProtectedRoute>
                    <ChatPage />
                  </ProtectedRoute>
                }
              />
              <Route path="/" element={<Navigate to="/chat" replace />} />

              {/* Admin Routes */}
              <Route path="/admin/login" element={<AdminLoginPage />} />
              <Route
                path="/admin"
                element={
                  <AdminProtectedRoute>
                    <AdminLayout />
                  </AdminProtectedRoute>
                }
              >
                <Route
                  index
                  element={<Navigate to="/admin/dashboard" replace />}
                />
                <Route path="dashboard" element={<AdminDashboard />} />
                <Route path="users" element={<UserManagementPage />} />
                <Route path="sessions" element={<SessionManagementPage />} />
                <Route path="messages" element={<MessageManagementPage />} />
                <Route path="admins" element={<AdminManagementPage />} />
                <Route path="activity-logs" element={<ActivityLogsPage />} />
                <Route path="tokens" element={<TokenManagementPage />} />
                <Route path="profile" element={<AdminProfilePage />} />
              </Route>
            </Routes>
          </AdminProvider>
        </ChatProvider>
      </AuthProvider>
    </Router>
  );
}

// Temporary placeholder component for unimplemented pages
const ComingSoon = ({ title }) => (
  <div
    style={{
      display: "flex",
      flexDirection: "column",
      alignItems: "center",
      justifyContent: "center",
      minHeight: "60vh",
      color: "rgba(255, 255, 255, 0.6)",
      textAlign: "center",
    }}
  >
    <svg
      xmlns="http://www.w3.org/2000/svg"
      viewBox="0 0 24 24"
      fill="currentColor"
      width="64"
      height="64"
      style={{ marginBottom: "16px", opacity: 0.5 }}
    >
      <path d="M19 3H5c-1.1 0-2 .9-2 2v14c0 1.1.9 2 2 2h14c1.1 0 2-.9 2-2V5c0-1.1-.9-2-2-2zm-5 14H7v-2h7v2zm3-4H7v-2h10v2zm0-4H7V7h10v2z" />
    </svg>
    <h2 style={{ color: "#fff", marginBottom: "8px" }}>{title}</h2>
    <p>Bu sayfa yakında eklenecek</p>
  </div>
);

export default App;
