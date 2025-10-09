# Admin Panel API Documentation

This document describes the admin panel endpoints for managing users and admins with hierarchical authorization.

## Authorization Hierarchy

The admin system follows a **staircase authorization** model:

| Level | Role | Can Manage |
|-------|------|------------|
| 0 | Super Admin | Level 1 (Admins) and Level 2 (Moderators) |
| 1 | Admin | Level 2 (Moderators) only |
| 2 | Moderator | Cannot manage other admins |

### Key Authorization Rules:
- ✅ Super Admins (Level 0) can manage all admins except other super admins
- ✅ Admins (Level 1) can only manage Moderators (Level 2)
- ✅ Moderators (Level 2) cannot manage other admins
- ✅ No admin can delete or deactivate themselves
- ✅ Super admin accounts cannot be deleted or deactivated
- ✅ Super admins cannot be created via API

---

## User Management Endpoints

Base URL: `/api/v1/admin/users`

**Authentication:** Admin JWT Token Required

### 1. Get All Users

Retrieve a paginated list of all users.

**Endpoint:** `GET /api/v1/admin/users`

**Query Parameters:**
- `page` (default: 0) - Page number
- `size` (default: 10) - Page size
- `sortBy` (default: createdAt) - Field to sort by
- `sortDirection` (default: desc) - Sort direction (asc/desc)

**Response:**
```json
{
  "users": [
    {
      "id": 1,
      "username": "johndoe",
      "email": "john@example.com",
      "firstName": "John",
      "lastName": "Doe",
      "profilePicture": "https://example.com/pic.jpg",
      "isActive": true,
      "emailVerified": true,
      "loginAttempts": 0,
      "lockedUntil": null,
      "createdAt": "2024-01-15T10:30:00",
      "updatedAt": "2024-03-20T14:45:00",
      "lastLoginAt": "2024-10-07T08:15:00"
    }
  ],
  "currentPage": 0,
  "totalPages": 5,
  "totalItems": 50,
  "pageSize": 10
}
```

---

### 2. Search Users

Search users by username or email.

**Endpoint:** `GET /api/v1/admin/users/search`

**Query Parameters:**
- `q` (required) - Search term
- `page` (default: 0) - Page number
- `size` (default: 10) - Page size

**Response:** Same structure as Get All Users

---

### 3. Get User by ID

**Endpoint:** `GET /api/v1/admin/users/{userId}`

**Response:** Single user object

---

### 4. Create User

**Endpoint:** `POST /api/v1/admin/users`

**Request Body:**
```json
{
  "username": "newuser",
  "email": "newuser@example.com",
  "password": "SecurePass123",
  "firstName": "New",
  "lastName": "User",
  "profilePicture": "https://example.com/pic.jpg",
  "isActive": true,
  "emailVerified": false
}
```

**Validation:**
- `username`: 3-50 characters, required
- `email`: Valid email, max 255 characters, required
- `password`: Min 8 characters, required
- `firstName`, `lastName`: Max 100 characters, optional
- `profilePicture`: Max 500 characters, optional

**Response:** Created user object with status 201

---

### 5. Update User

**Endpoint:** `PUT /api/v1/admin/users/{userId}`

**Request Body:** (All fields optional)
```json
{
  "email": "updated@example.com",
  "firstName": "Updated",
  "lastName": "Name",
  "profilePicture": "https://example.com/new.jpg",
  "isActive": true,
  "emailVerified": true
}
```

**Response:** Updated user object

---

### 6. Delete User (Soft Delete)

**Endpoint:** `DELETE /api/v1/admin/users/{userId}`

**Response:**
```json
{
  "success": true,
  "message": "User deactivated successfully"
}
```

---

### 7. Activate User

**Endpoint:** `POST /api/v1/admin/users/{userId}/activate`

**Response:** Updated user object with `isActive: true`

---

### 8. Deactivate User

**Endpoint:** `POST /api/v1/admin/users/{userId}/deactivate`

**Response:** Updated user object with `isActive: false`

---

### 9. Reset User Password

**Endpoint:** `POST /api/v1/admin/users/{userId}/reset-password`

**Request Body:**
```json
{
  "newPassword": "NewSecurePass123"
}
```

**Response:**
```json
{
  "success": true,
  "message": "User password reset successfully"
}
```

---

### 10. Unlock User Account

**Endpoint:** `POST /api/v1/admin/users/{userId}/unlock`

**Response:** Updated user object with `loginAttempts: 0` and `lockedUntil: null`

---

## Admin Management Endpoints

Base URL: `/api/v1/admin/admins`

**Authentication:** Admin JWT Token Required

### 1. Get All Admins

Retrieve a paginated list of admins (filtered by permission level).

**Endpoint:** `GET /api/v1/admin/admins`

**Query Parameters:**
- `page` (default: 0) - Page number
- `size` (default: 10) - Page size
- `sortBy` (default: createdAt) - Field to sort by
- `sortDirection` (default: desc) - Sort direction (asc/desc)

**Permission Filtering:**
- Super Admin (Level 0): Sees all admins
- Admin (Level 1): Sees Level 1 and Level 2
- Moderator (Level 2): Sees only Level 2

**Response:**
```json
{
  "admins": [
    {
      "id": 1,
      "username": "admin_user",
      "email": "admin@example.com",
      "firstName": "Admin",
      "lastName": "User",
      "profilePicture": "https://example.com/admin.jpg",
      "level": 1,
      "permissions": ["user_management", "content_moderation"],
      "isActive": true,
      "loginAttempts": 0,
      "lockedUntil": null,
      "createdBy": null,
      "createdAt": "2024-01-01T00:00:00",
      "updatedAt": "2024-03-20T14:45:00",
      "lastLoginAt": "2024-10-07T08:15:00"
    }
  ],
  "currentPage": 0,
  "totalPages": 2,
  "totalItems": 15,
  "pageSize": 10
}
```

---

### 2. Get Admin by ID

**Endpoint:** `GET /api/v1/admin/admins/{adminId}`

**Authorization:** Must have permission to view target admin level

**Response:** Single admin object

---

### 3. Create Admin

**Endpoint:** `POST /api/v1/admin/admins`

**Request Body:**
```json
{
  "username": "newadmin",
  "email": "newadmin@example.com",
  "password": "SecureAdminPass123",
  "firstName": "New",
  "lastName": "Admin",
  "profilePicture": "https://example.com/admin.jpg",
  "level": 2,
  "permissions": ["content_moderation"],
  "isActive": true
}
```

**Validation:**
- `username`: 3-50 characters, required
- `email`: Valid email, max 255 characters, required
- `password`: Min 8 characters, required
- `level`: 1 (Admin) or 2 (Moderator), required
- `firstName`, `lastName`: Max 100 characters, optional
- `permissions`: Array of strings, optional

**Authorization Rules:**
- Super Admin can create Level 1 or 2
- Level 1 Admin can only create Level 2
- Level 0 (Super Admin) cannot be created via API

**Response:** Created admin object with status 201

---

### 4. Update Admin

**Endpoint:** `PUT /api/v1/admin/admins/{adminId}`

**Request Body:** (All fields optional)
```json
{
  "email": "updated@example.com",
  "firstName": "Updated",
  "lastName": "Name",
  "profilePicture": "https://example.com/new.jpg",
  "level": 2,
  "permissions": ["content_moderation", "user_support"],
  "isActive": true
}
```

**Authorization:** Must have permission to manage target admin

**Special Rules:**
- Cannot change to/from level 0
- Cannot change admin to a level you can't manage
- Level changes follow staircase authorization

**Response:** Updated admin object

---

### 5. Delete Admin (Soft Delete)

**Endpoint:** `DELETE /api/v1/admin/admins/{adminId}`

**Authorization:** Must have permission to delete target admin

**Restrictions:**
- Cannot delete yourself
- Cannot delete super admin (Level 0)
- Cannot delete an admin at your level or higher

**Response:**
```json
{
  "success": true,
  "message": "Admin deactivated successfully"
}
```

---

### 6. Activate Admin

**Endpoint:** `POST /api/v1/admin/admins/{adminId}/activate`

**Authorization:** Must have permission to manage target admin

**Response:** Updated admin object with `isActive: true`

---

### 7. Deactivate Admin

**Endpoint:** `POST /api/v1/admin/admins/{adminId}/deactivate`

**Authorization:** Must have permission to manage target admin

**Restrictions:**
- Cannot deactivate yourself
- Cannot deactivate super admin (Level 0)

**Response:** Updated admin object with `isActive: false`

---

### 8. Reset Admin Password

**Endpoint:** `POST /api/v1/admin/admins/{adminId}/reset-password`

**Request Body:**
```json
{
  "newPassword": "NewSecureAdminPass123"
}
```

**Authorization:** Must have permission to manage target admin

**Response:**
```json
{
  "success": true,
  "message": "Admin password reset successfully"
}
```

---

### 9. Unlock Admin Account

**Endpoint:** `POST /api/v1/admin/admins/{adminId}/unlock`

**Authorization:** Must have permission to manage target admin

**Response:** Updated admin object with `loginAttempts: 0` and `lockedUntil: null`

---

## Common Error Responses

### 400 Bad Request
```json
{
  "timestamp": "2024-10-09T10:30:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Username already exists",
  "path": "/api/v1/admin/users"
}
```

### 401 Unauthorized
```json
{
  "timestamp": "2024-10-09T10:30:00",
  "status": 401,
  "error": "Unauthorized",
  "message": "Admin level 1 cannot delete admin level 1. Insufficient permissions.",
  "path": "/api/v1/admin/admins/5"
}
```

### 404 Not Found
```json
{
  "timestamp": "2024-10-09T10:30:00",
  "status": 404,
  "error": "Not Found",
  "message": "User not found with ID: 999",
  "path": "/api/v1/admin/users/999"
}
```

---

## Status Codes

| Code | Meaning | When Used |
|------|---------|-----------|
| 200 | OK | Successful GET, PUT, POST operations |
| 201 | Created | Successful resource creation |
| 400 | Bad Request | Validation errors, business rule violations |
| 401 | Unauthorized | Missing/invalid token, insufficient permissions |
| 404 | Not Found | Resource not found |
| 500 | Internal Server Error | Unexpected server errors |

---

## Authorization Examples

### Example 1: Super Admin Creates Admin
```http
POST /api/v1/admin/admins
Authorization: Bearer <super_admin_token>
Content-Type: application/json

{
  "username": "newadmin",
  "email": "newadmin@example.com",
  "password": "SecurePass123",
  "level": 1,
  "permissions": ["user_management"]
}
```
✅ **SUCCESS** - Super admin can create Level 1

---

### Example 2: Level 1 Admin Tries to Create Level 1
```http
POST /api/v1/admin/admins
Authorization: Bearer <level1_admin_token>

{
  "level": 1
}
```
❌ **FAIL** - Level 1 cannot create Level 1
```json
{
  "status": 401,
  "message": "Admin level 1 cannot create admin level 1. Insufficient permissions."
}
```

---

### Example 3: Level 1 Admin Creates Moderator
```http
POST /api/v1/admin/admins
Authorization: Bearer <level1_admin_token>

{
  "level": 2
}
```
✅ **SUCCESS** - Level 1 can create Level 2

---

### Example 4: Admin Tries to Delete Super Admin
```http
DELETE /api/v1/admin/admins/1
Authorization: Bearer <level1_admin_token>
```
❌ **FAIL** - Cannot delete super admin
```json
{
  "status": 400,
  "message": "Cannot delete super admin account"
}
```

---

## Best Practices

1. **Always check permissions** before attempting admin operations
2. **Use pagination** for listing endpoints to avoid performance issues
3. **Validate user input** before submitting requests
4. **Handle 401 errors** gracefully in your UI
5. **Log admin actions** for audit trails (future feature)
6. **Use search** for finding specific users/admins
7. **Reset passwords** responsibly - inform users of the change
8. **Don't hard-code** admin levels - use constants/enums

---

## Rate Limiting

All admin endpoints are rate-limited:
- 100 requests per minute per admin
- Failed authorization attempts count towards limit
- Exceeding limit returns 429 Too Many Requests

---

## Audit Logging (Future Feature)

All admin actions should be logged for compliance:
- User/admin creation, modification, deletion
- Password resets
- Account activations/deactivations
- Permission changes
- Attempted unauthorized actions
