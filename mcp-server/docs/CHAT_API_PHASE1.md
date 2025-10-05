# 🤖 Chat API - Phase 1: LLM Integration

## ✅ Implementation Complete!

### 🎯 Features Implemented

1. **Streaming Chat** (Server-Sent Events) - Real-time response generation
2. **Non-Streaming Chat** - Wait for complete response
3. **Auto-Session Creation** - Automatically creates session if not specified
4. **Conversation History** - Maintains context across messages
5. **Ollama (Llama3) Integration** - Local LLM running on localhost:11434
6. **Message Persistence** - All messages saved to database
7. **Session Statistics** - Auto-updates message count and token usage

---

## 📡 API Endpoints

### 1. Streaming Chat (Recommended)
**POST** `/api/v1/chat/stream`

**Description:** Real-time chat with streaming response. Text appears as it's generated!

**Headers:**
```
Authorization: Bearer <jwt_token>
Content-Type: application/json
```

**Request Body:**
```json
{
  "message": "Explain quantum computing in simple terms",
  "model": "llama3",              // Optional (default: llama3)
  "sessionId": "uuid-string",      // Optional (auto-creates if not provided)
  "sessionTitle": "Quantum Chat"   // Optional (used when auto-creating session)
}
```

**Response:** Server-Sent Events (SSE) Stream

```
event: session
data: {"sessionId":"abc-123","userMessageId":456}

event: message
data: Quantum

event: message
data:  computing

event: message
data:  is...

event: done
data: {"assistantMessageId":457}
```

**Client-Side Example (JavaScript):**
```javascript
const eventSource = new EventSource('/api/v1/chat/stream', {
  headers: {
    'Authorization': 'Bearer YOUR_TOKEN'
  }
});

eventSource.addEventListener('session', (e) => {
  const { sessionId, userMessageId } = JSON.parse(e.data);
  console.log('Session:', sessionId);
});

eventSource.addEventListener('message', (e) => {
  const chunk = e.data;
  console.log('Chunk:', chunk);
  // Append to UI
});

eventSource.addEventListener('done', (e) => {
  const { assistantMessageId } = JSON.parse(e.data);
  console.log('Complete! Message ID:', assistantMessageId);
  eventSource.close();
});
```

---

### 2. Non-Streaming Chat
**POST** `/api/v1/chat`

**Description:** Wait for complete LLM response before returning.

**Request Body:**
```json
{
  "message": "What is the capital of France?",
  "model": "llama3",
  "sessionId": "uuid-string",
  "sessionTitle": "Geography Quiz"
}
```

**Response:** `200 OK`
```json
{
  "sessionId": "abc-123-def-456",
  "userMessageId": 123,
  "assistantMessageId": 124,
  "userMessage": "What is the capital of France?",
  "assistantMessage": "The capital of France is Paris...",
  "model": "llama3",
  "tokenCount": 45,
  "timestamp": "2025-10-05T14:30:00",
  "isNewSession": false
}
```

---

### 3. Chat for Specific Session (Streaming)
**POST** `/api/v1/chat/sessions/{sessionId}/stream`

**Description:** Stream chat for an existing session (sessionId in URL).

**Request Body:**
```json
{
  "message": "Continue our conversation..."
}
```

**Response:** SSE Stream (same format as endpoint 1)

---

### 4. Chat for Specific Session (Non-Streaming)
**POST** `/api/v1/chat/sessions/{sessionId}`

**Description:** Non-streaming chat for an existing session.

**Request Body:**
```json
{
  "message": "Tell me more"
}
```

**Response:** Same as endpoint 2

---

## 🔄 How It Works

### Flow Diagram

```
User sends message
     ↓
[Auto-create session if needed]
     ↓
Save USER message to DB
     ↓
Fetch conversation history
     ↓
Build context (last N messages)
     ↓
Call Ollama API (Llama3)
     ↓
[Stream response OR wait for complete]
     ↓
Save ASSISTANT message to DB
     ↓
Update session stats
     ↓
Return response to user
```

### Auto-Session Creation

If `sessionId` is **not provided**:
1. New session is created automatically
2. Title from `sessionTitle` (or defaults to "New Chat")
3. Model from `model` field (or defaults to "llama3")
4. `isNewSession: true` in response

If `sessionId` **is provided**:
1. Verifies session exists
2. Verifies user owns the session
3. Uses existing session
4. `isNewSession: false` in response

---

## 💾 Database Operations

### Messages Saved

1. **USER Message:**
   - Role: `USER`
   - Content: User's input
   - Timestamp: Auto-generated
   - Token count: Estimated (length / 4)

2. **ASSISTANT Message:**
   - Role: `ASSISTANT`
   - Content: LLM response
   - Timestamp: Auto-generated
   - Token count: Estimated

### Session Updates

After each chat:
- `messageCount` incremented by 2 (user + assistant)
- `tokenUsage` updated with total tokens
- `lastAccessedAt` updated to current time

---

## 🎯 Conversation Context

The system maintains conversation history:

1. Retrieves all messages from session (ordered by timestamp)
2. Converts to Ollama format
3. Sends full history to LLM
4. LLM has complete context for coherent responses

**Example Context:**
```json
[
  {"role": "user", "content": "What is AI?"},
  {"role": "assistant", "content": "AI is..."},
  {"role": "user", "content": "Give me an example"},
  {"role": "assistant", "content": "For example..."},
  {"role": "user", "content": "Tell me more"} // Current message
]
```

---

## 🚀 Streaming vs Non-Streaming

### Streaming (Recommended) ✅

**Pros:**
- Real-time user experience
- Text appears as it's generated
- Better perceived performance
- Can show progress indicator
- More engaging UI

**Cons:**
- Slightly more complex client code
- Requires SSE support
- Need to handle connection management

**Use When:**
- Building chat interfaces
- Want typing effect
- Long responses expected

### Non-Streaming

**Pros:**
- Simpler client implementation
- Single HTTP request/response
- Easier error handling
- Better for API-to-API calls

**Cons:**
- User waits for complete response
- No progress indication
- Appears slower (even if same speed)

**Use When:**
- Building APIs for other services
- Batch processing
- Don't need UI streaming

---

## 🛡️ Security

- ✅ JWT Authentication required
- ✅ User ID extracted from token
- ✅ Session ownership verified
- ✅ Cannot access other users' sessions
- ✅ Input validation (max 10,000 chars)

---

## 📊 Request Validation

### ChatRequest Fields

| Field | Required | Max Length | Default | Notes |
|-------|----------|------------|---------|-------|
| `message` | ✅ Yes | 10,000 chars | - | User's message |
| `model` | ❌ No | 50 chars | `llama3` | LLM model to use |
| `sessionId` | ❌ No | - | Auto-created | Existing session ID |
| `sessionTitle` | ❌ No | 255 chars | "New Chat" | For new sessions |

---

## ⚙️ Configuration

### application.properties

```properties
# Ollama Configuration
app.ai.ollama.enabled=true
app.ai.ollama.base-url=http://localhost:11434
app.ai.ollama.default-model=llama3
app.ai.ollama.api-key=ollama
```

### Dependencies Added

```xml
<!-- WebFlux for Reactive HTTP Client and Streaming -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-webflux</artifactId>
</dependency>
```

---

## 🧪 Testing

### 1. Test with cURL (Non-Streaming)

```bash
curl -X POST http://localhost:8080/api/v1/chat \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "message": "What is the meaning of life?"
  }'
```

### 2. Test with cURL (Streaming)

```bash
curl -X POST http://localhost:8080/api/v1/chat/stream \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -H "Accept: text/event-stream" \
  -d '{
    "message": "Write a short poem"
  }'
```

---

## 📁 Files Created

```
config/
  └── OllamaConfig.java              // Ollama configuration

dto/chat/
  ├── ChatRequest.java                // Chat request DTO
  └── ChatResponse.java               // Chat response DTO

dto/ollama/
  ├── OllamaMessage.java              // Ollama message format
  ├── OllamaChatRequest.java          // Ollama API request
  └── OllamaChatResponse.java         // Ollama API response

services/
  ├── OllamaService.java              // LLM communication
  └── ChatService.java                // Chat orchestration

controllers/
  └── ChatController.java             // REST endpoints
```

---

## 🎊 Success Criteria

After implementation, you can:

✅ Send messages and get LLM responses  
✅ See streaming responses in real-time  
✅ Auto-create sessions on first message  
✅ Continue conversations with context  
✅ View message history in database  
✅ Track token usage per session  
✅ Use both streaming and non-streaming modes  

---

## ⏭️ Next Steps (Phase 2)

Now that chat works, we'll add Message CRUD:

1. **GET** `/api/v1/sessions/{sessionId}/messages` - Read message history
2. **GET** `/api/v1/messages/{messageId}` - Read single message
3. **PUT** `/api/v1/messages/{messageId}` - Edit message
4. **DELETE** `/api/v1/messages/{messageId}` - Delete message
5. **POST** `/api/v1/sessions/{sessionId}/regenerate` - Regenerate last response

---

## 🎯 Key Features

✨ **Auto-Session Creation** - No manual session management needed  
✨ **Streaming Support** - Real-time chat experience  
✨ **Context-Aware** - Full conversation history  
✨ **Token Tracking** - Monitors usage automatically  
✨ **Clean Architecture** - Separated concerns (Service → Controller)  
✨ **Production Ready** - Error handling, logging, validation  

🚀 **Chat is now fully functional!** Test it and let me know how it works!
