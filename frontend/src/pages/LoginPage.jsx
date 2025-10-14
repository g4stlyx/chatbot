import { useState, useEffect } from "react";
import { Link, useNavigate } from "react-router-dom";
import { useAuth } from "../context/AuthContext";

const LoginPage = () => {
  const navigate = useNavigate();
  const { login, isAuthenticated } = useAuth();
  const [formData, setFormData] = useState({
    username: "",
    password: "",
  });
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);
  const [isEmailVerificationError, setIsEmailVerificationError] =
    useState(false);

  const handleChange = (e) => {
    setFormData({
      ...formData,
      [e.target.name]: e.target.value,
    });
    // Don't clear error on input change - let it persist
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    setError("");
    setIsEmailVerificationError(false);

    const result = await login(formData.username, formData.password);

    if (result.success) {
      navigate("/chat");
    } else {
      // Check if it's an email verification error
      const errorMsg = result.error || "";
      if (
        errorMsg.toLowerCase().includes("email") &&
        errorMsg.toLowerCase().includes("verif")
      ) {
        setIsEmailVerificationError(true);
      }
      setError(result.error);
    }

    setLoading(false);
  };

  return (
    <div className="auth-container">
      <div className="auth-card">
        <h1 className="auth-title">Welcome Back</h1>
        <p className="auth-subtitle">Login to your chatbot account</p>

        {error && (
          <div
            className="error-message"
            style={
              isEmailVerificationError
                ? {
                    backgroundColor: "#fef3c7",
                    color: "#92400e",
                    border: "1px solid #fbbf24",
                  }
                : {}
            }
          >
            {isEmailVerificationError && (
              <span style={{ marginRight: "8px", fontSize: "18px" }}>⚠️</span>
            )}
            {error}
          </div>
        )}

        <form onSubmit={handleSubmit}>
          <div className="form-group">
            <label htmlFor="username">Username or Email</label>
            <input
              type="text"
              id="username"
              name="username"
              value={formData.username}
              onChange={handleChange}
              required
              placeholder="username or email"
            />
          </div>

          <div className="form-group">
            <label htmlFor="password">Password</label>
            <input
              type="password"
              id="password"
              name="password"
              value={formData.password}
              onChange={handleChange}
              required
              placeholder="••••••••"
            />
          </div>

          <div style={{ textAlign: "right", marginBottom: "15px" }}>
            <Link
              to="/forgot-password"
              className="auth-link"
              style={{ fontSize: "14px" }}
            >
              Forgot password?
            </Link>
          </div>

          <button type="submit" className="btn-primary" disabled={loading}>
            {loading ? "Logging in..." : "Login"}
          </button>
        </form>

        <div className="auth-footer">
          Don't have an account?{" "}
          <Link to="/register" className="auth-link">
            Sign up
          </Link>
        </div>
      </div>
    </div>
  );
};

export default LoginPage;
