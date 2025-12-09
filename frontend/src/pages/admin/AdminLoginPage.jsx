import { useState } from "react";
import { useNavigate, useLocation } from "react-router-dom";
import { useAdmin } from "../../context/AdminContext";
import "./AdminLoginPage.css";

const AdminLoginPage = () => {
  const [formData, setFormData] = useState({
    username: "",
    password: "",
  });
  const [twoFACode, setTwoFACode] = useState("");
  const [requires2FA, setRequires2FA] = useState(false);
  const [twoFAUsername, setTwoFAUsername] = useState("");
  const [error, setError] = useState("");
  const [isLoading, setIsLoading] = useState(false);

  const { login, verify2FA, isAuthenticated } = useAdmin();
  const navigate = useNavigate();
  const location = useLocation();

  // If already authenticated, redirect to dashboard
  if (isAuthenticated) {
    const from = location.state?.from?.pathname || "/admin/dashboard";
    navigate(from, { replace: true });
    return null;
  }

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData((prev) => ({
      ...prev,
      [name]: value,
    }));
    setError("");
  };

  const handleTwoFACodeChange = (e) => {
    // Only allow numbers and limit to 6 digits
    const value = e.target.value.replace(/\D/g, "").slice(0, 6);
    setTwoFACode(value);
    setError("");
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setIsLoading(true);
    setError("");

    try {
      const result = await login(formData);

      if (result.success) {
        const from = location.state?.from?.pathname || "/admin/dashboard";
        navigate(from, { replace: true });
      } else if (result.requires2FA || result.error === "2FA verification required") {
        // 2FA is required, show 2FA input
        setRequires2FA(true);
        setTwoFAUsername(result.username || formData.username);
        setError(""); // Clear any error
      } else {
        setError(result.error || result.message || "Giriş başarısız");
      }
    } catch (err) {
      setError("Bir hata oluştu. Lütfen tekrar deneyin.");
    } finally {
      setIsLoading(false);
    }
  };

  const handle2FASubmit = async (e) => {
    e.preventDefault();
    
    if (twoFACode.length !== 6) {
      setError("Lütfen 6 haneli doğrulama kodunu girin");
      return;
    }

    setIsLoading(true);
    setError("");

    const result = await verify2FA(twoFAUsername, twoFACode);

    if (result.success) {
      const from = location.state?.from?.pathname || "/admin/dashboard";
      navigate(from, { replace: true });
    } else {
      setError(result.error);
    }

    setIsLoading(false);
  };

  const handleBack = () => {
    setRequires2FA(false);
    setTwoFACode("");
    setTwoFAUsername("");
    setError("");
  };

  return (
    <div className="admin-login-page">
      <div className="admin-login-container">
        <div className="admin-login-header">
          <div className="admin-logo">
            <svg
              xmlns="http://www.w3.org/2000/svg"
              viewBox="0 0 24 24"
              fill="currentColor"
              width="48"
              height="48"
            >
              <path d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm-1 17.93c-3.95-.49-7-3.85-7-7.93 0-.62.08-1.21.21-1.79L9 15v1c0 1.1.9 2 2 2v1.93zm6.9-2.54c-.26-.81-1-1.39-1.9-1.39h-1v-3c0-.55-.45-1-1-1H8v-2h2c.55 0 1-.45 1-1V7h2c1.1 0 2-.9 2-2v-.41c2.93 1.19 5 4.06 5 7.41 0 2.08-.8 3.97-2.1 5.39z" />
            </svg>
          </div>
          <h1>Admin Panel</h1>
          <p>
            {requires2FA
              ? "İki faktörlü doğrulama gerekiyor"
              : "Yönetim paneline erişmek için giriş yapın"}
          </p>
        </div>

        {error && (
          <div className="admin-error-message">
            <svg
              xmlns="http://www.w3.org/2000/svg"
              viewBox="0 0 24 24"
              fill="currentColor"
              width="20"
              height="20"
            >
              <path d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm1 15h-2v-2h2v2zm0-4h-2V7h2v6z" />
            </svg>
            <span>{error}</span>
          </div>
        )}

        {!requires2FA ? (
          <form onSubmit={handleSubmit} className="admin-login-form">
            <div className="admin-form-group">
              <label htmlFor="username">Kullanıcı Adı</label>
              <input
                type="text"
                id="username"
                name="username"
                value={formData.username}
                onChange={handleChange}
                placeholder="Admin kullanıcı adınızı girin"
                required
                autoComplete="username"
              />
            </div>

            <div className="admin-form-group">
              <label htmlFor="password">Şifre</label>
              <input
                type="password"
                id="password"
                name="password"
                value={formData.password}
                onChange={handleChange}
                placeholder="Şifrenizi girin"
                required
                autoComplete="current-password"
              />
            </div>

            <button
              type="submit"
              className="admin-login-button"
              disabled={isLoading}
            >
              {isLoading ? (
                <>
                  <span className="admin-button-spinner"></span>
                  Giriş yapılıyor...
                </>
              ) : (
                "Giriş Yap"
              )}
            </button>
          </form>
        ) : (
          <form onSubmit={handle2FASubmit} className="admin-login-form">
            <div className="admin-2fa-info">
              <svg
                xmlns="http://www.w3.org/2000/svg"
                viewBox="0 0 24 24"
                fill="currentColor"
                width="24"
                height="24"
              >
                <path d="M18 8h-1V6c0-2.76-2.24-5-5-5S7 3.24 7 6v2H6c-1.1 0-2 .9-2 2v10c0 1.1.9 2 2 2h12c1.1 0 2-.9 2-2V10c0-1.1-.9-2-2-2zm-6 9c-1.1 0-2-.9-2-2s.9-2 2-2 2 .9 2 2-.9 2-2 2zm3.1-9H8.9V6c0-1.71 1.39-3.1 3.1-3.1 1.71 0 3.1 1.39 3.1 3.1v2z" />
              </svg>
              <span>
                Authenticator uygulamanızdan 6 haneli doğrulama kodunu girin
              </span>
            </div>

            <div className="admin-form-group">
              <label htmlFor="twoFACode">Doğrulama Kodu</label>
              <input
                type="text"
                id="twoFACode"
                name="twoFACode"
                value={twoFACode}
                onChange={handleTwoFACodeChange}
                placeholder="000000"
                maxLength={6}
                required
                autoComplete="one-time-code"
                className="two-fa-input"
                autoFocus
              />
            </div>

            <button
              type="submit"
              className="admin-login-button"
              disabled={isLoading || twoFACode.length !== 6}
            >
              {isLoading ? (
                <>
                  <span className="admin-button-spinner"></span>
                  Doğrulanıyor...
                </>
              ) : (
                "Doğrula"
              )}
            </button>

            <button
              type="button"
              className="admin-back-button"
              onClick={handleBack}
              disabled={isLoading}
            >
              ← Geri Dön
            </button>
          </form>
        )}

        <div className="admin-login-footer">
          <a href="/login" className="back-to-user">
            ← Kullanıcı girişine dön
          </a>
        </div>
      </div>
    </div>
  );
};

export default AdminLoginPage;
