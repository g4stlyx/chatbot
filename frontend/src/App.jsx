import {
  BrowserRouter as Router,
  Routes,
  Route,
  Navigate,
} from "react-router-dom";
import { AuthProvider } from "./context/AuthContext";
import { ChatProvider } from "./context/ChatContext";
import LoginPage from "./pages/LoginPage";
import RegisterPage from "./pages/RegisterPage";
import VerificationPendingPage from "./pages/VerificationPendingPage";
import EmailVerifyPage from "./pages/EmailVerifyPage";
import ForgotPasswordPage from "./pages/ForgotPasswordPage";
import ResetPasswordPage from "./pages/ResetPasswordPage";
import ProfilePage from "./pages/ProfilePage";
import ChatPage from "./pages/ChatPage";
import ProtectedRoute from "./components/auth/ProtectedRoute";
import "./App.css";

function App() {
  return (
    <Router>
      <AuthProvider>
        <ChatProvider>
          <Routes>
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
          </Routes>
        </ChatProvider>
      </AuthProvider>
    </Router>
  );
}

export default App;
