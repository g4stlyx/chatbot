# Chatbot Frontend

A modern React-based frontend for the Chatbot AI application built with Vite.

## 🚀 Features

- ✅ **User Authentication** - Login and registration with JWT
- ✅ **Real-time Chat** - Chat with AI assistant (Llama3 via Ollama)
- ✅ **Session Management** - Create, view, and delete chat sessions
- ✅ **Message History** - Full conversation persistence
- ✅ **Responsive Design** - Clean and modern UI
- ✅ **Protected Routes** - Secure authenticated pages
- 🚧 **Streaming Support** - Real-time response streaming (hook ready)

## 📁 Project Structure

```
frontend/
├── src/
│   ├── components/
│   │   ├── auth/
│   │   │   └── ProtectedRoute.jsx    # Route protection
│   │   └── chat/
│   │       ├── ChatWindow.jsx         # Main chat interface
│   │       ├── MessageList.jsx        # Message display
│   │       ├── MessageInput.jsx       # Message input field
│   │       └── Sidebar.jsx            # Session sidebar
│   ├── context/
│   │   ├── AuthContext.jsx            # Authentication state
│   │   └── ChatContext.jsx            # Chat state management
│   ├── hooks/
│   │   └── useStreamingChat.js        # SSE streaming hook
│   ├── pages/
│   │   ├── ChatPage.jsx               # Main chat page
│   │   ├── LoginPage.jsx              # Login page
│   │   └── RegisterPage.jsx           # Registration page
│   ├── services/
│   │   └── api.js                     # API service layer
│   ├── App.jsx                        # Main app component
│   ├── App.css                        # App styles
│   ├── main.jsx                       # Entry point
│   └── index.css                      # Global styles
├── index.html                         # HTML template
├── vite.config.js                     # Vite configuration
├── package.json                       # Dependencies
└── .env                               # Environment variables
```

## 🛠️ Setup Instructions

### Prerequisites

- Node.js 18+ and npm
- Backend API running on `http://localhost:8080`

### Installation

1. **Install Dependencies**

   ```bash
   cd frontend
   npm install
   ```

2. **Configure Environment**

   The `.env` file is already configured:

   ```
   VITE_API_BASE_URL=http://localhost:8080
   VITE_APP_NAME=Chatbot AI
   ```

3. **Start Development Server**

   ```bash
   npm run dev
   ```

   The app will be available at `http://localhost:3000`

4. **Build for Production**
   ```bash
   npm run build
   ```

## 🎯 Usage

### 1. Register/Login

- Navigate to `http://localhost:3000`
- Create a new account or login with existing credentials
- JWT token is automatically stored and managed

### 2. Start Chatting

- Click "New Chat" to start a conversation
- Type your message and press Enter (Shift+Enter for new line)
- AI responses appear in real-time

### 3. Manage Sessions

- View all your chat sessions in the left sidebar
- Click on a session to view its history
- Delete sessions using the 🗑️ button

## 🔧 Key Technologies

- **React 18** - UI library
- **React Router** - Client-side routing
- **Axios** - HTTP client with interceptors
- **Vite** - Fast build tool and dev server
- **Context API** - State management
- **date-fns** - Date formatting utilities

## 🌐 API Integration

The frontend connects to the backend API with:

- **Base URL**: `http://localhost:8080`
- **Authentication**: JWT Bearer tokens
- **Auto-retry**: Automatic token refresh on 401
- **Proxy**: Vite dev server proxies `/api` to backend

### API Endpoints Used

```javascript
// Authentication
POST /api/auth/register
POST /api/auth/login
POST /api/auth/logout

// Chat
POST /api/v1/chat
POST /api/v1/chat/sessions/:id

// Sessions
GET /api/v1/sessions
GET /api/v1/sessions/:id
DELETE /api/v1/sessions/:id

// Messages
GET /api/v1/sessions/:sessionId/messages
DELETE /api/v1/messages/:messageId
```

## 🎨 Customization

### Styling

- Edit `src/index.css` for global styles
- Edit `src/App.css` for auth page styles
- Edit `src/pages/ChatPage.css` for chat interface styles

### Colors

CSS variables in `src/index.css`:

```css
--primary-color: #4f46e5;
--bg-color: #f9fafb;
--surface-color: #ffffff;
--text-primary: #111827;
--text-secondary: #6b7280;
```

## 🚧 Future Enhancements

- [ ] Implement streaming chat (hook already created)
- [ ] Message editing functionality
- [ ] Message regeneration
- [ ] Dark mode support
- [ ] Markdown rendering for AI responses
- [ ] File upload support
- [ ] Export chat history
- [ ] User profile management

## 📝 Development Scripts

```bash
npm run dev      # Start dev server
npm run build    # Build for production
npm run preview  # Preview production build
npm run lint     # Lint code
```

## 🐛 Troubleshooting

**CORS Issues**

- Ensure backend CORS is configured for `http://localhost:3000`
- Check `application.properties` in backend

**API Connection**

- Verify backend is running on port 8080
- Check browser console for network errors
- Verify JWT token in localStorage

**Login Issues**

- Clear localStorage and try again
- Check backend logs for authentication errors
- Verify email and password are correct

## 📄 License

This project is part of the full-stack Chatbot application.
