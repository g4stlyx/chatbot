import { NavLink, useNavigate } from "react-router-dom";
import { useAdmin } from "../../context/AdminContext";
import { useState } from "react";
import "./AdminSidebar.css";

const AdminSidebar = () => {
  const {
    admin,
    logout,
    canManageAdmins,
    canAccessSystemLogs,
    isSuperAdmin,
    isAdmin,
    isModerator,
  } = useAdmin();
  const navigate = useNavigate();
  const [collapsed, setCollapsed] = useState(false);

  const handleLogout = async () => {
    await logout();
    navigate("/admin/login");
  };

  const getLevelBadge = () => {
    if (isSuperAdmin) return { text: "Super Admin", class: "level-0" };
    if (isAdmin) return { text: "Admin", class: "level-1" };
    if (isModerator) return { text: "Moderator", class: "level-2" };
    return { text: "Unknown", class: "" };
  };

  const levelBadge = getLevelBadge();

  return (
    <aside className={`admin-sidebar ${collapsed ? "collapsed" : ""}`}>
      <div className="admin-sidebar-header">
        <div className="admin-sidebar-logo">
          <svg
            xmlns="http://www.w3.org/2000/svg"
            viewBox="0 0 24 24"
            fill="currentColor"
            width="32"
            height="32"
          >
            <path d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm-1 17.93c-3.95-.49-7-3.85-7-7.93 0-.62.08-1.21.21-1.79L9 15v1c0 1.1.9 2 2 2v1.93zm6.9-2.54c-.26-.81-1-1.39-1.9-1.39h-1v-3c0-.55-.45-1-1-1H8v-2h2c.55 0 1-.45 1-1V7h2c1.1 0 2-.9 2-2v-.41c2.93 1.19 5 4.06 5 7.41 0 2.08-.8 3.97-2.1 5.39z" />
          </svg>
          {!collapsed && <span>Admin Panel</span>}
        </div>
        <button
          className="sidebar-toggle"
          onClick={() => setCollapsed(!collapsed)}
          title={collapsed ? "Genişlet" : "Daralt"}
        >
          <svg
            xmlns="http://www.w3.org/2000/svg"
            viewBox="0 0 24 24"
            fill="currentColor"
            width="20"
            height="20"
          >
            {collapsed ? (
              <path d="M10 6L8.59 7.41 13.17 12l-4.58 4.59L10 18l6-6z" />
            ) : (
              <path d="M15.41 7.41L14 6l-6 6 6 6 1.41-1.41L10.83 12z" />
            )}
          </svg>
        </button>
      </div>

      <nav className="admin-sidebar-nav">
        <ul>
          <li>
            <NavLink
              to="/admin/dashboard"
              className={({ isActive }) => (isActive ? "active" : "")}
            >
              <svg
                xmlns="http://www.w3.org/2000/svg"
                viewBox="0 0 24 24"
                fill="currentColor"
                width="20"
                height="20"
              >
                <path d="M3 13h8V3H3v10zm0 8h8v-6H3v6zm10 0h8V11h-8v10zm0-18v6h8V3h-8z" />
              </svg>
              {!collapsed && <span>Dashboard</span>}
            </NavLink>
          </li>

          <li>
            <NavLink
              to="/admin/users"
              className={({ isActive }) => (isActive ? "active" : "")}
            >
              <svg
                xmlns="http://www.w3.org/2000/svg"
                viewBox="0 0 24 24"
                fill="currentColor"
                width="20"
                height="20"
              >
                <path d="M16 11c1.66 0 2.99-1.34 2.99-3S17.66 5 16 5c-1.66 0-3 1.34-3 3s1.34 3 3 3zm-8 0c1.66 0 2.99-1.34 2.99-3S9.66 5 8 5C6.34 5 5 6.34 5 8s1.34 3 3 3zm0 2c-2.33 0-7 1.17-7 3.5V19h14v-2.5c0-2.33-4.67-3.5-7-3.5zm8 0c-.29 0-.62.02-.97.05 1.16.84 1.97 1.97 1.97 3.45V19h6v-2.5c0-2.33-4.67-3.5-7-3.5z" />
              </svg>
              {!collapsed && <span>Kullanıcılar</span>}
            </NavLink>
          </li>

          <li>
            <NavLink
              to="/admin/sessions"
              className={({ isActive }) => (isActive ? "active" : "")}
            >
              <svg
                xmlns="http://www.w3.org/2000/svg"
                viewBox="0 0 24 24"
                fill="currentColor"
                width="20"
                height="20"
              >
                <path d="M20 2H4c-1.1 0-2 .9-2 2v18l4-4h14c1.1 0 2-.9 2-2V4c0-1.1-.9-2-2-2zm0 14H6l-2 2V4h16v12z" />
              </svg>
              {!collapsed && <span>Oturumlar</span>}
            </NavLink>
          </li>

          <li>
            <NavLink
              to="/admin/messages"
              className={({ isActive }) => (isActive ? "active" : "")}
            >
              <svg
                xmlns="http://www.w3.org/2000/svg"
                viewBox="0 0 24 24"
                fill="currentColor"
                width="20"
                height="20"
              >
                <path d="M20 2H4c-1.1 0-1.99.9-1.99 2L2 22l4-4h14c1.1 0 2-.9 2-2V4c0-1.1-.9-2-2-2zm-2 12H6v-2h12v2zm0-3H6V9h12v2zm0-3H6V6h12v2z" />
              </svg>
              {!collapsed && <span>Mesajlar</span>}
            </NavLink>
          </li>

          {canManageAdmins() && (
            <li>
              <NavLink
                to="/admin/admins"
                className={({ isActive }) => (isActive ? "active" : "")}
              >
                <svg
                  xmlns="http://www.w3.org/2000/svg"
                  viewBox="0 0 24 24"
                  fill="currentColor"
                  width="20"
                  height="20"
                >
                  <path d="M12 1L3 5v6c0 5.55 3.84 10.74 9 12 5.16-1.26 9-6.45 9-12V5l-9-4zm0 10.99h7c-.53 4.12-3.28 7.79-7 8.94V12H5V6.3l7-3.11v8.8z" />
                </svg>
                {!collapsed && <span>Adminler</span>}
              </NavLink>
            </li>
          )}

          {canAccessSystemLogs() && (
            <>
              <li className="nav-divider">
                {!collapsed && <span>Sistem</span>}
              </li>
              <li>
                <NavLink
                  to="/admin/activity-logs"
                  className={({ isActive }) => (isActive ? "active" : "")}
                >
                  <svg
                    xmlns="http://www.w3.org/2000/svg"
                    viewBox="0 0 24 24"
                    fill="currentColor"
                    width="20"
                    height="20"
                  >
                    <path d="M19 3h-4.18C14.4 1.84 13.3 1 12 1c-1.3 0-2.4.84-2.82 2H5c-1.1 0-2 .9-2 2v14c0 1.1.9 2 2 2h14c1.1 0 2-.9 2-2V5c0-1.1-.9-2-2-2zm-7 0c.55 0 1 .45 1 1s-.45 1-1 1-1-.45-1-1 .45-1 1-1zm2 14H7v-2h7v2zm3-4H7v-2h10v2zm0-4H7V7h10v2z" />
                  </svg>
                  {!collapsed && <span>Aktivite Logları</span>}
                </NavLink>
              </li>
              <li>
                <NavLink
                  to="/admin/tokens"
                  className={({ isActive }) => (isActive ? "active" : "")}
                >
                  <svg
                    xmlns="http://www.w3.org/2000/svg"
                    viewBox="0 0 24 24"
                    fill="currentColor"
                    width="20"
                    height="20"
                  >
                    <path d="M18 8h-1V6c0-2.76-2.24-5-5-5S7 3.24 7 6v2H6c-1.1 0-2 .9-2 2v10c0 1.1.9 2 2 2h12c1.1 0 2-.9 2-2V10c0-1.1-.9-2-2-2zm-6 9c-1.1 0-2-.9-2-2s.9-2 2-2 2 .9 2 2-.9 2-2 2zm3.1-9H8.9V6c0-1.71 1.39-3.1 3.1-3.1 1.71 0 3.1 1.39 3.1 3.1v2z" />
                  </svg>
                  {!collapsed && <span>Token Yönetimi</span>}
                </NavLink>
              </li>
            </>
          )}
        </ul>
      </nav>

      <div className="admin-sidebar-footer">
        <div className="admin-user-info">
          <div className="admin-avatar">
            {admin?.profilePicture ? (
              <img src={admin.profilePicture} alt={admin.username} />
            ) : (
              <span>{admin?.username?.charAt(0).toUpperCase()}</span>
            )}
          </div>
          {!collapsed && (
            <div className="admin-user-details">
              <span className="admin-username">{admin?.username}</span>
              <span className={`admin-level-badge ${levelBadge.class}`}>
                {levelBadge.text}
              </span>
            </div>
          )}
        </div>

        <div className="admin-sidebar-actions">
          <NavLink
            to="/admin/profile"
            className="admin-action-btn"
            title="Profil"
          >
            <svg
              xmlns="http://www.w3.org/2000/svg"
              viewBox="0 0 24 24"
              fill="currentColor"
              width="20"
              height="20"
            >
              <path d="M12 12c2.21 0 4-1.79 4-4s-1.79-4-4-4-4 1.79-4 4 1.79 4 4 4zm0 2c-2.67 0-8 1.34-8 4v2h16v-2c0-2.66-5.33-4-8-4z" />
            </svg>
          </NavLink>
          <button
            onClick={handleLogout}
            className="admin-action-btn logout-btn"
            title="Çıkış Yap"
          >
            <svg
              xmlns="http://www.w3.org/2000/svg"
              viewBox="0 0 24 24"
              fill="currentColor"
              width="20"
              height="20"
            >
              <path d="M17 7l-1.41 1.41L18.17 11H8v2h10.17l-2.58 2.58L17 17l5-5zM4 5h8V3H4c-1.1 0-2 .9-2 2v14c0 1.1.9 2 2 2h8v-2H4V5z" />
            </svg>
          </button>
        </div>
      </div>
    </aside>
  );
};

export default AdminSidebar;
