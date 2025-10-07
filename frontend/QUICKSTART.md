# Quick Start Guide - Chatbot Frontend

## 🚀 Get Started in 3 Steps

### Step 1: Install Dependencies

```bash
cd c:\chatbot\chatbot\frontend
npm install
```

### Step 2: Make Sure Backend is Running

The backend API should be running on `http://localhost:8080`

If not started yet:

```bash
cd c:\chatbot\chatbot\mcp-server
mvn spring-boot:run
```

### Step 3: Start Frontend

```bash
npm run dev
```

Open your browser and go to: **http://localhost:3000**

## 📝 First Time Usage

1. **Register an Account**

   - Click "Sign up" on the login page
   - Fill in username, email, and password
   - You'll be automatically logged in

2. **Start Chatting**

   - Click "New Chat" button
   - Type your message
   - Press Enter to send (Shift+Enter for new line)

3. **Manage Conversations**
   - All conversations appear in the left sidebar
   - Click to switch between conversations
   - Use 🗑️ button to delete conversations

## 🔧 Troubleshooting

**Cannot connect to backend?**

- Check if backend is running on port 8080
- Check backend console for errors
- Make sure Redis and MySQL are running

**Login not working?**

- Clear browser localStorage
- Check backend logs
- Verify user exists in database

**Package installation fails?**

- Try: `npm cache clean --force`
- Then: `npm install` again

## 📁 Project Files

All your React code is in: `c:\chatbot\chatbot\frontend\src\`

- `pages/` - Login, Register, Chat pages
- `components/` - Reusable UI components
- `context/` - Global state management
- `services/` - API calls
- `hooks/` - Custom React hooks

## 🎯 What's Included

✅ Complete authentication system
✅ Real-time chat interface
✅ Session/conversation management
✅ Message history
✅ Responsive design
✅ Auto token refresh
✅ Protected routes

Enjoy your chatbot! 🤖💬
