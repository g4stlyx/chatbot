import { useChat } from "../../context/ChatContext";
import { useNavigate } from "react-router-dom";
import { formatDistanceToNow } from "date-fns";

const Sidebar = ({ user, onLogout }) => {
  const navigate = useNavigate();
  const {
    sessions,
    currentSession,
    selectSession,
    createNewSession,
    deleteSession,
  } = useChat();

  const handleDeleteSession = (e, sessionId) => {
    e.stopPropagation();
    if (window.confirm("Are you sure you want to delete this conversation?")) {
      deleteSession(sessionId);
    }
  };

  return (
    <div className="sidebar">
      <div className="sidebar-header">
        <h2>💬 Chatbot AI</h2>
        <div className="user-info">{user?.username || user?.email}</div>
      </div>

      <div className="sidebar-content">
        <button className="new-chat-btn" onClick={createNewSession}>
          + New Chat
        </button>

        <ul className="session-list">
          {sessions.map((session) => (
            <li
              key={session.sessionId}
              className={`session-item ${
                currentSession?.sessionId === session.sessionId ? "active" : ""
              }`}
              onClick={() => selectSession(session.sessionId)}
            >
              <div style={{ flex: 1, minWidth: 0 }}>
                <div className="session-title">
                  {session.title || "New Conversation"}
                </div>
                <div className="session-date">
                  {session.createdAt &&
                    formatDistanceToNow(new Date(session.createdAt), {
                      addSuffix: true,
                    })}
                </div>
              </div>
              <button
                className="delete-session-btn"
                onClick={(e) => handleDeleteSession(e, session.sessionId)}
                title="Delete conversation"
              >
                🗑️
              </button>
            </li>
          ))}
        </ul>

        {sessions.length === 0 && (
          <div
            style={{
              color: "#9ca3af",
              textAlign: "center",
              padding: "20px",
              fontSize: "14px",
            }}
          >
            No conversations yet. Start a new chat!
          </div>
        )}
      </div>

      <div className="sidebar-footer">
        <button
          className="profile-btn"
          onClick={() => navigate("/profile")}
          style={{
            width: "100%",
            padding: "10px",
            marginBottom: "10px",
            background: "transparent",
            border: "1px solid #4b5563",
            color: "white",
            borderRadius: "8px",
            cursor: "pointer",
            transition: "all 0.2s",
          }}
          onMouseEnter={(e) => {
            e.currentTarget.style.background = "#374151";
          }}
          onMouseLeave={(e) => {
            e.currentTarget.style.background = "transparent";
          }}
        >
          👤 My Profile
        </button>
        <button className="logout-btn" onClick={onLogout}>
          Logout
        </button>
      </div>
    </div>
  );
};

export default Sidebar;
