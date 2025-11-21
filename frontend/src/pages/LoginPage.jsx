import { useState, useEffect } from "react";
import { Link, useNavigate } from "react-router-dom";
import { useAuth } from "../context/AuthContext";
import "./AuthPages.css"; // Import the CSS file

const TypewriterLine = ({ text, style }) => {
  const [displayedText, setDisplayedText] = useState("");

  useEffect(() => {
    let i = 0;
    const timer = setInterval(() => {
      setDisplayedText(text.substring(0, i + 1));
      i++;
      if (i > text.length) clearInterval(timer);
    }, 50 + Math.random() * 30);
    return () => clearInterval(timer);
  }, [text]);

  return (
    <div className="code-line" style={style}>
      {displayedText}
      <span className="cursor"></span>
    </div>
  );
};

const TypewriterEffect = () => {
  const [lines, setLines] = useState([]);

  useEffect(() => {
    const codeSnippets = [
      "const ai = new Chatbot();",
      "await ai.connect();",
      "console.log('Hello, Human!');",
      "// Ready to assist you...",
      "system.init({ secure: true });",
      "analyzing_data...",
      "optimizing_neural_net();",
      "access_granted => true",
      "import { Future } from 'now';",
      "while(alive) { learn(); }",
      "if (error) fix(error);",
      "return new Solution();",
      "user.authenticate(token);",
      "encrypting_stream...",
      "context.load(history);",
      "await response.generate();",
    ];

    const addLine = () => {
      setLines((prev) => {
        if (prev.length > 12) return prev; // Limit max concurrent lines

        let attempts = 0;
        let position = null;

        while (attempts < 10) {
          const top = Math.random() * 95; // 0-95% height
          const left = Math.random() * 70; // 0-70% width (leave room for text length)

          // Check collision with existing lines
          const collision = prev.some((line) => {
            const lineTop = parseFloat(line.style.top);
            const lineLeft = parseFloat(line.style.left);

            // Approximate collision box: 6% height, 30% width
            const verticalOverlap = Math.abs(lineTop - top) < 6;
            const horizontalOverlap = Math.abs(lineLeft - left) < 30;

            return verticalOverlap && horizontalOverlap;
          });

          if (!collision) {
            position = { top, left };
            break;
          }
          attempts++;
        }

        if (!position) return prev; // Could not find space

        const id = Date.now() + Math.random();
        const text =
          codeSnippets[Math.floor(Math.random() * codeSnippets.length)];

        const style = {
          top: `${position.top}%`,
          left: `${position.left}%`,
          position: "absolute",
          whiteSpace: "nowrap",
          opacity: 0.3 + Math.random() * 0.5,
          fontSize: `${0.8 + Math.random() * 0.4}rem`,
          color: "rgba(255, 255, 255, 0.8)",
          textShadow: "0 0 5px rgba(0,0,0,0.3)",
        };

        // Remove line after some time
        setTimeout(() => {
          setLines((current) => current.filter((l) => l.id !== id));
        }, 4000 + Math.random() * 3000);

        return [...prev, { id, text, style }];
      });
    };

    // Initial lines
    addLine();
    addLine();
    addLine();

    const interval = setInterval(addLine, 800); // Add new line every 800ms
    return () => clearInterval(interval);
  }, []);

  return (
    <div
      className="code-animation-container"
      style={{
        position: "absolute",
        top: 0,
        left: 0,
        width: "100%",
        height: "100%",
        overflow: "hidden",
        pointerEvents: "none", // Allow clicks to pass through
        zIndex: 1,
      }}
    >
      {lines.map((line) => (
        <TypewriterLine key={line.id} text={line.text} style={line.style} />
      ))}
    </div>
  );
};

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
    <div className="login-split-screen">
      <div className="login-left-section">
        <div className="login-form-wrapper">
          <h1 className="auth-title" style={{ textAlign: "left" }}>
            Welcome Back
          </h1>
          <p className="auth-subtitle" style={{ textAlign: "left" }}>
            Login to your chatbot account
          </p>

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

      <div className="login-right-section">
        <TypewriterEffect />
        <div
          className="login-visual-content"
          style={{ zIndex: 2, position: "relative" }}
        >
          <h2 className="login-title-overlay">
            Your Private Chat with Artificial Intelligence
          </h2>
        </div>
      </div>
    </div>
  );
};

export default LoginPage;
