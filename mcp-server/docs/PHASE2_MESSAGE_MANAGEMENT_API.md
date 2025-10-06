# Phase 2: Message Management API Documentation

## Overview
Complete CRUD operations for managing chat messages with advanced features like message editing, response regeneration, and conversation history management.

---

## Endpoints

### 1. Get Message History
**GET** `/api/v1/sessions/{sessionId}/messages`

Get all messages in a chat session (conversation history).

**Headers:**
```
Authorization: Bearer <JWT_TOKEN>
```

**Response:** `200 OK`
```json
{
  "sessionId": "550e8400-e29b-41d4-a716-446655440000",
  "totalMessages": 4,
  "messages": [
    {
      "id": 1,
      "sessionId": "550e8400-e29b-41d4-a716-446655440000",
      "role": "user",
      "content": "What is machine learning?",
      "tokenCount": 5,
      "model": "llama3",
      "timestamp": "2025-10-06T20:00:00"
    },
    {
      "id": 2,
      "sessionId": "550e8400-e29b-41d4-a716-446655440000",
      "role": "assistant",
      "content": "Machine learning is a subset of artificial intelligence...",
      "tokenCount": 150,
      "model": "llama3",
      "timestamp": "2025-10-06T20:00:05"
    }
  ]
}
```

**cURL Example:**
```bash
curl -X GET http://localhost:8080/api/v1/sessions/550e8400-e29b-41d4-a716-446655440000/messages \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

---

### 2. Get Single Message
**GET** `/api/v1/messages/{messageId}`

Retrieve a specific message by ID.

**Headers:**
```
Authorization: Bearer <JWT_TOKEN>
```

**Response:** `200 OK`
```json
{
  "id": 1,
  "sessionId": "550e8400-e29b-41d4-a716-446655440000",
  "role": "user",
  "content": "What is machine learning?",
  "tokenCount": 5,
  "model": "llama3",
  "timestamp": "2025-10-06T20:00:00"
}
```

**cURL Example:**
```bash
curl -X GET http://localhost:8080/api/v1/messages/1 \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

---

### 3. Update/Edit Message
**PUT** `/api/v1/messages/{messageId}`

Edit a user message. Only USER messages can be edited (not assistant or system messages).

**Features:**
- Edit message content
- Optionally regenerate assistant's response after edit
- Automatically deletes subsequent messages when regenerating

**Headers:**
```
Authorization: Bearer <JWT_TOKEN>
Content-Type: application/json
```

**Request Body:**
```json
{
  "content": "What is deep learning?",
  "regenerateResponse": true
}
```

**Response:** `200 OK`
```json
{
  "id": 1,
  "sessionId": "550e8400-e29b-41d4-a716-446655440000",
  "role": "user",
  "content": "What is deep learning?",
  "tokenCount": 5,
  "model": "llama3",
  "timestamp": "2025-10-06T20:00:00"
}
```

**Use Cases:**
1. **Fix typos:** Edit message without regenerating response
   ```json
   {
     "content": "What is machine learning?",
     "regenerateResponse": false
   }
   ```

2. **Change question:** Edit and get new AI response
   ```json
   {
     "content": "What is deep learning instead?",
     "regenerateResponse": true
   }
   ```

**cURL Example:**
```bash
curl -X PUT http://localhost:8080/api/v1/messages/1 \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "content": "What is deep learning?",
    "regenerateResponse": true
  }'
```

---

### 4. Delete Message
**DELETE** `/api/v1/messages/{messageId}`

Delete a message from conversation history.

**Behavior:**
- **Deleting USER message:** Also deletes the corresponding ASSISTANT response
- **Deleting ASSISTANT message:** Only deletes that specific message
- **Token counts:** Automatically updated in session statistics

**Headers:**
```
Authorization: Bearer <JWT_TOKEN>
```

**Response:** `204 No Content`

**cURL Example:**
```bash
curl -X DELETE http://localhost:8080/api/v1/messages/1 \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

**Example Flow:**
```
Before deletion:
  Message 1 (USER): "What is ML?"
  Message 2 (ASSISTANT): "ML is..."
  Message 3 (USER): "Give example"
  Message 4 (ASSISTANT): "Sure, here's..."

Delete Message 1 → Deletes Messages 1 and 2
Delete Message 3 → Deletes Messages 3 and 4
Delete Message 4 → Only deletes Message 4
```

---

### 5. Regenerate Last Response
**POST** `/api/v1/sessions/{sessionId}/regenerate`

Regenerate the last assistant response in the conversation.

**Features:**
- Deletes the last assistant message
- Regenerates response using current conversation history
- Optionally use a different model for regeneration

**Headers:**
```
Authorization: Bearer <JWT_TOKEN>
Content-Type: application/json
```

**Request Body (Optional):**
```json
{
  "model": "llama3",
  "fromMessageId": null
}
```

**Response:** `200 OK`
```json
{
  "id": 5,
  "sessionId": "550e8400-e29b-41d4-a716-446655440000",
  "role": "assistant",
  "content": "Here's a different perspective on machine learning...",
  "tokenCount": 180,
  "model": "llama3",
  "timestamp": "2025-10-06T20:05:00"
}
```

**Use Cases:**

1. **Simple regeneration** (use session's model):
   ```bash
   curl -X POST http://localhost:8080/api/v1/sessions/550e8400-e29b-41d4-a716-446655440000/regenerate \
     -H "Authorization: Bearer YOUR_JWT_TOKEN"
   ```

2. **Regenerate with different model:**
   ```bash
   curl -X POST http://localhost:8080/api/v1/sessions/550e8400-e29b-41d4-a716-446655440000/regenerate \
     -H "Authorization: Bearer YOUR_JWT_TOKEN" \
     -H "Content-Type: application/json" \
     -d '{"model": "llama3"}'
   ```

---

## Security & Authorization

All endpoints verify:
1. **JWT Token validity**
2. **Session ownership:** User can only access their own messages
3. **Message access:** User can only modify/delete messages in their sessions

**Error Responses:**

**401 Unauthorized:**
```json
{
  "error": "Unauthorized",
  "message": "Invalid or expired token"
}
```

**403 Forbidden:**
```json
{
  "error": "Forbidden",
  "message": "Access denied"
}
```

**404 Not Found:**
```json
{
  "error": "Not Found",
  "message": "Message not found"
}
```

**400 Bad Request:**
```json
{
  "error": "Bad Request",
  "message": "Only user messages can be edited"
}
```

---

## Advanced Features

### 1. Message Editing with Regeneration

When you edit a user message with `regenerateResponse: true`:

**What happens:**
1. Update the user message content
2. Delete all messages after this message
3. Rebuild conversation history up to this point
4. Call Ollama API to generate new response
5. Save new assistant message
6. Update session statistics

**Example:**
```
Original conversation:
  Msg 1 (USER): "What is ML?"
  Msg 2 (ASSISTANT): "ML is..."
  Msg 3 (USER): "Tell me more"
  Msg 4 (ASSISTANT): "Sure..."

Edit Msg 1 to "What is DL?" with regenerate=true:
  Msg 1 (USER): "What is DL?" [UPDATED]
  Msg 2 (ASSISTANT): [DELETED]
  Msg 3 (USER): [DELETED]
  Msg 4 (ASSISTANT): [DELETED]
  Msg 5 (ASSISTANT): "DL is..." [NEW]

Result:
  Msg 1 (USER): "What is DL?"
  Msg 5 (ASSISTANT): "DL is..."
```

### 2. Conversation Branching

You can create conversation branches by:
1. Editing an earlier message
2. Regenerating the response
3. Continuing from that point

This allows exploring different conversation paths.

### 3. Token Management

- **Token counts** are automatically tracked
- **Session statistics** updated on every operation
- Deleting messages reduces `tokenUsage`
- Adding messages increases `tokenUsage`

---

## Best Practices

### 1. Message History Pagination
For long conversations, consider implementing pagination:
```
GET /api/v1/sessions/{sessionId}/messages?page=0&size=50
```

### 2. Rate Limiting
Regenerate operations call the LLM, so consider:
- Rate limiting regenerate endpoints
- Caching recent responses
- User quotas for regenerations

### 3. Error Handling
Always handle these scenarios:
- Session not found
- Message not found
- User doesn't own the session
- Cannot edit assistant messages
- LLM service unavailable

### 4. Optimistic UI Updates
When editing/deleting messages:
1. Update UI immediately
2. Send API request
3. Roll back UI if request fails

---

## Testing Workflow

### Complete Message CRUD Flow:

```bash
# 1. Create a chat session (from Phase 1)
SESSION_ID=$(curl -X POST http://localhost:8080/api/v1/sessions \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"title": "Test Session", "model": "llama3"}' \
  | jq -r '.sessionId')

# 2. Send a message
MESSAGE_ID=$(curl -X POST http://localhost:8080/api/v1/chat \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d "{\"sessionId\": \"$SESSION_ID\", \"message\": \"What is ML?\"}" \
  | jq -r '.messageId')

# 3. Get message history
curl http://localhost:8080/api/v1/sessions/$SESSION_ID/messages \
  -H "Authorization: Bearer $TOKEN"

# 4. Edit the user message
curl -X PUT http://localhost:8080/api/v1/messages/$MESSAGE_ID \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"content": "What is DL?", "regenerateResponse": true}'

# 5. Regenerate last response
curl -X POST http://localhost:8080/api/v1/sessions/$SESSION_ID/regenerate \
  -H "Authorization: Bearer $TOKEN"

# 6. Delete a message
curl -X DELETE http://localhost:8080/api/v1/messages/$MESSAGE_ID \
  -H "Authorization: Bearer $TOKEN"
```

---

## Status

✅ **Phase 2 Complete**
- GET message history
- GET single message
- PUT edit message (with optional regenerate)
- DELETE message (with cascade for user messages)
- POST regenerate last response

**Next Phase: User Profile Management**

---

## Notes

- All endpoints use the **Turkish locale fix** for role names (`toLowerCase(Locale.ENGLISH)`)
- Message timestamps are immutable (cannot be changed after creation)
- Session statistics (`messageCount`, `tokenUsage`) are automatically maintained
- Only USER messages can be edited (business rule)
- Regeneration always uses the latest conversation context
