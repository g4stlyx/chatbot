import { useState } from "react";
import { useChat } from "../../context/ChatContext";
import { useNavigate } from "react-router-dom";
import { formatDistanceToNow } from "date-fns";
import SessionActions from "./SessionActions";
import { sessionAPI } from "../../services/api";

const Sidebar = ({ user, onLogout }) => {
  const navigate = useNavigate();
  const {
    sessions,
    currentSession,
    selectSession,
    createNewSession,
    deleteSession,
    renameSession,
    archiveSession,
    pauseSession,
    activateSession,
  } = useChat();

  const [editingSessionId, setEditingSessionId] = useState(null);
  const [editTitle, setEditTitle] = useState("");
  const [statusFilter, setStatusFilter] = useState("ALL");
  const [searchQuery, setSearchQuery] = useState("");
  const [searchResults, setSearchResults] = useState(null);
  const [isSearching, setIsSearching] = useState(false);

  // Use search results if available, otherwise filter sessions
  const displayedSessions = searchResults !== null ? searchResults : sessions;

  // Filter sessions based on status
  const filteredSessions = displayedSessions.filter((session) => {
    if (statusFilter === "ALL") return true;
    return session.status === statusFilter;
  });

  const handleSearch = async (query) => {
    setSearchQuery(query);

    if (!query.trim()) {
      setSearchResults(null);
      return;
    }

    try {
      setIsSearching(true);
      const response = await sessionAPI.searchSessions(query);
      const data = response.data.data || response.data;
      setSearchResults(data.sessions || []);
    } catch (error) {
      console.error("Search failed:", error);
      setSearchResults([]);
    } finally {
      setIsSearching(false);
    }
  };

  const clearSearch = () => {
    setSearchQuery("");
    setSearchResults(null);
  };

  const handleRename = (sessionId) => {
    const session = sessions.find((s) => s.sessionId === sessionId);
    if (session) {
      setEditingSessionId(sessionId);
      setEditTitle(session.title || "");
    }
  };

  const handleSaveRename = async (sessionId) => {
    if (
      editTitle.trim() &&
      editTitle !== sessions.find((s) => s.sessionId === sessionId)?.title
    ) {
      try {
        await renameSession(sessionId, editTitle.trim());
      } catch (error) {
        console.error("Failed to rename session:", error);
      }
    }
    setEditingSessionId(null);
    setEditTitle("");
  };

  const handleCancelRename = () => {
    setEditingSessionId(null);
    setEditTitle("");
  };

  const handleDeleteSession = (sessionId) => {
    if (window.confirm("Are you sure you want to delete this conversation?")) {
      deleteSession(sessionId);
    }
  };

  const handleArchive = async (sessionId) => {
    try {
      await archiveSession(sessionId);
    } catch (error) {
      console.error("Failed to archive session:", error);
    }
  };

  const handlePause = async (sessionId) => {
    try {
      await pauseSession(sessionId);
    } catch (error) {
      console.error("Failed to pause session:", error);
    }
  };

  const handleActivate = async (sessionId) => {
    try {
      await activateSession(sessionId);
    } catch (error) {
      console.error("Failed to activate session:", error);
    }
  };

  const getStatusBadge = (status) => {
    switch (status) {
      case "ACTIVE":
        return (
          <span className="status-badge active" title="Active">
            ●
          </span>
        );
      case "PAUSED":
        return (
          <span className="status-badge paused" title="Paused">
            ●
          </span>
        );
      case "ARCHIVED":
        return (
          <span className="status-badge archived" title="Archived">
            ●
          </span>
        );
      default:
        return null;
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

        {/* Search Box */}
        <div className="search-box">
          <input
            type="text"
            placeholder="Search conversations..."
            value={searchQuery}
            onChange={(e) => handleSearch(e.target.value)}
            className="search-input"
          />
          {searchQuery && (
            <button
              onClick={clearSearch}
              className="clear-search-btn"
              title="Clear search"
            >
              ✕
            </button>
          )}
          {isSearching && <span className="search-loading">🔍</span>}
        </div>

        {/* Navigation Links */}
        <div className="sidebar-nav">
          <button onClick={() => navigate("/projects")} className="nav-btn">
            📁 Projects
          </button>
          <button
            onClick={() => navigate("/public-sessions")}
            className="nav-btn"
          >
            🌐 Public Sessions
          </button>
        </div>

        {/* Status Filter */}
        <div className="status-filter">
          <select
            value={statusFilter}
            onChange={(e) => setStatusFilter(e.target.value)}
            className="filter-select"
          >
            <option value="ALL">All Conversations</option>
            <option value="ACTIVE">Active</option>
            <option value="PAUSED">Paused</option>
            <option value="ARCHIVED">Archived</option>
          </select>
        </div>

        <ul className="session-list">
          {filteredSessions.map((session) => (
            <li
              key={session.sessionId}
              className={`session-item ${
                currentSession?.sessionId === session.sessionId ? "active" : ""
              }`}
              onClick={() =>
                !editingSessionId && selectSession(session.sessionId)
              }
            >
              <div className="session-main">
                {getStatusBadge(session.status)}

                {editingSessionId === session.sessionId ? (
                  <input
                    type="text"
                    value={editTitle}
                    onChange={(e) => setEditTitle(e.target.value)}
                    onBlur={() => handleSaveRename(session.sessionId)}
                    onKeyDown={(e) => {
                      if (e.key === "Enter") {
                        handleSaveRename(session.sessionId);
                      } else if (e.key === "Escape") {
                        handleCancelRename();
                      }
                    }}
                    onClick={(e) => e.stopPropagation()}
                    autoFocus
                    className="session-title-input"
                  />
                ) : (
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
                )}

                <SessionActions
                  session={session}
                  onRename={handleRename}
                  onArchive={handleArchive}
                  onPause={handlePause}
                  onActivate={handleActivate}
                  onDelete={handleDeleteSession}
                />
              </div>
            </li>
          ))}
        </ul>

        {filteredSessions.length === 0 && (
          <div
            style={{
              color: "#9ca3af",
              textAlign: "center",
              padding: "20px",
              fontSize: "14px",
            }}
          >
            {searchQuery
              ? "No conversations found matching your search."
              : statusFilter === "ALL"
              ? "No conversations yet. Start a new chat!"
              : `No ${statusFilter.toLowerCase()} conversations.`}
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
