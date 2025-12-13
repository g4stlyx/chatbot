import { useState, useEffect } from "react";
import { useAdmin } from "../../context/AdminContext";
import { adminPromptInjectionAPI } from "../../services/adminApi";
import "./PromptInjectionLogsPage.css";

const PromptInjectionLogsPage = () => {
  const { canAccessSystemLogs, isSuperAdmin } = useAdmin();
  const [logs, setLogs] = useState([]);
  const [statistics, setStatistics] = useState(null);
  const [loading, setLoading] = useState(false);
  const [statsLoading, setStatsLoading] = useState(false);
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
    severity: "",
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
      const response = await adminPromptInjectionAPI.getAllLogs({
        page: currentPage,
        size: pageSize,
        sortBy,
        sortDirection,
        ...(filters.userId && { userId: filters.userId }),
        ...(filters.severity && { severity: filters.severity }),
      });

      console.log("Prompt Injection Logs Response:", response.data);
      console.log("Response type:", typeof response.data);
      console.log("Is Array?:", Array.isArray(response.data));

      // Backend response'u kontrol et ve doğru field'ı kullan
      let logsData = [];
      if (Array.isArray(response.data)) {
        // Response direkt array ise
        logsData = response.data;
      } else if (response.data.logs && Array.isArray(response.data.logs)) {
        // Backend 'logs' field'ında dönüyor
        logsData = response.data.logs;
      } else if (
        response.data.content &&
        Array.isArray(response.data.content)
      ) {
        // Spring Boot Page response pattern
        logsData = response.data.content;
      } else if (response.data.data && Array.isArray(response.data.data)) {
        // Custom wrapper pattern
        logsData = response.data.data;
      }

      console.log("Extracted logs:", logsData);
      setLogs(logsData);
      setTotalPages(
        response.data.totalPages ||
          Math.ceil(
            (response.data.totalElements || logsData.length) / pageSize
          ) ||
          1
      );
      setTotalItems(
        response.data.totalElements ||
          response.data.totalItems ||
          logsData.length ||
          0
      );
    } catch (err) {
      console.error("Logs fetch error:", err);
      setError(err.response?.data?.message || "Loglar yüklenirken hata oluştu");
    } finally {
      setLoading(false);
    }
  };

  const fetchStatistics = async () => {
    try {
      setStatsLoading(true);
      const response = await adminPromptInjectionAPI.getStatistics();
      setStatistics(response.data);
    } catch (err) {
      console.error("İstatistikler yüklenemedi:", err);
    } finally {
      setStatsLoading(false);
    }
  };

  const handleViewDetails = async (logId) => {
    try {
      const response = await adminPromptInjectionAPI.getLogById(logId);
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
      await adminPromptInjectionAPI.deleteLog(logId);
      fetchLogs();
      fetchStatistics();
      setShowDetailModal(false);
    } catch (err) {
      setError(err.response?.data?.message || "Log silinirken hata oluştu");
    }
  };

  const getSeverityBadge = (severity) => {
    const badges = {
      LOW: { class: "badge-low", text: "Düşük" },
      MEDIUM: { class: "badge-medium", text: "Orta" },
      HIGH: { class: "badge-high", text: "Yüksek" },
      CRITICAL: { class: "badge-critical", text: "Kritik" },
    };
    return badges[severity] || { class: "", text: severity };
  };

  const handleFilterChange = (key, value) => {
    setFilters((prev) => ({ ...prev, [key]: value }));
    setCurrentPage(0);
  };

  const clearFilters = () => {
    setFilters({ userId: "", severity: "" });
    setCurrentPage(0);
  };

  if (!canAccessSystemLogs()) {
    return (
      <div className="prompt-injection-logs-page">
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
    <div className="prompt-injection-logs-page">
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
              <path d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm-1 17.93c-3.95-.49-7-3.85-7-7.93 0-.62.08-1.21.21-1.79L9 15v1c0 1.1.9 2 2 2v1.93zm6.9-2.54c-.26-.81-1-1.39-1.9-1.39h-1v-3c0-.55-.45-1-1-1H8v-2h2c.55 0 1-.45 1-1V7h2c1.1 0 2-.9 2-2v-.41c2.93 1.19 5 4.06 5 7.41 0 2.08-.8 3.97-2.1 5.39z" />
            </svg>
            Prompt Injection Logları
          </h1>
          <p className="page-description">
            Prompt injection denemeleri ve güvenlik logları
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
            <div className="stat-icon critical">
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
              <span className="stat-label">Toplam Deneme</span>
              <span className="stat-value">
                {statistics.totalAttempts || 0}
              </span>
            </div>
          </div>

          <div className="stat-card">
            <div className="stat-icon high">
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
              <span className="stat-label">Kritik</span>
              <span className="stat-value">
                {statistics.severityBreakdown?.CRITICAL || 0}
              </span>
            </div>
          </div>

          <div className="stat-card">
            <div className="stat-icon medium">
              <svg
                xmlns="http://www.w3.org/2000/svg"
                viewBox="0 0 24 24"
                fill="currentColor"
                width="24"
                height="24"
              >
                <path d="M11 15h2v2h-2zm0-8h2v6h-2zm.99-5C6.47 2 2 6.48 2 12s4.47 10 9.99 10C17.52 22 22 17.52 22 12S17.52 2 11.99 2zM12 20c-4.42 0-8-3.58-8-8s3.58-8 8-8 8 3.58 8 8-3.58 8-8 8z" />
              </svg>
            </div>
            <div className="stat-content">
              <span className="stat-label">Yüksek</span>
              <span className="stat-value">
                {statistics.severityBreakdown?.HIGH || 0}
              </span>
            </div>
          </div>

          <div className="stat-card">
            <div className="stat-icon low">
              <svg
                xmlns="http://www.w3.org/2000/svg"
                viewBox="0 0 24 24"
                fill="currentColor"
                width="24"
                height="24"
              >
                <path d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm-2 15l-5-5 1.41-1.41L10 14.17l7.59-7.59L19 8l-9 9z" />
              </svg>
            </div>
            <div className="stat-content">
              <span className="stat-label">Orta & Düşük</span>
              <span className="stat-value">
                {(statistics.severityBreakdown?.MEDIUM || 0) +
                  (statistics.severityBreakdown?.LOW || 0)}
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
            <label>Şiddet Seviyesi</label>
            <select
              value={filters.severity}
              onChange={(e) => handleFilterChange("severity", e.target.value)}
            >
              <option value="">Tümü</option>
              <option value="LOW">Düşük</option>
              <option value="MEDIUM">Orta</option>
              <option value="HIGH">Yüksek</option>
              <option value="CRITICAL">Kritik</option>
            </select>
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
            <p>Henüz prompt injection denemesi kaydedilmemiş.</p>
          </div>
        ) : (
          <>
            <div className="table-container">
              <table className="logs-table">
                <thead>
                  <tr>
                    <th>ID</th>
                    <th>Kullanıcı ID</th>
                    <th>Şiddet</th>
                    <th>Pattern</th>
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
                          className={`severity-badge ${
                            getSeverityBadge(log.severity).class
                          }`}
                        >
                          {getSeverityBadge(log.severity).text}
                        </span>
                      </td>
                      <td className="pattern-cell">
                        {log.detectedPattern || "N/A"}
                      </td>
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
                    <span className="detail-label">Şiddet Seviyesi:</span>
                    <span
                      className={`severity-badge ${
                        getSeverityBadge(selectedLog.severity).class
                      }`}
                    >
                      {getSeverityBadge(selectedLog.severity).text}
                    </span>
                  </div>
                  <div className="detail-item">
                    <span className="detail-label">Tarih:</span>
                    <span className="detail-value">
                      {new Date(selectedLog.createdAt).toLocaleString("tr-TR")}
                    </span>
                  </div>
                </div>
              </div>

              <div className="detail-section">
                <h3>Tespit Edilen Pattern</h3>
                <div className="code-block">
                  {selectedLog.detectedPattern || "N/A"}
                </div>
              </div>

              <div className="detail-section">
                <h3>Orijinal Mesaj</h3>
                <div className="code-block">
                  {selectedLog.originalMessage || "N/A"}
                </div>
              </div>

              {selectedLog.ipAddress && (
                <div className="detail-section">
                  <h3>Ek Bilgiler</h3>
                  <div className="detail-grid">
                    <div className="detail-item">
                      <span className="detail-label">IP Adresi:</span>
                      <span className="detail-value">
                        {selectedLog.ipAddress}
                      </span>
                    </div>
                  </div>
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

export default PromptInjectionLogsPage;
