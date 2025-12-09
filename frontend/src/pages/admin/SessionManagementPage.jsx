import { useState, useEffect } from "react";
import { adminSessionAPI } from "../../services/adminApi";
import "./SessionManagementPage.css";

const SessionManagementPage = () => {
  const [sessions, setSessions] = useState([]);
  const [loading, setLoading] = useState(false);
  const [searchTerm, setSearchTerm] = useState("");
  const [statusFilter, setStatusFilter] = useState("ALL");
  const [currentPage, setCurrentPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [pageSize, setPageSize] = useState(10);
  const [sortBy, setSortBy] = useState("createdAt");
  const [sortDirection, setSortDirection] = useState("desc");
  const [selectedSession, setSelectedSession] = useState(null);
  const [showDeleteModal, setShowDeleteModal] = useState(false);
  const [showFlagModal, setShowFlagModal] = useState(false);
  const [flagReason, setFlagReason] = useState("");
  const [flagType, setFlagType] = useState("OTHER");

  useEffect(() => {
    fetchSessions();
  }, [currentPage, pageSize, statusFilter, sortBy, sortDirection]);

  const fetchSessions = async () => {
    try {
      setLoading(true);
      const params = {
        page: currentPage,
        size: pageSize,
        sortBy,
        sortDirection,
      };

      if (statusFilter !== "ALL") {
        params.status = statusFilter;
      }

      const response = await adminSessionAPI.getAllSessions(params);

      // Backend returns: { sessions: [], currentPage, totalPages, totalElements, pageSize }
      const data = response.data || response;
      console.log("First session:", data.sessions?.[0]);
      setSessions(data.sessions || []);
      setTotalPages(data.totalPages || 0);
    } catch (error) {
      console.error("Error fetching sessions:", error);
    } finally {
      setLoading(false);
    }
  };

  const handleSearch = async () => {
    if (!searchTerm.trim()) {
      fetchSessions();
      return;
    }

    try {
      setLoading(true);
      const response = await adminSessionAPI.searchSessions({
        query: searchTerm,
        page: currentPage,
        size: pageSize,
      });
      const data = response.data || response;
      setSessions(data.sessions || []);
      setTotalPages(data.totalPages || 0);
    } catch (error) {
      console.error("Error searching sessions:", error);
    } finally {
      setLoading(false);
    }
  };

  const handleDeleteSession = async () => {
    if (!selectedSession) return;

    try {
      await adminSessionAPI.deleteSession(selectedSession.sessionId);
      setShowDeleteModal(false);
      setSelectedSession(null);
      fetchSessions();
    } catch (error) {
      console.error("Error deleting session:", error);
      alert("Failed to delete session");
    }
  };

  const handleArchiveSession = async (sessionId) => {
    try {
      await adminSessionAPI.archiveSession(sessionId);
      fetchSessions();
    } catch (error) {
      console.error("Error archiving session:", error);
      alert("Failed to archive session");
    }
  };

  const handleFlagSession = async () => {
    if (!selectedSession || !flagReason.trim()) {
      alert("Please provide a reason for flagging");
      return;
    }

    try {
      await adminSessionAPI.flagSession(selectedSession.sessionId, {
        flagType: flagType,
        reason: flagReason,
      });
      setShowFlagModal(false);
      setSelectedSession(null);
      setFlagReason("");
      setFlagType("OTHER");
      fetchSessions();
    } catch (error) {
      console.error("Error flagging session:", error);
      alert("Failed to flag session");
    }
  };

  const handleUnflagSession = async (sessionId) => {
    try {
      await adminSessionAPI.unflagSession(sessionId);
      fetchSessions();
    } catch (error) {
      console.error("Error unflagging session:", error);
      alert("Failed to unflag session");
    }
  };

  const getStatusBadgeClass = (status) => {
    switch (status) {
      case "ACTIVE":
        return "status-badge status-active";
      case "PAUSED":
        return "status-badge status-paused";
      case "ARCHIVED":
        return "status-badge status-archived";
      default:
        return "status-badge";
    }
  };

  const formatDate = (dateString) => {
    return new Date(dateString).toLocaleString("tr-TR", {
      year: "numeric",
      month: "short",
      day: "numeric",
      hour: "2-digit",
      minute: "2-digit",
    });
  };

  return (
    <div className="session-management">
      <div className="page-header">
        <h1>Session Management</h1>
        <p>Manage and moderate all chat sessions</p>
      </div>

      {/* Filters and Search */}
      <div className="filters-section">
        <div className="search-box">
          <input
            type="text"
            placeholder="Search sessions by title..."
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
            onKeyPress={(e) => e.key === "Enter" && handleSearch()}
          />
          <button onClick={handleSearch} className="btn-search">
            🔍 Search
          </button>
        </div>

        <div className="filters-row">
          <div className="filter-group">
            <label>Status:</label>
            <select
              value={statusFilter}
              onChange={(e) => {
                setStatusFilter(e.target.value);
                setCurrentPage(0);
              }}
            >
              <option value="ALL">All</option>
              <option value="ACTIVE">Active</option>
              <option value="PAUSED">Paused</option>
              <option value="ARCHIVED">Archived</option>
            </select>
          </div>

          <div className="filter-group">
            <label>Sort By:</label>
            <select value={sortBy} onChange={(e) => setSortBy(e.target.value)}>
              <option value="createdAt">Created Date</option>
              <option value="updatedAt">Last Updated</option>
              <option value="title">Title</option>
            </select>
          </div>

          <div className="filter-group">
            <label>Order:</label>
            <select
              value={sortDirection}
              onChange={(e) => setSortDirection(e.target.value)}
            >
              <option value="desc">Descending</option>
              <option value="asc">Ascending</option>
            </select>
          </div>

          <div className="filter-group">
            <label>Per Page:</label>
            <select
              value={pageSize}
              onChange={(e) => {
                setPageSize(Number(e.target.value));
                setCurrentPage(0);
              }}
            >
              <option value="10">10</option>
              <option value="20">20</option>
              <option value="50">50</option>
            </select>
          </div>
        </div>
      </div>

      {/* Sessions Table */}
      {loading ? (
        <div className="loading">Loading sessions...</div>
      ) : (
        <>
          <div className="table-container">
            <table className="sessions-table">
              <thead>
                <tr>
                  <th>ID</th>
                  <th>Title</th>
                  <th>User</th>
                  <th>Status</th>
                  <th>Messages</th>
                  <th>Flagged</th>
                  <th>Created</th>
                  <th>Actions</th>
                </tr>
              </thead>
              <tbody>
                {sessions.length === 0 ? (
                  <tr>
                    <td colSpan="8" className="no-data">
                      No sessions found
                    </td>
                  </tr>
                ) : (
                  sessions.map((session) => (
                    <tr key={session.sessionId}>
                      <td>
                        <div className="id-cell">
                          <span title={session.sessionId}>
                            {session.sessionId?.substring(0, 8)}...
                          </span>
                          <button
                            className="btn-copy-id"
                            onClick={() => {
                              navigator.clipboard.writeText(session.sessionId);
                              alert("Session ID copied!");
                            }}
                            title="Copy full ID"
                          >
                            📋
                          </button>
                        </div>
                      </td>
                      <td className="session-title">
                        {session.title || "Untitled Session"}
                      </td>
                      <td>
                        {session.username ? (
                          <div className="user-info">
                            <div>{session.username}</div>
                            <div className="user-email">
                              {session.userEmail}
                            </div>
                          </div>
                        ) : (
                          "N/A"
                        )}
                      </td>
                      <td>
                        <span className={getStatusBadgeClass(session.status)}>
                          {session.status}
                        </span>
                      </td>
                      <td>{session.messageCount || 0}</td>
                      <td>
                        {session.isFlagged ? (
                          <span className="flag-badge flagged">🚩 Yes</span>
                        ) : (
                          <span className="flag-badge">No</span>
                        )}
                      </td>
                      <td>{formatDate(session.createdAt)}</td>
                      <td>
                        <div className="action-buttons">
                          {session.status !== "ARCHIVED" && (
                            <button
                              onClick={() =>
                                handleArchiveSession(session.sessionId)
                              }
                              className="btn-action btn-archive"
                              title="Archive"
                            >
                              📦
                            </button>
                          )}
                          {session.isFlagged ? (
                            <button
                              onClick={() =>
                                handleUnflagSession(session.sessionId)
                              }
                              className="btn-action btn-unflag"
                              title="Unflag"
                            >
                              ✓
                            </button>
                          ) : (
                            <button
                              onClick={() => {
                                setSelectedSession(session);
                                setShowFlagModal(true);
                              }}
                              className="btn-action btn-flag"
                              title="Flag"
                            >
                              🚩
                            </button>
                          )}
                          <button
                            onClick={() => {
                              setSelectedSession(session);
                              setShowDeleteModal(true);
                            }}
                            className="btn-action btn-delete"
                            title="Delete"
                          >
                            🗑️
                          </button>
                        </div>
                      </td>
                    </tr>
                  ))
                )}
              </tbody>
            </table>
          </div>

          {/* Pagination */}
          {totalPages > 0 && (
            <div className="pagination">
              <button
                onClick={() => setCurrentPage((prev) => Math.max(0, prev - 1))}
                disabled={currentPage === 0}
                className="btn-page"
              >
                Previous
              </button>
              <span className="page-info">
                Page {currentPage + 1} of {totalPages}
              </span>
              <button
                onClick={() =>
                  setCurrentPage((prev) => Math.min(totalPages - 1, prev + 1))
                }
                disabled={currentPage >= totalPages - 1}
                className="btn-page"
              >
                Next
              </button>
            </div>
          )}
        </>
      )}

      {/* Delete Modal */}
      {showDeleteModal && (
        <div
          className="modal-overlay"
          onClick={() => setShowDeleteModal(false)}
        >
          <div className="modal" onClick={(e) => e.stopPropagation()}>
            <h2>Delete Session</h2>
            <p>Are you sure you want to delete this session?</p>
            <p className="session-info">
              <strong>{selectedSession?.title || "Untitled Session"}</strong>
            </p>
            <p className="warning">This action cannot be undone.</p>
            <div className="modal-actions">
              <button
                onClick={() => setShowDeleteModal(false)}
                className="btn-cancel"
              >
                Cancel
              </button>
              <button
                onClick={handleDeleteSession}
                className="btn-confirm-delete"
              >
                Delete
              </button>
            </div>
          </div>
        </div>
      )}

      {/* Flag Modal */}
      {showFlagModal && (
        <div className="modal-overlay" onClick={() => setShowFlagModal(false)}>
          <div className="modal" onClick={(e) => e.stopPropagation()}>
            <h2>Flag Session</h2>
            <p>Provide a reason for flagging this session:</p>
            <p className="session-info">
              <strong>{selectedSession?.title || "Untitled Session"}</strong>
            </p>

            <div className="form-group">
              <label>Flag Type:</label>
              <select
                value={flagType}
                onChange={(e) => setFlagType(e.target.value)}
                className="flag-type-select"
              >
                <option value="INAPPROPRIATE_CONTENT">
                  Inappropriate Content
                </option>
                <option value="SPAM">Spam</option>
                <option value="ABUSE">Abuse</option>
                <option value="POLICY_VIOLATION">Policy Violation</option>
                <option value="OTHER">Other</option>
              </select>
            </div>

            <textarea
              value={flagReason}
              onChange={(e) => setFlagReason(e.target.value)}
              placeholder="Enter reason for flagging..."
              rows="4"
              className="flag-reason-input"
            />
            <div className="modal-actions">
              <button
                onClick={() => {
                  setShowFlagModal(false);
                  setFlagReason("");
                  setFlagType("OTHER");
                }}
                className="btn-cancel"
              >
                Cancel
              </button>
              <button onClick={handleFlagSession} className="btn-confirm">
                Flag Session
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default SessionManagementPage;
