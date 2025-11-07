# Phase 2: Message Management - Quick Reference

## 🚀 Quick Start

### 1. Get All Messages in a Session
```bash
curl http://localhost:8080/api/v1/sessions/{sessionId}/messages \
  -H "Authorization: Bearer YOUR_TOKEN"
```

### 2. Get Single Message
```bash
curl http://localhost:8080/api/v1/messages/{messageId} \
  -H "Authorization: Bearer YOUR_TOKEN"
```

### 3. Edit Message (No Regenerate)
```bash
curl -X PUT http://localhost:8080/api/v1/messages/{messageId} \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"content": "New content", "regenerateResponse": false}'
```

### 4. Edit Message (With Regenerate)
```bash
curl -X PUT http://localhost:8080/api/v1/messages/{messageId} \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"content": "New question", "regenerateResponse": true}'
```

### 5. Delete Message
```bash
curl -X DELETE http://localhost:8080/api/v1/messages/{messageId} \
  -H "Authorization: Bearer YOUR_TOKEN"
```

### 6. Regenerate Last Response
```bash
curl -X POST http://localhost:8080/api/v1/sessions/{sessionId}/regenerate \
  -H "Authorization: Bearer YOUR_TOKEN"
```

---

## 📋 Response Formats

### Message History Response
```json
{
  "sessionId": "uuid",
  "totalMessages": 4,
  "messages": [
    {
      "id": 1,
      "sessionId": "uuid",
      "role": "user",
      "content": "What is ML?",
      "tokenCount": 5,
      "model": "llama3",
      "timestamp": "2025-10-06T20:00:00"
    }
  ]
}
```

### Single Message Response
```json
{
  "id": 1,
  "sessionId": "uuid",
  "role": "user",
  "content": "Message content",
  "tokenCount": 10,
  "model": "llama3",
  "timestamp": "2025-10-06T20:00:00"
}
```

---

## ⚠️ Important Rules

### ✅ DO:
- Edit USER messages only
- Delete messages to clean up conversations
- Use regenerate for different AI responses
- Check token usage in responses

### ❌ DON'T:
- Try to edit ASSISTANT or SYSTEM messages
- Delete sessions without cleaning messages first
- Regenerate too frequently (rate limits)
- Forget authorization headers

---

## 🔑 Key Features

| Feature | Endpoint | Method |
|---------|----------|--------|
| List Messages | `/sessions/{id}/messages` | GET |
| Get Message | `/messages/{id}` | GET |
| Edit Message | `/messages/{id}` | PUT |
| Delete Message | `/messages/{id}` | DELETE |
| Regenerate | `/sessions/{id}/regenerate` | POST |

---

## 🎯 Common Use Cases

### Fix a Typo
```json
PUT /messages/123
{
  "content": "Fixed typo",
  "regenerateResponse": false
}
```

### Ask Different Question
```json
PUT /messages/123
{
  "content": "Different question?",
  "regenerateResponse": true
}
```

### Get Better Response
```bash
POST /sessions/abc/regenerate
```

### Remove Mistake
```bash
DELETE /messages/123
# Deletes user message + AI response
```

---

## 🐛 Common Errors

### 403 Forbidden
```json
{"error": "Access denied"}
```
→ Session doesn't belong to you

### 400 Bad Request
```json
{"error": "Only user messages can be edited"}
```
→ Tried to edit ASSISTANT message

### 404 Not Found
```json
{"error": "Message not found"}
```
→ Invalid message ID

---

## 📊 What Gets Updated

### Edit without Regenerate
- ✅ Message content
- ❌ Nothing else

### Edit with Regenerate
- ✅ Message content
- ✅ Deletes subsequent messages
- ✅ Creates new AI response
- ✅ Updates session stats

### Delete Message
- ✅ Deletes message
- ✅ Deletes next AI response (if USER msg)
- ✅ Updates session stats

### Regenerate
- ✅ Deletes last AI response
- ✅ Creates new AI response
- ✅ Updates session stats

---

## 🔒 Security

All endpoints require:
1. Valid JWT token
2. User owns the session
3. Message exists

---

## ⚡ Status Codes

| Code | Meaning |
|------|---------|
| 200 | Success |
| 204 | Deleted successfully |
| 400 | Bad request |
| 401 | Not authenticated |
| 403 | Access denied |
| 404 | Not found |

---

## 📝 Postman Collection

Import: `postman_files/Phase2_Message_Management.postman_collection.json`

Variables needed:
- `base_url` - http://localhost:8080
- `jwt_token` - Your JWT token
- `session_id` - Session UUID
- `message_id` - Message ID

---

## 🎓 Pro Tips

1. **Pagination**: For long chats, add `?page=0&size=50`
2. **Caching**: Store recent messages client-side
3. **Optimistic UI**: Update UI before API response
4. **Error Handling**: Always handle 403/404 errors
5. **Token Tracking**: Monitor token usage for billing

---

**Quick Reference Version 1.0**  
**Phase 2 Complete** ✅
