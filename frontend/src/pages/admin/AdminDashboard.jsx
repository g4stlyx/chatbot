import { useState, useEffect } from "react";
import { useAdmin } from "../../context/AdminContext";
import {
  adminUserAPI,
  adminSessionAPI,
  adminMessageAPI,
  adminManagementAPI,
} from "../../services/adminApi";
import "./AdminDashboard.css";

const AdminDashboard = () => {
  const { admin, isSuperAdmin, isAdmin } = useAdmin();
  const [stats, setStats] = useState({
    users: { total: 0, active: 0, inactive: 0, locked: 0 },
    sessions: { total: 0, active: 0, paused: 0, archived: 0, flagged: 0 },
    messages: { total: 0, flagged: 0 },
    admins: { total: 0 },
  });
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    fetchDashboardStats();
  }, []);

  const fetchDashboardStats = async () => {
    setLoading(true);
    setError(null);

    try {
      // Fetch users
      const usersResponse = await adminUserAPI.getAllUsers({
        page: 0,
        size: 1,
      });
      const usersData = usersResponse.data;

      // Fetch sessions
      const sessionsResponse = await adminSessionAPI.getAllSessions({
        page: 0,
        size: 1,
      });
      const sessionsData = sessionsResponse.data;

      // Fetch flagged sessions
      const flaggedSessionsResponse = await adminSessionAPI.getAllSessions({
        page: 0,
        size: 1,
        isFlagged: true,
      });

      // Fetch messages
      const messagesResponse = await adminMessageAPI.getAllMessages({
        page: 0,
        size: 1,
      });
      const messagesData = messagesResponse.data;

      // Fetch flagged messages
      const flaggedMessagesResponse = await adminMessageAPI.getAllMessages({
        page: 0,
        size: 1,
        isFlagged: true,
      });

      // Fetch admins count (only for Level 0-1)
      let adminsCount = 0;
      if (isSuperAdmin || isAdmin) {
        try {
          const adminsResponse = await adminManagementAPI.getAllAdmins({
            page: 0,
            size: 1,
          });
          adminsCount =
            adminsResponse.data.totalItems ||
            adminsResponse.data.totalElements ||
            0;
        } catch (err) {
          console.log("Could not fetch admin count:", err);
        }
      }

      setStats({
        users: {
          total: usersData.totalItems || usersData.totalElements || 0,
          active: 0, // Will need separate API call or backend enhancement
          inactive: 0,
          locked: 0,
        },
        sessions: {
          total: sessionsData.totalItems || sessionsData.totalElements || 0,
          active: 0,
          paused: 0,
          archived: 0,
          flagged:
            flaggedSessionsResponse.data.totalItems ||
            flaggedSessionsResponse.data.totalElements ||
            0,
        },
        messages: {
          total: messagesData.totalItems || messagesData.totalElements || 0,
          flagged:
            flaggedMessagesResponse.data.totalItems ||
            flaggedMessagesResponse.data.totalElements ||
            0,
        },
        admins: {
          total: adminsCount,
        },
      });
    } catch (err) {
      console.error("Dashboard stats error:", err);
      setError("İstatistikler yüklenirken bir hata oluştu.");
    } finally {
      setLoading(false);
    }
  };

  const StatCard = ({ title, value, icon, color, subtitle }) => (
    <div className={`stat-card ${color}`}>
      <div className="stat-card-icon">{icon}</div>
      <div className="stat-card-content">
        <span className="stat-card-value">{loading ? "..." : value}</span>
        <span className="stat-card-title">{title}</span>
        {subtitle && <span className="stat-card-subtitle">{subtitle}</span>}
      </div>
    </div>
  );

  return (
    <div className="admin-dashboard">
      <div className="admin-page-header">
        <h1>Dashboard</h1>
        <p>Hoş geldiniz, {admin?.firstName || admin?.username}!</p>
      </div>

      {error && (
        <div className="admin-alert admin-alert-error">
          <svg
            xmlns="http://www.w3.org/2000/svg"
            viewBox="0 0 24 24"
            fill="currentColor"
            width="20"
            height="20"
          >
            <path d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm1 15h-2v-2h2v2zm0-4h-2V7h2v6z" />
          </svg>
          <span>{error}</span>
        </div>
      )}

      <div className="dashboard-stats-grid">
        <StatCard
          title="Toplam Kullanıcı"
          value={stats.users.total}
          color="blue"
          icon={
            <svg
              xmlns="http://www.w3.org/2000/svg"
              viewBox="0 0 24 24"
              fill="currentColor"
              width="28"
              height="28"
            >
              <path d="M16 11c1.66 0 2.99-1.34 2.99-3S17.66 5 16 5c-1.66 0-3 1.34-3 3s1.34 3 3 3zm-8 0c1.66 0 2.99-1.34 2.99-3S9.66 5 8 5C6.34 5 5 6.34 5 8s1.34 3 3 3zm0 2c-2.33 0-7 1.17-7 3.5V19h14v-2.5c0-2.33-4.67-3.5-7-3.5zm8 0c-.29 0-.62.02-.97.05 1.16.84 1.97 1.97 1.97 3.45V19h6v-2.5c0-2.33-4.67-3.5-7-3.5z" />
            </svg>
          }
        />

        <StatCard
          title="Toplam Oturum"
          value={stats.sessions.total}
          color="green"
          subtitle={
            stats.sessions.flagged > 0
              ? `${stats.sessions.flagged} işaretli`
              : null
          }
          icon={
            <svg
              xmlns="http://www.w3.org/2000/svg"
              viewBox="0 0 24 24"
              fill="currentColor"
              width="28"
              height="28"
            >
              <path d="M20 2H4c-1.1 0-2 .9-2 2v18l4-4h14c1.1 0 2-.9 2-2V4c0-1.1-.9-2-2-2zm0 14H6l-2 2V4h16v12z" />
            </svg>
          }
        />

        <StatCard
          title="Toplam Mesaj"
          value={stats.messages.total}
          color="purple"
          subtitle={
            stats.messages.flagged > 0
              ? `${stats.messages.flagged} işaretli`
              : null
          }
          icon={
            <svg
              xmlns="http://www.w3.org/2000/svg"
              viewBox="0 0 24 24"
              fill="currentColor"
              width="28"
              height="28"
            >
              <path d="M20 2H4c-1.1 0-1.99.9-1.99 2L2 22l4-4h14c1.1 0 2-.9 2-2V4c0-1.1-.9-2-2-2zm-2 12H6v-2h12v2zm0-3H6V9h12v2zm0-3H6V6h12v2z" />
            </svg>
          }
        />

        {(isSuperAdmin || isAdmin) && (
          <StatCard
            title="Toplam Admin"
            value={stats.admins.total}
            color="orange"
            icon={
              <svg
                xmlns="http://www.w3.org/2000/svg"
                viewBox="0 0 24 24"
                fill="currentColor"
                width="28"
                height="28"
              >
                <path d="M12 1L3 5v6c0 5.55 3.84 10.74 9 12 5.16-1.26 9-6.45 9-12V5l-9-4zm0 10.99h7c-.53 4.12-3.28 7.79-7 8.94V12H5V6.3l7-3.11v8.8z" />
              </svg>
            }
          />
        )}
      </div>

      <div className="dashboard-content-grid">
        {/* Quick Actions */}
        <div className="admin-card">
          <h3 className="admin-card-title">Hızlı İşlemler</h3>
          <div className="quick-actions-grid">
            <a href="/admin/users" className="quick-action-item">
              <div className="quick-action-icon blue">
                <svg
                  xmlns="http://www.w3.org/2000/svg"
                  viewBox="0 0 24 24"
                  fill="currentColor"
                  width="24"
                  height="24"
                >
                  <path d="M15 12c2.21 0 4-1.79 4-4s-1.79-4-4-4-4 1.79-4 4 1.79 4 4 4zm-9-2V7H4v3H1v2h3v3h2v-3h3v-2H6zm9 4c-2.67 0-8 1.34-8 4v2h16v-2c0-2.66-5.33-4-8-4z" />
                </svg>
              </div>
              <span>Yeni Kullanıcı</span>
            </a>

            <a
              href="/admin/sessions?isFlagged=true"
              className="quick-action-item"
            >
              <div className="quick-action-icon red">
                <svg
                  xmlns="http://www.w3.org/2000/svg"
                  viewBox="0 0 24 24"
                  fill="currentColor"
                  width="24"
                  height="24"
                >
                  <path d="M14.4 6L14 4H5v17h2v-7h5.6l.4 2h7V6z" />
                </svg>
              </div>
              <span>İşaretli Oturumlar</span>
            </a>

            <a
              href="/admin/messages?isFlagged=true"
              className="quick-action-item"
            >
              <div className="quick-action-icon orange">
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
              <span>İşaretli Mesajlar</span>
            </a>

            <a href="/admin/users?locked=true" className="quick-action-item">
              <div className="quick-action-icon purple">
                <svg
                  xmlns="http://www.w3.org/2000/svg"
                  viewBox="0 0 24 24"
                  fill="currentColor"
                  width="24"
                  height="24"
                >
                  <path d="M18 8h-1V6c0-2.76-2.24-5-5-5S7 3.24 7 6v2H6c-1.1 0-2 .9-2 2v10c0 1.1.9 2 2 2h12c1.1 0 2-.9 2-2V10c0-1.1-.9-2-2-2zm-6 9c-1.1 0-2-.9-2-2s.9-2 2-2 2 .9 2 2-.9 2-2 2zm3.1-9H8.9V6c0-1.71 1.39-3.1 3.1-3.1 1.71 0 3.1 1.39 3.1 3.1v2z" />
                </svg>
              </div>
              <span>Kilitli Hesaplar</span>
            </a>
          </div>
        </div>

        {/* Recent Activity Placeholder */}
        <div className="admin-card">
          <h3 className="admin-card-title">Sistem Bilgisi</h3>
          <div className="system-info-list">
            <div className="system-info-item">
              <span className="info-label">Admin Seviyesi</span>
              <span className="info-value">
                {isSuperAdmin
                  ? "Super Admin (Level 0)"
                  : isAdmin
                  ? "Admin (Level 1)"
                  : "Moderator (Level 2)"}
              </span>
            </div>
            <div className="system-info-item">
              <span className="info-label">Son Giriş</span>
              <span className="info-value">
                {admin?.lastLoginAt
                  ? new Date(admin.lastLoginAt).toLocaleString("tr-TR")
                  : "Bilinmiyor"}
              </span>
            </div>
            <div className="system-info-item">
              <span className="info-label">Email</span>
              <span className="info-value">{admin?.email}</span>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default AdminDashboard;
