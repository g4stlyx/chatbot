import { useState } from "react";
import { authAPI } from "../services/api";
import { Link } from "react-router-dom";
import "./AuthPages.css";

const ForgotPasswordPage = () => {
  const [email, setEmail] = useState("");
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");
  const [success, setSuccess] = useState(false);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError("");
    setLoading(true);

    try {
      const response = await authAPI.forgotPassword(email);
      if (response.data.success) {
        setSuccess(true);
      }
    } catch (err) {
      setError(
        err.response?.data?.message ||
          "Failed to send reset email. Please try again."
      );
    } finally {
      setLoading(false);
    }
  };

  if (success) {
    return (
      <div className="auth-page">
        <div className="auth-container">
          <div className="auth-card">
            <div className="success-icon">✓</div>
            <h1>Check Your Email</h1>
            <p className="success-message">
              If an account exists with the email <strong>{email}</strong>,
              we've sent password reset instructions to that address.
            </p>
            <p className="info-text">
              Please check your inbox and spam folder. The reset link will
              expire in 24 hours.
            </p>
            <div className="auth-footer">
              <Link to="/login" className="btn btn-primary">
                Return to Login
              </Link>
            </div>
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
            <h1>Forgot Password?</h1>
            <p>
              Enter your email address and we'll send you instructions to reset
              your password.
            </p>
          </div>

          {error && <div className="alert alert-error">{error}</div>}

          <form onSubmit={handleSubmit} className="auth-form">
            <div className="form-group">
              <label htmlFor="email">Email Address</label>
              <input
                type="email"
                id="email"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                placeholder="you@example.com"
                required
                disabled={loading}
              />
            </div>

            <button
              type="submit"
              className="btn btn-primary btn-full"
              disabled={loading}
            >
              {loading ? "Sending..." : "Send Reset Link"}
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

export default ForgotPasswordPage;
