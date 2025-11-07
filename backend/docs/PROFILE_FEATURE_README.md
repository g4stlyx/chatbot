# Profile Management Feature

## Overview

This feature provides comprehensive profile management capabilities for both regular users and administrators. Users can view and update their profile information, change passwords, and manage account status. Admins have additional capabilities including viewing other admin profiles and managing admin account status with proper permission checks.

## Structure

### DTOs (`com.g4.chatbot.dto.profile`)

1. **UserProfileDTO** - User profile response model
   - Contains all user profile information
   - Includes account status and verification flags

2. **AdminProfileDTO** - Admin profile response model
   - Contains all admin profile information
   - Includes admin level, permissions, and hierarchy information

3. **UpdateUserProfileRequest** - User profile update request
   - Email, firstName, lastName, profilePicture
   - All fields optional with validation

4. **UpdateAdminProfileRequest** - Admin profile update request
   - Email, firstName, lastName, profilePicture
   - All fields optional with validation

5. **ChangePasswordRequest** - Password change request
   - Current password, new password, confirm password
   - Includes validation rules

### Services (`com.g4.chatbot.services`)

1. **UserProfileService**
   - `getUserProfile(Long userId)` - Get user profile
   - `updateUserProfile(Long userId, UpdateUserProfileRequest)` - Update profile
   - `changePassword(Long userId, ChangePasswordRequest)` - Change password
   - `deactivateAccount(Long userId)` - Deactivate account
   - `reactivateAccount(Long userId)` - Reactivate account

2. **AdminProfileService**
   - `getAdminProfile(Long adminId)` - Get admin profile
   - `updateAdminProfile(Long adminId, UpdateAdminProfileRequest)` - Update profile
   - `changePassword(Long adminId, ChangePasswordRequest)` - Change password
   - `deactivateAccount(Long adminId, Long requestingAdminId)` - Deactivate with permission check
   - `reactivateAccount(Long adminId, Long requestingAdminId)` - Reactivate with permission check

### Controllers (`com.g4.chatbot.controllers`)

1. **UserProfileController** (`/api/v1/user/profile`)
   - `GET /` - Get current user profile
   - `PUT /` - Update current user profile
   - `POST /change-password` - Change password
   - `POST /deactivate` - Deactivate account
   - `POST /reactivate` - Reactivate account

2. **AdminProfileController** (`/api/v1/admin/profile`)
   - `GET /` - Get current admin profile
   - `GET /{adminId}` - Get admin profile by ID
   - `PUT /` - Update current admin profile
   - `POST /change-password` - Change password
   - `POST /{adminId}/deactivate` - Deactivate admin account
   - `POST /{adminId}/reactivate` - Reactivate admin account

## Key Features

### User Profile Management

1. **View Profile**
   - Users can view their complete profile information
   - Includes account creation, last update, and last login timestamps

2. **Update Profile**
   - Update email, first name, last name, and profile picture
   - Email change resets email verification status
   - Validates email uniqueness across both user and admin tables

3. **Password Management**
   - Secure password change with current password verification
   - New password must be different from current password
   - Minimum 8 characters requirement
   - Password confirmation validation

4. **Account Status**
   - Self-service account deactivation
   - Self-service account reactivation
   - Account status is preserved (not deleted)

### Admin Profile Management

1. **View Profiles**
   - Admins can view their own profile
   - Higher-level admins can view other admin profiles
   - Includes admin-specific information (level, permissions)

2. **Update Profile**
   - Same capabilities as user profile update
   - Email change validated against both tables

3. **Password Management**
   - Identical to user password management
   - Separate endpoint for admins

4. **Account Status Management**
   - Permission-based account deactivation/reactivation
   - Hierarchy enforcement:
     - Level 0 (Super Admin) > Level 1 (Admin) > Level 2 (Moderator)
   - Super admin accounts cannot be deactivated
   - Only higher-level admins can manage lower-level admin accounts

## Security Features

1. **Authentication**
   - All endpoints require valid JWT tokens
   - User endpoints require user tokens
   - Admin endpoints require admin tokens

2. **Authorization**
   - Users can only manage their own profiles
   - Admin hierarchy enforced for admin management
   - Permission checks before status changes

3. **Password Security**
   - Passwords are hashed with unique salts
   - Current password verification required for changes
   - Password strength validation

4. **Data Validation**
   - Email format validation
   - Field length constraints
   - Duplicate email prevention

## Error Handling

All endpoints return proper HTTP status codes and error messages:

- `200 OK` - Successful operation
- `400 Bad Request` - Validation errors, duplicate emails, or business rule violations
- `401 Unauthorized` - Invalid or missing authentication token
- `404 Not Found` - Resource not found

Error responses follow consistent format:
```json
{
  "timestamp": "2024-10-07T09:30:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Email is already in use",
  "path": "/api/v1/user/profile"
}
```

## Documentation

- **API Documentation**: `docs/PROFILE_API.md`
  - Complete endpoint documentation
  - Request/response examples
  - Authentication requirements
  - Error responses

- **Postman Collection**: `postman_files/Profile_Management_API.postman_collection.json`
  - Ready-to-use API requests
  - Pre-configured variables
  - All endpoints included

## Usage Examples

### User Profile Update
```bash
curl -X PUT http://localhost:8080/api/v1/user/profile \
  -H "Authorization: Bearer <user_token>" \
  -H "Content-Type: application/json" \
  -d '{
    "firstName": "John",
    "lastName": "Doe",
    "email": "john.doe@example.com"
  }'
```

### Change Password
```bash
curl -X POST http://localhost:8080/api/v1/user/profile/change-password \
  -H "Authorization: Bearer <user_token>" \
  -H "Content-Type: application/json" \
  -d '{
    "currentPassword": "oldPass123",
    "newPassword": "newPass456",
    "confirmPassword": "newPass456"
  }'
```

### Admin Deactivate Another Admin
```bash
curl -X POST http://localhost:8080/api/v1/admin/profile/5/deactivate \
  -H "Authorization: Bearer <admin_token>"
```

## Testing

Use the provided Postman collection for testing:

1. Import `Profile_Management_API.postman_collection.json` into Postman
2. Set the `base_url` variable (default: http://localhost:8080)
3. Obtain tokens through auth endpoints and set `user_token` and `admin_token` variables
4. Test all endpoints with various scenarios

## Future Enhancements

Potential improvements for future iterations:

1. **Profile Picture Upload**
   - Direct file upload support
   - Image processing and storage
   - Thumbnail generation

2. **Email Verification**
   - Automatic email verification on email change
   - Verification email sending

3. **Admin Activity Logging**
   - Log profile changes
   - Track admin actions on other admin accounts

4. **Profile History**
   - Track profile change history
   - Audit trail for security

5. **Two-Factor Authentication**
   - Enable 2FA on profile
   - Manage backup codes

6. **API Rate Limiting**
   - Rate limit profile update operations
   - Prevent abuse

7. **Soft Delete**
   - Full account deletion with soft delete
   - Data retention policies

8. **Profile Privacy Settings**
   - Control profile visibility
   - Privacy preferences
