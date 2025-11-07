# Admin Activity Logs & Token Management API

## Overview
This document describes the new admin panel endpoints for activity log monitoring and token management. These are **Level 0 (Super Admin) only** features for system monitoring and maintenance.

---

## Authorization

### Access Levels
- **Level 0 (Super Admin)**: Full access to all endpoints
- **Level 1-2**: No access (403 Forbidden)

### Authentication
All endpoints require:
```
Authorization: Bearer <jwt_token>
```

---

## Activity Logs API

### 1. Get All Activity Logs

**Endpoint**: `GET /api/v1/admin/activity-logs`

**Description**: Retrieve all admin activity logs with optional filtering and pagination.

**Query Parameters**:
| Parameter | Type | Required | Default | Description |
|-----------|------|----------|---------|-------------|
| page | integer | No | 0 | Page number (0-indexed) |
| size | integer | No | 20 | Items per page |
| sortBy | string | No | createdAt | Sort field |
| sortDirection | string | No | desc | Sort direction (asc/desc) |
| adminId | Long | No | - | Filter by admin ID |
| action | string | No | - | Filter by action type |
| resourceType | string | No | - | Filter by resource type |
| startDate | DateTime | No | - | Filter from date (ISO 8601) |

**Example Request**:
```bash
GET /api/v1/admin/activity-logs?page=0&size=20&adminId=1&action=DELETE_USER
Authorization: Bearer <token>
```

**Example Response**:
```json
{
  "success": true,
  "data": {
    "logs": [
      {
        "id": 1,
        "adminId": 1,
        "adminUsername": "superadmin",
        "adminEmail": "admin@example.com",
        "action": "DELETE_USER",
        "resourceType": "USER",
        "resourceId": "123",
        "details": "{\"userId\": 123, \"username\": \"john_doe\"}",
        "ipAddress": "192.168.1.100",
        "userAgent": "Mozilla/5.0...",
        "createdAt": "2024-10-11T10:30:00"
      }
    ],
    "currentPage": 0,
    "totalPages": 5,
    "totalElements": 95,
    "pageSize": 20,
    "hasNext": true,
    "hasPrevious": false
  }
}
```

---

### 2. Get Activity Log by ID

**Endpoint**: `GET /api/v1/admin/activity-logs/{logId}`

**Description**: Retrieve a specific activity log entry by ID.

**Path Parameters**:
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| logId | Long | Yes | Activity log ID |

**Example Request**:
```bash
GET /api/v1/admin/activity-logs/1
Authorization: Bearer <token>
```

**Example Response**:
```json
{
  "success": true,
  "data": {
    "id": 1,
    "adminId": 1,
    "adminUsername": "superadmin",
    "adminEmail": "admin@example.com",
    "action": "FLAG_MESSAGE",
    "resourceType": "MESSAGE",
    "resourceId": "456",
    "details": "{\"messageId\": 456, \"reason\": \"Inappropriate content\"}",
    "ipAddress": "192.168.1.100",
    "userAgent": "Mozilla/5.0...",
    "createdAt": "2024-10-11T10:30:00"
  }
}
```

---

### 3. Delete Activity Log

**Endpoint**: `DELETE /api/v1/admin/activity-logs/{logId}`

**Description**: Delete a specific activity log entry (for cleanup purposes).

**Path Parameters**:
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| logId | Long | Yes | Activity log ID |

**Example Request**:
```bash
DELETE /api/v1/admin/activity-logs/1
Authorization: Bearer <token>
```

**Example Response**:
```json
{
  "success": true,
  "message": "Activity log deleted successfully"
}
```

---

### 4. Get Activity Statistics

**Endpoint**: `GET /api/v1/admin/activity-logs/stats/{adminId}`

**Description**: Get activity statistics for a specific admin.

**Path Parameters**:
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| adminId | Long | Yes | Admin ID |

**Query Parameters**:
| Parameter | Type | Required | Default | Description |
|-----------|------|----------|---------|-------------|
| since | DateTime | No | 30 days ago | Start date for stats (ISO 8601) |

**Example Request**:
```bash
GET /api/v1/admin/activity-logs/stats/1?since=2024-10-01T00:00:00
Authorization: Bearer <token>
```

**Example Response**:
```json
{
  "success": true,
  "data": {
    "adminId": 1,
    "activityCount": 145,
    "since": "2024-10-01T00:00:00"
  }
}
```

---

## Password Reset Tokens API

### 1. Get All Password Reset Tokens

**Endpoint**: `GET /api/v1/admin/tokens/password-reset`

**Description**: Retrieve all password reset tokens with optional filtering.

**Query Parameters**:
| Parameter | Type | Required | Default | Description |
|-----------|------|----------|---------|-------------|
| page | integer | No | 0 | Page number |
| size | integer | No | 20 | Items per page |
| sortBy | string | No | createdDate | Sort field |
| sortDirection | string | No | desc | Sort direction |
| userType | string | No | - | Filter by user type (user/admin) |
| includeExpired | boolean | No | true | Include expired tokens |

**Example Request**:
```bash
GET /api/v1/admin/tokens/password-reset?userType=user&includeExpired=false
Authorization: Bearer <token>
```

**Example Response**:
```json
{
  "success": true,
  "data": {
    "tokens": [
      {
        "id": 1,
        "token": "abc123...",
        "userId": 10,
        "userType": "user",
        "username": "john_doe",
        "email": "john@example.com",
        "expiryDate": "2024-10-11T11:00:00",
        "createdDate": "2024-10-11T10:45:00",
        "attemptCount": 0,
        "requestingIp": "192.168.1.50",
        "expired": false
      }
    ],
    "currentPage": 0,
    "totalPages": 3,
    "totalElements": 45,
    "pageSize": 20,
    "hasNext": true,
    "hasPrevious": false
  }
}
```

---

### 2. Get Password Reset Token by ID

**Endpoint**: `GET /api/v1/admin/tokens/password-reset/{tokenId}`

**Description**: Retrieve a specific password reset token by ID.

**Example Response**:
```json
{
  "success": true,
  "data": {
    "id": 1,
    "token": "abc123...",
    "userId": 10,
    "userType": "user",
    "username": "john_doe",
    "email": "john@example.com",
    "expiryDate": "2024-10-11T11:00:00",
    "createdDate": "2024-10-11T10:45:00",
    "attemptCount": 0,
    "requestingIp": "192.168.1.50",
    "expired": false
  }
}
```

---

### 3. Delete Password Reset Token

**Endpoint**: `DELETE /api/v1/admin/tokens/password-reset/{tokenId}`

**Description**: Delete a specific password reset token (for cleanup/security).

**Example Response**:
```json
{
  "success": true,
  "message": "Password reset token deleted successfully"
}
```

---

## Verification Tokens API

### 1. Get All Verification Tokens

**Endpoint**: `GET /api/v1/admin/tokens/verification`

**Description**: Retrieve all email verification tokens with optional filtering.

**Query Parameters**: Same as password reset tokens

**Example Response**:
```json
{
  "success": true,
  "data": {
    "tokens": [
      {
        "id": 1,
        "token": "xyz789...",
        "userId": 15,
        "userType": "user",
        "username": "jane_smith",
        "email": "jane@example.com",
        "expiryDate": "2024-10-12T10:45:00",
        "createdDate": "2024-10-11T10:45:00",
        "expired": false
      }
    ],
    "currentPage": 0,
    "totalPages": 2,
    "totalElements": 28,
    "pageSize": 20,
    "hasNext": true,
    "hasPrevious": false
  }
}
```

---

### 2. Get Verification Token by ID

**Endpoint**: `GET /api/v1/admin/tokens/verification/{tokenId}`

**Description**: Retrieve a specific verification token by ID.

---

### 3. Delete Verification Token

**Endpoint**: `DELETE /api/v1/admin/tokens/verification/{tokenId}`

**Description**: Delete a specific verification token.

---

## Error Responses

### 403 Forbidden (Non-Level 0 Admin)
```json
{
  "success": false,
  "message": "Access denied. Only Level 0 Super Admins can view activity logs."
}
```

### 404 Not Found
```json
{
  "success": false,
  "message": "Activity log not found with ID: 123"
}
```

### 500 Internal Server Error
```json
{
  "success": false,
  "message": "Failed to fetch activity logs: <error details>"
}
```

---

## Common Use Cases

### 1. Monitor Admin Activities
```bash
# Get recent admin actions
GET /api/v1/admin/activity-logs?page=0&size=50&sortDirection=desc
```

### 2. Audit Specific Admin
```bash
# Get all actions by admin ID 5 in the last month
GET /api/v1/admin/activity-logs?adminId=5&startDate=2024-09-11T00:00:00
```

### 3. Track User Deletions
```bash
# Find all user deletion actions
GET /api/v1/admin/activity-logs?action=DELETE_USER&resourceType=USER
```

### 4. Cleanup Expired Tokens
```bash
# Get expired password reset tokens
GET /api/v1/admin/tokens/password-reset?includeExpired=false

# Delete specific expired token
DELETE /api/v1/admin/tokens/password-reset/123
```

### 5. Monitor Pending Verifications
```bash
# Get active verification tokens
GET /api/v1/admin/tokens/verification?includeExpired=false&userType=user
```

---

## Best Practices

1. **Regular Monitoring**: Check activity logs regularly for suspicious patterns
2. **Token Cleanup**: Periodically delete expired tokens to keep database clean
3. **Audit Trail**: Use activity logs to maintain accountability
4. **Security**: Activity logs and tokens contain sensitive data - Level 0 access only
5. **Performance**: Use pagination and filtering to manage large datasets
6. **Retention**: Consider implementing automated cleanup for old logs

---

## Database Schema

### admin_activity_log Table
```sql
CREATE TABLE admin_activity_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    admin_id BIGINT NOT NULL,
    action VARCHAR(100) NOT NULL,
    resource_type VARCHAR(50) NOT NULL,
    resource_id VARCHAR(100),
    details JSON,
    ip_address VARCHAR(45),
    user_agent TEXT,
    created_at TIMESTAMP NOT NULL,
    FOREIGN KEY (admin_id) REFERENCES admins(id)
);
```

### Token Tables (Existing)
- `password_reset_tokens`: Stores password reset tokens
- `verification_tokens`: Stores email verification tokens

---

## Testing

Use the provided Postman collection:
- File: `5. Admin_Panel_API.postman_collection.json`
- Sections:
  - Activity Logs (Level 0 Only) - 4 requests
  - Token Management (Level 0 Only) - 6 requests

---

## Implementation Summary

### New Files Created
1. **DTOs** (5 files):
   - `AdminActivityLogDTO.java`
   - `AdminActivityLogListResponse.java`
   - `PasswordResetTokenDTO.java`
   - `VerificationTokenDTO.java`
   - `TokenListResponse.java`

2. **Services** (2 files):
   - `AdminActivityLogService.java`
   - `AdminTokenManagementService.java`

3. **Controllers** (2 files):
   - `AdminActivityLogController.java` - 4 endpoints
   - `AdminTokenManagementController.java` - 6 endpoints

4. **Repository Updates** (2 files):
   - `PasswordResetTokenRepository.java` - Added pagination support
   - `VerificationTokenRepository.java` - Added pagination support

### Total Endpoints: 10
- Activity Logs: 4 endpoints
- Password Reset Tokens: 3 endpoints
- Verification Tokens: 3 endpoints

All endpoints require Level 0 (Super Admin) authorization.
