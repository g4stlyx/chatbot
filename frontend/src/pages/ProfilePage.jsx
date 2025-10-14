import { useState, useEffect } from "react";
import { profileAPI } from "../services/api";
import { useNavigate } from "react-router-dom";
import "./ProfilePage.css";

const ProfilePage = () => {
  const navigate = useNavigate();
  const [profile, setProfile] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [success, setSuccess] = useState("");
  const [isEditing, setIsEditing] = useState(false);
  const [showPasswordModal, setShowPasswordModal] = useState(false);

  // Form states
  const [formData, setFormData] = useState({
    email: "",
    firstName: "",
    lastName: "",
    profilePicture: "",
  });

  const [passwordData, setPasswordData] = useState({
    currentPassword: "",
    newPassword: "",
    confirmPassword: "",
  });

  useEffect(() => {
    fetchProfile();
  }, []);

  const fetchProfile = async () => {
    try {
      setLoading(true);
      const response = await profileAPI.getProfile();
      setProfile(response.data);
      setFormData({
        email: response.data.email || "",
        firstName: response.data.firstName || "",
        lastName: response.data.lastName || "",
        profilePicture: response.data.profilePicture || "",
      });
    } catch (err) {
      setError(err.response?.data?.message || "Failed to load profile");
    } finally {
      setLoading(false);
    }
  };

  const handleInputChange = (e) => {
    const { name, value } = e.target;
    setFormData((prev) => ({
      ...prev,
      [name]: value,
    }));
  };

  const handlePasswordChange = (e) => {
    const { name, value } = e.target;
    setPasswordData((prev) => ({
      ...prev,
      [name]: value,
    }));
  };

  const handleUpdateProfile = async (e) => {
    e.preventDefault();
    setError("");
    setSuccess("");

    try {
      const response = await profileAPI.updateProfile(formData);
      setProfile(response.data);
      setIsEditing(false);
      setSuccess("Profile updated successfully!");
      setTimeout(() => setSuccess(""), 3000);
    } catch (err) {
      setError(err.response?.data?.message || "Failed to update profile");
    }
  };

  const handleChangePassword = async (e) => {
    e.preventDefault();
    setError("");
    setSuccess("");

    if (passwordData.newPassword !== passwordData.confirmPassword) {
      setError("New passwords do not match");
      return;
    }

    if (passwordData.newPassword.length < 8) {
      setError("Password must be at least 8 characters long");
      return;
    }

    try {
      await profileAPI.changePassword({
        currentPassword: passwordData.currentPassword,
        newPassword: passwordData.newPassword,
        confirmPassword: passwordData.confirmPassword,
      });
      setSuccess("Password changed successfully!");
      setShowPasswordModal(false);
      setPasswordData({
        currentPassword: "",
        newPassword: "",
        confirmPassword: "",
      });
      setTimeout(() => setSuccess(""), 3000);
    } catch (err) {
      setError(err.response?.data?.message || "Failed to change password");
    }
  };

  const handleDeactivateAccount = async () => {
    if (
      !window.confirm(
        "Are you sure you want to deactivate your account? You can reactivate it later by logging in."
      )
    ) {
      return;
    }

    try {
      await profileAPI.deactivateAccount();
      localStorage.removeItem("token");
      localStorage.removeItem("user");
      navigate("/login");
    } catch (err) {
      setError(err.response?.data?.message || "Failed to deactivate account");
    }
  };

  const handleCancelEdit = () => {
    setIsEditing(false);
    setFormData({
      email: profile.email || "",
      firstName: profile.firstName || "",
      lastName: profile.lastName || "",
      profilePicture: profile.profilePicture || "",
    });
    setError("");
  };

  if (loading) {
    return (
      <div className="profile-page">
        <div className="profile-loading">Loading profile...</div>
      </div>
    );
  }

  if (!profile) {
    return (
      <div className="profile-page">
        <div className="profile-error">Failed to load profile</div>
      </div>
    );
  }

  return (
    <div className="profile-page">
      <div className="profile-container">
        <div className="profile-header">
          <h1>My Profile</h1>
          <button className="back-btn" onClick={() => navigate("/chat")}>
            ← Back to Chat
          </button>
        </div>

        {error && <div className="alert alert-error">{error}</div>}
        {success && <div className="alert alert-success">{success}</div>}

        <div className="profile-card">
          {/* Profile Picture Section */}
          <div className="profile-picture-section">
            <div className="profile-avatar">
              {profile.profilePicture ? (
                <img src={profile.profilePicture} alt="Profile" />
              ) : (
                <div className="avatar-placeholder">
                  {profile.firstName?.[0] || profile.username?.[0] || "U"}
                </div>
              )}
            </div>
            <h2>
              {profile.firstName || profile.lastName
                ? `${profile.firstName || ""} ${profile.lastName || ""}`
                : profile.username}
            </h2>
            <p className="profile-username">@{profile.username}</p>
          </div>

          {/* Profile Information */}
          {!isEditing ? (
            <div className="profile-info">
              <div className="info-row">
                <label>Email:</label>
                <span>{profile.email}</span>
              </div>
              <div className="info-row">
                <label>First Name:</label>
                <span>{profile.firstName || "Not set"}</span>
              </div>
              <div className="info-row">
                <label>Last Name:</label>
                <span>{profile.lastName || "Not set"}</span>
              </div>
              <div className="info-row">
                <label>Email Verified:</label>
                <span
                  className={
                    profile.emailVerified ? "verified" : "not-verified"
                  }
                >
                  {profile.emailVerified ? "✓ Verified" : "✗ Not Verified"}
                </span>
              </div>
              <div className="info-row">
                <label>Account Status:</label>
                <span className={profile.isActive ? "active" : "inactive"}>
                  {profile.isActive ? "Active" : "Inactive"}
                </span>
              </div>
              <div className="info-row">
                <label>Member Since:</label>
                <span>{new Date(profile.createdAt).toLocaleDateString()}</span>
              </div>
              {profile.lastLoginAt && (
                <div className="info-row">
                  <label>Last Login:</label>
                  <span>{new Date(profile.lastLoginAt).toLocaleString()}</span>
                </div>
              )}

              <div className="profile-actions">
                <button
                  className="btn btn-primary"
                  onClick={() => setIsEditing(true)}
                >
                  Edit Profile
                </button>
                <button
                  className="btn btn-secondary"
                  onClick={() => setShowPasswordModal(true)}
                >
                  Change Password
                </button>
                <button
                  className="btn btn-danger"
                  onClick={handleDeactivateAccount}
                >
                  Deactivate Account
                </button>
              </div>
            </div>
          ) : (
            /* Edit Form */
            <form className="profile-edit-form" onSubmit={handleUpdateProfile}>
              <div className="form-group">
                <label htmlFor="email">Email</label>
                <input
                  type="email"
                  id="email"
                  name="email"
                  value={formData.email}
                  onChange={handleInputChange}
                  required
                />
                <small>Changing email will require re-verification</small>
              </div>

              <div className="form-group">
                <label htmlFor="firstName">First Name</label>
                <input
                  type="text"
                  id="firstName"
                  name="firstName"
                  value={formData.firstName}
                  onChange={handleInputChange}
                  maxLength={100}
                />
              </div>

              <div className="form-group">
                <label htmlFor="lastName">Last Name</label>
                <input
                  type="text"
                  id="lastName"
                  name="lastName"
                  value={formData.lastName}
                  onChange={handleInputChange}
                  maxLength={100}
                />
              </div>

              <div className="form-group">
                <label htmlFor="profilePicture">Profile Picture URL</label>
                <input
                  type="url"
                  id="profilePicture"
                  name="profilePicture"
                  value={formData.profilePicture}
                  onChange={handleInputChange}
                  maxLength={500}
                  placeholder="https://example.com/your-photo.jpg"
                />
              </div>

              <div className="form-actions">
                <button type="submit" className="btn btn-primary">
                  Save Changes
                </button>
                <button
                  type="button"
                  className="btn btn-secondary"
                  onClick={handleCancelEdit}
                >
                  Cancel
                </button>
              </div>
            </form>
          )}
        </div>

        {/* Password Change Modal */}
        {showPasswordModal && (
          <div
            className="modal-overlay"
            onClick={() => setShowPasswordModal(false)}
          >
            <div className="modal-content" onClick={(e) => e.stopPropagation()}>
              <div className="modal-header">
                <h2>Change Password</h2>
                <button
                  className="modal-close"
                  onClick={() => setShowPasswordModal(false)}
                >
                  ×
                </button>
              </div>

              <form onSubmit={handleChangePassword}>
                <div className="form-group">
                  <label htmlFor="currentPassword">Current Password</label>
                  <input
                    type="password"
                    id="currentPassword"
                    name="currentPassword"
                    value={passwordData.currentPassword}
                    onChange={handlePasswordChange}
                    required
                  />
                </div>

                <div className="form-group">
                  <label htmlFor="newPassword">New Password</label>
                  <input
                    type="password"
                    id="newPassword"
                    name="newPassword"
                    value={passwordData.newPassword}
                    onChange={handlePasswordChange}
                    required
                    minLength={8}
                  />
                  <small>Minimum 8 characters</small>
                </div>

                <div className="form-group">
                  <label htmlFor="confirmPassword">Confirm New Password</label>
                  <input
                    type="password"
                    id="confirmPassword"
                    name="confirmPassword"
                    value={passwordData.confirmPassword}
                    onChange={handlePasswordChange}
                    required
                  />
                </div>

                <div className="modal-actions">
                  <button type="submit" className="btn btn-primary">
                    Change Password
                  </button>
                  <button
                    type="button"
                    className="btn btn-secondary"
                    onClick={() => setShowPasswordModal(false)}
                  >
                    Cancel
                  </button>
                </div>
              </form>
            </div>
          </div>
        )}
      </div>
    </div>
  );
};

export default ProfilePage;
