import { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import { sessionAPI } from "../services/api";
import "./PublicSessionsPage.css";

const PublicSessionsPage = () => {
  const navigate = useNavigate();
  const [sessions, setSessions] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [copiedId, setCopiedId] = useState(null);

  useEffect(() => {
    fetchPublicSessions();
  }, []);

  const fetchPublicSessions = async () => {
    try {
      setLoading(true);
      setError(null);
      const response = await sessionAPI.getPublicSessions();
      const data = response.data.data || response.data;
      setSessions(data.sessions || data || []);
    } catch (err) {
      console.error("Error fetching public sessions:", err);
      setError("Failed to load public sessions");
    } finally {
      setLoading(false);
    }
  };

  const handleCopySession = async (sessionId) => {
    try {
      setCopiedId(sessionId);
      await sessionAPI.copyPublicSession(sessionId);
      setTimeout(() => {
        navigate("/chat");
      }, 500);
    } catch (err) {
      console.error("Error copying session:", err);
      alert(err.response?.data?.message || "Failed to copy session");
      setCopiedId(null);
    }
  };

  const formatDate = (dateString) => {
    const date = new Date(dateString);
    const now = new Date();
    const diffMs = now - date;
    const diffMins = Math.floor(diffMs / 60000);
    const diffHours = Math.floor(diffMs / 3600000);
    const diffDays = Math.floor(diffMs / 86400000);

    if (diffMins < 60) return `${diffMins}m ago`;
    if (diffHours < 24) return `${diffHours}h ago`;
    if (diffDays < 30) return `${diffDays}d ago`;
    return date.toLocaleDateString();
  };

  if (loading) {
    return (
      <div className="public-sessions-page">
        <div className="loading-state">Loading public sessions...</div>
      </div>
    );
  }

  return (
    <div className="public-sessions-page">
      <div className="page-header">
        <div className="header-content">
          <h1>🌐 Public Sessions</h1>
          <p>Discover and copy conversations shared by the community</p>
        </div>
        <button className="btn-back" onClick={() => navigate("/chat")}>
          ← Back to Chat
        </button>
      </div>

      {error && <div className="error-message">{error}</div>}

      {sessions.length === 0 ? (
        <div className="empty-state">
          <div className="empty-icon">🌐</div>
          <h2>No Public Sessions Yet</h2>
          <p>
            Be the first to share a conversation! Make a session public from
            your chat sidebar.
          </p>
        </div>
      ) : (
        <div className="sessions-grid">
          {sessions.map((session) => (
            <div key={session.sessionId} className="session-card">
              <div className="session-header">
                <div className="session-title-wrapper">
                  <h3>{session.title || "Untitled Session"}</h3>
                  <span className="public-badge">🌐 Public</span>
                </div>
                <div className="session-meta">
                  <span className="session-user">
                    👤 {session.username || "Anonymous"}
                  </span>
                  <span className="session-date">
                    📅 {formatDate(session.createdAt)}
                  </span>
                </div>
              </div>

              <div className="session-stats">
                <span title="Message count">
                  💬 {session.messageCount || 0} messages
                </span>
                <span
                  title="Status"
                  className={`status-badge status-${session.status?.toLowerCase()}`}
                >
                  {session.status || "ACTIVE"}
                </span>
              </div>

              {session.lastMessage && (
                <div className="session-preview">
                  <p>
                    {session.lastMessage.substring(0, 150)}
                    {session.lastMessage.length > 150 ? "..." : ""}
                  </p>
                </div>
              )}

              <div className="session-actions">
                <button
                  className={`btn-copy ${
                    copiedId === session.sessionId ? "copied" : ""
                  }`}
                  onClick={() => handleCopySession(session.sessionId)}
                  disabled={copiedId === session.sessionId}
                >
                  {copiedId === session.sessionId
                    ? "✓ Copied!"
                    : "📋 Copy to My Sessions"}
                </button>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
};

export default PublicSessionsPage;
