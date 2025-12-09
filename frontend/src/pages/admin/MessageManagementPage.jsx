import { useState, useEffect } from "react";
import { adminMessageAPI } from "../../services/adminApi";
import "./MessageManagementPage.css";

const MessageManagementPage = () => {
  const [messages, setMessages] = useState([]);
  const [loading, setLoading] = useState(false);
  const [sessionFilter, setSessionFilter] = useState("");
  const [currentPage, setCurrentPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [pageSize, setPageSize] = useState(20);
  const [sortBy, setSortBy] = useState("createdAt");
  const [sortDirection, setSortDirection] = useState("desc");
  const [selectedMessage, setSelectedMessage] = useState(null);
  const [showDeleteModal, setShowDeleteModal] = useState(false);
  const [showFlagModal, setShowFlagModal] = useState(false);
  const [showMessageModal, setShowMessageModal] = useState(false);
  const [flagReason, setFlagReason] = useState("");
  const [flagType, setFlagType] = useState("OTHER");

  useEffect(() => {
    fetchMessages();
  }, [currentPage, pageSize, sortBy, sortDirection, sessionFilter]);

  const fetchMessages = async () => {
    try {
      setLoading(true);

      if (sessionFilter && sessionFilter.trim()) {
        // Fetch messages for specific session (accepts UUID)
        const response = await adminMessageAPI.getMessagesBySession(
          sessionFilter.trim()
        );
        const data = response.data || response;
        setMessages(data.messages || data || []);
        setTotalPages(1);
      } else {
        // Fetch all messages with pagination
        const params = {
          page: currentPage,
          size: pageSize,
          sortBy,
          sortDirection,
        };
        const response = await adminMessageAPI.getAllMessages(params);
        const data = response.data || response;
        setMessages(data.messages || []);
        setTotalPages(data.totalPages || 0);
      }
    } catch (error) {
      console.error("Error fetching messages:", error);
    } finally {
      setLoading(false);
    }
  };

  const handleDeleteMessage = async () => {
    if (!selectedMessage) return;

    try {
      await adminMessageAPI.deleteMessage(selectedMessage.id);
      setShowDeleteModal(false);
      setSelectedMessage(null);
      fetchMessages();
    } catch (error) {
      console.error("Error deleting message:", error);
      alert("Failed to delete message");
    }
  };

  const handleFlagMessage = async () => {
    if (!selectedMessage || !flagReason.trim()) {
      alert("Please provide a reason for flagging");
      return;
    }

    try {
      await adminMessageAPI.flagMessage(selectedMessage.id, {
        flagType: flagType,
        reason: flagReason,
      });
      setShowFlagModal(false);
      setSelectedMessage(null);
      setFlagReason("");
      setFlagType("OTHER");
      fetchMessages();
    } catch (error) {
      console.error("Error flagging message:", error);
      alert("Failed to flag message");
    }
  };

  const handleUnflagMessage = async (messageId) => {
    try {
      await adminMessageAPI.unflagMessage(messageId);
      fetchMessages();
    } catch (error) {
      console.error("Error unflagging message:", error);
      alert("Failed to unflag message");
    }
  };

  const getRoleBadgeClass = (role) => {
    return role === "user"
      ? "role-badge role-user"
      : "role-badge role-assistant";
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

  const truncateText = (text, maxLength = 100) => {
    if (!text) return "";
    return text.length > maxLength
      ? text.substring(0, maxLength) + "..."
      : text;
  };

  return (
    <div className="message-management">
      <div className="page-header">
        <h1>Message Management</h1>
        <p>View and moderate all chat messages</p>
      </div>

      {/* Filters */}
      <div className="filters-section">
        <div className="filter-row">
          <div className="filter-group">
            <label>Session ID Filter:</label>
            <input
              type="text"
              placeholder="Paste session UUID here..."
              value={sessionFilter}
              onChange={(e) => {
                setSessionFilter(e.target.value);
                setCurrentPage(0);
              }}
              className="session-filter-input"
            />
            {sessionFilter && (
              <button
                onClick={() => setSessionFilter("")}
                className="btn-clear-filter"
              >
                Clear
              </button>
            )}
          </div>

          <div className="filter-group">
            <label>Sort By:</label>
            <select value={sortBy} onChange={(e) => setSortBy(e.target.value)}>
              <option value="createdAt">Created Date</option>
              <option value="id">Message ID</option>
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

      {/* Messages Table */}
      {loading ? (
        <div className="loading">Loading messages...</div>
      ) : (
        <>
          <div className="table-container">
            <table className="messages-table">
              <thead>
                <tr>
                  <th>ID</th>
                  <th>Session ID</th>
                  <th>Role</th>
                  <th>Content</th>
                  <th>Flagged</th>
                  <th>Created</th>
                  <th>Actions</th>
                </tr>
              </thead>
              <tbody>
                {messages.length === 0 ? (
                  <tr>
                    <td colSpan="7" className="no-data">
                      No messages found
                    </td>
                  </tr>
                ) : (
                  messages.map((message) => (
                    <tr key={message.id}>
                      <td>{message.id}</td>
                      <td>
                        <button
                          className="session-link"
                          onClick={() =>
                            setSessionFilter(message.sessionId.toString())
                          }
                        >
                          #{message.sessionId}
                        </button>
                      </td>
                      <td>
                        <span className={getRoleBadgeClass(message.role)}>
                          {message.role}
                        </span>
                      </td>
                      <td className="message-content">
                        <div
                          className="content-preview"
                          onClick={() => {
                            setSelectedMessage(message);
                            setShowMessageModal(true);
                          }}
                          title="Click to view full message"
                        >
                          {truncateText(message.content, 150)}
                        </div>
                      </td>
                      <td>
                        {message.isFlagged ? (
                          <span className="flag-badge flagged">🚩 Yes</span>
                        ) : (
                          <span className="flag-badge">No</span>
                        )}
                      </td>
                      <td>{formatDate(message.createdAt)}</td>
                      <td>
                        <div className="action-buttons">
                          <button
                            onClick={() => {
                              setSelectedMessage(message);
                              setShowMessageModal(true);
                            }}
                            className="btn-action btn-view"
                            title="View Full Message"
                          >
                            👁️
                          </button>
                          {message.isFlagged ? (
                            <button
                              onClick={() => handleUnflagMessage(message.id)}
                              className="btn-action btn-unflag"
                              title="Unflag"
                            >
                              ✓
                            </button>
                          ) : (
                            <button
                              onClick={() => {
                                setSelectedMessage(message);
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
                              setSelectedMessage(message);
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
          {!sessionFilter && totalPages > 0 && (
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

      {/* View Message Modal */}
      {showMessageModal && selectedMessage && (
        <div
          className="modal-overlay"
          onClick={() => setShowMessageModal(false)}
        >
          <div
            className="modal modal-large"
            onClick={(e) => e.stopPropagation()}
          >
            <div className="modal-header">
              <h2>Message Details</h2>
              <button
                className="btn-close"
                onClick={() => setShowMessageModal(false)}
              >
                ✕
              </button>
            </div>
            <div className="message-details">
              <div className="detail-row">
                <span className="detail-label">ID:</span>
                <span>{selectedMessage.id}</span>
              </div>
              <div className="detail-row">
                <span className="detail-label">Session ID:</span>
                <span>#{selectedMessage.sessionId}</span>
              </div>
              <div className="detail-row">
                <span className="detail-label">Role:</span>
                <span className={getRoleBadgeClass(selectedMessage.role)}>
                  {selectedMessage.role}
                </span>
              </div>
              <div className="detail-row">
                <span className="detail-label">Created:</span>
                <span>{formatDate(selectedMessage.createdAt)}</span>
              </div>
              <div className="detail-row full-width">
                <span className="detail-label">Content:</span>
                <div className="message-full-content">
                  {selectedMessage.content}
                </div>
              </div>
            </div>
          </div>
        </div>
      )}

      {/* Delete Modal */}
      {showDeleteModal && (
        <div
          className="modal-overlay"
          onClick={() => setShowDeleteModal(false)}
        >
          <div className="modal" onClick={(e) => e.stopPropagation()}>
            <h2>Delete Message</h2>
            <p>Are you sure you want to delete this message?</p>
            <div className="message-preview">
              <strong>Content:</strong>
              <p>{truncateText(selectedMessage?.content, 200)}</p>
            </div>
            <p className="warning">This action cannot be undone.</p>
            <div className="modal-actions">
              <button
                onClick={() => setShowDeleteModal(false)}
                className="btn-cancel"
              >
                Cancel
              </button>
              <button
                onClick={handleDeleteMessage}
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
            <h2>Flag Message</h2>
            <p>Provide a reason for flagging this message:</p>
            <div className="message-preview">
              <strong>Content:</strong>
              <p>{truncateText(selectedMessage?.content, 200)}</p>
            </div>

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
              <button onClick={handleFlagMessage} className="btn-confirm">
                Flag Message
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default MessageManagementPage;
