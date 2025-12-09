import { useState, useEffect } from "react";
import { useAdmin } from "../../context/AdminContext";
import { adminActivityLogAPI } from "../../services/adminApi";
import "./ActivityLogsPage.css";

const ActivityLogsPage = () => {
  const { hasLevel } = useAdmin();
  const [logs, setLogs] = useState([]);
  const [stats, setStats] = useState(null);
  const [loading, setLoading] = useState(false);
  const [currentPage, setCurrentPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [pageSize, setPageSize] = useState(20);
  const [sortBy, setSortBy] = useState("timestamp");
  const [sortDirection, setSortDirection] = useState("desc");
  const [actionFilter, setActionFilter] = useState("");
  const [adminFilter, setAdminFilter] = useState("");
  const [showDeleteModal, setShowDeleteModal] = useState(false);
  const [selectedLog, setSelectedLog] = useState(null);
  const [showDetailsModal, setShowDetailsModal] = useState(false);

  // Only Level 0 (Super Admin) can access
  const canAccessLogs = hasLevel(0);

  useEffect(() => {
    if (canAccessLogs) {
      fetchLogs();
      fetchStats();
    }
  }, [
    currentPage,
    pageSize,
    sortBy,
    sortDirection,
    actionFilter,
    adminFilter,
    canAccessLogs,
  ]);

  const fetchLogs = async () => {
    try {
      setLoading(true);
      const params = {
        page: currentPage,
        size: pageSize,
        sortBy,
        sortDirection,
      };

      if (actionFilter) params.action = actionFilter;
      if (adminFilter) params.adminId = parseInt(adminFilter);

      const response = await adminActivityLogAPI.getAllLogs(params);
      const data = response.data || response;
      setLogs(data.logs || data.content || []);
      setTotalPages(data.totalPages || 0);
    } catch (error) {
      console.error("Error fetching logs:", error);
    } finally {
      setLoading(false);
    }
  };

  const fetchStats = async () => {
    try {
      const statsData = await adminActivityLogAPI.getStats();
      setStats(statsData);
    } catch (error) {
      console.error("Error fetching stats:", error);
    }
  };

  const handleDeleteLog = async () => {
    if (!selectedLog) return;

    try {
      await adminActivityLogAPI.deleteLog(selectedLog.id);
      setShowDeleteModal(false);
      setSelectedLog(null);
      fetchLogs();
      fetchStats();
    } catch (error) {
      console.error("Error deleting log:", error);
      alert("Failed to delete log");
    }
  };

  const getActionBadgeClass = (action) => {
    if (action.includes("CREATE")) return "action-badge action-create";
    if (action.includes("UPDATE") || action.includes("EDIT"))
      return "action-badge action-update";
    if (action.includes("DELETE")) return "action-badge action-delete";
    if (action.includes("LOGIN")) return "action-badge action-login";
    if (action.includes("LOGOUT")) return "action-badge action-logout";
    return "action-badge";
  };

  const formatDate = (dateString) => {
    return new Date(dateString).toLocaleString("tr-TR", {
      year: "numeric",
      month: "short",
      day: "numeric",
      hour: "2-digit",
      minute: "2-digit",
      second: "2-digit",
    });
  };

  if (!canAccessLogs) {
    return (
      <div className="activity-logs">
        <div className="access-denied">
          <h1>⛔ Access Denied</h1>
          <p>You don't have permission to view activity logs.</p>
          <p>Required level: 0 (Super Admin only)</p>
        </div>
      </div>
    );
  }

  return (
    <div className="activity-logs">
      <div className="page-header">
        <div>
          <h1>Activity Logs</h1>
          <p>Monitor all admin activities and actions</p>
        </div>
      </div>

      {/* Statistics Cards */}
      {stats && (
        <div className="stats-grid">
          <div className="stat-card">
            <div className="stat-icon">📊</div>
            <div className="stat-content">
              <div className="stat-value">{stats.totalLogs || 0}</div>
              <div className="stat-label">Total Logs</div>
            </div>
          </div>
          <div className="stat-card">
            <div className="stat-icon">👥</div>
            <div className="stat-content">
              <div className="stat-value">{stats.uniqueAdmins || 0}</div>
              <div className="stat-label">Active Admins</div>
            </div>
          </div>
          <div className="stat-card">
            <div className="stat-icon">⚡</div>
            <div className="stat-content">
              <div className="stat-value">{stats.actionsToday || 0}</div>
              <div className="stat-label">Actions Today</div>
            </div>
          </div>
          <div className="stat-card">
            <div className="stat-icon">🔄</div>
            <div className="stat-content">
              <div className="stat-value">{stats.actionsThisWeek || 0}</div>
              <div className="stat-label">Actions This Week</div>
            </div>
          </div>
        </div>
      )}

      {/* Filters */}
      <div className="filters-section">
        <div className="filters-row">
          <div className="filter-group">
            <label>Action Type:</label>
            <select
              value={actionFilter}
              onChange={(e) => {
                setActionFilter(e.target.value);
                setCurrentPage(0);
              }}
            >
              <option value="">All Actions</option>
              <option value="CREATE">Create</option>
              <option value="UPDATE">Update</option>
              <option value="DELETE">Delete</option>
              <option value="LOGIN">Login</option>
              <option value="LOGOUT">Logout</option>
            </select>
          </div>

          <div className="filter-group">
            <label>Admin ID:</label>
            <input
              type="number"
              placeholder="Filter by admin ID..."
              value={adminFilter}
              onChange={(e) => {
                setAdminFilter(e.target.value);
                setCurrentPage(0);
              }}
            />
          </div>

          <div className="filter-group">
            <label>Sort By:</label>
            <select value={sortBy} onChange={(e) => setSortBy(e.target.value)}>
              <option value="timestamp">Timestamp</option>
              <option value="action">Action</option>
              <option value="adminId">Admin ID</option>
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
              <option value="20">20</option>
              <option value="50">50</option>
              <option value="100">100</option>
            </select>
          </div>
        </div>
      </div>

      {/* Logs Table */}
      {loading ? (
        <div className="loading">Loading activity logs...</div>
      ) : (
        <>
          <div className="table-container">
            <table className="logs-table">
              <thead>
                <tr>
                  <th>ID</th>
                  <th>Timestamp</th>
                  <th>Admin</th>
                  <th>Action</th>
                  <th>Details</th>
                  <th>IP Address</th>
                  <th>Actions</th>
                </tr>
              </thead>
              <tbody>
                {logs.length === 0 ? (
                  <tr>
                    <td colSpan="7" className="no-data">
                      No logs found
                    </td>
                  </tr>
                ) : (
                  logs.map((log) => (
                    <tr key={log.id}>
                      <td>{log.id}</td>
                      <td>{formatDate(log.timestamp)}</td>
                      <td>
                        {log.admin ? (
                          <div className="admin-info">
                            <div className="admin-name">
                              {log.admin.username}
                            </div>
                            <div className="admin-id">ID: {log.adminId}</div>
                          </div>
                        ) : (
                          `ID: ${log.adminId}`
                        )}
                      </td>
                      <td>
                        <span className={getActionBadgeClass(log.action)}>
                          {log.action}
                        </span>
                      </td>
                      <td className="log-details">
                        {log.details ? (
                          <button
                            className="btn-view-details"
                            onClick={() => {
                              setSelectedLog(log);
                              setShowDetailsModal(true);
                            }}
                          >
                            View Details
                          </button>
                        ) : (
                          <span className="no-details">No details</span>
                        )}
                      </td>
                      <td>{log.ipAddress || "N/A"}</td>
                      <td>
                        <button
                          onClick={() => {
                            setSelectedLog(log);
                            setShowDeleteModal(true);
                          }}
                          className="btn-action btn-delete"
                          title="Delete"
                        >
                          🗑️
                        </button>
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

      {/* Details Modal */}
      {showDetailsModal && selectedLog && (
        <div
          className="modal-overlay"
          onClick={() => setShowDetailsModal(false)}
        >
          <div
            className="modal modal-large"
            onClick={(e) => e.stopPropagation()}
          >
            <div className="modal-header">
              <h2>Log Details</h2>
              <button
                className="btn-close"
                onClick={() => setShowDetailsModal(false)}
              >
                ✕
              </button>
            </div>
            <div className="log-detail-content">
              <div className="detail-row">
                <span className="detail-label">Log ID:</span>
                <span>{selectedLog.id}</span>
              </div>
              <div className="detail-row">
                <span className="detail-label">Timestamp:</span>
                <span>{formatDate(selectedLog.timestamp)}</span>
              </div>
              <div className="detail-row">
                <span className="detail-label">Admin:</span>
                <span>
                  {selectedLog.admin?.username || `ID: ${selectedLog.adminId}`}
                </span>
              </div>
              <div className="detail-row">
                <span className="detail-label">Action:</span>
                <span className={getActionBadgeClass(selectedLog.action)}>
                  {selectedLog.action}
                </span>
              </div>
              <div className="detail-row">
                <span className="detail-label">IP Address:</span>
                <span>{selectedLog.ipAddress || "N/A"}</span>
              </div>
              <div className="detail-row full-width">
                <span className="detail-label">Details:</span>
                <pre className="details-content">
                  {selectedLog.details || "No details available"}
                </pre>
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
            <h2>Delete Log</h2>
            <p>Are you sure you want to delete this activity log?</p>
            <div className="log-info">
              <strong>Log ID:</strong> {selectedLog?.id}
              <br />
              <strong>Action:</strong> {selectedLog?.action}
              <br />
              <strong>Timestamp:</strong>{" "}
              {selectedLog && formatDate(selectedLog.timestamp)}
            </div>
            <p className="warning">This action cannot be undone.</p>
            <div className="modal-actions">
              <button
                onClick={() => setShowDeleteModal(false)}
                className="btn-cancel"
              >
                Cancel
              </button>
              <button onClick={handleDeleteLog} className="btn-confirm-delete">
                Delete
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default ActivityLogsPage;
