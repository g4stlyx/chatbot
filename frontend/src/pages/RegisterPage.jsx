import { useState, useEffect } from "react";
import { Link, useNavigate } from "react-router-dom";
import { useAuth } from "../context/AuthContext";
import "./AuthPages.css";

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
        if (prev.length > 12) return prev;

        let attempts = 0;
        let position = null;

        while (attempts < 10) {
          const top = Math.random() * 95;
          const left = Math.random() * 70;

          const collision = prev.some((line) => {
            const lineTop = parseFloat(line.style.top);
            const lineLeft = parseFloat(line.style.left);
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

        if (!position) return prev;

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

        setTimeout(() => {
          setLines((current) => current.filter((l) => l.id !== id));
        }, 4000 + Math.random() * 3000);

        return [...prev, { id, text, style }];
      });
    };

    addLine();
    addLine();
    addLine();

    const interval = setInterval(addLine, 800);
    return () => clearInterval(interval);
  }, []);

  return (
    <div className="code-animation-container">
      {lines.map((line) => (
        <TypewriterLine key={line.id} text={line.text} style={line.style} />
      ))}
    </div>
  );
};

const RegisterPage = () => {
  const navigate = useNavigate();
  const { register, isAuthenticated } = useAuth();
  const [formData, setFormData] = useState({
    username: "",
    email: "",
    password: "",
    confirmPassword: "",
    firstName: "",
    lastName: "",
  });
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);

  const handleChange = (e) => {
    setFormData({
      ...formData,
      [e.target.name]: e.target.value,
    });
    setError("");
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    setError("");

    // Validation
    if (formData.password !== formData.confirmPassword) {
      setError("Passwords do not match");
      setLoading(false);
      return;
    }

    if (formData.password.length < 6) {
      setError("Password must be at least 6 characters");
      setLoading(false);
      return;
    }

    const result = await register({
      username: formData.username,
      email: formData.email,
      password: formData.password,
      firstName: formData.firstName || null,
      lastName: formData.lastName || null,
    });

    if (result.success) {
      // Navigate to verification pending page instead of chat
      navigate("/verification-pending", {
        state: { email: formData.email },
      });
    } else {
      setError(result.error);
    }

    setLoading(false);
  };

  return (
    <div className="login-split-screen">
      <div className="login-left-section">
        <div className="login-form-wrapper">
          <h1 className="auth-title" style={{ textAlign: "left" }}>
            Create Account
          </h1>
          <p className="auth-subtitle" style={{ textAlign: "left" }}>
            Sign up for a new chatbot account
          </p>

          {error && <div className="error-message">{error}</div>}

          <form onSubmit={handleSubmit}>
            <div className="form-group">
              <label htmlFor="username">Username</label>
              <input
                type="text"
                id="username"
                name="username"
                value={formData.username}
                onChange={handleChange}
                required
                placeholder="johndoe"
              />
            </div>

            <div className="form-group">
              <label htmlFor="email">Email Address</label>
              <input
                type="email"
                id="email"
                name="email"
                value={formData.email}
                onChange={handleChange}
                required
                placeholder="you@example.com"
              />
            </div>

            <div className="form-group">
              <label htmlFor="firstName">First Name (Optional)</label>
              <input
                type="text"
                id="firstName"
                name="firstName"
                value={formData.firstName}
                onChange={handleChange}
                placeholder="John"
              />
            </div>

            <div className="form-group">
              <label htmlFor="lastName">Last Name (Optional)</label>
              <input
                type="text"
                id="lastName"
                name="lastName"
                value={formData.lastName}
                onChange={handleChange}
                placeholder="Doe"
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

            <div className="form-group">
              <label htmlFor="confirmPassword">Confirm Password</label>
              <input
                type="password"
                id="confirmPassword"
                name="confirmPassword"
                value={formData.confirmPassword}
                onChange={handleChange}
                required
                placeholder="••••••••"
              />
            </div>

            <button type="submit" className="btn-primary" disabled={loading}>
              {loading ? "Creating account..." : "Sign Up"}
            </button>
          </form>

          <div className="auth-footer">
            Already have an account?{" "}
            <Link to="/login" className="auth-link">
              Login
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

export default RegisterPage;
