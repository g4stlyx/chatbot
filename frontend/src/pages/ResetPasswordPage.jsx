import { useState, useEffect } from "react";
import { authAPI } from "../services/api";
import { Link, useSearchParams, useNavigate } from "react-router-dom";
import "./AuthPages.css";

const ResetPasswordPage = () => {
  const [searchParams] = useSearchParams();
  const navigate = useNavigate();
  const token = searchParams.get("token");

  const [formData, setFormData] = useState({
    newPassword: "",
    confirmPassword: "",
  });
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");
  const [success, setSuccess] = useState(false);

  useEffect(() => {
    if (!token) {
      setError(
        "Invalid or missing reset token. Please request a new reset link."
      );
    }
  }, [token]);

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData((prev) => ({
      ...prev,
      [name]: value,
    }));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError("");

    // Validation
    if (formData.newPassword.length < 8) {
      setError("Password must be at least 8 characters long");
      return;
    }

    if (formData.newPassword !== formData.confirmPassword) {
      setError("Passwords do not match");
      return;
    }

    setLoading(true);

    try {
      const response = await authAPI.resetPassword(token, formData.newPassword);
      if (response.data.success) {
        setSuccess(true);
        setTimeout(() => {
          navigate("/login");
        }, 3000);
      }
    } catch (err) {
      setError(
        err.response?.data?.message ||
          "Failed to reset password. The link may have expired. Please request a new one."
      );
    } finally {
      setLoading(false);
    }
  };

  if (!token && !error) {
    return (
      <div className="auth-page">
        <div className="auth-container">
          <div className="auth-card">
            <div className="error-icon">⚠</div>
            <h1>Invalid Link</h1>
            <p>This password reset link is invalid or has been used.</p>
            <Link to="/forgot-password" className="btn btn-primary">
              Request New Link
            </Link>
          </div>
        </div>
      </div>
    );
  }

  if (success) {
    return (
      <div className="auth-page">
        <div className="auth-container">
          <div className="auth-card">
            <div className="success-icon">✓</div>
            <h1>Password Reset Successful!</h1>
            <p className="success-message">
              Your password has been reset successfully. You can now log in with
              your new password.
            </p>
            <p className="info-text">Redirecting to login page...</p>
            <Link to="/login" className="btn btn-primary">
              Go to Login
            </Link>
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className="auth-page">
      <div className="auth-container">
        <div className="auth-card">
          <div className="auth-header">
            <h1>Reset Your Password</h1>
            <p>Enter your new password below.</p>
          </div>

          {error && <div className="alert alert-error">{error}</div>}

          <form onSubmit={handleSubmit} className="auth-form">
            <div className="form-group">
              <label htmlFor="newPassword">New Password</label>
              <input
                type="password"
                id="newPassword"
                name="newPassword"
                value={formData.newPassword}
                onChange={handleChange}
                placeholder="Enter new password"
                required
                minLength={8}
                disabled={loading}
              />
              <small>Minimum 8 characters</small>
            </div>

            <div className="form-group">
              <label htmlFor="confirmPassword">Confirm New Password</label>
              <input
                type="password"
                id="confirmPassword"
                name="confirmPassword"
                value={formData.confirmPassword}
                onChange={handleChange}
                placeholder="Confirm new password"
                required
                disabled={loading}
              />
            </div>

            <button
              type="submit"
              className="btn btn-primary btn-full"
              disabled={loading || !token}
            >
              {loading ? "Resetting..." : "Reset Password"}
            </button>
          </form>

          <div className="auth-footer">
            <p>
              Remember your password?{" "}
              <Link to="/login" className="auth-link">
                Back to Login
              </Link>
            </p>
          </div>
        </div>
      </div>
    </div>
  );
};

export default ResetPasswordPage;
