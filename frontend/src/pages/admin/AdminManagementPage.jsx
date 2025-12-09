import { useState, useEffect } from "react";
import { useAdmin } from "../../context/AdminContext";
import { adminManagementAPI } from "../../services/adminApi";
import "./AdminManagementPage.css";

const AdminManagementPage = () => {
  const { admin: currentAdmin, hasLevel } = useAdmin();
  const [admins, setAdmins] = useState([]);
  const [loading, setLoading] = useState(false);
  const [currentPage, setCurrentPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [pageSize, setPageSize] = useState(10);
  const [sortBy, setSortBy] = useState("createdAt");
  const [sortDirection, setSortDirection] = useState("desc");
  const [showModal, setShowModal] = useState(false);
  const [modalMode, setModalMode] = useState("create"); // 'create' or 'edit'
  const [selectedAdmin, setSelectedAdmin] = useState(null);
  const [showDeleteModal, setShowDeleteModal] = useState(false);
  const [formData, setFormData] = useState({
    username: "",
    email: "",
    password: "",
    firstName: "",
    lastName: "",
    level: 2,
    permissions: [],
  });

  // Check if user has permission (Level 0 or 1)
  const canManageAdmins = hasLevel(1);

  useEffect(() => {
    if (canManageAdmins) {
      fetchAdmins();
    }
  }, [currentPage, pageSize, sortBy, sortDirection, canManageAdmins]);

  const fetchAdmins = async () => {
    try {
      setLoading(true);
      const params = {
        page: currentPage,
        size: pageSize,
        sortBy,
        sortDirection,
      };
      const response = await adminManagementAPI.getAllAdmins(params);
      const data = response.data || response;
      setAdmins(data.admins || data.content || []);
      setTotalPages(data.totalPages || 0);
    } catch (error) {
      console.error("Error fetching admins:", error);
    } finally {
      setLoading(false);
    }
  };

  const handleOpenCreateModal = () => {
    setModalMode("create");
    setFormData({
      username: "",
      email: "",
      password: "",
      firstName: "",
      lastName: "",
      level: 2,
      permissions: [],
    });
    setShowModal(true);
  };

  const handleOpenEditModal = (admin) => {
    setModalMode("edit");
    setSelectedAdmin(admin);
    setFormData({
      username: admin.username,
      email: admin.email,
      password: "", // Don't prefill password
      firstName: admin.firstName || "",
      lastName: admin.lastName || "",
      level: admin.level,
      permissions: admin.permissions || [],
    });
    setShowModal(true);
  };

  const handleSubmit = async (e) => {
    e.preventDefault();

    // Level validation - can't create admin with higher or equal level
    if (formData.level <= currentAdmin.level && modalMode === "create") {
      alert(
        `You can only create admins with level higher than ${currentAdmin.level}`
      );
      return;
    }

    if (
      modalMode === "edit" &&
      selectedAdmin.level <= currentAdmin.level &&
      formData.level <= currentAdmin.level
    ) {
      alert(
        `You cannot edit admins with level ${selectedAdmin.level} or lower`
      );
      return;
    }

    try {
      if (modalMode === "create") {
        await adminManagementAPI.createAdmin(formData);
      } else {
        const updateData = { ...formData };
        if (!updateData.password) {
          delete updateData.password; // Don't update password if empty
        }
        await adminManagementAPI.updateAdmin(selectedAdmin.id, updateData);
      }
      setShowModal(false);
      setSelectedAdmin(null);
      fetchAdmins();
    } catch (error) {
      console.error("Error saving admin:", error);
      alert(error.response?.data?.message || "Failed to save admin");
    }
  };

  const handleDeleteAdmin = async () => {
    if (!selectedAdmin) return;

    // Can't delete admins with same or lower level
    if (selectedAdmin.level <= currentAdmin.level) {
      alert(
        `You cannot delete admins with level ${selectedAdmin.level} or lower`
      );
      return;
    }

    try {
      await adminManagementAPI.deleteAdmin(selectedAdmin.id);
      setShowDeleteModal(false);
      setSelectedAdmin(null);
      fetchAdmins();
    } catch (error) {
      console.error("Error deleting admin:", error);
      alert("Failed to delete admin");
    }
  };

  const handleToggleActive = async (admin) => {
    if (admin.level <= currentAdmin.level) {
      alert(`You cannot modify admins with level ${admin.level} or lower`);
      return;
    }

    try {
      if (admin.isActive) {
        await adminManagementAPI.deactivateAdmin(admin.id);
      } else {
        await adminManagementAPI.activateAdmin(admin.id);
      }
      fetchAdmins();
    } catch (error) {
      console.error("Error toggling admin status:", error);
      alert("Failed to update admin status");
    }
  };

  const handleUnlockAdmin = async (adminId) => {
    try {
      await adminManagementAPI.unlockAdmin(adminId);
      fetchAdmins();
    } catch (error) {
      console.error("Error unlocking admin:", error);
      alert("Failed to unlock admin");
    }
  };

  const handleResetPassword = async (adminId) => {
    const newPassword = prompt("Enter new password:");
    if (!newPassword || newPassword.length < 6) {
      alert("Password must be at least 6 characters");
      return;
    }

    try {
      await adminManagementAPI.resetAdminPassword(adminId, { newPassword });
      alert("Password reset successful");
    } catch (error) {
      console.error("Error resetting password:", error);
      alert("Failed to reset password");
    }
  };

  const getLevelBadge = (level) => {
    switch (level) {
      case 0:
        return <span className="level-badge level-super">Super Admin</span>;
      case 1:
        return <span className="level-badge level-admin">Admin</span>;
      case 2:
        return <span className="level-badge level-moderator">Moderator</span>;
      default:
        return <span className="level-badge">{level}</span>;
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

  if (!canManageAdmins) {
    return (
      <div className="admin-management">
        <div className="access-denied">
          <h1>⛔ Access Denied</h1>
          <p>You don't have permission to manage admins.</p>
          <p>Required level: 0 or 1</p>
        </div>
      </div>
    );
  }

  return (
    <div className="admin-management">
      <div className="page-header">
        <div>
          <h1>Admin Management</h1>
          <p>Manage admin accounts and permissions</p>
        </div>
        <button onClick={handleOpenCreateModal} className="btn-create">
          ➕ Create Admin
        </button>
      </div>

      {/* Filters */}
      <div className="filters-section">
        <div className="filters-row">
          <div className="filter-group">
            <label>Sort By:</label>
            <select value={sortBy} onChange={(e) => setSortBy(e.target.value)}>
              <option value="createdAt">Created Date</option>
              <option value="username">Username</option>
              <option value="level">Level</option>
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

      {/* Admins Table */}
      {loading ? (
        <div className="loading">Loading admins...</div>
      ) : (
        <>
          <div className="table-container">
            <table className="admins-table">
              <thead>
                <tr>
                  <th>ID</th>
                  <th>Username</th>
                  <th>Email</th>
                  <th>Name</th>
                  <th>Level</th>
                  <th>Status</th>
                  <th>Locked</th>
                  <th>Created</th>
                  <th>Actions</th>
                </tr>
              </thead>
              <tbody>
                {admins.length === 0 ? (
                  <tr>
                    <td colSpan="9" className="no-data">
                      No admins found
                    </td>
                  </tr>
                ) : (
                  admins.map((admin) => (
                    <tr
                      key={admin.id}
                      className={
                        admin.id === currentAdmin.id ? "current-admin" : ""
                      }
                    >
                      <td>{admin.id}</td>
                      <td>
                        {admin.username}
                        {admin.id === currentAdmin.id && (
                          <span className="you-badge">You</span>
                        )}
                      </td>
                      <td>{admin.email}</td>
                      <td>
                        {admin.firstName} {admin.lastName}
                      </td>
                      <td>{getLevelBadge(admin.level)}</td>
                      <td>
                        <span
                          className={`status-badge ${
                            admin.isActive ? "status-active" : "status-inactive"
                          }`}
                        >
                          {admin.isActive ? "Active" : "Inactive"}
                        </span>
                      </td>
                      <td>
                        {admin.lockedUntil ? (
                          <span className="locked-badge">🔒 Locked</span>
                        ) : (
                          <span className="unlocked-badge">🔓 No</span>
                        )}
                      </td>
                      <td>{formatDate(admin.createdAt)}</td>
                      <td>
                        <div className="action-buttons">
                          {admin.level > currentAdmin.level && (
                            <>
                              <button
                                onClick={() => handleOpenEditModal(admin)}
                                className="btn-action btn-edit"
                                title="Edit"
                              >
                                ✏️
                              </button>
                              <button
                                onClick={() => handleToggleActive(admin)}
                                className={`btn-action ${
                                  admin.isActive
                                    ? "btn-deactivate"
                                    : "btn-activate"
                                }`}
                                title={
                                  admin.isActive ? "Deactivate" : "Activate"
                                }
                              >
                                {admin.isActive ? "🔴" : "🟢"}
                              </button>
                              {admin.lockedUntil && (
                                <button
                                  onClick={() => handleUnlockAdmin(admin.id)}
                                  className="btn-action btn-unlock"
                                  title="Unlock"
                                >
                                  🔓
                                </button>
                              )}
                              <button
                                onClick={() => handleResetPassword(admin.id)}
                                className="btn-action btn-reset"
                                title="Reset Password"
                              >
                                🔑
                              </button>
                              <button
                                onClick={() => {
                                  setSelectedAdmin(admin);
                                  setShowDeleteModal(true);
                                }}
                                className="btn-action btn-delete"
                                title="Delete"
                              >
                                🗑️
                              </button>
                            </>
                          )}
                          {admin.level <= currentAdmin.level &&
                            admin.id !== currentAdmin.id && (
                              <span className="no-permission">
                                🔒 No Permission
                              </span>
                            )}
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

      {/* Create/Edit Modal */}
      {showModal && (
        <div className="modal-overlay" onClick={() => setShowModal(false)}>
          <div className="modal" onClick={(e) => e.stopPropagation()}>
            <h2>{modalMode === "create" ? "Create Admin" : "Edit Admin"}</h2>
            <form onSubmit={handleSubmit}>
              <div className="form-group">
                <label>Username *</label>
                <input
                  type="text"
                  value={formData.username}
                  onChange={(e) =>
                    setFormData({ ...formData, username: e.target.value })
                  }
                  required
                  disabled={modalMode === "edit"}
                />
              </div>

              <div className="form-group">
                <label>Email *</label>
                <input
                  type="email"
                  value={formData.email}
                  onChange={(e) =>
                    setFormData({ ...formData, email: e.target.value })
                  }
                  required
                />
              </div>

              <div className="form-group">
                <label>
                  Password{" "}
                  {modalMode === "create"
                    ? "*"
                    : "(leave empty to keep current)"}
                </label>
                <input
                  type="password"
                  value={formData.password}
                  onChange={(e) =>
                    setFormData({ ...formData, password: e.target.value })
                  }
                  required={modalMode === "create"}
                  minLength={6}
                />
              </div>

              <div className="form-row">
                <div className="form-group">
                  <label>First Name</label>
                  <input
                    type="text"
                    value={formData.firstName}
                    onChange={(e) =>
                      setFormData({ ...formData, firstName: e.target.value })
                    }
                  />
                </div>

                <div className="form-group">
                  <label>Last Name</label>
                  <input
                    type="text"
                    value={formData.lastName}
                    onChange={(e) =>
                      setFormData({ ...formData, lastName: e.target.value })
                    }
                  />
                </div>
              </div>

              <div className="form-group">
                <label>Admin Level *</label>
                <select
                  value={formData.level}
                  onChange={(e) =>
                    setFormData({ ...formData, level: Number(e.target.value) })
                  }
                  required
                >
                  {currentAdmin.level === 0 && (
                    <option value="0">Super Admin (0)</option>
                  )}
                  {currentAdmin.level <= 0 && (
                    <option value="1">Admin (1)</option>
                  )}
                  <option value="2">Moderator (2)</option>
                </select>
                <small>
                  You can only create admins with level higher than yours (
                  {currentAdmin.level})
                </small>
              </div>

              <div className="modal-actions">
                <button
                  type="button"
                  onClick={() => setShowModal(false)}
                  className="btn-cancel"
                >
                  Cancel
                </button>
                <button type="submit" className="btn-submit">
                  {modalMode === "create" ? "Create" : "Update"}
                </button>
              </div>
            </form>
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
            <h2>Delete Admin</h2>
            <p>Are you sure you want to delete this admin account?</p>
            <div className="admin-info">
              <strong>{selectedAdmin?.username}</strong> ({selectedAdmin?.email}
              )
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
                onClick={handleDeleteAdmin}
                className="btn-confirm-delete"
              >
                Delete
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default AdminManagementPage;
