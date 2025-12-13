import { useState, useEffect } from "react";
import { useAdmin } from "../../context/AdminContext";
import { adminAuthErrorLogsAPI } from "../../services/adminApi";
import "./AuthErrorLogsPage.css";

const AuthErrorLogsPage = () => {
  const { canAccessSystemLogs, isSuperAdmin } = useAdmin();
  const [logs, setLogs] = useState([]);
  const [statistics, setStatistics] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const [selectedLog, setSelectedLog] = useState(null);
  const [showDetailModal, setShowDetailModal] = useState(false);

  // Pagination and filters
  const [currentPage, setCurrentPage] = useState(0);
  const [pageSize, setPageSize] = useState(20);
  const [totalPages, setTotalPages] = useState(0);
  const [totalItems, setTotalItems] = useState(0);
  const [sortBy, setSortBy] = useState("createdAt");
  const [sortDirection, setSortDirection] = useState("desc");
  const [filters, setFilters] = useState({
    userId: "",
    errorType: "",
    ipAddress: "",
  });

  useEffect(() => {
    if (canAccessSystemLogs()) {
      fetchLogs();
      fetchStatistics();
    }
  }, [currentPage, pageSize, sortBy, sortDirection, filters]);

  const fetchLogs = async () => {
    try {
      setLoading(true);
      setError(null);
      const response = await adminAuthErrorLogsAPI.getAllLogs({
        page: currentPage,
        size: pageSize,
        sortBy,
        sortDirection,
        ...(filters.userId && { userId: filters.userId }),
        ...(filters.errorType && { errorType: filters.errorType }),
        ...(filters.ipAddress && { ipAddress: filters.ipAddress }),
      });

      const logsData =
        response.data.logs || response.data.content || response.data || [];
      setLogs(Array.isArray(logsData) ? logsData : []);
      setTotalPages(response.data.totalPages || 0);
      setTotalItems(
        response.data.totalElements || response.data.totalItems || 0
      );
    } catch (err) {
      setError(err.response?.data?.message || "Loglar yüklenirken hata oluştu");
    } finally {
      setLoading(false);
    }
  };

  const fetchStatistics = async () => {
    try {
      const response = await adminAuthErrorLogsAPI.getStatistics();
      setStatistics(response.data);
    } catch (err) {
      console.error("İstatistikler yüklenemedi:", err);
    }
  };

  const handleViewDetails = async (logId) => {
    try {
      const response = await adminAuthErrorLogsAPI.getLogById(logId);
      setSelectedLog(response.data);
      setShowDetailModal(true);
    } catch (err) {
      setError(err.response?.data?.message || "Log detayı yüklenemedi");
    }
  };

  const handleDeleteLog = async (logId) => {
    if (!isSuperAdmin) {
      setError("Sadece Super Admin logları silebilir");
      return;
    }

    if (!window.confirm("Bu log kaydını silmek istediğinizden emin misiniz?")) {
      return;
    }

    try {
      await adminAuthErrorLogsAPI.deleteLog(logId);
      fetchLogs();
      fetchStatistics();
      setShowDetailModal(false);
    } catch (err) {
      setError(err.response?.data?.message || "Log silinirken hata oluştu");
    }
  };

  const getErrorTypeBadge = (errorType) => {
    const badges = {
      UNAUTHORIZED_401: { class: "badge-warning", text: "401 Unauthorized" },
      FORBIDDEN_403: { class: "badge-danger", text: "403 Forbidden" },
      NOT_FOUND_404: { class: "badge-info", text: "404 Not Found" },
      INVALID_TOKEN: { class: "badge-error", text: "Invalid Token" },
      ACCESS_DENIED: { class: "badge-critical", text: "Access Denied" },
    };
    return badges[errorType] || { class: "", text: errorType };
  };

  const handleFilterChange = (key, value) => {
    setFilters((prev) => ({ ...prev, [key]: value }));
    setCurrentPage(0);
  };

  const clearFilters = () => {
    setFilters({ userId: "", errorType: "", ipAddress: "" });
    setCurrentPage(0);
  };

  if (!canAccessSystemLogs()) {
    return (
      <div className="auth-error-logs-page">
        <div className="access-denied">
          <svg
            xmlns="http://www.w3.org/2000/svg"
            viewBox="0 0 24 24"
            fill="currentColor"
            width="48"
            height="48"
          >
            <path d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm1 15h-2v-2h2v2zm0-4h-2V7h2v6z" />
          </svg>
          <h3>Erişim Reddedildi</h3>
          <p>Bu sayfaya erişim izniniz yok.</p>
        </div>
      </div>
    );
  }

  return (
    <div className="auth-error-logs-page">
      <div className="page-header">
        <div className="header-content">
          <h1>
            <svg
              xmlns="http://www.w3.org/2000/svg"
              viewBox="0 0 24 24"
              fill="currentColor"
              width="32"
              height="32"
            >
              <path d="M18 8h-1V6c0-2.76-2.24-5-5-5S7 3.24 7 6v2H6c-1.1 0-2 .9-2 2v10c0 1.1.9 2 2 2h12c1.1 0 2-.9 2-2V10c0-1.1-.9-2-2-2zM8.9 6c0-1.71 1.39-3.1 3.1-3.1s3.1 1.39 3.1 3.1v2H8.9V6zM12 17c-1.1 0-2-.9-2-2s.9-2 2-2 2 .9 2 2-.9 2-2 2z" />
            </svg>
            Authentication Error Logları
          </h1>
          <p className="page-description">
            Kimlik doğrulama hataları ve güvenlik logları
          </p>
        </div>
        <button
          className="btn-refresh"
          onClick={() => {
            fetchLogs();
            fetchStatistics();
          }}
          disabled={loading}
          title="Yenile"
        >
          <svg
            xmlns="http://www.w3.org/2000/svg"
            viewBox="0 0 24 24"
            fill="currentColor"
            width="20"
            height="20"
          >
            <path d="M17.65 6.35C16.2 4.9 14.21 4 12 4c-4.42 0-7.99 3.58-7.99 8s3.57 8 7.99 8c3.73 0 6.84-2.55 7.73-6h-2.08c-.82 2.33-3.04 4-5.65 4-3.31 0-6-2.69-6-6s2.69-6 6-6c1.66 0 3.14.69 4.22 1.78L13 11h7V4l-2.35 2.35z" />
          </svg>
        </button>
      </div>

      {error && (
        <div className="alert alert-error">
          <svg
            xmlns="http://www.w3.org/2000/svg"
            viewBox="0 0 24 24"
            fill="currentColor"
            width="20"
            height="20"
          >
            <path d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm1 15h-2v-2h2v2zm0-4h-2V7h2v6z" />
          </svg>
          {error}
          <button onClick={() => setError(null)} className="close-btn">
            ×
          </button>
        </div>
      )}

      {/* Statistics Cards */}
      {statistics && (
        <div className="statistics-grid">
          <div className="stat-card">
            <div className="stat-icon total">
              <svg
                xmlns="http://www.w3.org/2000/svg"
                viewBox="0 0 24 24"
                fill="currentColor"
                width="24"
                height="24"
              >
                <path d="M19 3h-4.18C14.4 1.84 13.3 1 12 1c-1.3 0-2.4.84-2.82 2H5c-1.1 0-2 .9-2 2v14c0 1.1.9 2 2 2h14c1.1 0 2-.9 2-2V5c0-1.1-.9-2-2-2zm-7 0c.55 0 1 .45 1 1s-.45 1-1 1-1-.45-1-1 .45-1 1-1zm2 14H7v-2h7v2zm3-4H7v-2h10v2zm0-4H7V7h10v2z" />
              </svg>
            </div>
            <div className="stat-content">
              <span className="stat-label">Toplam Hata</span>
              <span className="stat-value">{statistics.totalErrors || 0}</span>
            </div>
          </div>

          <div className="stat-card">
            <div className="stat-icon critical">
              <svg
                xmlns="http://www.w3.org/2000/svg"
                viewBox="0 0 24 24"
                fill="currentColor"
                width="24"
                height="24"
              >
                <path d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm1 15h-2v-2h2v2zm0-4h-2V7h2v6z" />
              </svg>
            </div>
            <div className="stat-content">
              <span className="stat-label">Şüpheli IP'ler</span>
              <span className="stat-value">
                {statistics.suspiciousIps?.length || 0}
              </span>
            </div>
          </div>

          <div className="stat-card">
            <div className="stat-icon warning">
              <svg
                xmlns="http://www.w3.org/2000/svg"
                viewBox="0 0 24 24"
                fill="currentColor"
                width="24"
                height="24"
              >
                <path d="M1 21h22L12 2 1 21zm12-3h-2v-2h2v2zm0-4h-2v-4h2v4z" />
              </svg>
            </div>
            <div className="stat-content">
              <span className="stat-label">401 Unauthorized</span>
              <span className="stat-value">
                {statistics.errorTypeBreakdown?.UNAUTHORIZED_401 || 0}
              </span>
            </div>
          </div>

          <div className="stat-card">
            <div className="stat-icon danger">
              <svg
                xmlns="http://www.w3.org/2000/svg"
                viewBox="0 0 24 24"
                fill="currentColor"
                width="24"
                height="24"
              >
                <path d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm0 11c-.55 0-1-.45-1-1V8c0-.55.45-1 1-1s1 .45 1 1v4c0 .55-.45 1-1 1zm1 4h-2v-2h2v2z" />
              </svg>
            </div>
            <div className="stat-content">
              <span className="stat-label">403 Forbidden</span>
              <span className="stat-value">
                {statistics.errorTypeBreakdown?.FORBIDDEN_403 || 0}
              </span>
            </div>
          </div>
        </div>
      )}

      {/* Filters */}
      <div className="filters-section">
        <div className="filters-row">
          <div className="filter-group">
            <label>Kullanıcı ID</label>
            <input
              type="text"
              placeholder="User ID"
              value={filters.userId}
              onChange={(e) => handleFilterChange("userId", e.target.value)}
            />
          </div>
          <div className="filter-group">
            <label>Hata Tipi</label>
            <select
              value={filters.errorType}
              onChange={(e) => handleFilterChange("errorType", e.target.value)}
            >
              <option value="">Tümü</option>
              <option value="UNAUTHORIZED_401">401 Unauthorized</option>
              <option value="FORBIDDEN_403">403 Forbidden</option>
              <option value="NOT_FOUND_404">404 Not Found</option>
              <option value="INVALID_TOKEN">Invalid Token</option>
              <option value="ACCESS_DENIED">Access Denied</option>
            </select>
          </div>
          <div className="filter-group">
            <label>IP Adresi</label>
            <input
              type="text"
              placeholder="IP Address"
              value={filters.ipAddress}
              onChange={(e) => handleFilterChange("ipAddress", e.target.value)}
            />
          </div>
          <button onClick={clearFilters} className="btn-clear-filters">
            <svg
              xmlns="http://www.w3.org/2000/svg"
              viewBox="0 0 24 24"
              fill="currentColor"
              width="18"
              height="18"
            >
              <path d="M19 6.41L17.59 5 12 10.59 6.41 5 5 6.41 10.59 12 5 17.59 6.41 19 12 13.41 17.59 19 19 17.59 13.41 12z" />
            </svg>
            Filtreleri Temizle
          </button>
        </div>
      </div>

      {/* Logs Table */}
      <div className="logs-container">
        {loading ? (
          <div className="loading-state">
            <div className="spinner"></div>
            <p>Loglar yükleniyor...</p>
          </div>
        ) : logs.length === 0 ? (
          <div className="empty-state">
            <svg
              xmlns="http://www.w3.org/2000/svg"
              viewBox="0 0 24 24"
              fill="currentColor"
              width="48"
              height="48"
            >
              <path d="M19 3H5c-1.1 0-2 .9-2 2v14c0 1.1.9 2 2 2h14c1.1 0 2-.9 2-2V5c0-1.1-.9-2-2-2zm-5 14H7v-2h7v2zm3-4H7v-2h10v2zm0-4H7V7h10v2z" />
            </svg>
            <h3>Log Bulunamadı</h3>
            <p>Henüz authentication hatası kaydedilmemiş.</p>
          </div>
        ) : (
          <>
            <div className="table-container">
              <table className="logs-table">
                <thead>
                  <tr>
                    <th>ID</th>
                    <th>Kullanıcı ID</th>
                    <th>Hata Tipi</th>
                    <th>IP Adresi</th>
                    <th>Endpoint</th>
                    <th>Tarih</th>
                    <th>İşlemler</th>
                  </tr>
                </thead>
                <tbody>
                  {logs.map((log) => (
                    <tr key={log.id}>
                      <td>#{log.id}</td>
                      <td>{log.userId || "N/A"}</td>
                      <td>
                        <span
                          className={`error-type-badge ${
                            getErrorTypeBadge(log.errorType).class
                          }`}
                        >
                          {getErrorTypeBadge(log.errorType).text}
                        </span>
                      </td>
                      <td className="ip-cell">{log.ipAddress || "N/A"}</td>
                      <td className="endpoint-cell">{log.endpoint || "N/A"}</td>
                      <td>{new Date(log.createdAt).toLocaleString("tr-TR")}</td>
                      <td>
                        <div className="action-buttons">
                          <button
                            onClick={() => handleViewDetails(log.id)}
                            className="btn-view"
                            title="Detayları Gör"
                          >
                            <svg
                              xmlns="http://www.w3.org/2000/svg"
                              viewBox="0 0 24 24"
                              fill="currentColor"
                              width="18"
                              height="18"
                            >
                              <path d="M12 4.5C7 4.5 2.73 7.61 1 12c1.73 4.39 6 7.5 11 7.5s9.27-3.11 11-7.5c-1.73-4.39-6-7.5-11-7.5zM12 17c-2.76 0-5-2.24-5-5s2.24-5 5-5 5 2.24 5 5-2.24 5-5 5zm0-8c-1.66 0-3 1.34-3 3s1.34 3 3 3 3-1.34 3-3-1.34-3-3-3z" />
                            </svg>
                          </button>
                          {isSuperAdmin && (
                            <button
                              onClick={() => handleDeleteLog(log.id)}
                              className="btn-delete"
                              title="Sil"
                            >
                              <svg
                                xmlns="http://www.w3.org/2000/svg"
                                viewBox="0 0 24 24"
                                fill="currentColor"
                                width="18"
                                height="18"
                              >
                                <path d="M6 19c0 1.1.9 2 2 2h8c1.1 0 2-.9 2-2V7H6v12zM19 4h-3.5l-1-1h-5l-1 1H5v2h14V4z" />
                              </svg>
                            </button>
                          )}
                        </div>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>

            {/* Pagination */}
            <div className="pagination">
              <div className="pagination-info">
                Toplam {totalItems} kayıt içinden {currentPage * pageSize + 1}-
                {Math.min((currentPage + 1) * pageSize, totalItems)} arası
                gösteriliyor
              </div>
              <div className="pagination-controls">
                <button
                  onClick={() => setCurrentPage(0)}
                  disabled={currentPage === 0}
                  className="btn-pagination"
                >
                  ««
                </button>
                <button
                  onClick={() =>
                    setCurrentPage((prev) => Math.max(0, prev - 1))
                  }
                  disabled={currentPage === 0}
                  className="btn-pagination"
                >
                  «
                </button>
                <span className="page-indicator">
                  Sayfa {currentPage + 1} / {totalPages}
                </span>
                <button
                  onClick={() =>
                    setCurrentPage((prev) => Math.min(totalPages - 1, prev + 1))
                  }
                  disabled={currentPage >= totalPages - 1}
                  className="btn-pagination"
                >
                  »
                </button>
                <button
                  onClick={() => setCurrentPage(totalPages - 1)}
                  disabled={currentPage >= totalPages - 1}
                  className="btn-pagination"
                >
                  »»
                </button>
              </div>
            </div>
          </>
        )}
      </div>

      {/* Detail Modal */}
      {showDetailModal && selectedLog && (
        <div
          className="modal-overlay"
          onClick={() => setShowDetailModal(false)}
        >
          <div className="modal-content" onClick={(e) => e.stopPropagation()}>
            <div className="modal-header">
              <h2>Log Detayları</h2>
              <button
                onClick={() => setShowDetailModal(false)}
                className="modal-close"
              >
                ×
              </button>
            </div>
            <div className="modal-body">
              <div className="detail-section">
                <h3>Genel Bilgiler</h3>
                <div className="detail-grid">
                  <div className="detail-item">
                    <span className="detail-label">Log ID:</span>
                    <span className="detail-value">#{selectedLog.id}</span>
                  </div>
                  <div className="detail-item">
                    <span className="detail-label">Kullanıcı ID:</span>
                    <span className="detail-value">
                      {selectedLog.userId || "N/A"}
                    </span>
                  </div>
                  <div className="detail-item">
                    <span className="detail-label">Hata Tipi:</span>
                    <span
                      className={`error-type-badge ${
                        getErrorTypeBadge(selectedLog.errorType).class
                      }`}
                    >
                      {getErrorTypeBadge(selectedLog.errorType).text}
                    </span>
                  </div>
                  <div className="detail-item">
                    <span className="detail-label">Tarih:</span>
                    <span className="detail-value">
                      {new Date(selectedLog.createdAt).toLocaleString("tr-TR")}
                    </span>
                  </div>
                  <div className="detail-item">
                    <span className="detail-label">IP Adresi:</span>
                    <span className="detail-value">
                      {selectedLog.ipAddress || "N/A"}
                    </span>
                  </div>
                  <div className="detail-item">
                    <span className="detail-label">Endpoint:</span>
                    <span className="detail-value">
                      {selectedLog.endpoint || "N/A"}
                    </span>
                  </div>
                </div>
              </div>

              {selectedLog.errorMessage && (
                <div className="detail-section">
                  <h3>Hata Mesajı</h3>
                  <div className="code-block">{selectedLog.errorMessage}</div>
                </div>
              )}

              {selectedLog.userAgent && (
                <div className="detail-section">
                  <h3>User Agent</h3>
                  <div className="code-block">{selectedLog.userAgent}</div>
                </div>
              )}
            </div>
            <div className="modal-footer">
              {isSuperAdmin && (
                <button
                  onClick={() => handleDeleteLog(selectedLog.id)}
                  className="btn-delete-modal"
                >
                  <svg
                    xmlns="http://www.w3.org/2000/svg"
                    viewBox="0 0 24 24"
                    fill="currentColor"
                    width="18"
                    height="18"
                  >
                    <path d="M6 19c0 1.1.9 2 2 2h8c1.1 0 2-.9 2-2V7H6v12zM19 4h-3.5l-1-1h-5l-1 1H5v2h14V4z" />
                  </svg>
                  Sil
                </button>
              )}
              <button
                onClick={() => setShowDetailModal(false)}
                className="btn-close-modal"
              >
                Kapat
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default AuthErrorLogsPage;
