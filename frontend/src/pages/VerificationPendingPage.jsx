import { useLocation, useNavigate } from "react-router-dom";
import { useEffect, useState } from "react";
import { authAPI } from "../services/api";
import "./AuthPages.css";

const VerificationPendingPage = () => {
  const navigate = useNavigate();
  const location = useLocation();
  const email = location.state?.email;

  const [resending, setResending] = useState(false);
  const [resendSuccess, setResendSuccess] = useState(false);
  const [resendError, setResendError] = useState("");
  const [cooldown, setCooldown] = useState(0);

  useEffect(() => {
    // Redirect to register if no email provided
    if (!email) {
      navigate("/register");
    }
  }, [email, navigate]);

  useEffect(() => {
    // Cooldown timer
    if (cooldown > 0) {
      const timer = setTimeout(() => setCooldown(cooldown - 1), 1000);
      return () => clearTimeout(timer);
    }
  }, [cooldown]);

  const handleResendEmail = async () => {
    if (cooldown > 0) return;

    setResending(true);
    setResendError("");
    setResendSuccess(false);

    try {
      await authAPI.resendVerification(email);
      setResendSuccess(true);
      setCooldown(60); // 60 seconds cooldown
      setTimeout(() => setResendSuccess(false), 5000);
    } catch (err) {
      setResendError(
        err.response?.data?.message || "Failed to resend verification email"
      );
    } finally {
      setResending(false);
    }
  };

  return (
    <div className="auth-page">
      <div className="auth-container">
        <div className="auth-card">
          <div
            style={{
              fontSize: "64px",
              marginBottom: "20px",
              textAlign: "center",
            }}
          >
            📧
          </div>

          <div className="auth-header">
            <h1>Check Your Mailbox</h1>
            <p>
              We've sent a verification email to
              <br />
              <strong>{email}</strong>
            </p>
          </div>

          {resendSuccess && (
            <div className="alert alert-success">
              Verification email sent successfully! Please check your inbox.
            </div>
          )}

          {resendError && (
            <div className="alert alert-error">{resendError}</div>
          )}

          <div
            style={{
              backgroundColor: "#f3f4f6",
              padding: "20px",
              borderRadius: "8px",
              marginBottom: "25px",
              textAlign: "left",
            }}
          >
            <p
              style={{
                color: "#4b5563",
                fontSize: "14px",
                marginBottom: "10px",
                fontWeight: "600",
              }}
            >
              Next steps:
            </p>
            <ol
              style={{
                color: "#6b7280",
                fontSize: "14px",
                paddingLeft: "20px",
                margin: 0,
                lineHeight: "1.8",
              }}
            >
              <li>Check your inbox (and spam folder)</li>
              <li>Click the verification link in the email</li>
              <li>Return to login with your credentials</li>
            </ol>
          </div>

          <div
            style={{
              backgroundColor: "#fef3c7",
              border: "1px solid #fbbf24",
              borderRadius: "8px",
              padding: "15px",
              marginBottom: "25px",
            }}
          >
            <p
              style={{
                color: "#92400e",
                fontSize: "14px",
                margin: 0,
                display: "flex",
                alignItems: "center",
                justifyContent: "center",
              }}
            >
              <span style={{ marginRight: "8px", fontSize: "18px" }}>⚠️</span>
              You must verify your email before logging in
            </p>
          </div>

          <button
            onClick={handleResendEmail}
            disabled={resending || cooldown > 0}
            className="btn btn-primary btn-full"
            style={{ marginBottom: "15px" }}
          >
            {resending
              ? "Sending..."
              : cooldown > 0
              ? `Resend in ${cooldown}s`
              : "Resend Verification Email"}
          </button>

          <button
            onClick={() => navigate("/login")}
            className="btn btn-secondary btn-full"
            style={{ marginBottom: "15px" }}
          >
            Go to Login
          </button>

          <div className="auth-footer">
            <p>
              Wrong email?{" "}
              <button
                onClick={() => navigate("/register")}
                className="auth-link"
                style={{
                  background: "none",
                  border: "none",
                  padding: 0,
                  font: "inherit",
                  cursor: "pointer",
                }}
              >
                Try signing up again
              </button>
            </p>
          </div>
        </div>
      </div>
    </div>
  );
};

export default VerificationPendingPage;
