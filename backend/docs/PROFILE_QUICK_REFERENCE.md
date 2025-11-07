# Profile Management Quick Reference

## Endpoints Summary

### User Profile Endpoints
| Method | Endpoint | Description | Auth |
|--------|----------|-------------|------|
| GET | `/api/v1/user/profile` | Get current user profile | User Token |
| PUT | `/api/v1/user/profile` | Update current user profile | User Token |
| POST | `/api/v1/user/profile/change-password` | Change user password | User Token |
| POST | `/api/v1/user/profile/deactivate` | Deactivate user account | User Token |
| POST | `/api/v1/user/profile/reactivate` | Reactivate user account | User Token |

### Admin Profile Endpoints
| Method | Endpoint | Description | Auth |
|--------|----------|-------------|------|
| GET | `/api/v1/admin/profile` | Get current admin profile | Admin Token |
| GET | `/api/v1/admin/profile/{adminId}` | Get admin profile by ID | Admin Token |
| PUT | `/api/v1/admin/profile` | Update current admin profile | Admin Token |
| POST | `/api/v1/admin/profile/change-password` | Change admin password | Admin Token |
| POST | `/api/v1/admin/profile/{adminId}/deactivate` | Deactivate admin account | Admin Token (Super/Higher Level) |
| POST | `/api/v1/admin/profile/{adminId}/reactivate` | Reactivate admin account | Admin Token (Super/Higher Level) |

## Request/Response Examples

### Get Profile (User)
**Request:**
```http
GET /api/v1/user/profile
Authorization: Bearer <user_token>
```

**Response:**
```json
{
  "id": 1,
  "username": "johndoe",
  "email": "john@example.com",
  "firstName": "John",
  "lastName": "Doe",
  "profilePicture": "https://example.com/pic.jpg",
  "isActive": true,
  "emailVerified": true,
  "createdAt": "2024-01-15T10:30:00",
  "updatedAt": "2024-03-20T14:45:00",
  "lastLoginAt": "2024-10-07T08:15:00"
}
```

### Update Profile (User)
**Request:**
```http
PUT /api/v1/user/profile
Authorization: Bearer <user_token>
Content-Type: application/json

{
  "email": "newemail@example.com",
  "firstName": "John",
  "lastName": "Doe Updated"
}
```

**Response:**
```json
{
  "id": 1,
  "username": "johndoe",
  "email": "newemail@example.com",
  "firstName": "John",
  "lastName": "Doe Updated",
  "emailVerified": false,
  ...
}
```

### Change Password
**Request:**
```http
POST /api/v1/user/profile/change-password
Authorization: Bearer <user_token>
Content-Type: application/json

{
  "currentPassword": "oldPass123",
  "newPassword": "newPass456",
  "confirmPassword": "newPass456"
}
```

**Response:**
```json
{
  "success": true,
  "message": "Password changed successfully"
}
```

### Get Admin Profile
**Request:**
```http
GET /api/v1/admin/profile
Authorization: Bearer <admin_token>
```

**Response:**
```json
{
  "id": 1,
  "username": "admin_user",
  "email": "admin@example.com",
  "firstName": "Admin",
  "lastName": "User",
  "profilePicture": "https://example.com/admin.jpg",
  "level": 0,
  "permissions": ["user_management", "content_moderation"],
  "isActive": true,
  "createdBy": null,
  "createdAt": "2024-01-01T00:00:00",
  "updatedAt": "2024-03-20T14:45:00",
  "lastLoginAt": "2024-10-07T08:15:00"
}
```

## Validation Rules

### Email
- Must be valid email format
- Maximum 255 characters
- Must be unique across users and admins

### First Name & Last Name
- Maximum 100 characters each
- Optional fields

### Profile Picture
- Maximum 500 characters
- URL format expected

### Password
- Minimum 8 characters
- Must be different from current password
- New password and confirm password must match

## Admin Levels

| Level | Role | Permissions |
|-------|------|-------------|
| 0 | Super Admin | Full access, can manage all admins |
| 1 | Admin | Can manage moderators |
| 2 | Moderator | Limited admin access |

**Hierarchy Rule:** Lower level number = Higher privileges

## Business Rules

### Profile Updates
1. ✅ All profile fields are optional in update requests
2. ✅ Email change resets `emailVerified` to false (for users)
3. ✅ Email uniqueness checked across both users and admins tables
4. ✅ Username cannot be changed
5. ✅ UpdatedAt timestamp automatically updated

### Password Changes
1. ✅ Current password must be verified
2. ✅ New password must be different from current
3. ✅ New password and confirm password must match
4. ✅ Minimum 8 characters required
5. ✅ New salt generated on password change

### Account Deactivation
1. ✅ Users can deactivate/reactivate their own accounts
2. ✅ Admins require proper hierarchy level to deactivate other admins
3. ✅ Super admin (level 0) accounts cannot be deactivated
4. ✅ Deactivation is reversible (not deletion)

### Admin Management
1. ✅ Only super admins or higher-level admins can manage lower-level admins
2. ✅ Cannot deactivate an admin with equal or higher level
3. ✅ Super admins can manage all admin accounts

## Error Responses

### Common Errors

**Email Already In Use**
```json
{
  "timestamp": "2024-10-07T09:30:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Email is already in use",
  "path": "/api/v1/user/profile"
}
```

**Incorrect Current Password**
```json
{
  "timestamp": "2024-10-07T09:30:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Current password is incorrect",
  "path": "/api/v1/user/profile/change-password"
}
```

**Password Mismatch**
```json
{
  "timestamp": "2024-10-07T09:30:00",
  "status": 400,
  "error": "Bad Request",
  "message": "New password and confirm password do not match",
  "path": "/api/v1/user/profile/change-password"
}
```

**Insufficient Permissions**
```json
{
  "timestamp": "2024-10-07T09:30:00",
  "status": 400,
  "error": "Bad Request",
  "message": "You don't have permission to deactivate this admin account",
  "path": "/api/v1/admin/profile/5/deactivate"
}
```

**User Not Found**
```json
{
  "timestamp": "2024-10-07T09:30:00",
  "status": 404,
  "error": "Not Found",
  "message": "User not found with ID: 123",
  "path": "/api/v1/user/profile"
}
```

**Unauthorized**
```json
{
  "timestamp": "2024-10-07T09:30:00",
  "status": 401,
  "error": "Unauthorized",
  "message": "Invalid or missing authentication token",
  "path": "/api/v1/user/profile"
}
```

## Status Codes

| Code | Meaning | Common Scenarios |
|------|---------|------------------|
| 200 | OK | Successful GET, PUT, POST operations |
| 400 | Bad Request | Validation errors, duplicate email, business rule violations |
| 401 | Unauthorized | Missing/invalid token, expired token |
| 404 | Not Found | User/Admin not found |
| 500 | Internal Server Error | Unexpected server errors |

## Code Structure

### Files Created

**DTOs:**
- `UserProfileDTO.java` - User profile response
- `AdminProfileDTO.java` - Admin profile response
- `UpdateUserProfileRequest.java` - User update request
- `UpdateAdminProfileRequest.java` - Admin update request
- `ChangePasswordRequest.java` - Password change request

**Services:**
- `UserProfileService.java` - User profile business logic
- `AdminProfileService.java` - Admin profile business logic

**Controllers:**
- `UserProfileController.java` - User profile endpoints
- `AdminProfileController.java` - Admin profile endpoints

**Documentation:**
- `PROFILE_API.md` - Complete API documentation
- `PROFILE_FEATURE_README.md` - Feature overview
- `PROFILE_QUICK_REFERENCE.md` - This file

**Postman:**
- `Profile_Management_API.postman_collection.json` - API test collection

## Testing Checklist

### User Profile Tests
- [ ] Get user profile successfully
- [ ] Update email (verify emailVerified reset)
- [ ] Update first name and last name
- [ ] Update profile picture
- [ ] Change password successfully
- [ ] Change password with wrong current password
- [ ] Change password with mismatched confirm password
- [ ] Deactivate account
- [ ] Reactivate account
- [ ] Try to use duplicate email
- [ ] Validate field length limits

### Admin Profile Tests
- [ ] Get current admin profile
- [ ] Get other admin profile by ID
- [ ] Update admin profile
- [ ] Change admin password
- [ ] Super admin deactivates lower-level admin
- [ ] Admin tries to deactivate higher-level admin (should fail)
- [ ] Admin tries to deactivate super admin (should fail)
- [ ] Reactivate admin account
- [ ] Verify admin hierarchy enforcement

## Integration Points

### Existing Features
- Uses `UserRepository` and `AdminRepository` for data access
- Uses `PasswordService` for password hashing
- Uses JWT authentication from `JwtUtils`
- Uses exception handling from `GlobalExceptionHandler`

### Database Tables
- `users` - User profiles
- `admins` - Admin profiles
- No new tables required

### Security
- JWT authentication required for all endpoints
- User ID extracted from authentication token
- Admin level checked for hierarchy enforcement

## Quick Tips

1. **Always validate tokens** - All endpoints require valid JWT tokens
2. **Check admin levels** - Hierarchy matters for admin management
3. **Email uniqueness** - Checked across both users and admins tables
4. **Optional fields** - All update fields are optional, only send what changes
5. **Password security** - Current password always verified before change
6. **Error handling** - Use try-catch blocks, proper error messages returned
7. **Logging** - All operations logged with user/admin IDs
8. **Transactions** - @Transactional used for data consistency

## Common Use Cases

### User Changes Email
1. User sends PUT request with new email
2. System validates email uniqueness
3. Email updated, emailVerified reset to false
4. New verification email should be sent (if implemented)

### Admin Deactivates Moderator
1. Admin (level 1) sends deactivate request for moderator (level 2)
2. System checks hierarchy: 1 < 2, permission granted
3. Moderator account set to inactive
4. Action logged (if activity logging implemented)

### User Changes Password
1. User sends password change request
2. System verifies current password
3. System validates new password requirements
4. New salt generated, password hashed
5. User and salt updated in database
