import { useState, useEffect } from "react";
import { useAdmin } from "../../context/AdminContext";
import { adminDatabaseBackupAPI } from "../../services/adminApi";
import "./DatabaseBackupPage.css";

const DatabaseBackupPage = () => {
  const { canAccessSystemLogs } = useAdmin();
  const [status, setStatus] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const [success, setSuccess] = useState(null);
  const [backupInProgress, setBackupInProgress] = useState(false);

  useEffect(() => {
    fetchBackupStatus();
  }, []);

  const fetchBackupStatus = async () => {
    try {
      setLoading(true);
      setError(null);
      const response = await adminDatabaseBackupAPI.getStatus();
      setStatus(response.data);
    } catch (err) {
      setError(
        err.response?.data?.message || "Backup durumu alınırken hata oluştu"
      );
    } finally {
      setLoading(false);
    }
  };

  const handleCreateBackup = async () => {
    if (backupInProgress) return;

    if (
      !window.confirm(
        "Database backup'ı başlatmak istediğinizden emin misiniz? Bu işlem biraz zaman alabilir."
      )
    ) {
      return;
    }

    try {
      setBackupInProgress(true);
      setError(null);
      setSuccess(null);
      const response = await adminDatabaseBackupAPI.createBackup();
      setSuccess(
        response.data?.message ||
          "Backup işlemi başlatıldı. Backup tamamlandığında e-posta adresinize gönderilecektir."
      );
      // Refresh status after backup
      setTimeout(() => {
        fetchBackupStatus();
        setBackupInProgress(false);
      }, 2000);
    } catch (err) {
      setError(
        err.response?.data?.message || "Backup oluşturulurken hata oluştu"
      );
      setBackupInProgress(false);
    }
  };

  if (!canAccessSystemLogs()) {
    return (
      <div className="db-backup-page">
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
    <div className="db-backup-page">
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
              <path d="M20 6h-2.18c.11-.31.18-.65.18-1 0-1.66-1.34-3-3-3-1.05 0-1.96.54-2.5 1.35l-.5.67-.5-.68C10.96 2.54 10.05 2 9 2 7.34 2 6 3.34 6 5c0 .35.07.69.18 1H4c-1.11 0-1.99.89-1.99 2L2 19c0 1.11.89 2 2 2h16c1.11 0 2-.89 2-2V8c0-1.11-.89-2-2-2zm-5-2c.55 0 1 .45 1 1s-.45 1-1 1-1-.45-1-1 .45-1 1-1zM9 4c.55 0 1 .45 1 1s-.45 1-1 1-1-.45-1-1 .45-1 1-1zm11 15H4v-2h16v2zm0-5H4V8h5.08L7 10.83 8.62 12 11 8.76l1-1.36 1 1.36L15.38 12 17 10.83 14.92 8H20v6z" />
            </svg>
            Database Backup
          </h1>
          <p className="page-description">
            Database yedeklemesi yapın ve e-posta ile alın
          </p>
        </div>
        <button
          className="btn-refresh"
          onClick={fetchBackupStatus}
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
        </div>
      )}

      {success && (
        <div className="alert alert-success">
          <svg
            xmlns="http://www.w3.org/2000/svg"
            viewBox="0 0 24 24"
            fill="currentColor"
            width="20"
            height="20"
          >
            <path d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm-2 15l-5-5 1.41-1.41L10 14.17l7.59-7.59L19 8l-9 9z" />
          </svg>
          {success}
        </div>
      )}

      <div className="backup-content">
        <div className="backup-info-card">
          <div className="card-header">
            <h3>Backup Bilgileri</h3>
            <div className="status-badge">
              {loading ? (
                <span className="badge badge-loading">Yükleniyor...</span>
              ) : status?.lastBackupTime ? (
                <span className="badge badge-success">Aktif</span>
              ) : (
                <span className="badge badge-warning">Henüz Backup Yok</span>
              )}
            </div>
          </div>
          <div className="card-content">
            {loading ? (
              <div className="loading-state">
                <div className="spinner"></div>
                <p>Backup bilgileri yükleniyor...</p>
              </div>
            ) : status ? (
              <div className="backup-details">
                <div className="detail-row">
                  <span className="detail-label">Son Backup Zamanı:</span>
                  <span className="detail-value">
                    {status.lastBackupTime
                      ? new Date(status.lastBackupTime).toLocaleString("tr-TR")
                      : "Henüz backup yapılmadı"}
                  </span>
                </div>
                <div className="detail-row">
                  <span className="detail-label">Zamanlanmış Backup:</span>
                  <span className="detail-value">
                    {status.scheduledTime || "Tanımsız"}
                  </span>
                </div>
                <div className="detail-row">
                  <span className="detail-label">Manuel Tetikleme:</span>
                  <span className="detail-value">
                    {status.manualTriggerAvailable ? (
                      <span className="status-active">Aktif</span>
                    ) : (
                      <span className="status-inactive">Pasif</span>
                    )}
                  </span>
                </div>
              </div>
            ) : (
              <p className="no-data">Backup bilgisi yüklenemedi</p>
            )}
          </div>
        </div>

        <div className="backup-action-card">
          <div className="action-header">
            <svg
              xmlns="http://www.w3.org/2000/svg"
              viewBox="0 0 24 24"
              fill="currentColor"
              width="48"
              height="48"
            >
              <path d="M19.35 10.04C18.67 6.59 15.64 4 12 4c-1.48 0-2.85.43-4.01 1.17l1.46 1.46C10.21 6.23 11.08 6 12 6c3.04 0 5.5 2.46 5.5 5.5v.5H19c1.66 0 3 1.34 3 3 0 1.13-.64 2.11-1.56 2.62l1.45 1.45C23.16 18.16 24 16.68 24 15c0-2.64-2.05-4.78-4.65-4.96zM3 5.27l2.75 2.74C2.56 8.15 0 10.77 0 14c0 3.31 2.69 6 6 6h11.73l2 2L21 20.73 4.27 4 3 5.27zM7.73 10l8 8H6c-2.21 0-4-1.79-4-4s1.79-4 4-4h1.73z" />
            </svg>
            <h3>Yeni Backup Oluştur</h3>
            <p>
              Database'in tam yedeğini alın. Backup oluşturulduktan sonra
              e-posta adresinize gönderilecektir.
            </p>
          </div>
          <div className="action-content">
            <button
              className={`btn-backup ${backupInProgress ? "loading" : ""}`}
              onClick={handleCreateBackup}
              disabled={backupInProgress || loading}
            >
              {backupInProgress ? (
                <>
                  <div className="spinner-small"></div>
                  <span>Backup Oluşturuluyor...</span>
                </>
              ) : (
                <>
                  <svg
                    xmlns="http://www.w3.org/2000/svg"
                    viewBox="0 0 24 24"
                    fill="currentColor"
                    width="20"
                    height="20"
                  >
                    <path d="M19 12v7H5v-7H3v7c0 1.1.9 2 2 2h14c1.1 0 2-.9 2-2v-7h-2zm-6 .67l2.59-2.58L17 11.5l-5 5-5-5 1.41-1.41L11 12.67V3h2z" />
                  </svg>
                  <span>Backup Oluştur</span>
                </>
              )}
            </button>
            <div className="backup-notice">
              <svg
                xmlns="http://www.w3.org/2000/svg"
                viewBox="0 0 24 24"
                fill="currentColor"
                width="16"
                height="16"
              >
                <path d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm1 15h-2v-6h2v6zm0-8h-2V7h2v2z" />
              </svg>
              <span>
                Backup işlemi arka planda çalışır ve tamamlandığında e-posta ile
                bilgilendirilirsiniz.
              </span>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default DatabaseBackupPage;
