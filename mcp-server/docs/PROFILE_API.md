# Profile API Documentation

This document describes the profile management endpoints for both Users and Admins.

## User Profile Endpoints

Base URL: `/api/v1/user/profile`

### 1. Get User Profile

Retrieve the current authenticated user's profile information.

**Endpoint:** `GET /api/v1/user/profile`

**Authentication:** Required (User JWT Token)

**Response:**
```json
{
  "id": 1,
  "username": "johndoe",
  "email": "john.doe@example.com",
  "firstName": "John",
  "lastName": "Doe",
  "profilePicture": "https://example.com/profile.jpg",
  "isActive": true,
  "emailVerified": true,
  "createdAt": "2024-01-15T10:30:00",
  "updatedAt": "2024-03-20T14:45:00",
  "lastLoginAt": "2024-10-07T08:15:00"
}
```

**Status Codes:**
- `200 OK` - Profile retrieved successfully
- `401 Unauthorized` - Invalid or missing authentication token
- `404 Not Found` - User not found

---

### 2. Update User Profile

Update the current authenticated user's profile information.

**Endpoint:** `PUT /api/v1/user/profile`

**Authentication:** Required (User JWT Token)

**Request Body:**
```json
{
  "email": "newemail@example.com",
  "firstName": "John",
  "lastName": "Doe",
  "profilePicture": "https://example.com/new-profile.jpg"
}
```

**Note:** All fields are optional. Only provide the fields you want to update.

**Response:**
```json
{
  "id": 1,
  "username": "johndoe",
  "email": "newemail@example.com",
  "firstName": "John",
  "lastName": "Doe",
  "profilePicture": "https://example.com/new-profile.jpg",
  "isActive": true,
  "emailVerified": false,
  "createdAt": "2024-01-15T10:30:00",
  "updatedAt": "2024-10-07T09:20:00",
  "lastLoginAt": "2024-10-07T08:15:00"
}
```

**Important Notes:**
- If email is changed, `emailVerified` will be reset to `false`
- Maximum field lengths:
  - email: 255 characters
  - firstName: 100 characters
  - lastName: 100 characters
  - profilePicture: 500 characters

**Status Codes:**
- `200 OK` - Profile updated successfully
- `400 Bad Request` - Invalid data or email already in use
- `401 Unauthorized` - Invalid or missing authentication token
- `404 Not Found` - User not found

---

### 3. Change Password

Change the current authenticated user's password.

**Endpoint:** `POST /api/v1/user/profile/change-password`

**Authentication:** Required (User JWT Token)

**Request Body:**
```json
{
  "currentPassword": "oldPassword123",
  "newPassword": "newPassword456",
  "confirmPassword": "newPassword456"
}
```

**Validation Rules:**
- `currentPassword` is required and must match the existing password
- `newPassword` is required and must be at least 8 characters
- `confirmPassword` must match `newPassword`
- `newPassword` must be different from `currentPassword`

**Response:**
```json
{
  "success": true,
  "message": "Password changed successfully"
}
```

**Status Codes:**
- `200 OK` - Password changed successfully
- `400 Bad Request` - Validation error or incorrect current password
- `401 Unauthorized` - Invalid or missing authentication token

---

### 4. Deactivate Account

Deactivate the current authenticated user's account.

**Endpoint:** `POST /api/v1/user/profile/deactivate`

**Authentication:** Required (User JWT Token)

**Response:**
```json
{
  "success": true,
  "message": "Account deactivated successfully"
}
```

**Status Codes:**
- `200 OK` - Account deactivated successfully
- `401 Unauthorized` - Invalid or missing authentication token

---

### 5. Reactivate Account

Reactivate the current authenticated user's account.

**Endpoint:** `POST /api/v1/user/profile/reactivate`

**Authentication:** Required (User JWT Token)

**Response:**
```json
{
  "success": true,
  "message": "Account reactivated successfully"
}
```

**Status Codes:**
- `200 OK` - Account reactivated successfully
- `401 Unauthorized` - Invalid or missing authentication token

---

## Admin Profile Endpoints

Base URL: `/api/v1/admin/profile`

### 1. Get Current Admin Profile

Retrieve the current authenticated admin's profile information.

**Endpoint:** `GET /api/v1/admin/profile`

**Authentication:** Required (Admin JWT Token)

**Response:**
```json
{
  "id": 1,
  "username": "admin_user",
  "email": "admin@example.com",
  "firstName": "Admin",
  "lastName": "User",
  "profilePicture": "https://example.com/admin-profile.jpg",
  "level": 0,
  "permissions": ["user_management", "content_moderation", "system_settings"],
  "isActive": true,
  "createdBy": null,
  "createdAt": "2024-01-01T00:00:00",
  "updatedAt": "2024-03-20T14:45:00",
  "lastLoginAt": "2024-10-07T08:15:00"
}
```

**Admin Levels:**
- `0` - Super Admin
- `1` - Admin
- `2` - Moderator

**Status Codes:**
- `200 OK` - Profile retrieved successfully
- `401 Unauthorized` - Invalid or missing authentication token
- `404 Not Found` - Admin not found

---

### 2. Get Admin Profile by ID

Retrieve any admin's profile information by their ID (for super admins and higher-level admins).

**Endpoint:** `GET /api/v1/admin/profile/{adminId}`

**Authentication:** Required (Admin JWT Token)

**Path Parameters:**
- `adminId` - The ID of the admin whose profile to retrieve

**Response:**
```json
{
  "id": 5,
  "username": "moderator_user",
  "email": "mod@example.com",
  "firstName": "Mod",
  "lastName": "User",
  "profilePicture": "https://example.com/mod-profile.jpg",
  "level": 2,
  "permissions": ["content_moderation"],
  "isActive": true,
  "createdBy": 1,
  "createdAt": "2024-02-15T10:30:00",
  "updatedAt": "2024-03-20T14:45:00",
  "lastLoginAt": "2024-10-07T08:15:00"
}
```

**Status Codes:**
- `200 OK` - Profile retrieved successfully
- `401 Unauthorized` - Invalid or missing authentication token
- `404 Not Found` - Admin not found

---

### 3. Update Admin Profile

Update the current authenticated admin's profile information.

**Endpoint:** `PUT /api/v1/admin/profile`

**Authentication:** Required (Admin JWT Token)

**Request Body:**
```json
{
  "email": "newemail@example.com",
  "firstName": "Admin",
  "lastName": "User",
  "profilePicture": "https://example.com/new-admin-profile.jpg"
}
```

**Note:** All fields are optional. Only provide the fields you want to update.

**Response:**
```json
{
  "id": 1,
  "username": "admin_user",
  "email": "newemail@example.com",
  "firstName": "Admin",
  "lastName": "User",
  "profilePicture": "https://example.com/new-admin-profile.jpg",
  "level": 0,
  "permissions": ["user_management", "content_moderation", "system_settings"],
  "isActive": true,
  "createdBy": null,
  "createdAt": "2024-01-01T00:00:00",
  "updatedAt": "2024-10-07T09:20:00",
  "lastLoginAt": "2024-10-07T08:15:00"
}
```

**Important Notes:**
- Maximum field lengths:
  - email: 255 characters
  - firstName: 100 characters
  - lastName: 100 characters
  - profilePicture: 500 characters

**Status Codes:**
- `200 OK` - Profile updated successfully
- `400 Bad Request` - Invalid data or email already in use
- `401 Unauthorized` - Invalid or missing authentication token
- `404 Not Found` - Admin not found

---

### 4. Change Admin Password

Change the current authenticated admin's password.

**Endpoint:** `POST /api/v1/admin/profile/change-password`

**Authentication:** Required (Admin JWT Token)

**Request Body:**
```json
{
  "currentPassword": "oldAdminPass123",
  "newPassword": "newAdminPass456",
  "confirmPassword": "newAdminPass456"
}
```

**Validation Rules:**
- `currentPassword` is required and must match the existing password
- `newPassword` is required and must be at least 8 characters
- `confirmPassword` must match `newPassword`
- `newPassword` must be different from `currentPassword`

**Response:**
```json
{
  "success": true,
  "message": "Password changed successfully"
}
```

**Status Codes:**
- `200 OK` - Password changed successfully
- `400 Bad Request` - Validation error or incorrect current password
- `401 Unauthorized` - Invalid or missing authentication token

---

### 5. Deactivate Admin Account

Deactivate an admin account. Only super admins or higher-level admins can deactivate other admin accounts.

**Endpoint:** `POST /api/v1/admin/profile/{adminId}/deactivate`

**Authentication:** Required (Admin JWT Token)

**Path Parameters:**
- `adminId` - The ID of the admin to deactivate

**Authorization Rules:**
- Requesting admin must be a super admin (level 0) or have a higher level than the target admin
- Super admin accounts (level 0) cannot be deactivated

**Response:**
```json
{
  "success": true,
  "message": "Admin account deactivated successfully"
}
```

**Status Codes:**
- `200 OK` - Account deactivated successfully
- `400 Bad Request` - Insufficient permissions or attempting to deactivate super admin
- `401 Unauthorized` - Invalid or missing authentication token
- `404 Not Found` - Admin not found

---

### 6. Reactivate Admin Account

Reactivate an admin account. Only super admins or higher-level admins can reactivate other admin accounts.

**Endpoint:** `POST /api/v1/admin/profile/{adminId}/reactivate`

**Authentication:** Required (Admin JWT Token)

**Path Parameters:**
- `adminId` - The ID of the admin to reactivate

**Authorization Rules:**
- Requesting admin must be a super admin (level 0) or have a higher level than the target admin

**Response:**
```json
{
  "success": true,
  "message": "Admin account reactivated successfully"
}
```

**Status Codes:**
- `200 OK` - Account reactivated successfully
- `400 Bad Request` - Insufficient permissions
- `401 Unauthorized` - Invalid or missing authentication token
- `404 Not Found` - Admin not found

---

## Common Error Responses

### 400 Bad Request
```json
{
  "timestamp": "2024-10-07T09:30:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Email is already in use",
  "path": "/api/v1/user/profile"
}
```

### 401 Unauthorized
```json
{
  "timestamp": "2024-10-07T09:30:00",
  "status": 401,
  "error": "Unauthorized",
  "message": "Invalid or missing authentication token",
  "path": "/api/v1/user/profile"
}
```

### 404 Not Found
```json
{
  "timestamp": "2024-10-07T09:30:00",
  "status": 404,
  "error": "Not Found",
  "message": "User not found with ID: 123",
  "path": "/api/v1/user/profile"
}
```

---

## Authentication

All profile endpoints require JWT authentication. Include the JWT token in the Authorization header:

```
Authorization: Bearer <your-jwt-token>
```

The token should be obtained through the authentication endpoints (`/api/v1/auth/login` or `/api/v1/auth/register`).

---

## Notes

1. **User vs Admin Tokens**: Make sure to use the correct token type (user or admin) for the corresponding endpoints.

2. **Email Verification**: When a user changes their email, the `emailVerified` flag is reset to `false`, and a new verification email should be sent (if implemented).

3. **Admin Hierarchy**: Admin levels follow a hierarchy where lower numbers have more privileges:
   - Level 0 (Super Admin) > Level 1 (Admin) > Level 2 (Moderator)

4. **Account Deactivation**: Deactivated accounts can still be reactivated. This is different from account deletion.

5. **Password Security**: Passwords are hashed with salt and stored securely. Plain text passwords are never stored.
