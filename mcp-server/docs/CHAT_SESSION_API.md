# Chat Session Management API

## Overview
This document describes the Chat Session CRUD API implementation for the MCP Server.

## Architecture

### Layers Implemented
1. **DTOs** (Data Transfer Objects) - `com.g4.chatbot.dto.session`
2. **Service Layer** - `com.g4.chatbot.services.ChatSessionService`
3. **Controller Layer** - `com.g4.chatbot.controllers.ChatSessionController`
4. **Exception Handling** - `com.g4.chatbot.exception`

## API Endpoints

All endpoints require JWT authentication via `Authorization: Bearer <token>` header.

### 1. Create Session
**POST** `/api/v1/sessions`

**Request Body:**
```json
{
  "title": "My Chat Session",        // Optional, defaults to "New Chat"
  "model": "gpt-3.5-turbo",          // Optional, defaults to "gpt-3.5-turbo"
  "isPublic": false                  // Optional, defaults to false
}
```

**Response:** `201 Created`
```json
{
  "sessionId": "uuid-string",
  "title": "My Chat Session",
  "model": "gpt-3.5-turbo",
  "status": "ACTIVE",
  "messageCount": 0,
  "tokenUsage": 0,
  "isPublic": false,
  "createdAt": "2025-10-03T10:30:00",
  "updatedAt": "2025-10-03T10:30:00",
  "lastAccessedAt": "2025-10-03T10:30:00",
  "expiresAt": null
}
```

---

### 2. Get All User Sessions (Paginated)
**GET** `/api/v1/sessions?page=0&size=10`

**Query Parameters:**
- `page` (optional, default: 0) - Page number
- `size` (optional, default: 10) - Page size

**Response:** `200 OK`
```json
{
  "sessions": [
    {
      "sessionId": "uuid-string",
      "title": "My Chat Session",
      "model": "gpt-3.5-turbo",
      "status": "ACTIVE",
      "messageCount": 15,
      "tokenUsage": 2500,
      "isPublic": false,
      "createdAt": "2025-10-03T10:30:00",
      "updatedAt": "2025-10-03T10:35:00",
      "lastAccessedAt": "2025-10-03T10:35:00",
      "expiresAt": null
    }
  ],
  "totalPages": 5,
  "totalElements": 50,
  "currentPage": 0,
  "pageSize": 10
}
```

---

### 3. Get Active Sessions
**GET** `/api/v1/sessions/active`

**Response:** `200 OK`
```json
[
  {
    "sessionId": "uuid-string",
    "title": "Active Chat",
    "model": "gpt-3.5-turbo",
    "status": "ACTIVE",
    // ... other fields
  }
]
```

---

### 4. Get Specific Session
**GET** `/api/v1/sessions/{sessionId}`

**Response:** `200 OK`
```json
{
  "sessionId": "uuid-string",
  "title": "My Chat Session",
  "model": "gpt-3.5-turbo",
  "status": "ACTIVE",
  // ... other fields
}
```

**Error Responses:**
- `404 Not Found` - Session doesn't exist
- `403 Forbidden` - Session belongs to another user

---

### 5. Update Session
**PUT** `/api/v1/sessions/{sessionId}`

**Request Body:** (all fields optional)
```json
{
  "title": "Updated Title",
  "model": "gpt-4",
  "isPublic": true
}
```

**Response:** `200 OK`
```json
{
  "sessionId": "uuid-string",
  "title": "Updated Title",
  "model": "gpt-4",
  "status": "ACTIVE",
  // ... other fields
}
```

---

### 6. Delete Session (Soft Delete)
**DELETE** `/api/v1/sessions/{sessionId}`

**Response:** `204 No Content`

**Note:** This performs a soft delete by changing the status to `DELETED`. The session remains in the database.

---

### 7. Archive Session
**POST** `/api/v1/sessions/{sessionId}/archive`

**Response:** `200 OK`
```json
{
  "sessionId": "uuid-string",
  "status": "ARCHIVED",
  // ... other fields
}
```

---

### 8. Pause Session
**POST** `/api/v1/sessions/{sessionId}/pause`

**Response:** `200 OK`
```json
{
  "sessionId": "uuid-string",
  "status": "PAUSED",
  // ... other fields
}
```

---

### 9. Activate/Resume Session
**POST** `/api/v1/sessions/{sessionId}/activate`

**Response:** `200 OK`
```json
{
  "sessionId": "uuid-string",
  "status": "ACTIVE",
  // ... other fields
}
```

---

## Security

### Authorization Rules
- Users can only access their own sessions
- User ID is extracted from JWT token (stored in `authentication.getDetails()`)
- All session operations verify ownership before execution
- Attempting to access another user's session returns `403 Forbidden`

### Authentication
- All endpoints require valid JWT token
- Token must be passed in `Authorization: Bearer <token>` header
- Token is validated by `JwtAuthFilter`

---

## Session Status Flow

```
CREATE → ACTIVE
         ↓
ACTIVE ←→ PAUSED
  ↓
ARCHIVED
  ↓
DELETED
```

**Status Descriptions:**
- `ACTIVE` - Normal, active session that can send/receive messages
- `PAUSED` - Temporarily paused, can be reactivated
- `ARCHIVED` - Old session, kept for reference
- `DELETED` - Soft-deleted, not visible to user but kept in database

---

## Error Handling

### Global Exception Handler
All errors are handled by `GlobalExceptionHandler` which returns consistent error responses:

```json
{
  "status": 404,
  "error": "Not Found",
  "message": "Session not found with id: xyz",
  "path": "/api/v1/sessions/xyz",
  "timestamp": "2025-10-03T10:30:00"
}
```

### Common HTTP Status Codes
- `200 OK` - Successful GET/PUT/POST operations
- `201 Created` - Successful session creation
- `204 No Content` - Successful deletion
- `400 Bad Request` - Validation errors
- `403 Forbidden` - Unauthorized access to resource
- `404 Not Found` - Resource not found
- `500 Internal Server Error` - Unexpected server error

---

## Validation

### CreateSessionRequest
- `title`: Max 255 characters
- `model`: Max 50 characters
- All fields are optional

### UpdateSessionRequest
- `title`: Max 255 characters
- `model`: Max 50 characters
- All fields are optional

---

## Database Operations

### Automatic Timestamp Management
- `createdAt` - Set automatically on creation (@PrePersist)
- `updatedAt` - Updated automatically on modification (@PreUpdate)
- `lastAccessedAt` - Updated when session is retrieved

### Soft Delete
- Sessions are not physically deleted from database
- Status is changed to `DELETED`
- Can be filtered out in queries

---

## Files Created

### DTOs
- `CreateSessionRequest.java` - Request DTO for creating sessions
- `UpdateSessionRequest.java` - Request DTO for updating sessions
- `SessionResponse.java` - Response DTO with builder pattern
- `SessionListResponse.java` - Paginated response wrapper

### Service
- `ChatSessionService.java` - Business logic layer with all CRUD operations

### Controller
- `ChatSessionController.java` - REST API endpoints

### Exceptions
- `ResourceNotFoundException.java` - For 404 errors
- `UnauthorizedException.java` - For 403 errors
- `ErrorResponse.java` - Standardized error response format
- `GlobalExceptionHandler.java` - Global exception handling

---

## Testing with cURL

### Create a session
```bash
curl -X POST http://localhost:8080/api/v1/sessions \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "title": "My First Chat",
    "model": "gpt-3.5-turbo"
  }'
```

### Get all sessions
```bash
curl -X GET "http://localhost:8080/api/v1/sessions?page=0&size=10" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

### Get a specific session
```bash
curl -X GET http://localhost:8080/api/v1/sessions/{sessionId} \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

### Update a session
```bash
curl -X PUT http://localhost:8080/api/v1/sessions/{sessionId} \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Updated Title"
  }'
```

### Delete a session
```bash
curl -X DELETE http://localhost:8080/api/v1/sessions/{sessionId} \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

---

## Next Steps

After implementing chat sessions, the next phase will be:

1. **Message CRUD Operations**
   - Add messages to sessions
   - Read all messages in a session
   - Delete messages
   - (Optional) Update messages

2. **OpenAI Integration**
   - Connect to OpenAI API
   - Send user messages
   - Receive AI responses
   - Store conversation history

3. **Redis Caching**
   - Cache active sessions
   - Cache recent messages
   - Improve performance

4. **Rate Limiting**
   - Limit chat requests per user
   - Prevent abuse
