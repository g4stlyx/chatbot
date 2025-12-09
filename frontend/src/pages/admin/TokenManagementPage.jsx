import { useState, useEffect } from "react";
import { useAdmin } from "../../context/AdminContext";
import { adminTokenAPI } from "../../services/adminApi";
import "./TokenManagementPage.css";

const TokenManagementPage = () => {
  const { hasLevel } = useAdmin();
  const [activeTab, setActiveTab] = useState("password-reset"); // 'password-reset' or 'verification'
  const [passwordResetTokens, setPasswordResetTokens] = useState([]);
  const [verificationTokens, setVerificationTokens] = useState([]);
  const [loading, setLoading] = useState(false);
  const [currentPage, setCurrentPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [pageSize, setPageSize] = useState(20);
  const [sortBy, setSortBy] = useState("createdAt");
  const [sortDirection, setSortDirection] = useState("desc");
  const [showDeleteModal, setShowDeleteModal] = useState(false);
  const [selectedToken, setSelectedToken] = useState(null);

  // Only Level 0 (Super Admin) can access
  const canAccessTokens = hasLevel(0);

  useEffect(() => {
    if (canAccessTokens) {
      fetchTokens();
    }
  }, [
    activeTab,
    currentPage,
    pageSize,
    sortBy,
    sortDirection,
    canAccessTokens,
  ]);

  const fetchTokens = async () => {
    try {
      setLoading(true);
      const params = {
        page: currentPage,
        size: pageSize,
        sortBy,
        sortDirection,
      };

      if (activeTab === "password-reset") {
        const response = await adminTokenAPI.getAllPasswordResetTokens(params);
        const data = response.data || response;
        setPasswordResetTokens(data.tokens || data.content || []);
        setTotalPages(data.totalPages || 0);
      } else {
        const response = await adminTokenAPI.getAllVerificationTokens(params);
        const data = response.data || response;
        setVerificationTokens(data.tokens || data.content || []);
        setTotalPages(data.totalPages || 0);
      }
    } catch (error) {
      console.error("Error fetching tokens:", error);
    } finally {
      setLoading(false);
    }
  };

  const handleDeleteToken = async () => {
    if (!selectedToken) return;

    try {
      if (activeTab === "password-reset") {
        await adminTokenAPI.deletePasswordResetToken(selectedToken.id);
      } else {
        await adminTokenAPI.deleteVerificationToken(selectedToken.id);
      }
      setShowDeleteModal(false);
      setSelectedToken(null);
      fetchTokens();
    } catch (error) {
      console.error("Error deleting token:", error);
      alert("Failed to delete token");
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

  const isExpired = (expiryDate) => {
    return new Date(expiryDate) < new Date();
  };

  const truncateToken = (token) => {
    if (!token) return "N/A";
    return token.length > 20 ? `${token.substring(0, 20)}...` : token;
  };

  if (!canAccessTokens) {
    return (
      <div className="token-management">
        <div className="access-denied">
          <h1>⛔ Access Denied</h1>
          <p>You don't have permission to manage tokens.</p>
          <p>Required level: 0 (Super Admin only)</p>
        </div>
      </div>
    );
  }

  const currentTokens =
    activeTab === "password-reset" ? passwordResetTokens : verificationTokens;

  return (
    <div className="token-management">
      <div className="page-header">
        <div>
          <h1>Token Management</h1>
          <p>Manage password reset and email verification tokens</p>
        </div>
      </div>

      {/* Tabs */}
      <div className="tabs">
        <button
          className={`tab ${activeTab === "password-reset" ? "active" : ""}`}
          onClick={() => {
            setActiveTab("password-reset");
            setCurrentPage(0);
          }}
        >
          🔑 Password Reset Tokens
        </button>
        <button
          className={`tab ${activeTab === "verification" ? "active" : ""}`}
          onClick={() => {
            setActiveTab("verification");
            setCurrentPage(0);
          }}
        >
          ✉️ Verification Tokens
        </button>
      </div>

      {/* Filters */}
      <div className="filters-section">
        <div className="filters-row">
          <div className="filter-group">
            <label>Sort By:</label>
            <select value={sortBy} onChange={(e) => setSortBy(e.target.value)}>
              <option value="createdAt">Created Date</option>
              <option value="expiresAt">Expiry Date</option>
              <option value="userId">User ID</option>
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

      {/* Tokens Table */}
      {loading ? (
        <div className="loading">Loading tokens...</div>
      ) : (
        <>
          <div className="table-container">
            <table className="tokens-table">
              <thead>
                <tr>
                  <th>ID</th>
                  <th>User</th>
                  <th>Token</th>
                  <th>Created</th>
                  <th>Expires</th>
                  <th>Status</th>
                  <th>Actions</th>
                </tr>
              </thead>
              <tbody>
                {currentTokens.length === 0 ? (
                  <tr>
                    <td colSpan="7" className="no-data">
                      No{" "}
                      {activeTab === "password-reset"
                        ? "password reset"
                        : "verification"}{" "}
                      tokens found
                    </td>
                  </tr>
                ) : (
                  currentTokens.map((token) => {
                    const expired = isExpired(token.expiresAt);
                    return (
                      <tr
                        key={token.id}
                        className={expired ? "expired-row" : ""}
                      >
                        <td>{token.id}</td>
                        <td>
                          {token.user ? (
                            <div className="user-info">
                              <div className="user-name">
                                {token.user.username}
                              </div>
                              <div className="user-email">
                                {token.user.email}
                              </div>
                            </div>
                          ) : (
                            `User ID: ${token.userId}`
                          )}
                        </td>
                        <td>
                          <code className="token-code">
                            {truncateToken(token.token)}
                          </code>
                        </td>
                        <td>{formatDate(token.createdAt)}</td>
                        <td>{formatDate(token.expiresAt)}</td>
                        <td>
                          {expired ? (
                            <span className="status-badge status-expired">
                              ⏰ Expired
                            </span>
                          ) : (
                            <span className="status-badge status-active">
                              ✅ Active
                            </span>
                          )}
                        </td>
                        <td>
                          <button
                            onClick={() => {
                              setSelectedToken(token);
                              setShowDeleteModal(true);
                            }}
                            className="btn-action btn-delete"
                            title="Delete Token"
                          >
                            🗑️
                          </button>
                        </td>
                      </tr>
                    );
                  })
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
            <h2>Delete Token</h2>
            <p>
              Are you sure you want to delete this{" "}
              {activeTab === "password-reset"
                ? "password reset"
                : "verification"}{" "}
              token?
            </p>
            <div className="token-info">
              <div>
                <strong>Token ID:</strong> {selectedToken?.id}
              </div>
              <div>
                <strong>User:</strong>{" "}
                {selectedToken?.user?.username ||
                  `ID: ${selectedToken?.userId}`}
              </div>
              <div>
                <strong>Created:</strong>{" "}
                {selectedToken && formatDate(selectedToken.createdAt)}
              </div>
              <div>
                <strong>Expires:</strong>{" "}
                {selectedToken && formatDate(selectedToken.expiresAt)}
              </div>
            </div>
            <p className="warning">
              Deleting this token will prevent the user from completing their{" "}
              {activeTab === "password-reset"
                ? "password reset"
                : "email verification"}
              .
            </p>
            <div className="modal-actions">
              <button
                onClick={() => setShowDeleteModal(false)}
                className="btn-cancel"
              >
                Cancel
              </button>
              <button
                onClick={handleDeleteToken}
                className="btn-confirm-delete"
              >
                Delete Token
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default TokenManagementPage;
