# Admin Panel Feature - Complete Guide

## Overview

The Admin Panel provides comprehensive user and admin management capabilities with a hierarchical authorization system. This feature enables administrators to manage platform users and other administrators based on their permission levels.

## Key Features

### 1. User Management
- **CRUD Operations**: Create, Read, Update, Delete users
- **Account Control**: Activate, deactivate, unlock accounts
- **Password Management**: Reset user passwords
- **Search & Filter**: Search users by username or email
- **Pagination**: Efficient data retrieval with pagination support

### 2. Admin Management
- **CRUD Operations**: Create, Read, Update, Delete admins
- **Hierarchical Control**: Staircase authorization model
- **Level Management**: Manage admins across three permission levels
- **Account Control**: Activate, deactivate, unlock admin accounts
- **Password Reset**: Administrative password reset capability

### 3. Staircase Authorization Model

The system implements a three-tier hierarchical authorization model:

```
Level 0 (Super Admin)
    ├── Can manage Level 1 Admins
    ├── Can manage Level 2 Moderators
    └── Cannot be deleted or deactivated

Level 1 (Admin)
    ├── Can only manage Level 2 Moderators
    └── Cannot manage Super Admins or other Level 1 Admins

Level 2 (Moderator)
    └── Cannot manage other administrators
```

#### Authorization Rules

1. **Super Admin (Level 0)**
   - Full control over Level 1 and Level 2 administrators
   - Can create admins at any level
   - Can modify and delete lower-level admins
   - Protected from deletion and deactivation

2. **Admin (Level 1)**
   - Can only manage Level 2 Moderators
   - Cannot create or modify Super Admins or other Level 1 Admins
   - Can create Level 2 Moderators only

3. **Moderator (Level 2)**
   - Cannot manage other administrators
   - Limited to user management operations
   - Cannot access admin management endpoints

4. **Self-Management Restrictions**
   - Administrators cannot deactivate themselves
   - Administrators cannot delete themselves
   - Administrators cannot reset their own passwords via admin endpoints

## Architecture

### Component Structure

```
Controllers
    ├── UserManagementController (User CRUD endpoints)
    └── AdminManagementController (Admin CRUD endpoints)

Services
    ├── UserManagementService (User business logic)
    └── AdminManagementService (Admin business logic + permission validation)

Repositories
    ├── UserRepository (User data access)
    └── AdminRepository (Admin data access)

DTOs
    ├── User DTOs (UserManagementDTO, CreateUserRequest, UpdateUserRequest, etc.)
    └── Admin DTOs (AdminManagementDTO, CreateAdminRequest, UpdateAdminRequest, etc.)
```

### Data Flow

```
Request → Controller (JWT Auth) → Service (Permission Validation) → Repository → Database
                                       ↓
                                  Business Logic
                                       ↓
                                  Response DTO
```

## API Endpoints

### User Management Endpoints

| Method | Endpoint | Description | Authorization |
|--------|----------|-------------|---------------|
| GET | `/api/v1/admin/users` | List all users (paginated) | Any Admin |
| GET | `/api/v1/admin/users/search` | Search users | Any Admin |
| GET | `/api/v1/admin/users/{id}` | Get user details | Any Admin |
| POST | `/api/v1/admin/users` | Create new user | Any Admin |
| PUT | `/api/v1/admin/users/{id}` | Update user | Any Admin |
| DELETE | `/api/v1/admin/users/{id}` | Delete user (soft) | Any Admin |
| POST | `/api/v1/admin/users/{id}/activate` | Activate user | Any Admin |
| POST | `/api/v1/admin/users/{id}/deactivate` | Deactivate user | Any Admin |
| POST | `/api/v1/admin/users/{id}/reset-password` | Reset password | Any Admin |
| POST | `/api/v1/admin/users/{id}/unlock` | Unlock account | Any Admin |

### Admin Management Endpoints

| Method | Endpoint | Description | Authorization |
|--------|----------|-------------|---------------|
| GET | `/api/v1/admin/admins` | List admins (filtered by permission) | Any Admin |
| GET | `/api/v1/admin/admins/{id}` | Get admin details | Permission-based |
| POST | `/api/v1/admin/admins` | Create new admin | Permission-based |
| PUT | `/api/v1/admin/admins/{id}` | Update admin | Permission-based |
| DELETE | `/api/v1/admin/admins/{id}` | Delete admin (soft) | Permission-based |
| POST | `/api/v1/admin/admins/{id}/activate` | Activate admin | Permission-based |
| POST | `/api/v1/admin/admins/{id}/deactivate` | Deactivate admin | Permission-based |
| POST | `/api/v1/admin/admins/{id}/reset-password` | Reset password | Permission-based |
| POST | `/api/v1/admin/admins/{id}/unlock` | Unlock account | Permission-based |

## Permission Validation

The `AdminManagementService` implements comprehensive permission validation:

```java
private void validateAdminPermission(Admin currentAdmin, Admin targetAdmin, String action) {
    // Super Admin protection
    if (targetAdmin.getLevel() == 0) {
        throw new UnauthorizedException("Cannot " + action + " super admin");
    }
    
    // Self-management protection
    if (currentAdmin.getId().equals(targetAdmin.getId())) {
        throw new UnauthorizedException("Cannot " + action + " yourself");
    }
    
    // Permission hierarchy validation
    if (currentAdmin.getLevel() >= targetAdmin.getLevel()) {
        throw new UnauthorizedException("Insufficient permissions");
    }
}
```

## Security Features

### 1. Authentication
- JWT-based authentication required for all endpoints
- Token validation via Spring Security filters
- Admin role verification

### 2. Authorization
- Hierarchical permission model
- Operation-level permission checks
- Self-management restrictions

### 3. Data Protection
- Soft delete implementation (preserve data integrity)
- Password hashing with Argon2id
- Sensitive data exclusion in responses

### 4. Audit Trail
- Comprehensive logging of all admin actions
- User creation, modification, and deletion tracking
- Failed authorization attempt logging

## Data Models

### UserManagementDTO
```json
{
  "id": 1,
  "username": "johndoe",
  "email": "john@example.com",
  "firstName": "John",
  "lastName": "Doe",
  "profilePicture": "https://example.com/pic.jpg",
  "isActive": true,
  "isDeleted": false,
  "emailVerified": true,
  "accountLocked": false,
  "failedLoginAttempts": 0,
  "createdAt": "2024-01-15T10:30:00",
  "updatedAt": "2024-01-15T10:30:00"
}
```

### AdminManagementDTO
```json
{
  "id": 1,
  "username": "admin01",
  "email": "admin@example.com",
  "firstName": "Admin",
  "lastName": "User",
  "level": 1,
  "permissions": ["user_management", "content_moderation"],
  "isActive": true,
  "isDeleted": false,
  "accountLocked": false,
  "createdAt": "2024-01-15T10:30:00",
  "updatedAt": "2024-01-15T10:30:00"
}
```

## Usage Examples

### Example 1: Super Admin Creates Level 1 Admin

```bash
curl -X POST http://localhost:8080/api/v1/admin/admins \
  -H "Authorization: Bearer SUPER_ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin01",
    "email": "admin01@example.com",
    "password": "SecurePass123",
    "firstName": "John",
    "lastName": "Admin",
    "level": 1,
    "permissions": ["user_management", "content_moderation"],
    "isActive": true
  }'
```

**Result**: ✅ Success - Super Admin can create Level 1 Admin

### Example 2: Level 1 Admin Attempts to Create Another Level 1 Admin

```bash
curl -X POST http://localhost:8080/api/v1/admin/admins \
  -H "Authorization: Bearer LEVEL_1_ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin02",
    "level": 1,
    ...
  }'
```

**Result**: ❌ Error 403 - Insufficient permissions

### Example 3: Level 1 Admin Creates Level 2 Moderator

```bash
curl -X POST http://localhost:8080/api/v1/admin/admins \
  -H "Authorization: Bearer LEVEL_1_ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "moderator01",
    "level": 2,
    ...
  }'
```

**Result**: ✅ Success - Level 1 Admin can create Level 2 Moderator

### Example 4: Search Users by Username or Email

```bash
curl -X GET "http://localhost:8080/api/v1/admin/users/search?q=john&page=0&size=20" \
  -H "Authorization: Bearer ADMIN_TOKEN"
```

### Example 5: Reset User Password

```bash
curl -X POST http://localhost:8080/api/v1/admin/users/123/reset-password \
  -H "Authorization: Bearer ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "newPassword": "NewSecurePass123"
  }'
```

## Error Handling

### Common Error Scenarios

1. **Insufficient Permissions (403)**
   - Attempting to manage higher-level admins
   - Attempting unauthorized operations

2. **User Not Found (404)**
   - Invalid user ID
   - Deleted user access attempt

3. **Validation Errors (400)**
   - Invalid email format
   - Weak password
   - Missing required fields

4. **Unauthorized Access (401)**
   - Invalid or expired JWT token
   - Missing authentication

5. **Business Logic Errors (400)**
   - Self-management attempt
   - Super admin deletion attempt
   - Duplicate username/email

## Best Practices

### 1. Permission Management
- Always use the lowest permission level necessary
- Regularly audit admin permissions
- Review Level 0 admin assignments carefully

### 2. User Management
- Use soft delete to preserve data integrity
- Implement proper password reset workflows
- Monitor failed login attempts

### 3. Security
- Rotate admin passwords regularly
- Use strong password policies
- Monitor unauthorized access attempts
- Review audit logs regularly

### 4. Data Management
- Use pagination for large datasets
- Implement proper search indexing
- Regular database backups
- Monitor account statuses

## Testing

### Unit Testing
- Service layer permission validation
- Business logic verification
- Edge case handling

### Integration Testing
- End-to-end endpoint testing
- Authorization flow verification
- Database transaction testing

### Security Testing
- Permission bypass attempts
- JWT token validation
- SQL injection prevention
- XSS attack prevention

## Performance Considerations

1. **Pagination**: All list endpoints support pagination to handle large datasets efficiently
2. **Indexing**: Database indexes on username, email, and level fields
3. **Caching**: Consider implementing Redis caching for frequently accessed data
4. **Query Optimization**: Efficient JPA queries with proper fetch strategies

## Deployment Checklist

- [ ] Database migrations applied
- [ ] Environment variables configured
- [ ] Super admin account created
- [ ] JWT secret configured
- [ ] CORS settings verified
- [ ] Logging configured
- [ ] Rate limiting enabled
- [ ] SSL/TLS configured
- [ ] Backup strategy implemented
- [ ] Monitoring setup complete

## Troubleshooting

### Issue: "Insufficient permissions" Error

**Cause**: Attempting to manage an admin at the same or higher level

**Solution**: 
- Verify your admin level
- Ensure you're managing lower-level admins only
- Contact a Super Admin for higher-level operations

### Issue: Cannot Delete Super Admin

**Cause**: System protection prevents super admin deletion

**Solution**: This is by design. Super admins cannot be deleted to prevent system lockout.

### Issue: Self-Management Restriction

**Cause**: Attempting to deactivate/delete your own account

**Solution**: Have another authorized admin perform the operation.

## Future Enhancements

1. **Role-Based Permissions**: More granular permission system beyond levels
2. **Audit Dashboard**: Visual audit trail and analytics
3. **Bulk Operations**: Batch user/admin management
4. **Advanced Search**: Full-text search with filters
5. **Activity Monitoring**: Real-time admin activity tracking
6. **Two-Factor Authentication**: Additional security layer for admin accounts
7. **IP Whitelisting**: Restrict admin access by IP address
8. **Session Management**: Active session tracking and termination

## Support

For issues or questions:
- Check the API documentation: `ADMIN_PANEL_API.md`
- Review the quick reference: `ADMIN_PANEL_QUICK_REFERENCE.md`
- Check implementation details: `IMPLEMENTATION_SUMMARY_ADMIN_PANEL.md`
- Review audit logs for permission issues

## Version History

- **v1.0.0**: Initial release with hierarchical admin management and user CRUD
