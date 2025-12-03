import { useState, useEffect, useCallback } from "react";
import { adminUserAPI } from "../../services/adminApi";
import { useAdmin } from "../../context/AdminContext";
import UserFormModal from "../../components/admin/UserFormModal";
import ConfirmModal from "../../components/admin/ConfirmModal";
import "./UserManagementPage.css";

const UserManagementPage = () => {
  const { canDelete } = useAdmin();
  const [users, setUsers] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [success, setSuccess] = useState(null);

  // Pagination
  const [currentPage, setCurrentPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [totalItems, setTotalItems] = useState(0);
  const [pageSize, setPageSize] = useState(10);

  // Search
  const [searchQuery, setSearchQuery] = useState("");
  const [isSearching, setIsSearching] = useState(false);

  // Modals
  const [showCreateModal, setShowCreateModal] = useState(false);
  const [showEditModal, setShowEditModal] = useState(false);
  const [showDeleteModal, setShowDeleteModal] = useState(false);
  const [showResetPasswordModal, setShowResetPasswordModal] = useState(false);
  const [selectedUser, setSelectedUser] = useState(null);
  const [newPassword, setNewPassword] = useState("");

  // Fetch users
  const fetchUsers = useCallback(async () => {
    setLoading(true);
    setError(null);

    try {
      let response;
      if (searchQuery.trim()) {
        response = await adminUserAPI.searchUsers(
          searchQuery,
          currentPage,
          pageSize
        );
      } else {
        response = await adminUserAPI.getAllUsers({
          page: currentPage,
          size: pageSize,
          sortBy: "createdAt",
          sortDirection: "desc",
        });
      }

      const data = response.data;
      setUsers(data.users || data.content || []);
      setTotalPages(data.totalPages || 0);
      setTotalItems(data.totalItems || data.totalElements || 0);
    } catch (err) {
      console.error("Fetch users error:", err);
      setError("Kullanıcılar yüklenirken bir hata oluştu.");
    } finally {
      setLoading(false);
    }
  }, [currentPage, pageSize, searchQuery]);

  useEffect(() => {
    fetchUsers();
  }, [fetchUsers]);

  // Search handler with debounce
  useEffect(() => {
    const timer = setTimeout(() => {
      if (isSearching) {
        setCurrentPage(0);
        fetchUsers();
      }
    }, 500);

    return () => clearTimeout(timer);
  }, [searchQuery]);

  const handleSearch = (e) => {
    setSearchQuery(e.target.value);
    setIsSearching(true);
  };

  // Clear messages after 5 seconds
  useEffect(() => {
    if (success || error) {
      const timer = setTimeout(() => {
        setSuccess(null);
        setError(null);
      }, 5000);
      return () => clearTimeout(timer);
    }
  }, [success, error]);

  // User actions
  const handleCreateUser = async (userData) => {
    try {
      await adminUserAPI.createUser(userData);
      setSuccess("Kullanıcı başarıyla oluşturuldu.");
      setShowCreateModal(false);
      fetchUsers();
    } catch (err) {
      setError(
        err.response?.data?.message ||
          "Kullanıcı oluşturulurken bir hata oluştu."
      );
    }
  };

  const handleUpdateUser = async (userData) => {
    try {
      await adminUserAPI.updateUser(selectedUser.id, userData);
      setSuccess("Kullanıcı başarıyla güncellendi.");
      setShowEditModal(false);
      setSelectedUser(null);
      fetchUsers();
    } catch (err) {
      setError(
        err.response?.data?.message ||
          "Kullanıcı güncellenirken bir hata oluştu."
      );
    }
  };

  const handleDeleteUser = async () => {
    try {
      await adminUserAPI.deleteUser(selectedUser.id);
      setSuccess("Kullanıcı başarıyla silindi.");
      setShowDeleteModal(false);
      setSelectedUser(null);
      fetchUsers();
    } catch (err) {
      setError(
        err.response?.data?.message || "Kullanıcı silinirken bir hata oluştu."
      );
    }
  };

  const handleActivateUser = async (userId) => {
    try {
      await adminUserAPI.activateUser(userId);
      setSuccess("Kullanıcı aktif edildi.");
      fetchUsers();
    } catch (err) {
      setError(
        err.response?.data?.message ||
          "Kullanıcı aktif edilirken bir hata oluştu."
      );
    }
  };

  const handleDeactivateUser = async (userId) => {
    try {
      await adminUserAPI.deactivateUser(userId);
      setSuccess("Kullanıcı deaktif edildi.");
      fetchUsers();
    } catch (err) {
      setError(
        err.response?.data?.message ||
          "Kullanıcı deaktif edilirken bir hata oluştu."
      );
    }
  };

  const handleUnlockUser = async (userId) => {
    try {
      await adminUserAPI.unlockUser(userId);
      setSuccess("Kullanıcı hesabının kilidi açıldı.");
      fetchUsers();
    } catch (err) {
      setError(
        err.response?.data?.message || "Hesap kilidi açılırken bir hata oluştu."
      );
    }
  };

  const handleVerifyEmail = async (userId) => {
    try {
      await adminUserAPI.verifyEmail(userId);
      setSuccess("Kullanıcı emaili doğrulandı.");
      fetchUsers();
    } catch (err) {
      setError(
        err.response?.data?.message || "Email doğrulanırken bir hata oluştu."
      );
    }
  };

  const handleResetPassword = async () => {
    if (!newPassword || newPassword.length < 8) {
      setError("Şifre en az 8 karakter olmalıdır.");
      return;
    }

    try {
      await adminUserAPI.resetUserPassword(selectedUser.id, newPassword);
      setSuccess("Kullanıcı şifresi sıfırlandı.");
      setShowResetPasswordModal(false);
      setSelectedUser(null);
      setNewPassword("");
    } catch (err) {
      setError(
        err.response?.data?.message || "Şifre sıfırlanırken bir hata oluştu."
      );
    }
  };

  const formatDate = (dateString) => {
    if (!dateString) return "-";
    return new Date(dateString).toLocaleString("tr-TR", {
      day: "2-digit",
      month: "2-digit",
      year: "numeric",
      hour: "2-digit",
      minute: "2-digit",
    });
  };

  const isLocked = (user) => {
    return user.lockedUntil && new Date(user.lockedUntil) > new Date();
  };

  return (
    <div className="user-management-page">
      <div className="admin-page-header">
        <div>
          <h1>Kullanıcı Yönetimi</h1>
          <p>Sistemdeki kullanıcıları yönetin</p>
        </div>
        <button
          className="admin-btn admin-btn-primary"
          onClick={() => setShowCreateModal(true)}
        >
          <svg
            xmlns="http://www.w3.org/2000/svg"
            viewBox="0 0 24 24"
            fill="currentColor"
            width="20"
            height="20"
          >
            <path d="M19 13h-6v6h-2v-6H5v-2h6V5h2v6h6v2z" />
          </svg>
          Yeni Kullanıcı
        </button>
      </div>

      {/* Alerts */}
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

      {success && (
        <div className="admin-alert admin-alert-success">
          <svg
            xmlns="http://www.w3.org/2000/svg"
            viewBox="0 0 24 24"
            fill="currentColor"
            width="20"
            height="20"
          >
            <path d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm-2 15l-5-5 1.41-1.41L10 14.17l7.59-7.59L19 8l-9 9z" />
          </svg>
          <span>{success}</span>
        </div>
      )}

      {/* Search and Filters */}
      <div className="admin-toolbar">
        <div className="admin-search">
          <input
            type="text"
            className="admin-form-input admin-search-input"
            placeholder="Kullanıcı adı veya email ile ara..."
            value={searchQuery}
            onChange={handleSearch}
          />
        </div>
        <div className="admin-toolbar-info">
          Toplam: <strong>{totalItems}</strong> kullanıcı
        </div>
      </div>

      {/* Users Table */}
      <div className="admin-card">
        {loading ? (
          <div className="admin-loading-container">
            <div className="admin-spinner"></div>
          </div>
        ) : users.length === 0 ? (
          <div className="admin-empty-state">
            <svg
              xmlns="http://www.w3.org/2000/svg"
              viewBox="0 0 24 24"
              fill="currentColor"
            >
              <path d="M16 11c1.66 0 2.99-1.34 2.99-3S17.66 5 16 5c-1.66 0-3 1.34-3 3s1.34 3 3 3zm-8 0c1.66 0 2.99-1.34 2.99-3S9.66 5 8 5C6.34 5 5 6.34 5 8s1.34 3 3 3zm0 2c-2.33 0-7 1.17-7 3.5V19h14v-2.5c0-2.33-4.67-3.5-7-3.5z" />
            </svg>
            <h3>Kullanıcı Bulunamadı</h3>
            <p>Arama kriterlerine uygun kullanıcı bulunamadı.</p>
          </div>
        ) : (
          <div className="table-responsive">
            <table className="admin-table">
              <thead>
                <tr>
                  <th>Kullanıcı</th>
                  <th>Email</th>
                  <th>Durum</th>
                  <th>Email Doğrulanmış</th>
                  <th>Son Giriş</th>
                  <th>Kayıt Tarihi</th>
                  <th>İşlemler</th>
                </tr>
              </thead>
              <tbody>
                {users.map((user) => (
                  <tr key={user.id}>
                    <td>
                      <div className="user-cell">
                        <div className="user-avatar">
                          {user.profilePicture ? (
                            <img
                              src={user.profilePicture}
                              alt={user.username}
                            />
                          ) : (
                            <span>
                              {user.username?.charAt(0).toUpperCase()}
                            </span>
                          )}
                        </div>
                        <div className="user-info">
                          <span className="user-name">{user.username}</span>
                          {user.firstName && user.lastName && (
                            <span className="user-fullname">
                              {user.firstName} {user.lastName}
                            </span>
                          )}
                        </div>
                      </div>
                    </td>
                    <td>{user.email}</td>
                    <td>
                      {isLocked(user) ? (
                        <span className="admin-badge admin-badge-danger">
                          Kilitli
                        </span>
                      ) : user.isActive ? (
                        <span className="admin-badge admin-badge-success">
                          Aktif
                        </span>
                      ) : (
                        <span className="admin-badge admin-badge-warning">
                          Pasif
                        </span>
                      )}
                    </td>
                    <td>
                      {user.emailVerified ? (
                        <span className="admin-badge admin-badge-success">
                          Doğrulanmış
                        </span>
                      ) : (
                        <span className="admin-badge admin-badge-warning">
                          Bekliyor
                        </span>
                      )}
                    </td>
                    <td>{formatDate(user.lastLoginAt)}</td>
                    <td>{formatDate(user.createdAt)}</td>
                    <td>
                      <div className="action-buttons">
                        <button
                          className="admin-btn-icon"
                          title="Düzenle"
                          onClick={() => {
                            setSelectedUser(user);
                            setShowEditModal(true);
                          }}
                        >
                          <svg
                            xmlns="http://www.w3.org/2000/svg"
                            viewBox="0 0 24 24"
                            fill="currentColor"
                            width="18"
                            height="18"
                          >
                            <path d="M3 17.25V21h3.75L17.81 9.94l-3.75-3.75L3 17.25zM20.71 7.04c.39-.39.39-1.02 0-1.41l-2.34-2.34c-.39-.39-1.02-.39-1.41 0l-1.83 1.83 3.75 3.75 1.83-1.83z" />
                          </svg>
                        </button>

                        <button
                          className="admin-btn-icon"
                          title="Şifre Sıfırla"
                          onClick={() => {
                            setSelectedUser(user);
                            setShowResetPasswordModal(true);
                          }}
                        >
                          <svg
                            xmlns="http://www.w3.org/2000/svg"
                            viewBox="0 0 24 24"
                            fill="currentColor"
                            width="18"
                            height="18"
                          >
                            <path d="M18 8h-1V6c0-2.76-2.24-5-5-5S7 3.24 7 6v2H6c-1.1 0-2 .9-2 2v10c0 1.1.9 2 2 2h12c1.1 0 2-.9 2-2V10c0-1.1-.9-2-2-2zm-6 9c-1.1 0-2-.9-2-2s.9-2 2-2 2 .9 2 2-.9 2-2 2zm3.1-9H8.9V6c0-1.71 1.39-3.1 3.1-3.1 1.71 0 3.1 1.39 3.1 3.1v2z" />
                          </svg>
                        </button>

                        {isLocked(user) && (
                          <button
                            className="admin-btn-icon"
                            title="Kilidi Aç"
                            onClick={() => handleUnlockUser(user.id)}
                          >
                            <svg
                              xmlns="http://www.w3.org/2000/svg"
                              viewBox="0 0 24 24"
                              fill="currentColor"
                              width="18"
                              height="18"
                            >
                              <path d="M12 17c1.1 0 2-.9 2-2s-.9-2-2-2-2 .9-2 2 .9 2 2 2zm6-9h-1V6c0-2.76-2.24-5-5-5S7 3.24 7 6h1.9c0-1.71 1.39-3.1 3.1-3.1 1.71 0 3.1 1.39 3.1 3.1v2H6c-1.1 0-2 .9-2 2v10c0 1.1.9 2 2 2h12c1.1 0 2-.9 2-2V10c0-1.1-.9-2-2-2zm0 12H6V10h12v10z" />
                            </svg>
                          </button>
                        )}

                        {!user.emailVerified && (
                          <button
                            className="admin-btn-icon"
                            title="Email Doğrula"
                            onClick={() => handleVerifyEmail(user.id)}
                          >
                            <svg
                              xmlns="http://www.w3.org/2000/svg"
                              viewBox="0 0 24 24"
                              fill="currentColor"
                              width="18"
                              height="18"
                            >
                              <path d="M9 16.17L4.83 12l-1.42 1.41L9 19 21 7l-1.41-1.41z" />
                            </svg>
                          </button>
                        )}

                        {user.isActive ? (
                          <button
                            className="admin-btn-icon"
                            title="Deaktif Et"
                            onClick={() => handleDeactivateUser(user.id)}
                          >
                            <svg
                              xmlns="http://www.w3.org/2000/svg"
                              viewBox="0 0 24 24"
                              fill="currentColor"
                              width="18"
                              height="18"
                            >
                              <path d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm0 18c-4.42 0-8-3.58-8-8 0-1.85.63-3.55 1.69-4.9L16.9 18.31C15.55 19.37 13.85 20 12 20zm6.31-3.1L7.1 5.69C8.45 4.63 10.15 4 12 4c4.42 0 8 3.58 8 8 0 1.85-.63 3.55-1.69 4.9z" />
                            </svg>
                          </button>
                        ) : (
                          <button
                            className="admin-btn-icon"
                            title="Aktif Et"
                            onClick={() => handleActivateUser(user.id)}
                          >
                            <svg
                              xmlns="http://www.w3.org/2000/svg"
                              viewBox="0 0 24 24"
                              fill="currentColor"
                              width="18"
                              height="18"
                            >
                              <path d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm-2 15l-5-5 1.41-1.41L10 14.17l7.59-7.59L19 8l-9 9z" />
                            </svg>
                          </button>
                        )}

                        {canDelete() && (
                          <button
                            className="admin-btn-icon danger"
                            title="Sil"
                            onClick={() => {
                              setSelectedUser(user);
                              setShowDeleteModal(true);
                            }}
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
        )}

        {/* Pagination */}
        {totalPages > 1 && (
          <div className="admin-pagination">
            <button
              onClick={() => setCurrentPage(0)}
              disabled={currentPage === 0}
            >
              «
            </button>
            <button
              onClick={() => setCurrentPage((prev) => Math.max(0, prev - 1))}
              disabled={currentPage === 0}
            >
              ‹
            </button>

            <span className="admin-pagination-info">
              Sayfa {currentPage + 1} / {totalPages}
            </span>

            <button
              onClick={() =>
                setCurrentPage((prev) => Math.min(totalPages - 1, prev + 1))
              }
              disabled={currentPage >= totalPages - 1}
            >
              ›
            </button>
            <button
              onClick={() => setCurrentPage(totalPages - 1)}
              disabled={currentPage >= totalPages - 1}
            >
              »
            </button>
          </div>
        )}
      </div>

      {/* Create User Modal */}
      {showCreateModal && (
        <UserFormModal
          title="Yeni Kullanıcı Oluştur"
          onClose={() => setShowCreateModal(false)}
          onSubmit={handleCreateUser}
        />
      )}

      {/* Edit User Modal */}
      {showEditModal && selectedUser && (
        <UserFormModal
          title="Kullanıcı Düzenle"
          user={selectedUser}
          onClose={() => {
            setShowEditModal(false);
            setSelectedUser(null);
          }}
          onSubmit={handleUpdateUser}
        />
      )}

      {/* Delete Confirmation Modal */}
      {showDeleteModal && selectedUser && (
        <ConfirmModal
          title="Kullanıcı Sil"
          message={`"${selectedUser.username}" kullanıcısını silmek istediğinizden emin misiniz? Bu işlem geri alınamaz.`}
          confirmText="Sil"
          confirmType="danger"
          onClose={() => {
            setShowDeleteModal(false);
            setSelectedUser(null);
          }}
          onConfirm={handleDeleteUser}
        />
      )}

      {/* Reset Password Modal */}
      {showResetPasswordModal && selectedUser && (
        <div className="admin-modal-overlay">
          <div className="admin-modal">
            <div className="admin-modal-header">
              <h3>Şifre Sıfırla</h3>
              <button
                className="admin-modal-close"
                onClick={() => {
                  setShowResetPasswordModal(false);
                  setSelectedUser(null);
                  setNewPassword("");
                }}
              >
                ×
              </button>
            </div>
            <div className="admin-modal-body">
              <p className="modal-info">
                <strong>{selectedUser.username}</strong> kullanıcısının
                şifresini sıfırlıyorsunuz.
              </p>
              <div className="admin-form-group">
                <label className="admin-form-label">Yeni Şifre</label>
                <input
                  type="password"
                  className="admin-form-input"
                  value={newPassword}
                  onChange={(e) => setNewPassword(e.target.value)}
                  placeholder="Yeni şifre (min. 8 karakter)"
                  minLength={8}
                />
              </div>
            </div>
            <div className="admin-modal-footer">
              <button
                className="admin-btn admin-btn-secondary"
                onClick={() => {
                  setShowResetPasswordModal(false);
                  setSelectedUser(null);
                  setNewPassword("");
                }}
              >
                İptal
              </button>
              <button
                className="admin-btn admin-btn-primary"
                onClick={handleResetPassword}
                disabled={newPassword.length < 8}
              >
                Şifreyi Sıfırla
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default UserManagementPage;
