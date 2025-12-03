import { useState, useEffect } from "react";
import "./UserFormModal.css";

const UserFormModal = ({ title, user, onClose, onSubmit }) => {
  const isEdit = !!user;

  const [formData, setFormData] = useState({
    username: "",
    email: "",
    password: "",
    firstName: "",
    lastName: "",
    profilePicture: "",
    isActive: true,
    emailVerified: false,
  });

  const [errors, setErrors] = useState({});
  const [isSubmitting, setIsSubmitting] = useState(false);

  useEffect(() => {
    if (user) {
      setFormData({
        username: user.username || "",
        email: user.email || "",
        password: "", // Don't prefill password
        firstName: user.firstName || "",
        lastName: user.lastName || "",
        profilePicture: user.profilePicture || "",
        isActive: user.isActive ?? true,
        emailVerified: user.emailVerified ?? false,
      });
    }
  }, [user]);

  const handleChange = (e) => {
    const { name, value, type, checked } = e.target;
    setFormData((prev) => ({
      ...prev,
      [name]: type === "checkbox" ? checked : value,
    }));
    // Clear error when user types
    if (errors[name]) {
      setErrors((prev) => ({ ...prev, [name]: "" }));
    }
  };

  const validate = () => {
    const newErrors = {};

    if (!isEdit && !formData.username.trim()) {
      newErrors.username = "Kullanıcı adı gereklidir.";
    } else if (
      formData.username &&
      (formData.username.length < 3 || formData.username.length > 50)
    ) {
      newErrors.username = "Kullanıcı adı 3-50 karakter arasında olmalıdır.";
    }

    if (!formData.email.trim()) {
      newErrors.email = "Email gereklidir.";
    } else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(formData.email)) {
      newErrors.email = "Geçerli bir email adresi giriniz.";
    }

    if (!isEdit && !formData.password) {
      newErrors.password = "Şifre gereklidir.";
    } else if (formData.password && formData.password.length < 8) {
      newErrors.password = "Şifre en az 8 karakter olmalıdır.";
    }

    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleSubmit = async (e) => {
    e.preventDefault();

    if (!validate()) return;

    setIsSubmitting(true);

    const submitData = { ...formData };

    // For edit, only send password if it was changed
    if (isEdit && !submitData.password) {
      delete submitData.password;
    }

    // For edit, don't send username
    if (isEdit) {
      delete submitData.username;
    }

    // Remove empty optional fields
    if (!submitData.firstName) delete submitData.firstName;
    if (!submitData.lastName) delete submitData.lastName;
    if (!submitData.profilePicture) delete submitData.profilePicture;

    try {
      await onSubmit(submitData);
    } catch (err) {
      console.error("Submit error:", err);
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <div className="admin-modal-overlay" onClick={onClose}>
      <div
        className="admin-modal user-form-modal"
        onClick={(e) => e.stopPropagation()}
      >
        <div className="admin-modal-header">
          <h3>{title}</h3>
          <button className="admin-modal-close" onClick={onClose}>
            ×
          </button>
        </div>

        <form onSubmit={handleSubmit}>
          <div className="admin-modal-body">
            <div className="form-row">
              <div className="admin-form-group">
                <label className="admin-form-label">
                  Kullanıcı Adı {!isEdit && <span className="required">*</span>}
                </label>
                <input
                  type="text"
                  name="username"
                  className={`admin-form-input ${
                    errors.username ? "error" : ""
                  }`}
                  value={formData.username}
                  onChange={handleChange}
                  placeholder="Kullanıcı adı"
                  disabled={isEdit}
                />
                {errors.username && (
                  <span className="form-error">{errors.username}</span>
                )}
              </div>

              <div className="admin-form-group">
                <label className="admin-form-label">
                  Email <span className="required">*</span>
                </label>
                <input
                  type="email"
                  name="email"
                  className={`admin-form-input ${errors.email ? "error" : ""}`}
                  value={formData.email}
                  onChange={handleChange}
                  placeholder="email@example.com"
                />
                {errors.email && (
                  <span className="form-error">{errors.email}</span>
                )}
              </div>
            </div>

            <div className="admin-form-group">
              <label className="admin-form-label">
                Şifre {!isEdit && <span className="required">*</span>}
                {isEdit && (
                  <span className="form-hint">
                    (Değiştirmek istemiyorsanız boş bırakın)
                  </span>
                )}
              </label>
              <input
                type="password"
                name="password"
                className={`admin-form-input ${errors.password ? "error" : ""}`}
                value={formData.password}
                onChange={handleChange}
                placeholder={
                  isEdit ? "Yeni şifre (opsiyonel)" : "Şifre (min. 8 karakter)"
                }
              />
              {errors.password && (
                <span className="form-error">{errors.password}</span>
              )}
            </div>

            <div className="form-row">
              <div className="admin-form-group">
                <label className="admin-form-label">Ad</label>
                <input
                  type="text"
                  name="firstName"
                  className="admin-form-input"
                  value={formData.firstName}
                  onChange={handleChange}
                  placeholder="Ad"
                />
              </div>

              <div className="admin-form-group">
                <label className="admin-form-label">Soyad</label>
                <input
                  type="text"
                  name="lastName"
                  className="admin-form-input"
                  value={formData.lastName}
                  onChange={handleChange}
                  placeholder="Soyad"
                />
              </div>
            </div>

            <div className="admin-form-group">
              <label className="admin-form-label">Profil Resmi URL</label>
              <input
                type="url"
                name="profilePicture"
                className="admin-form-input"
                value={formData.profilePicture}
                onChange={handleChange}
                placeholder="https://example.com/image.jpg"
              />
            </div>

            <div className="form-row checkboxes">
              <label className="admin-form-checkbox">
                <input
                  type="checkbox"
                  name="isActive"
                  checked={formData.isActive}
                  onChange={handleChange}
                />
                <span>Aktif Hesap</span>
              </label>

              <label className="admin-form-checkbox">
                <input
                  type="checkbox"
                  name="emailVerified"
                  checked={formData.emailVerified}
                  onChange={handleChange}
                />
                <span>Email Doğrulanmış</span>
              </label>
            </div>
          </div>

          <div className="admin-modal-footer">
            <button
              type="button"
              className="admin-btn admin-btn-secondary"
              onClick={onClose}
              disabled={isSubmitting}
            >
              İptal
            </button>
            <button
              type="submit"
              className="admin-btn admin-btn-primary"
              disabled={isSubmitting}
            >
              {isSubmitting ? (
                <>
                  <span className="btn-spinner"></span>
                  {isEdit ? "Güncelleniyor..." : "Oluşturuluyor..."}
                </>
              ) : isEdit ? (
                "Güncelle"
              ) : (
                "Oluştur"
              )}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};

export default UserFormModal;
