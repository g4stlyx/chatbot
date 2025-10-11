# Admin Session & Message Management API Documentation

## Overview

This document describes the admin panel endpoints for managing chat sessions and messages. These endpoints allow administrators to monitor, moderate, and manage user conversations.

## Key Features

### Session Management
- **READ**: View all sessions with advanced filtering
- **DELETE**: Permanently remove sessions (Level 0-1 only)
- **ARCHIVE**: Soft delete sessions (Level 2+)
- **FLAG/UNFLAG**: Mark sessions for review

### Message Management
- **READ**: View all messages with advanced filtering
- **DELETE**: Permanently remove messages (Level 0-1 only)
- **FLAG/UNFLAG**: Mark messages for review

## Authorization Levels

| Level | Role | Permissions |
|-------|------|-------------|
| 0 | Super Admin | Full access - All operations |
| 1 | Admin | Full access - All operations |
| 2 | Moderator | Read, Archive, Flag/Unflag only |

## Session Management Endpoints

### Base URL
```
/api/v1/admin/sessions
```

### 1. Get All Sessions (with Filtering)

**Endpoint**: `GET /api/v1/admin/sessions`

**Authorization**: Admin JWT (Level 2+)

**Query Parameters**:
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| userId | Long | No | Filter by user ID |
| status | String | No | Filter by status (ACTIVE, PAUSED, ARCHIVED, DELETED) |
| isFlagged | Boolean | No | Filter by flagged status |
| isPublic | Boolean | No | Filter by public/private |
| page | Integer | No | Page number (default: 0) |
| size | Integer | No | Page size (default: 10) |
| sortBy | String | No | Sort field (default: createdAt) |
| sortDirection | String | No | Sort direction: asc/desc (default: desc) |

**Response**:
```json
{
  "sessions": [
    {
      "sessionId": "550e8400-e29b-41d4-a716-446655440000",
      "userId": 123,
      "username": "johndoe",
      "userEmail": "john@example.com",
      "title": "My Chat Session",
      "model": "gpt-4",
      "status": "ACTIVE",
      "messageCount": 25,
      "tokenUsage": 5420,
      "isPublic": false,
      "isFlagged": false,
      "createdAt": "2024-01-15T10:30:00",
      "updatedAt": "2024-01-15T14:20:00",
      "lastAccessedAt": "2024-01-15T14:20:00",
      "expiresAt": null
    }
  ],
  "currentPage": 0,
  "totalPages": 5,
  "totalElements": 50,
  "pageSize": 10
}
```

**Example Requests**:
```bash
# Get all sessions
GET /api/v1/admin/sessions

# Get sessions for specific user
GET /api/v1/admin/sessions?userId=123

# Get flagged sessions
GET /api/v1/admin/sessions?isFlagged=true

# Get active sessions for user
GET /api/v1/admin/sessions?userId=123&status=ACTIVE

# Get with pagination
GET /api/v1/admin/sessions?page=0&size=20&sortBy=createdAt&sortDirection=desc
```

---

### 2. Get Session by ID

**Endpoint**: `GET /api/v1/admin/sessions/{sessionId}`

**Authorization**: Admin JWT (Level 2+)

**Path Parameters**:
| Parameter | Type | Description |
|-----------|------|-------------|
| sessionId | String | Session UUID |

**Response**:
```json
{
  "sessionId": "550e8400-e29b-41d4-a716-446655440000",
  "userId": 123,
  "username": "johndoe",
  "userEmail": "john@example.com",
  "title": "My Chat Session",
  "model": "gpt-4",
  "status": "ACTIVE",
  "messageCount": 25,
  "tokenUsage": 5420,
  "isPublic": false,
  "isFlagged": false,
  "createdAt": "2024-01-15T10:30:00",
  "updatedAt": "2024-01-15T14:20:00",
  "lastAccessedAt": "2024-01-15T14:20:00",
  "expiresAt": null
}
```

**Error Responses**:
- `404 Not Found`: Session not found
- `401 Unauthorized`: Invalid/missing JWT token
- `403 Forbidden`: Insufficient permissions

---

### 3. Delete Session (Hard Delete)

**Endpoint**: `DELETE /api/v1/admin/sessions/{sessionId}`

**Authorization**: Admin JWT (Level 0-1 only)

**Path Parameters**:
| Parameter | Type | Description |
|-----------|------|-------------|
| sessionId | String | Session UUID |

**Response**:
```json
{
  "success": true,
  "message": "Session deleted successfully",
  "sessionId": "550e8400-e29b-41d4-a716-446655440000"
}
```

**Notes**:
- Permanently deletes the session and all associated messages
- Only Level 0-1 admins can perform this operation
- Cannot be undone

---

### 4. Archive Session (Soft Delete)

**Endpoint**: `POST /api/v1/admin/sessions/{sessionId}/archive`

**Authorization**: Admin JWT (Level 2+)

**Path Parameters**:
| Parameter | Type | Description |
|-----------|------|-------------|
| sessionId | String | Session UUID |

**Response**:
```json
{
  "sessionId": "550e8400-e29b-41d4-a716-446655440000",
  "status": "ARCHIVED",
  "userId": 123,
  ...
}
```

**Notes**:
- Changes session status to ARCHIVED
- Session and messages are preserved
- Can be un-archived if needed

---

### 5. Flag Session

**Endpoint**: `POST /api/v1/admin/sessions/{sessionId}/flag`

**Authorization**: Admin JWT (Level 2+)

**Path Parameters**:
| Parameter | Type | Description |
|-----------|------|-------------|
| sessionId | String | Session UUID |

**Request Body**:
```json
{
  "flagType": "INAPPROPRIATE_CONTENT",
  "reason": "Contains inappropriate language"
}
```

**Flag Types**:
- `INAPPROPRIATE_CONTENT`
- `SPAM`
- `ABUSE`
- `POLICY_VIOLATION`
- `OTHER`

**Response**:
```json
{
  "sessionId": "550e8400-e29b-41d4-a716-446655440000",
  "isFlagged": true,
  ...
}
```

---

### 6. Unflag Session

**Endpoint**: `POST /api/v1/admin/sessions/{sessionId}/unflag`

**Authorization**: Admin JWT (Level 2+)

**Path Parameters**:
| Parameter | Type | Description |
|-----------|------|-------------|
| sessionId | String | Session UUID |

**Response**:
```json
{
  "sessionId": "550e8400-e29b-41d4-a716-446655440000",
  "isFlagged": false,
  ...
}
```

---

## Message Management Endpoints

### Base URL
```
/api/v1/admin/messages
```

### 1. Get All Messages (with Filtering)

**Endpoint**: `GET /api/v1/admin/messages`

**Authorization**: Admin JWT (Level 2+)

**Query Parameters**:
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| sessionId | String | No | Filter by session ID |
| userId | Long | No | Filter by user ID |
| role | String | No | Filter by role (USER, ASSISTANT, SYSTEM) |
| isFlagged | Boolean | No | Filter by flagged status |
| page | Integer | No | Page number (default: 0) |
| size | Integer | No | Page size (default: 10) |
| sortBy | String | No | Sort field (default: timestamp) |
| sortDirection | String | No | Sort direction: asc/desc (default: desc) |

**Response**:
```json
{
  "messages": [
    {
      "id": 1001,
      "sessionId": "550e8400-e29b-41d4-a716-446655440000",
      "sessionTitle": "My Chat Session",
      "userId": 123,
      "username": "johndoe",
      "userEmail": "john@example.com",
      "role": "USER",
      "content": "What is artificial intelligence?",
      "tokenCount": 25,
      "model": "gpt-4",
      "isFlagged": false,
      "flagReason": null,
      "timestamp": "2024-01-15T10:30:00"
    }
  ],
  "currentPage": 0,
  "totalPages": 10,
  "totalElements": 100,
  "pageSize": 10
}
```

**Example Requests**:
```bash
# Get all messages
GET /api/v1/admin/messages

# Get messages for specific session
GET /api/v1/admin/messages?sessionId=550e8400-e29b-41d4-a716-446655440000

# Get user messages only
GET /api/v1/admin/messages?role=USER

# Get flagged messages
GET /api/v1/admin/messages?isFlagged=true

# Get messages by user
GET /api/v1/admin/messages?userId=123
```

---

### 2. Get Message by ID

**Endpoint**: `GET /api/v1/admin/messages/{messageId}`

**Authorization**: Admin JWT (Level 2+)

**Path Parameters**:
| Parameter | Type | Description |
|-----------|------|-------------|
| messageId | Long | Message ID |

**Response**:
```json
{
  "id": 1001,
  "sessionId": "550e8400-e29b-41d4-a716-446655440000",
  "sessionTitle": "My Chat Session",
  "userId": 123,
  "username": "johndoe",
  "userEmail": "john@example.com",
  "role": "USER",
  "content": "What is artificial intelligence?",
  "tokenCount": 25,
  "model": "gpt-4",
  "isFlagged": false,
  "flagReason": null,
  "timestamp": "2024-01-15T10:30:00"
}
```

---

### 3. Get Messages by Session

**Endpoint**: `GET /api/v1/admin/messages/session/{sessionId}`

**Authorization**: Admin JWT (Level 2+)

**Path Parameters**:
| Parameter | Type | Description |
|-----------|------|-------------|
| sessionId | String | Session UUID |

**Query Parameters**:
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| page | Integer | No | Page number (default: 0) |
| size | Integer | No | Page size (default: 10) |
| sortBy | String | No | Sort field (default: timestamp) |
| sortDirection | String | No | Sort direction: asc/desc (default: asc) |

**Response**: Same as "Get All Messages"

---

### 4. Delete Message (Hard Delete)

**Endpoint**: `DELETE /api/v1/admin/messages/{messageId}`

**Authorization**: Admin JWT (Level 0-1 only)

**Path Parameters**:
| Parameter | Type | Description |
|-----------|------|-------------|
| messageId | Long | Message ID |

**Response**:
```json
{
  "success": true,
  "message": "Message deleted successfully",
  "messageId": 1001
}
```

**Notes**:
- Permanently deletes the message
- **If deleting a USER message, automatically deletes the corresponding ASSISTANT response**
- Updates session message count and token usage accordingly
- Only Level 0-1 admins can perform this operation
- Cannot be undone

**Cascade Deletion Behavior**:
When a USER message is deleted, the system automatically:
1. Finds the next ASSISTANT message in the conversation
2. Deletes that ASSISTANT message as well
3. Updates the session statistics (message count and token usage) for both messages

This ensures conversation integrity by maintaining the question-answer pairs.

---

### 5. Flag Message

**Endpoint**: `POST /api/v1/admin/messages/{messageId}/flag`

**Authorization**: Admin JWT (Level 2+)

**Path Parameters**:
| Parameter | Type | Description |
|-----------|------|-------------|
| messageId | Long | Message ID |

**Request Body**:
```json
{
  "flagType": "INAPPROPRIATE",
  "reason": "Contains offensive language"
}
```

**Flag Types**:
- `INAPPROPRIATE`
- `SPAM`
- `HARMFUL`
- `POLICY_VIOLATION`
- `OTHER`

**Response**:
```json
{
  "id": 1001,
  "isFlagged": true,
  "flagReason": "INAPPROPRIATE: Contains offensive language",
  ...
}
```

---

### 6. Unflag Message

**Endpoint**: `POST /api/v1/admin/messages/{messageId}/unflag`

**Authorization**: Admin JWT (Level 2+)

**Path Parameters**:
| Parameter | Type | Description |
|-----------|------|-------------|
| messageId | Long | Message ID |

**Response**:
```json
{
  "id": 1001,
  "isFlagged": false,
  "flagReason": null,
  ...
}
```

---

## Error Responses

All endpoints may return the following error responses:

### 400 Bad Request
```json
{
  "timestamp": "2024-01-15T10:30:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Invalid request parameters",
  "path": "/api/v1/admin/sessions"
}
```

### 401 Unauthorized
```json
{
  "timestamp": "2024-01-15T10:30:00",
  "status": 401,
  "error": "Unauthorized",
  "message": "Invalid or missing JWT token",
  "path": "/api/v1/admin/sessions"
}
```

### 403 Forbidden
```json
{
  "timestamp": "2024-01-15T10:30:00",
  "status": 403,
  "error": "Forbidden",
  "message": "Only Level 0-1 admins can delete sessions",
  "path": "/api/v1/admin/sessions/xxx"
}
```

### 404 Not Found
```json
{
  "timestamp": "2024-01-15T10:30:00",
  "status": 404,
  "error": "Not Found",
  "message": "Session not found with ID: xxx",
  "path": "/api/v1/admin/sessions/xxx"
}
```

---

## Common Use Cases

### Use Case 1: Monitor User Activity
```bash
# Get all sessions for a user
GET /api/v1/admin/sessions?userId=123

# Get all messages from that user
GET /api/v1/admin/messages?userId=123
```

### Use Case 2: Content Moderation
```bash
# Find flagged content
GET /api/v1/admin/sessions?isFlagged=true
GET /api/v1/admin/messages?isFlagged=true

# Review and unflag if appropriate
POST /api/v1/admin/messages/1001/unflag
```

### Use Case 3: Clean Up Inappropriate Content
```bash
# Flag the session
POST /api/v1/admin/sessions/{sessionId}/flag
{
  "flagType": "INAPPROPRIATE_CONTENT",
  "reason": "Contains inappropriate language"
}

# Delete specific messages (Level 0-1 only)
DELETE /api/v1/admin/messages/1001
DELETE /api/v1/admin/messages/1002

# Archive the entire session
POST /api/v1/admin/sessions/{sessionId}/archive
```

### Use Case 4: Session Analysis
```bash
# Get session details
GET /api/v1/admin/sessions/{sessionId}

# Get all messages in that session
GET /api/v1/admin/messages/session/{sessionId}?sortDirection=asc
```

---

## Best Practices

### For Level 2 Moderators
1. Use **Flag** instead of delete for questionable content
2. **Archive** sessions instead of deleting them
3. Review context before taking action
4. Document flag reasons clearly

### For Level 0-1 Admins
1. **Delete** only when necessary (permanent action)
2. Review flagged content regularly
3. Consider archiving before deleting
4. Keep audit trail in mind

### General Guidelines
1. Always use filtering to narrow down results
2. Use pagination for large datasets
3. Check error responses for troubleshooting
4. Monitor flagged content regularly

---

## Database Schema Updates

The following fields have been added to support flagging:

### chat_sessions table
```sql
is_flagged BOOLEAN DEFAULT FALSE,
flag_reason TEXT,
flagged_by BIGINT,
flagged_at TIMESTAMP
```

### messages table
```sql
is_flagged BOOLEAN DEFAULT FALSE,
flag_reason TEXT,
flagged_by BIGINT,
flagged_at TIMESTAMP
```

---

## Security Considerations

1. **JWT Authentication**: All endpoints require valid admin JWT
2. **Role-Based Access**: Delete operations restricted to Level 0-1
3. **Audit Logging**: All admin actions are logged
4. **Data Preservation**: Prefer archiving over deletion
5. **Flag Tracking**: Flag reason and admin ID are recorded

---

## Version History

- **v1.0.0**: Initial release with full session and message management
