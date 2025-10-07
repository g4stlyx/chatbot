# React Frontend Structure - Complete Overview

## 📦 Installation Created

### Core Files

- ✅ `package.json` - Dependencies and scripts
- ✅ `vite.config.js` - Vite configuration with proxy
- ✅ `index.html` - HTML entry point
- ✅ `.env` - Environment variables
- ✅ `.gitignore` - Git ignore rules
- ✅ `.eslintrc.cjs` - ESLint configuration

### Application Structure

```
frontend/
├── src/
│   ├── main.jsx                      # React entry point
│   ├── App.jsx                       # Main app with routing
│   ├── App.css                       # Auth page styles
│   ├── index.css                     # Global styles
│   │
│   ├── pages/                        # Page components
│   │   ├── LoginPage.jsx            # Login page
│   │   ├── RegisterPage.jsx         # Registration page
│   │   ├── ChatPage.jsx             # Main chat interface
│   │   └── ChatPage.css             # Chat page styles
│   │
│   ├── components/                   # Reusable components
│   │   ├── auth/
│   │   │   └── ProtectedRoute.jsx   # Route protection
│   │   └── chat/
│   │       ├── ChatWindow.jsx       # Chat container
│   │       ├── MessageList.jsx      # Message display
│   │       ├── MessageInput.jsx     # Input component
│   │       └── Sidebar.jsx          # Session sidebar
│   │
│   ├── context/                      # React Context
│   │   ├── AuthContext.jsx          # Auth state
│   │   └── ChatContext.jsx          # Chat state
│   │
│   ├── services/                     # API layer
│   │   └── api.js                   # Axios config + endpoints
│   │
│   └── hooks/                        # Custom hooks
│       └── useStreamingChat.js      # SSE streaming
│
├── index.html                        # HTML template
├── vite.config.js                    # Vite config
├── package.json                      # Dependencies
├── .env                              # Environment
├── .gitignore                        # Git ignore
├── .eslintrc.cjs                     # Linting
├── README.md                         # Documentation
├── TODO.md                           # Task list
└── QUICKSTART.md                     # Quick guide
```

## 🎯 Key Features Implemented

### 1. Authentication System

- **LoginPage** - User login with JWT
- **RegisterPage** - New user registration
- **ProtectedRoute** - Secure route wrapper
- **AuthContext** - Global auth state
- Token storage in localStorage
- Automatic token injection in API calls
- Auto-redirect on 401 errors

### 2. Chat Interface

- **ChatWindow** - Main chat container
- **MessageList** - Scrollable message display
- **MessageInput** - Text input with Enter/Shift+Enter
- **Sidebar** - Session list and management
- Real-time message updates
- Session creation on first message

### 3. State Management

- **AuthContext** - User authentication state
- **ChatContext** - Chat sessions and messages
- Global state accessible via hooks
- Optimistic UI updates

### 4. API Integration

- Axios instance with interceptors
- JWT token auto-injection
- Automatic token refresh
- Error handling
- API endpoints for auth, chat, sessions, messages

### 5. Routing

- React Router v6
- Protected routes
- Auto-redirect logic
- Login/Register/Chat routes

## 🚀 Usage

### Install Dependencies

```bash
npm install
```

### Run Development Server

```bash
npm run dev
```

Server starts at: http://localhost:3000

### Build for Production

```bash
npm run build
```

### Preview Production Build

```bash
npm run preview
```

## 📡 API Endpoints Used

### Authentication

- `POST /api/auth/register` - Register user
- `POST /api/auth/login` - Login user
- `POST /api/auth/logout` - Logout user

### Chat

- `POST /api/v1/chat` - Send message (creates session)
- `POST /api/v1/chat/sessions/:id` - Send to specific session
- `POST /api/v1/chat/stream` - Streaming chat (SSE)
- `POST /api/v1/chat/sessions/:id/stream` - Streaming with session

### Sessions

- `GET /api/v1/sessions` - Get all user sessions
- `GET /api/v1/sessions/:id` - Get specific session
- `DELETE /api/v1/sessions/:id` - Delete session
- `PUT /api/v1/sessions/:id` - Update session

### Messages

- `GET /api/v1/sessions/:sessionId/messages` - Get messages
- `GET /api/v1/messages/:messageId` - Get single message
- `PUT /api/v1/messages/:messageId` - Edit message
- `DELETE /api/v1/messages/:messageId` - Delete message
- `POST /api/v1/sessions/:sessionId/regenerate` - Regenerate response

## 🎨 Styling

### CSS Variables (index.css)

```css
--primary-color: #4f46e5      /* Main brand color */
--primary-dark: #4338ca       /* Hover states */
--bg-color: #f9fafb          /* Background */
--surface-color: #ffffff      /* Cards/surfaces */
--text-primary: #111827       /* Primary text */
--text-secondary: #6b7280     /* Secondary text */
--border-color: #e5e7eb       /* Borders */
```

### Style Files

- `index.css` - Global styles and CSS variables
- `App.css` - Authentication page styles
- `ChatPage.css` - Chat interface styles (messages, sidebar, input)

## 🔧 Configuration

### Environment Variables (.env)

```
VITE_API_BASE_URL=http://localhost:8080
VITE_APP_NAME=Chatbot AI
```

### Vite Config

- Dev server on port 3000
- Proxy `/api` to backend
- React plugin enabled

### ESLint

- React 18 configuration
- React hooks rules
- React refresh plugin

## 🎓 How It Works

1. **User Visits App** → Redirected to login/register
2. **After Login** → JWT stored, redirected to /chat
3. **Chat Page** → Loads user's sessions
4. **Send Message** → Creates session (if new) → Saves message → Returns AI response
5. **Session Selection** → Loads message history
6. **Logout** → Clears token → Redirects to login

## 📚 Dependencies

### Production

- `react` - UI library
- `react-dom` - DOM rendering
- `react-router-dom` - Routing
- `axios` - HTTP client
- `date-fns` - Date utilities
- `react-markdown` - Markdown rendering (future)

### Development

- `vite` - Build tool
- `@vitejs/plugin-react` - React support
- `eslint` - Code linting
- `eslint-plugin-react` - React rules

## 🚧 Next Steps

See `TODO.md` for upcoming features:

- Streaming chat implementation
- Markdown rendering
- Message editing UI
- Dark mode
- And more...

## 📖 Documentation Files

- `README.md` - Full documentation
- `TODO.md` - Feature roadmap
- `QUICKSTART.md` - Quick start guide
- `STRUCTURE.md` - This file

---

**All files created and ready to use!** 🎉

To start developing:

```bash
cd frontend
npm install
npm run dev
```
