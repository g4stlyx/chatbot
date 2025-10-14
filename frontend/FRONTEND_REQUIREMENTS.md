# Frontend Requirements Based on Backend Analysis

## Executive Summary

After analyzing the complete backend codebase, this document outlines the frontend pages and features that need to be implemented to fully utilize the backend API capabilities.

---

## ✅ Currently Implemented Features

### Pages

1. **LoginPage** - User authentication
2. **RegisterPage** - User registration
3. **EmailVerifyPage** - Email verification
4. **VerificationPendingPage** - Waiting for email verification
5. **ChatPage** - Main chat interface with sessions

### Components

- ChatWindow
- MessageList (with markdown support)
- MessageInput
- Session list sidebar

---

## 🔴 MISSING FEATURES (Priority Order)

### 🚨 HIGH PRIORITY - Core User Features

#### 1. **User Profile Page** (`/profile`)

**Backend Endpoints Available:**

- `GET /api/v1/user/profile` - Get user profile
- `PUT /api/v1/user/profile` - Update profile
- `POST /api/v1/user/profile/change-password` - Change password
- `POST /api/v1/user/profile/deactivate` - Deactivate account
- `POST /api/v1/user/profile/reactivate` - Reactivate account

**Required UI Components:**

- Profile information display (username, email, firstName, lastName, profilePicture)
- Profile edit form
- Password change modal/section
- Account status indicator
- Deactivate/Reactivate account button
- Display account metadata (createdAt, lastLoginAt, emailVerified status)

**Data to Display:**

```javascript
{
  id,
    username,
    email,
    firstName,
    lastName,
    profilePicture,
    isActive,
    emailVerified,
    createdAt,
    updatedAt,
    lastLoginAt;
}
```

---

#### 2. **Forgot Password Flow** (`/forgot-password`, `/reset-password`)

**Backend Endpoints Available:**

- `POST /api/v1/auth/forgot-password` - Request password reset
- `POST /api/v1/auth/reset-password` - Reset password with token

**Required Pages:**

- **ForgotPasswordPage** - Enter email to request reset link
- **ResetPasswordPage** - Enter new password with token from email

**Required UI:**

- Email input form
- Token validation display
- New password form with confirmation
- Success/error messaging

---

#### 3. **Resend Verification Email Feature**

**Backend Endpoint:**

- `POST /api/v1/auth/resend-verification` - Resend verification email

**Required UI:**

- Button on VerificationPendingPage to resend email
- Cooldown timer to prevent spam
- Success/error toast notifications

---

#### 4. **Enhanced Message Management in Chat**

**Backend Endpoints Available:**

- `PUT /api/v1/messages/{messageId}` - Edit user messages
- `DELETE /api/v1/messages/{messageId}` - Delete messages
- `POST /api/v1/sessions/{sessionId}/regenerate` - Regenerate assistant response

**Required UI Components:**

- Message action menu (edit, delete buttons on hover/click)
- Edit message modal/inline editor
- Regenerate response button for assistant messages
- Confirmation dialog for deletions

---

#### 5. **Session Management Enhancement**

**Backend Endpoints Available:**

- `POST /api/v1/sessions` - Create new session
- `PUT /api/v1/sessions/{sessionId}` - Update session (rename)
- `POST /api/v1/sessions/{sessionId}/archive` - Archive session
- `POST /api/v1/sessions/{sessionId}/pause` - Pause session
- `GET /api/v1/sessions/active` - Get only active sessions
- Pagination support for sessions

**Required UI Features:**

- Rename session functionality
- Archive/Pause session buttons
- Session status indicators (ACTIVE, PAUSED, ARCHIVED, DELETED)
- Filter sessions by status
- Pagination controls for session list
- Session metadata display (message count, last updated)

---

### 🟡 MEDIUM PRIORITY - Admin Features

#### 6. **Admin Login System** (`/admin/login`)

**Backend Note:** Admins have separate authentication (same login endpoint but different role)

**Required:**

- Separate admin login page or role detection on login
- Admin role detection and routing
- Admin token management

---

#### 7. **Admin Dashboard** (`/admin/dashboard`)

**Backend Endpoints Available:**

- Multiple admin management endpoints

**Required Sections:**

- Overview statistics (total users, sessions, messages)
- Quick access to management panels
- Activity summary

---

#### 8. **Admin User Management Panel** (`/admin/users`)

**Backend Endpoints Available:**

- `GET /api/v1/admin/users` - List all users with pagination
- `GET /api/v1/admin/users/search?q=` - Search users
- `GET /api/v1/admin/users/{userId}` - Get user details
- `POST /api/v1/admin/users` - Create user
- `PUT /api/v1/admin/users/{userId}` - Update user
- `DELETE /api/v1/admin/users/{userId}` - Delete user (soft delete)
- `POST /api/v1/admin/users/{userId}/unlock` - Unlock locked account
- `POST /api/v1/admin/users/{userId}/activate` - Activate user
- `POST /api/v1/admin/users/{userId}/deactivate` - Deactivate user
- `POST /api/v1/admin/users/{userId}/verify-email` - Manually verify email
- `POST /api/v1/admin/users/{userId}/reset-password` - Admin reset user password

**Required UI:**

- User list table with pagination, sorting, filtering
- Search functionality
- User detail modal/page
- Create user form
- Edit user form
- Bulk action buttons (activate, deactivate, delete)
- Account status badges
- User action buttons (unlock, verify email, reset password)

**Data to Display:**

```javascript
{
  id,
    username,
    email,
    firstName,
    lastName,
    profilePicture,
    isActive,
    emailVerified,
    loginAttempts,
    lockedUntil,
    createdAt,
    updatedAt,
    lastLoginAt;
}
```

---

#### 9. **Admin Session Management Panel** (`/admin/sessions`)

**Backend Endpoints Available:**

- `GET /api/v1/admin/sessions` - List all sessions with filters
  - Filters: userId, status, isFlagged, isPublic
  - Pagination and sorting
- `GET /api/v1/admin/sessions/{sessionId}` - Get session details
- `DELETE /api/v1/admin/sessions/{sessionId}` - Hard delete session
- `POST /api/v1/admin/sessions/{sessionId}/flag` - Flag/unflag session
- `POST /api/v1/admin/sessions/{sessionId}/toggle-public` - Make public/private

**Required UI:**

- Session list table with advanced filtering
- Session status badges (ACTIVE, PAUSED, ARCHIVED, DELETED)
- Flag indicator
- Public/Private toggle
- View session details (including messages)
- Hard delete with confirmation
- Filter by user

---

#### 10. **Admin Message Management Panel** (`/admin/messages`)

**Backend Endpoints Available:**

- `GET /api/v1/admin/messages` - List all messages with filters
  - Filters: sessionId, userId, role, isFlagged
- `GET /api/v1/admin/messages/{messageId}` - Get message details
- `GET /api/v1/admin/messages/session/{sessionId}` - Get messages by session
- `DELETE /api/v1/admin/messages/{messageId}` - Hard delete message
- `POST /api/v1/admin/messages/{messageId}/flag` - Flag/unflag message

**Required UI:**

- Message list with filtering
- Message content viewer
- Flag/unflag functionality
- Delete with confirmation
- Filter by session, user, role
- Show flagged messages prominently

---

### 🟢 LOW PRIORITY - Super Admin Features

#### 11. **Admin Management Panel** (`/admin/admins`) - Level 0 & 1 Only

**Backend Endpoints Available:**

- `GET /api/v1/admin/admins` - List all admins (permission-based)
- `GET /api/v1/admin/admins/{adminId}` - Get admin details
- `POST /api/v1/admin/admins` - Create admin
- `PUT /api/v1/admin/admins/{adminId}` - Update admin
- `DELETE /api/v1/admin/admins/{adminId}` - Delete admin
- `POST /api/v1/admin/admins/{adminId}/activate` - Activate admin
- `POST /api/v1/admin/admins/{adminId}/deactivate` - Deactivate admin

**Authorization Rules:**

- Level 0 (Super Admin) can manage Level 1 & 2
- Level 1 (Admin) can only manage Level 2
- Level 2 (Moderator) cannot manage other admins

**Required UI:**

- Admin list table
- Level-based access control
- Create/Edit admin forms
- Admin level selector (0, 1, 2)
- Permission management UI
- Activate/Deactivate controls

**Admin Data:**

```javascript
{
  id, username, email, firstName, lastName, profilePicture,
  level, permissions[], isActive, loginAttempts, lockedUntil,
  createdBy, createdAt, updatedAt, lastLoginAt
}
```

---

#### 12. **Admin Profile Page** (`/admin/profile`)

**Backend Endpoints Available:**

- `GET /api/v1/admin/profile` - Get own profile
- `GET /api/v1/admin/profile/{adminId}` - Get any admin's profile
- `PUT /api/v1/admin/profile` - Update own profile
- `POST /api/v1/admin/profile/change-password` - Change own password
- `POST /api/v1/admin/profile/{adminId}/deactivate` - Deactivate admin
- `POST /api/v1/admin/profile/{adminId}/reactivate` - Reactivate admin

**Required UI:**

- Admin profile display
- Edit profile form
- Password change section
- Admin-specific information (level, permissions)

---

#### 13. **Activity Log Viewer** (`/admin/activity-logs`) - Level 0 Only

**Backend Endpoints Available:**

- `GET /api/v1/admin/activity-logs` - Get all activity logs
  - Filters: adminId, action, resourceType, startDate
  - Pagination and sorting
- `GET /api/v1/admin/activity-logs/{logId}` - Get specific log

**Required UI:**

- Activity log table
- Advanced filtering (by admin, action type, resource, date range)
- Log detail viewer
- Export functionality
- Action type badges
- Resource type indicators

**Log Data:**

```javascript
{
  id,
    adminId,
    adminUsername,
    action,
    resourceType,
    resourceId,
    description,
    ipAddress,
    userAgent,
    createdAt;
}
```

---

#### 14. **Token Management Panel** (`/admin/tokens`) - Level 0 Only

**Backend Endpoints Available:**

- `GET /api/v1/admin/tokens/password-reset` - List password reset tokens
  - Filters: userType, includeExpired
- `GET /api/v1/admin/tokens/password-reset/{tokenId}` - Get token details
- `DELETE /api/v1/admin/tokens/password-reset/{tokenId}` - Delete token
- `POST /api/v1/admin/tokens/password-reset/{tokenId}/invalidate` - Invalidate token
- `GET /api/v1/admin/tokens/verification` - List verification tokens
- `GET /api/v1/admin/tokens/verification/{tokenId}` - Get verification token
- `DELETE /api/v1/admin/tokens/verification/{tokenId}` - Delete token
- `POST /api/v1/admin/tokens/verification/{tokenId}/invalidate` - Invalidate token

**Required UI:**

- Two tabs: Password Reset Tokens, Verification Tokens
- Token list tables
- Filter by user type, expiration status
- Token status indicators (valid, expired, used)
- Invalidate/Delete actions
- Token details viewer

---

## 📋 API Integration Checklist

### ✅ Already Integrated

- [x] Auth: Register, Login
- [x] Auth: Email Verification
- [x] Chat: Send message (streaming)
- [x] Sessions: Get, Delete sessions
- [x] Messages: Get messages for session

### ❌ Need to Add to api.js

#### User Profile API

```javascript
export const profileAPI = {
  getProfile: () => api.get("/api/v1/user/profile"),
  updateProfile: (data) => api.put("/api/v1/user/profile", data),
  changePassword: (data) =>
    api.post("/api/v1/user/profile/change-password", data),
  deactivate: () => api.post("/api/v1/user/profile/deactivate"),
  reactivate: () => api.post("/api/v1/user/profile/reactivate"),
};
```

#### Auth Extensions

```javascript
export const authAPI = {
  // ... existing methods
  forgotPassword: (email) =>
    api.post("/api/v1/auth/forgot-password", { email }),
  resetPassword: (token, newPassword) =>
    api.post("/api/v1/auth/reset-password", { token, newPassword }),
  resendVerification: (email) =>
    api.post("/api/v1/auth/resend-verification", { email }),
};
```

#### Session Management Extensions

```javascript
export const sessionAPI = {
  // ... existing methods
  createSession: (title) => api.post("/api/v1/sessions", { title }),
  getActiveSessions: () => api.get("/api/v1/sessions/active"),
  archiveSession: (sessionId) =>
    api.post(`/api/v1/sessions/${sessionId}/archive`),
  pauseSession: (sessionId) => api.post(`/api/v1/sessions/${sessionId}/pause`),
};
```

#### Admin APIs

```javascript
export const adminUserAPI = {
  getAllUsers: (params) => api.get("/api/v1/admin/users", { params }),
  searchUsers: (q, page, size) =>
    api.get("/api/v1/admin/users/search", { params: { q, page, size } }),
  getUserById: (userId) => api.get(`/api/v1/admin/users/${userId}`),
  createUser: (data) => api.post("/api/v1/admin/users", data),
  updateUser: (userId, data) => api.put(`/api/v1/admin/users/${userId}`, data),
  deleteUser: (userId) => api.delete(`/api/v1/admin/users/${userId}`),
  unlockUser: (userId) => api.post(`/api/v1/admin/users/${userId}/unlock`),
  activateUser: (userId) => api.post(`/api/v1/admin/users/${userId}/activate`),
  deactivateUser: (userId) =>
    api.post(`/api/v1/admin/users/${userId}/deactivate`),
  verifyEmail: (userId) =>
    api.post(`/api/v1/admin/users/${userId}/verify-email`),
  resetUserPassword: (userId, newPassword) =>
    api.post(`/api/v1/admin/users/${userId}/reset-password`, { newPassword }),
};

export const adminSessionAPI = {
  getAllSessions: (params) => api.get("/api/v1/admin/sessions", { params }),
  getSession: (sessionId) => api.get(`/api/v1/admin/sessions/${sessionId}`),
  deleteSession: (sessionId) =>
    api.delete(`/api/v1/admin/sessions/${sessionId}`),
  flagSession: (sessionId, reason) =>
    api.post(`/api/v1/admin/sessions/${sessionId}/flag`, { reason }),
  togglePublic: (sessionId) =>
    api.post(`/api/v1/admin/sessions/${sessionId}/toggle-public`),
};

export const adminMessageAPI = {
  getAllMessages: (params) => api.get("/api/v1/admin/messages", { params }),
  getMessage: (messageId) => api.get(`/api/v1/admin/messages/${messageId}`),
  getMessagesBySession: (sessionId, params) =>
    api.get(`/api/v1/admin/messages/session/${sessionId}`, { params }),
  deleteMessage: (messageId) =>
    api.delete(`/api/v1/admin/messages/${messageId}`),
  flagMessage: (messageId, reason) =>
    api.post(`/api/v1/admin/messages/${messageId}/flag`, { reason }),
};

export const adminManagementAPI = {
  getAllAdmins: (params) => api.get("/api/v1/admin/admins", { params }),
  getAdmin: (adminId) => api.get(`/api/v1/admin/admins/${adminId}`),
  createAdmin: (data) => api.post("/api/v1/admin/admins", data),
  updateAdmin: (adminId, data) =>
    api.put(`/api/v1/admin/admins/${adminId}`, data),
  deleteAdmin: (adminId) => api.delete(`/api/v1/admin/admins/${adminId}`),
  activateAdmin: (adminId) =>
    api.post(`/api/v1/admin/admins/${adminId}/activate`),
  deactivateAdmin: (adminId) =>
    api.post(`/api/v1/admin/admins/${adminId}/deactivate`),
};

export const adminProfileAPI = {
  getProfile: () => api.get("/api/v1/admin/profile"),
  getAdminProfile: (adminId) => api.get(`/api/v1/admin/profile/${adminId}`),
  updateProfile: (data) => api.put("/api/v1/admin/profile", data),
  changePassword: (data) =>
    api.post("/api/v1/admin/profile/change-password", data),
  deactivate: (adminId) =>
    api.post(`/api/v1/admin/profile/${adminId}/deactivate`),
  reactivate: (adminId) =>
    api.post(`/api/v1/admin/profile/${adminId}/reactivate`),
};

export const adminActivityLogAPI = {
  getAllLogs: (params) => api.get("/api/v1/admin/activity-logs", { params }),
  getLog: (logId) => api.get(`/api/v1/admin/activity-logs/${logId}`),
};

export const adminTokenAPI = {
  getPasswordResetTokens: (params) =>
    api.get("/api/v1/admin/tokens/password-reset", { params }),
  getPasswordResetToken: (tokenId) =>
    api.get(`/api/v1/admin/tokens/password-reset/${tokenId}`),
  deletePasswordResetToken: (tokenId) =>
    api.delete(`/api/v1/admin/tokens/password-reset/${tokenId}`),
  invalidatePasswordResetToken: (tokenId) =>
    api.post(`/api/v1/admin/tokens/password-reset/${tokenId}/invalidate`),
  getVerificationTokens: (params) =>
    api.get("/api/v1/admin/tokens/verification", { params }),
  getVerificationToken: (tokenId) =>
    api.get(`/api/v1/admin/tokens/verification/${tokenId}`),
  deleteVerificationToken: (tokenId) =>
    api.delete(`/api/v1/admin/tokens/verification/${tokenId}`),
  invalidateVerificationToken: (tokenId) =>
    api.post(`/api/v1/admin/tokens/verification/${tokenId}/invalidate`),
};
```

---

## 🎨 UI/UX Recommendations

### Design System

- Use consistent color scheme for:
  - Admin features (different from user features)
  - Status indicators (active, inactive, flagged, etc.)
  - Role levels (Level 0, 1, 2)
- Implement toast notifications for all actions
- Add loading states for all async operations

### Navigation

- User sidebar: Profile, Settings, Logout
- Admin sidebar: Dashboard, Users, Sessions, Messages, Admins (if authorized), Activity Logs (Level 0), Tokens (Level 0)

### Responsive Design

- Mobile-first approach
- Collapsible sidebars
- Touch-friendly action buttons

---

## 🔒 Security Considerations

1. **Role-Based Access Control**

   - Implement route guards for admin pages
   - Check user role from JWT token
   - Hide unauthorized UI elements

2. **Admin Level Permissions**

   - Level 0: Full access
   - Level 1: Cannot access logs, tokens, or manage Level 0/1 admins
   - Level 2: No admin management access

3. **Confirmation Dialogs**
   - Always confirm destructive actions (delete, deactivate)
   - Show impact of actions (e.g., "deleting user message will also delete assistant response")

---

## 📊 Priority Implementation Order

1. **Phase 1 - Core User Features (Week 1-2)**

   - User Profile Page
   - Forgot/Reset Password Flow
   - Enhanced Message Management
   - Session Management Features

2. **Phase 2 - Admin Foundation (Week 3-4)**

   - Admin Login/Auth
   - Admin Dashboard
   - Admin User Management
   - Admin Profile

3. **Phase 3 - Advanced Admin Features (Week 5-6)**

   - Admin Session Management
   - Admin Message Management
   - Admin Management Panel (for Level 0-1)

4. **Phase 4 - Super Admin Features (Week 7)**
   - Activity Logs
   - Token Management

---

## 📁 Suggested File Structure

```
frontend/src/
├── pages/
│   ├── user/
│   │   ├── ProfilePage.jsx
│   │   ├── ForgotPasswordPage.jsx
│   │   └── ResetPasswordPage.jsx
│   └── admin/
│       ├── AdminDashboard.jsx
│       ├── AdminLoginPage.jsx
│       ├── UserManagementPage.jsx
│       ├── SessionManagementPage.jsx
│       ├── MessageManagementPage.jsx
│       ├── AdminManagementPage.jsx
│       ├── AdminProfilePage.jsx
│       ├── ActivityLogsPage.jsx
│       └── TokenManagementPage.jsx
├── components/
│   ├── user/
│   │   ├── ProfileForm.jsx
│   │   ├── PasswordChangeModal.jsx
│   │   └── AccountSettings.jsx
│   ├── admin/
│   │   ├── AdminSidebar.jsx
│   │   ├── UserTable.jsx
│   │   ├── SessionTable.jsx
│   │   ├── MessageTable.jsx
│   │   ├── AdminTable.jsx
│   │   ├── ActivityLogTable.jsx
│   │   ├── TokenTable.jsx
│   │   └── shared/
│   │       ├── StatusBadge.jsx
│   │       ├── ActionButton.jsx
│   │       └── ConfirmDialog.jsx
│   └── chat/
│       ├── MessageActions.jsx (new)
│       └── EditMessageModal.jsx (new)
└── services/
    ├── api.js (extended with all APIs above)
    └── adminApi.js (optional separate file)
```

---

## 🧪 Testing Recommendations

1. **Unit Tests**

   - API service functions
   - Form validation logic
   - Authorization helpers

2. **Integration Tests**

   - Full user flows (register → verify → profile → chat)
   - Admin flows (login → manage users → view logs)

3. **E2E Tests**
   - Critical user journeys
   - Admin permission enforcement

---

## 📝 Notes

- All admin endpoints require ADMIN role JWT token
- Level-based authorization is enforced on backend
- Activity logging is automatic for all admin actions
- Session status: ACTIVE, PAUSED, ARCHIVED, DELETED
- Message roles: USER, ASSISTANT
- Admin levels: 0 (Super Admin), 1 (Admin), 2 (Moderator)
