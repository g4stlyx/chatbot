import { useState, useEffect } from "react";
import { useAdmin } from "../../context/AdminContext";
import { adminProfileAPI, admin2FAAPI } from "../../services/adminApi";
import "./AdminProfilePage.css";

const AdminProfilePage = () => {
  const { admin, refreshAdmin } = useAdmin();
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");
  const [success, setSuccess] = useState("");

  // Profile edit state
  const [isEditingProfile, setIsEditingProfile] = useState(false);
  const [profileData, setProfileData] = useState({
    firstName: "",
    lastName: "",
    email: "",
  });

  // Password change state
  const [passwordData, setPasswordData] = useState({
    currentPassword: "",
    newPassword: "",
    confirmPassword: "",
  });
  const [isChangingPassword, setIsChangingPassword] = useState(false);

  // 2FA state
  const [twoFAStatus, setTwoFAStatus] = useState({
    enabled: false,
    loading: true,
  });
  const [twoFASetup, setTwoFASetup] = useState(null);
  const [twoFACode, setTwoFACode] = useState("");
  const [isSettingUp2FA, setIsSettingUp2FA] = useState(false);
  const [isDisabling2FA, setIsDisabling2FA] = useState(false);
  const [disableCode, setDisableCode] = useState("");

  // Initialize profile data and fetch 2FA status
  useEffect(() => {
    if (admin) {
      setProfileData({
        firstName: admin.firstName || "",
        lastName: admin.lastName || "",
        email: admin.email || "",
      });
    }
    fetch2FAStatus();
  }, [admin]);

  const fetch2FAStatus = async () => {
    try {
      const response = await admin2FAAPI.getStatus();
      setTwoFAStatus({
        enabled: response.data.enabled || response.data.twoFactorEnabled,
        loading: false,
      });
    } catch (err) {
      console.error("Failed to fetch 2FA status:", err);
      setTwoFAStatus({ enabled: false, loading: false });
    }
  };

  const clearMessages = () => {
    setError("");
    setSuccess("");
  };

  // Profile handlers
  const handleProfileChange = (e) => {
    const { name, value } = e.target;
    setProfileData((prev) => ({ ...prev, [name]: value }));
    clearMessages();
  };

  const handleProfileSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    clearMessages();

    try {
      await adminProfileAPI.updateProfile(profileData);
      await refreshAdmin();
      setSuccess("Profil başarıyla güncellendi");
      setIsEditingProfile(false);
    } catch (err) {
      setError(err.response?.data?.message || "Profil güncellenirken hata oluştu");
    } finally {
      setLoading(false);
    }
  };

  // Password handlers
  const handlePasswordChange = (e) => {
    const { name, value } = e.target;
    setPasswordData((prev) => ({ ...prev, [name]: value }));
    clearMessages();
  };

  const handlePasswordSubmit = async (e) => {
    e.preventDefault();
    clearMessages();

    if (passwordData.newPassword !== passwordData.confirmPassword) {
      setError("Yeni şifreler eşleşmiyor");
      return;
    }

    if (passwordData.newPassword.length < 8) {
      setError("Yeni şifre en az 8 karakter olmalıdır");
      return;
    }

    setLoading(true);

    try {
      await adminProfileAPI.changePassword({
        currentPassword: passwordData.currentPassword,
        newPassword: passwordData.newPassword,
      });
      setSuccess("Şifre başarıyla değiştirildi");
      setPasswordData({ currentPassword: "", newPassword: "", confirmPassword: "" });
      setIsChangingPassword(false);
    } catch (err) {
      setError(err.response?.data?.message || "Şifre değiştirilirken hata oluştu");
    } finally {
      setLoading(false);
    }
  };

  // 2FA handlers
  const handleSetup2FA = async () => {
    setLoading(true);
    clearMessages();

    try {
      const response = await admin2FAAPI.setup();
      setTwoFASetup(response.data);
      setIsSettingUp2FA(true);
    } catch (err) {
      setError(err.response?.data?.message || "2FA kurulumu başlatılırken hata oluştu");
    } finally {
      setLoading(false);
    }
  };

  const handleVerify2FA = async (e) => {
    e.preventDefault();
    
    if (twoFACode.length !== 6) {
      setError("Lütfen 6 haneli doğrulama kodunu girin");
      return;
    }

    setLoading(true);
    clearMessages();

    try {
      await admin2FAAPI.verify(twoFACode);
      setSuccess("İki faktörlü doğrulama başarıyla etkinleştirildi!");
      setTwoFAStatus({ enabled: true, loading: false });
      setIsSettingUp2FA(false);
      setTwoFASetup(null);
      setTwoFACode("");
    } catch (err) {
      setError(err.response?.data?.message || "Doğrulama kodu geçersiz");
    } finally {
      setLoading(false);
    }
  };

  const handleDisable2FA = async (e) => {
    e.preventDefault();
    
    if (disableCode.length !== 6) {
      setError("Lütfen 6 haneli doğrulama kodunu girin");
      return;
    }

    setLoading(true);
    clearMessages();

    try {
      await admin2FAAPI.disable(disableCode);
      setSuccess("İki faktörlü doğrulama devre dışı bırakıldı");
      setTwoFAStatus({ enabled: false, loading: false });
      setIsDisabling2FA(false);
      setDisableCode("");
    } catch (err) {
      setError(err.response?.data?.message || "Doğrulama kodu geçersiz");
    } finally {
      setLoading(false);
    }
  };

  const cancel2FASetup = () => {
    setIsSettingUp2FA(false);
    setTwoFASetup(null);
    setTwoFACode("");
    clearMessages();
  };

  const cancelDisable2FA = () => {
    setIsDisabling2FA(false);
    setDisableCode("");
    clearMessages();
  };

  const getLevelBadge = (level) => {
    const levels = {
      0: { label: "Süper Admin", className: "level-super" },
      1: { label: "Admin", className: "level-admin" },
      2: { label: "Moderatör", className: "level-moderator" },
    };
    return levels[level] || { label: "Bilinmiyor", className: "" };
  };

  const levelInfo = getLevelBadge(admin?.level);

  return (
    <div className="admin-profile-page">
      <div className="admin-page-header">
        <h1>Profil Ayarları</h1>
        <p>Hesap bilgilerinizi ve güvenlik ayarlarınızı yönetin</p>
      </div>

      {error && (
        <div className="admin-alert admin-alert-error">
          <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="currentColor" width="20" height="20">
            <path d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm1 15h-2v-2h2v2zm0-4h-2V7h2v6z" />
          </svg>
          <span>{error}</span>
        </div>
      )}

      {success && (
        <div className="admin-alert admin-alert-success">
          <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="currentColor" width="20" height="20">
            <path d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm-2 15l-5-5 1.41-1.41L10 14.17l7.59-7.59L19 8l-9 9z" />
          </svg>
          <span>{success}</span>
        </div>
      )}

      <div className="profile-sections">
        {/* Profile Info Section */}
        <section className="profile-section">
          <div className="section-header">
            <div className="section-title">
              <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="currentColor" width="24" height="24">
                <path d="M12 12c2.21 0 4-1.79 4-4s-1.79-4-4-4-4 1.79-4 4 1.79 4 4 4zm0 2c-2.67 0-8 1.34-8 4v2h16v-2c0-2.66-5.33-4-8-4z" />
              </svg>
              <h2>Profil Bilgileri</h2>
            </div>
            {!isEditingProfile && (
              <button className="edit-btn" onClick={() => setIsEditingProfile(true)}>
                <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="currentColor" width="16" height="16">
                  <path d="M3 17.25V21h3.75L17.81 9.94l-3.75-3.75L3 17.25zM20.71 7.04c.39-.39.39-1.02 0-1.41l-2.34-2.34c-.39-.39-1.02-.39-1.41 0l-1.83 1.83 3.75 3.75 1.83-1.83z" />
                </svg>
                Düzenle
              </button>
            )}
          </div>

          <div className="profile-info-grid">
            <div className="profile-avatar-section">
              <div className="profile-avatar-large">
                {admin?.profilePicture ? (
                  <img src={admin.profilePicture} alt={admin.username} />
                ) : (
                  <span>{admin?.username?.charAt(0).toUpperCase()}</span>
                )}
              </div>
              <span className={`level-badge ${levelInfo.className}`}>{levelInfo.label}</span>
            </div>

            {isEditingProfile ? (
              <form onSubmit={handleProfileSubmit} className="profile-form">
                <div className="form-row">
                  <div className="form-group">
                    <label>Ad</label>
                    <input
                      type="text"
                      name="firstName"
                      value={profileData.firstName}
                      onChange={handleProfileChange}
                      placeholder="Adınız"
                    />
                  </div>
                  <div className="form-group">
                    <label>Soyad</label>
                    <input
                      type="text"
                      name="lastName"
                      value={profileData.lastName}
                      onChange={handleProfileChange}
                      placeholder="Soyadınız"
                    />
                  </div>
                </div>
                <div className="form-group">
                  <label>E-posta</label>
                  <input
                    type="email"
                    name="email"
                    value={profileData.email}
                    onChange={handleProfileChange}
                    placeholder="E-posta adresiniz"
                  />
                </div>
                <div className="form-actions">
                  <button type="button" className="cancel-btn" onClick={() => setIsEditingProfile(false)}>
                    İptal
                  </button>
                  <button type="submit" className="save-btn" disabled={loading}>
                    {loading ? "Kaydediliyor..." : "Kaydet"}
                  </button>
                </div>
              </form>
            ) : (
              <div className="profile-details">
                <div className="detail-item">
                  <span className="detail-label">Kullanıcı Adı</span>
                  <span className="detail-value">{admin?.username}</span>
                </div>
                <div className="detail-item">
                  <span className="detail-label">Ad Soyad</span>
                  <span className="detail-value">
                    {admin?.firstName || admin?.lastName
                      ? `${admin?.firstName || ""} ${admin?.lastName || ""}`.trim()
                      : "Belirtilmemiş"}
                  </span>
                </div>
                <div className="detail-item">
                  <span className="detail-label">E-posta</span>
                  <span className="detail-value">{admin?.email || "Belirtilmemiş"}</span>
                </div>
                <div className="detail-item">
                  <span className="detail-label">Kayıt Tarihi</span>
                  <span className="detail-value">
                    {admin?.createdAt
                      ? new Date(admin.createdAt).toLocaleDateString("tr-TR")
                      : "Bilinmiyor"}
                  </span>
                </div>
              </div>
            )}
          </div>
        </section>

        {/* Security Section - 2FA */}
        <section className="profile-section security-section">
          <div className="section-header">
            <div className="section-title">
              <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="currentColor" width="24" height="24">
                <path d="M12 1L3 5v6c0 5.55 3.84 10.74 9 12 5.16-1.26 9-6.45 9-12V5l-9-4zm0 10.99h7c-.53 4.12-3.28 7.79-7 8.94V12H5V6.3l7-3.11v8.8z" />
              </svg>
              <h2>İki Faktörlü Doğrulama (2FA)</h2>
            </div>
            <div className={`status-badge ${twoFAStatus.enabled ? "enabled" : "disabled"}`}>
              {twoFAStatus.loading ? "Yükleniyor..." : twoFAStatus.enabled ? "Aktif" : "Pasif"}
            </div>
          </div>

          <div className="two-fa-content">
            {twoFAStatus.loading ? (
              <div className="loading-state">
                <div className="spinner"></div>
                <span>2FA durumu kontrol ediliyor...</span>
              </div>
            ) : isSettingUp2FA && twoFASetup ? (
              // 2FA Setup Flow
              <div className="two-fa-setup">
                <div className="setup-instructions">
                  <h3>2FA Kurulumu</h3>
                  <p>
                    1. Google Authenticator, Microsoft Authenticator veya benzeri bir uygulama indirin.
                  </p>
                  <p>2. Aşağıdaki QR kodu tarayın veya kodu manuel olarak girin.</p>
                </div>

                <div className="qr-section">
                  {twoFASetup.qrCode && (
                    <div className="qr-code">
                      <img src={twoFASetup.qrCode} alt="2FA QR Code" />
                    </div>
                  )}
                  <div className="secret-key">
                    <span className="key-label">Manuel Giriş Kodu:</span>
                    <code className="key-value">{twoFASetup.secret}</code>
                  </div>
                </div>

                <form onSubmit={handleVerify2FA} className="verify-form">
                  <div className="form-group">
                    <label>Doğrulama Kodu</label>
                    <input
                      type="text"
                      value={twoFACode}
                      onChange={(e) => setTwoFACode(e.target.value.replace(/\D/g, "").slice(0, 6))}
                      placeholder="000000"
                      maxLength={6}
                      className="code-input"
                      autoFocus
                    />
                    <span className="input-hint">Uygulamadaki 6 haneli kodu girin</span>
                  </div>
                  <div className="form-actions">
                    <button type="button" className="cancel-btn" onClick={cancel2FASetup}>
                      İptal
                    </button>
                    <button type="submit" className="save-btn" disabled={loading || twoFACode.length !== 6}>
                      {loading ? "Doğrulanıyor..." : "Etkinleştir"}
                    </button>
                  </div>
                </form>
              </div>
            ) : isDisabling2FA ? (
              // 2FA Disable Flow
              <div className="two-fa-disable">
                <div className="disable-warning">
                  <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="currentColor" width="24" height="24">
                    <path d="M1 21h22L12 2 1 21zm12-3h-2v-2h2v2zm0-4h-2v-4h2v4z" />
                  </svg>
                  <p>
                    <strong>Uyarı:</strong> İki faktörlü doğrulamayı devre dışı bırakmak hesabınızın
                    güvenliğini azaltır.
                  </p>
                </div>

                <form onSubmit={handleDisable2FA} className="verify-form">
                  <div className="form-group">
                    <label>Doğrulama Kodu</label>
                    <input
                      type="text"
                      value={disableCode}
                      onChange={(e) => setDisableCode(e.target.value.replace(/\D/g, "").slice(0, 6))}
                      placeholder="000000"
                      maxLength={6}
                      className="code-input"
                      autoFocus
                    />
                    <span className="input-hint">Devre dışı bırakmak için mevcut kodunuzu girin</span>
                  </div>
                  <div className="form-actions">
                    <button type="button" className="cancel-btn" onClick={cancelDisable2FA}>
                      İptal
                    </button>
                    <button type="submit" className="danger-btn" disabled={loading || disableCode.length !== 6}>
                      {loading ? "İşleniyor..." : "Devre Dışı Bırak"}
                    </button>
                  </div>
                </form>
              </div>
            ) : (
              // Default State - Show enable/disable button
              <div className="two-fa-status">
                {twoFAStatus.enabled ? (
                  <>
                    <div className="status-info success">
                      <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="currentColor" width="48" height="48">
                        <path d="M12 1L3 5v6c0 5.55 3.84 10.74 9 12 5.16-1.26 9-6.45 9-12V5l-9-4zm-2 16l-4-4 1.41-1.41L10 14.17l6.59-6.59L18 9l-8 8z" />
                      </svg>
                      <div className="status-text">
                        <h4>2FA Aktif</h4>
                        <p>Hesabınız iki faktörlü doğrulama ile korunuyor.</p>
                      </div>
                    </div>
                    <button className="danger-btn" onClick={() => setIsDisabling2FA(true)}>
                      <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="currentColor" width="16" height="16">
                        <path d="M19 6.41L17.59 5 12 10.59 6.41 5 5 6.41 10.59 12 5 17.59 6.41 19 12 13.41 17.59 19 19 17.59 13.41 12z" />
                      </svg>
                      Devre Dışı Bırak
                    </button>
                  </>
                ) : (
                  <>
                    <div className="status-info warning">
                      <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="currentColor" width="48" height="48">
                        <path d="M12 1L3 5v6c0 5.55 3.84 10.74 9 12 5.16-1.26 9-6.45 9-12V5l-9-4zm0 10.99h7c-.53 4.12-3.28 7.79-7 8.94V12H5V6.3l7-3.11v8.8z" />
                      </svg>
                      <div className="status-text">
                        <h4>2FA Pasif</h4>
                        <p>Hesabınızı daha güvenli hale getirmek için iki faktörlü doğrulamayı etkinleştirin.</p>
                      </div>
                    </div>
                    <button className="primary-btn" onClick={handleSetup2FA} disabled={loading}>
                      <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="currentColor" width="16" height="16">
                        <path d="M18 8h-1V6c0-2.76-2.24-5-5-5S7 3.24 7 6v2H6c-1.1 0-2 .9-2 2v10c0 1.1.9 2 2 2h12c1.1 0 2-.9 2-2V10c0-1.1-.9-2-2-2zm-6 9c-1.1 0-2-.9-2-2s.9-2 2-2 2 .9 2 2-.9 2-2 2zm3.1-9H8.9V6c0-1.71 1.39-3.1 3.1-3.1 1.71 0 3.1 1.39 3.1 3.1v2z" />
                      </svg>
                      {loading ? "Yükleniyor..." : "2FA Etkinleştir"}
                    </button>
                  </>
                )}
              </div>
            )}
          </div>
        </section>

        {/* Password Section */}
        <section className="profile-section">
          <div className="section-header">
            <div className="section-title">
              <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="currentColor" width="24" height="24">
                <path d="M18 8h-1V6c0-2.76-2.24-5-5-5S7 3.24 7 6v2H6c-1.1 0-2 .9-2 2v10c0 1.1.9 2 2 2h12c1.1 0 2-.9 2-2V10c0-1.1-.9-2-2-2zm-6 9c-1.1 0-2-.9-2-2s.9-2 2-2 2 .9 2 2-.9 2-2 2zm3.1-9H8.9V6c0-1.71 1.39-3.1 3.1-3.1 1.71 0 3.1 1.39 3.1 3.1v2z" />
              </svg>
              <h2>Şifre Değiştir</h2>
            </div>
            {!isChangingPassword && (
              <button className="edit-btn" onClick={() => setIsChangingPassword(true)}>
                <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="currentColor" width="16" height="16">
                  <path d="M3 17.25V21h3.75L17.81 9.94l-3.75-3.75L3 17.25zM20.71 7.04c.39-.39.39-1.02 0-1.41l-2.34-2.34c-.39-.39-1.02-.39-1.41 0l-1.83 1.83 3.75 3.75 1.83-1.83z" />
                </svg>
                Değiştir
              </button>
            )}
          </div>

          {isChangingPassword ? (
            <form onSubmit={handlePasswordSubmit} className="password-form">
              <div className="form-group">
                <label>Mevcut Şifre</label>
                <input
                  type="password"
                  name="currentPassword"
                  value={passwordData.currentPassword}
                  onChange={handlePasswordChange}
                  placeholder="Mevcut şifrenizi girin"
                  required
                />
              </div>
              <div className="form-row">
                <div className="form-group">
                  <label>Yeni Şifre</label>
                  <input
                    type="password"
                    name="newPassword"
                    value={passwordData.newPassword}
                    onChange={handlePasswordChange}
                    placeholder="Yeni şifrenizi girin"
                    required
                    minLength={8}
                  />
                </div>
                <div className="form-group">
                  <label>Yeni Şifre (Tekrar)</label>
                  <input
                    type="password"
                    name="confirmPassword"
                    value={passwordData.confirmPassword}
                    onChange={handlePasswordChange}
                    placeholder="Yeni şifrenizi tekrar girin"
                    required
                    minLength={8}
                  />
                </div>
              </div>
              <div className="form-actions">
                <button
                  type="button"
                  className="cancel-btn"
                  onClick={() => {
                    setIsChangingPassword(false);
                    setPasswordData({ currentPassword: "", newPassword: "", confirmPassword: "" });
                  }}
                >
                  İptal
                </button>
                <button type="submit" className="save-btn" disabled={loading}>
                  {loading ? "Değiştiriliyor..." : "Şifreyi Değiştir"}
                </button>
              </div>
            </form>
          ) : (
            <div className="password-info">
              <p>Hesabınızın güvenliği için şifrenizi düzenli olarak değiştirmenizi öneririz.</p>
            </div>
          )}
        </section>
      </div>
    </div>
  );
};

export default AdminProfilePage;
