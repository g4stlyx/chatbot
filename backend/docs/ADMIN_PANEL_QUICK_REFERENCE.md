# Admin Panel Quick Reference

## Quick Links
- **Full API Documentation**: [ADMIN_PANEL_API.md](ADMIN_PANEL_API.md)
- **Feature Overview**: [ADMIN_PANEL_FEATURE_README.md](ADMIN_PANEL_FEATURE_README.md)
- **Postman Collection**: [Admin_Panel_API.postman_collection.json](../postman_files/Admin_Panel_API.postman_collection.json)

## Authorization Matrix

| Operation | Super Admin (L0) | Admin (L1) | Moderator (L2) |
|-----------|:----------------:|:----------:|:--------------:|
| Manage Users | ✅ | ✅ | ✅ |
| Create L0 Admin | ✅ | ❌ | ❌ |
| Create L1 Admin | ✅ | ❌ | ❌ |
| Create L2 Admin | ✅ | ✅ | ❌ |
| Modify L0 Admin | ❌ | ❌ | ❌ |
| Modify L1 Admin | ✅ | ❌ | ❌ |
| Modify L2 Admin | ✅ | ✅ | ❌ |
| Delete L0 Admin | ❌ | ❌ | ❌ |
| Delete L1 Admin | ✅ | ❌ | ❌ |
| Delete L2 Admin | ✅ | ✅ | ❌ |
| View All Admins | ✅ (All) | ✅ (L1+L2) | ✅ (L2 only) |

## Endpoint Summary

### User Management (All Admins)

```
GET    /api/v1/admin/users                    # List users (paginated)
GET    /api/v1/admin/users/search?q={term}    # Search users
GET    /api/v1/admin/users/{id}               # Get user by ID
POST   /api/v1/admin/users                    # Create user
PUT    /api/v1/admin/users/{id}               # Update user
DELETE /api/v1/admin/users/{id}               # Delete user (soft)
POST   /api/v1/admin/users/{id}/activate      # Activate user
POST   /api/v1/admin/users/{id}/deactivate    # Deactivate user
POST   /api/v1/admin/users/{id}/reset-password # Reset password
POST   /api/v1/admin/users/{id}/unlock        # Unlock account
```

### Admin Management (Permission-Based)

```
GET    /api/v1/admin/admins                   # List admins (filtered)
GET    /api/v1/admin/admins/{id}              # Get admin by ID
POST   /api/v1/admin/admins                   # Create admin
PUT    /api/v1/admin/admins/{id}              # Update admin
DELETE /api/v1/admin/admins/{id}              # Delete admin (soft)
POST   /api/v1/admin/admins/{id}/activate     # Activate admin
POST   /api/v1/admin/admins/{id}/deactivate   # Deactivate admin
POST   /api/v1/admin/admins/{id}/reset-password # Reset password
POST   /api/v1/admin/admins/{id}/unlock       # Unlock account
```

## Common Request Bodies

### Create User
```json
{
  "username": "newuser",
  "email": "user@example.com",
  "password": "SecurePass123",
  "firstName": "John",
  "lastName": "Doe",
  "profilePicture": "https://example.com/pic.jpg",
  "isActive": true,
  "emailVerified": false
}
```

### Update User
```json
{
  "email": "newemail@example.com",
  "firstName": "Jane",
  "lastName": "Smith",
  "isActive": false,
  "emailVerified": true
}
```

### Create Admin
```json
{
  "username": "newadmin",
  "email": "admin@example.com",
  "password": "SecurePass123",
  "firstName": "Admin",
  "lastName": "User",
  "level": 2,
  "permissions": ["content_moderation"],
  "isActive": true
}
```

### Update Admin
```json
{
  "email": "newemail@example.com",
  "firstName": "Updated",
  "permissions": ["user_management", "content_moderation"],
  "isActive": true
}
```

### Reset Password
```json
{
  "newPassword": "NewSecurePass123"
}
```

## Query Parameters

### Pagination (All List Endpoints)
```
?page=0                # Page number (0-indexed)
&size=10              # Items per page
&sortBy=createdAt     # Sort field
&sortDirection=desc   # Sort direction (asc/desc)
```

### Search Users
```
?q=john               # Search term (username or email)
&page=0              # Page number
&size=20             # Items per page
```

## Response Codes

| Code | Meaning | Common Causes |
|------|---------|---------------|
| 200 | OK | Successful operation |
| 201 | Created | Resource created successfully |
| 400 | Bad Request | Validation error, invalid data |
| 401 | Unauthorized | Invalid/missing JWT token |
| 403 | Forbidden | Insufficient permissions |
| 404 | Not Found | Resource doesn't exist |
| 409 | Conflict | Duplicate username/email |
| 500 | Server Error | Internal server error |

## Error Response Format

```json
{
  "timestamp": "2024-01-15T10:30:00",
  "status": 403,
  "error": "Forbidden",
  "message": "Insufficient permissions to perform this action",
  "path": "/api/v1/admin/admins/5"
}
```

## Permission Rules (Quick)

### Staircase Model
- **L0** manages **L1** and **L2**
- **L1** manages **L2** only
- **L2** manages **nobody**

### Protection Rules
- ❌ Cannot manage yourself (delete/deactivate/reset-password)
- ❌ Cannot delete/deactivate Super Admin (L0)
- ❌ Cannot manage same-level or higher-level admins

## Code Examples

### cURL Commands

**List Users:**
```bash
curl -X GET "http://localhost:8080/api/v1/admin/users?page=0&size=10" \
  -H "Authorization: Bearer YOUR_TOKEN"
```

**Create User:**
```bash
curl -X POST http://localhost:8080/api/v1/admin/users \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"username":"newuser","email":"user@example.com","password":"Pass123","firstName":"John","lastName":"Doe","isActive":true,"emailVerified":false}'
```

**Search Users:**
```bash
curl -X GET "http://localhost:8080/api/v1/admin/users/search?q=john" \
  -H "Authorization: Bearer YOUR_TOKEN"
```

**Create Admin (Super Admin only for L1):**
```bash
curl -X POST http://localhost:8080/api/v1/admin/admins \
  -H "Authorization: Bearer SUPER_ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"username":"admin01","email":"admin@example.com","password":"Pass123","firstName":"Admin","lastName":"User","level":1,"permissions":["user_management"],"isActive":true}'
```

**Deactivate User:**
```bash
curl -X POST http://localhost:8080/api/v1/admin/users/123/deactivate \
  -H "Authorization: Bearer YOUR_TOKEN"
```

**Reset User Password:**
```bash
curl -X POST http://localhost:8080/api/v1/admin/users/123/reset-password \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"newPassword":"NewPass123"}'
```

### JavaScript/Axios

**List Users:**
```javascript
const response = await axios.get('/api/v1/admin/users', {
  params: { page: 0, size: 10 },
  headers: { Authorization: `Bearer ${token}` }
});
```

**Create User:**
```javascript
const userData = {
  username: 'newuser',
  email: 'user@example.com',
  password: 'SecurePass123',
  firstName: 'John',
  lastName: 'Doe',
  isActive: true,
  emailVerified: false
};

const response = await axios.post('/api/v1/admin/users', userData, {
  headers: { Authorization: `Bearer ${token}` }
});
```

**Search Users:**
```javascript
const response = await axios.get('/api/v1/admin/users/search', {
  params: { q: 'john', page: 0, size: 20 },
  headers: { Authorization: `Bearer ${token}` }
});
```

### Java/Spring RestTemplate

**List Users:**
```java
HttpHeaders headers = new HttpHeaders();
headers.setBearerAuth(token);
HttpEntity<Void> entity = new HttpEntity<>(headers);

String url = "http://localhost:8080/api/v1/admin/users?page=0&size=10";
ResponseEntity<UserListResponse> response = restTemplate.exchange(
    url, HttpMethod.GET, entity, UserListResponse.class
);
```

**Create Admin:**
```java
CreateAdminRequest request = new CreateAdminRequest();
request.setUsername("admin01");
request.setEmail("admin@example.com");
request.setPassword("SecurePass123");
request.setFirstName("Admin");
request.setLastName("User");
request.setLevel(2);
request.setPermissions(Arrays.asList("content_moderation"));

HttpHeaders headers = new HttpHeaders();
headers.setBearerAuth(token);
HttpEntity<CreateAdminRequest> entity = new HttpEntity<>(request, headers);

ResponseEntity<AdminManagementDTO> response = restTemplate.postForEntity(
    "http://localhost:8080/api/v1/admin/admins", entity, AdminManagementDTO.class
);
```

## Common Scenarios

### Scenario 1: Create Moderator
**Who can do it**: Super Admin (L0) or Admin (L1)
```bash
POST /api/v1/admin/admins
{
  "username": "moderator01",
  "level": 2,
  "permissions": ["content_moderation"]
}
```

### Scenario 2: Deactivate Problematic User
**Who can do it**: Any Admin
```bash
POST /api/v1/admin/users/{id}/deactivate
```

### Scenario 3: Reset Forgotten Admin Password
**Who can do it**: Higher-level admin only
```bash
POST /api/v1/admin/admins/{id}/reset-password
{
  "newPassword": "NewSecurePass123"
}
```

### Scenario 4: Unlock Locked Account
**Who can do it**: Any Admin (for users), Higher-level admin (for admins)
```bash
POST /api/v1/admin/users/{id}/unlock
# OR
POST /api/v1/admin/admins/{id}/unlock
```

### Scenario 5: Search for User
**Who can do it**: Any Admin
```bash
GET /api/v1/admin/users/search?q=john@example.com
```

## Validation Rules

### Password Requirements
- Minimum 8 characters
- Must contain uppercase and lowercase letters
- Must contain at least one number
- No common patterns

### Email Requirements
- Valid email format (RFC 5322)
- Must be unique across users/admins

### Username Requirements
- 3-50 characters
- Alphanumeric and underscores only
- Must be unique

### Admin Level
- Valid values: 0, 1, 2
- Cannot create admin with level higher than your own
- Cannot modify admin level after creation

## Troubleshooting

### "Insufficient permissions" Error
**Check**: Your admin level vs. target admin level
**Solution**: Use higher-level admin account

### "Cannot manage yourself" Error
**Check**: Are you trying to deactivate/delete your own account?
**Solution**: Have another admin perform the operation

### "User not found" Error
**Check**: Is the user ID correct? Is the user deleted?
**Solution**: Verify user ID, check if user is soft-deleted

### "Username already exists" Error
**Check**: Is the username unique?
**Solution**: Use a different username

### "Invalid token" Error
**Check**: Is your JWT token valid and not expired?
**Solution**: Re-authenticate to get a new token

## Testing Tips

1. **Use Postman Collection**: Import the provided collection for easy testing
2. **Set Environment Variables**: Configure `base_url`, `admin_token`, etc.
3. **Test Permission Boundaries**: Try operations with different admin levels
4. **Test Edge Cases**: Self-management, super admin protection, etc.
5. **Verify Soft Deletes**: Deleted entities should still exist in DB

## Security Reminders

- 🔒 Always use HTTPS in production
- 🔑 Keep JWT tokens secure
- 🔄 Rotate admin passwords regularly
- 📝 Monitor audit logs
- 🚫 Never share admin credentials
- ⚠️ Be careful with Super Admin access
- 🔍 Review permission changes regularly

## Performance Tips

- Use pagination for large datasets
- Implement proper indexing on search fields
- Cache frequently accessed data
- Monitor database query performance
- Use connection pooling

## Contact

For issues or questions, refer to:
- Full API documentation: `ADMIN_PANEL_API.md`
- Feature guide: `ADMIN_PANEL_FEATURE_README.md`
- Implementation details: `IMPLEMENTATION_SUMMARY_ADMIN_PANEL.md`
