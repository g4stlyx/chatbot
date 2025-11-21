# 🤖 Full-Stack AI Chatbot Application

A production-ready, enterprise-grade chatbot application built with Spring Boot and React, featuring advanced security, admin management, and AI integration with Llama3.

> Inspired by [MCP-Server](https://github.com/BilgisayarKavramlari/MCP-Server)

[![Java](https://img.shields.io/badge/Java-17-orange.svg)](https://openjdk.org/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.4.4-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![React](https://img.shields.io/badge/React-18+-blue.svg)](https://reactjs.org/)
[![MySQL](https://img.shields.io/badge/MySQL-8.0-blue.svg)](https://www.mysql.com/)
[![Redis](https://img.shields.io/badge/Redis-Cache-red.svg)](https://redis.io/)
[![Docker](https://img.shields.io/badge/Docker-Enabled-blue.svg)](https://www.docker.com/)

## 📋 Table of Contents

- [Features](#-features)
- [Architecture](#-architecture)
- [Tech Stack](#-tech-stack)
- [Project Structure](#-project-structure)
- [Getting Started](#-getting-started)
- [API Documentation](#-api-documentation)
- [Security Features](#-security-features)
- [Admin Panel](#-admin-panel)
- [Contributing](#-contributing)
- [License](#-license)

## ✨ Features

### Core Functionality
- 💬 **AI-Powered Chat** - Integration with Llama3 via Ollama for intelligent conversations
- 🔐 **Secure Authentication** - JWT-based auth with email verification and password reset
- 📝 **Session Management** - Create, update, delete, and organize chat sessions
- 🔍 **Search Functionality** - Search chat sessions by title
- 🌐 **Public Chat Sharing** - Share chat sessions publicly and copy others' conversations
- 💾 **Complete Message CRUD** - Edit, delete, and regenerate AI responses
- 👥 **User Profiles** - Profile management with password change functionality

### Advanced Features
- 🛡️ **Prompt Injection Protection** - Multi-layer security with pattern detection and logging
- ⚡ **Rate Limiting** - Email verification and API endpoint protection
- 📊 **Admin Dashboard** - Comprehensive admin panel with activity logging
- 🔒 **Role-Based Access Control** - Multi-level admin hierarchy (Level 0-2)
- 📧 **Email Notifications** - Security alerts and verification emails
- 🔄 **Message Regeneration** - Re-generate AI responses with different models
- 🎯 **Context Management** - Smart context window limiting (last 20 messages)

### Security & Monitoring
- 🔐 **Argon2 Password Hashing** - Industry-standard password security
- 🚨 **Security Event Logging** - Track prompt injection attempts with email alerts
- 📋 **Admin Activity Logging** - 38 operations logged across 6 services
- 🔍 **Token Management** - Secure handling of verification and reset tokens
- 🌐 **CORS Configuration** - Secure cross-origin resource sharing

## 🏗️ Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                         Frontend                             │
│              React + Vite + TailwindCSS                      │
│         (Protected Routes, Context API, Axios)               │
└───────────────────────┬─────────────────────────────────────┘
                        │ HTTP/REST API
┌───────────────────────▼─────────────────────────────────────┐
│                     Spring Boot Backend                      │
│  ┌──────────────┬──────────────┬──────────────────────────┐ │
│  │ Controllers  │  Services    │  Security Layer          │ │
│  │  (REST API)  │  (Business)  │  (JWT, Filters)          │ │
│  └──────────────┴──────────────┴──────────────────────────┘ │
└───────────────────────┬─────────────────────────────────────┘
                        │
        ┌───────────────┼───────────────┐
        │               │               │
┌───────▼──────┐ ┌──────▼──────┐ ┌─────▼──────┐
│    MySQL     │ │    Redis    │ │   Ollama   │
│  (Database)  │ │   (Cache)   │ │  (Llama3)  │
└──────────────┘ └─────────────┘ └────────────┘
```

## 🛠️ Tech Stack

### Backend
- **Framework:** Spring Boot 3.4.4
- **Language:** Java 17
- **Security:** Spring Security + JWT
- **Database:** MySQL 8.0
- **Cache:** Redis
- **ORM:** Spring Data JPA
- **Validation:** Jakarta Validation
- **Password:** Argon2 (Bouncy Castle)
- **Email:** Spring Mail (Gmail SMTP)
- **AI Integration:** Ollama (Llama3)

### Frontend
- **Framework:** React 18+
- **Build Tool:** Vite
- **Styling:** TailwindCSS
- **HTTP Client:** Axios
- **State Management:** Context API
- **Routing:** React Router

### DevOps
- **Containerization:** Docker + Docker Compose
- **API Testing:** Postman Collections (6 collections included)
- **Documentation:** Comprehensive Markdown docs

## 📁 Project Structure

```
chatbot/
├── backend/                    # Spring Boot Backend
│   ├── src/main/java/com/g4/chatbot/
│   │   ├── config/            # Security, CORS, Email configs
│   │   ├── controllers/       # REST API endpoints
│   │   ├── dto/               # Data Transfer Objects
│   │   ├── exception/         # Custom exceptions
│   │   ├── filters/           # JWT filters
│   │   ├── models/            # JPA entities
│   │   ├── repos/             # JPA repositories
│   │   ├── security/          # Security utilities
│   │   └── services/          # Business logic
│   ├── docs/                  # API documentation
│   ├── postman_files/         # Postman collections
│   └── pom.xml               # Maven dependencies
│
├── frontend/                  # React Frontend
│   ├── src/
│   │   ├── components/       # React components
│   │   ├── context/          # Context providers
│   │   ├── hooks/            # Custom hooks
│   │   ├── pages/            # Page components
│   │   └── services/         # API services
│   ├── package.json          # npm dependencies
│   └── vite.config.js        # Vite configuration
│
├── docs/                      # Project documentation
├── tools/                     # Utility scripts
├── docker-compose.yml         # Docker configuration
└── README.md                 # This file
```

## 🚀 Getting Started

### Prerequisites

- **Java 17+** - [Download](https://adoptium.net/)
- **Maven 3.8+** - [Download](https://maven.apache.org/download.cgi)
- **Node.js 18+** - [Download](https://nodejs.org/)
- **MySQL 8.0** - [Download](https://dev.mysql.com/downloads/)
- **Redis** - [Download](https://redis.io/download/)
- **Ollama** - [Download](https://ollama.ai/)
- **Docker** (Optional) - [Download](https://www.docker.com/)

### Quick Start with Docker

```bash
# Clone the repository
git clone https://github.com/g4stlyx/chatbot.git
cd chatbot

# Start all services with Docker Compose
docker-compose up -d

# The application will be available at:
# Backend: http://localhost:8080
# Frontend: http://localhost:3000
```

### Manual Setup

#### 1. Setup Backend

```bash
cd backend

# Configure environment variables
cp .env.example .env
# Edit .env with your database credentials

# Install dependencies and run
mvn clean install
mvn spring-boot:run

# Backend will start at http://localhost:8080
```

#### 2. Setup Database

```sql
-- Create database
CREATE DATABASE chatbot_db;

-- Tables are auto-created by JPA
```

#### 3. Setup Ollama & Pull Llama3

```bash
# Install Ollama (if not installed)
# Visit https://ollama.ai/

# Pull Llama3 model
ollama pull llama3

# Verify Ollama is running
curl http://localhost:11434/api/tags
```

#### 4. Setup Frontend

```bash
cd frontend

# Install dependencies
npm install

# Start development server
npm run dev

# Frontend will start at http://localhost:3000
```

### Default Admin Account

After first run, create an admin account:
```bash
# Use the registration endpoint with admin flag
# Or seed the database with admin credentials
```

## 📚 API Documentation

### Postman Collections

The project includes 6 comprehensive Postman collections in `backend/postman_files/`:

1. **0auth.postman_collection.json** - Authentication endpoints
2. **1chat_sessions.postman_collection.json** - Chat session management (15 requests)
3. **2chatbot(llama)_api_phase1.postman_collection.json** - Chat API
4. **3messages_phase2.postman_collection.json** - Message CRUD operations (6 requests)
5. **4profiles.postman_collection.json** - User profile management
6. **5admin_panel_api.postman_collection.json** - Admin operations

### Key Endpoints

#### Authentication
```
POST /api/v1/auth/register        # Register new user
POST /api/v1/auth/login           # Login user
POST /api/v1/auth/verify-email    # Verify email
POST /api/v1/auth/request-reset   # Request password reset
POST /api/v1/auth/reset-password  # Reset password
```

#### Chat Sessions
```
GET    /api/v1/sessions                        # Get user's sessions
POST   /api/v1/sessions                        # Create session
GET    /api/v1/sessions/{id}                   # Get session
PUT    /api/v1/sessions/{id}                   # Update session
DELETE /api/v1/sessions/{id}                   # Delete session
GET    /api/v1/sessions/search?q={term}        # Search sessions
PATCH  /api/v1/sessions/{id}/visibility        # Toggle public/private
GET    /api/v1/sessions/public                 # Get public sessions
POST   /api/v1/sessions/public/{id}/copy       # Copy public session
```

#### Messages
```
GET    /api/v1/sessions/{id}/messages          # Get messages
GET    /api/v1/messages/{id}                   # Get single message
PUT    /api/v1/messages/{id}                   # Edit message
DELETE /api/v1/messages/{id}                   # Delete message
POST   /api/v1/sessions/{id}/regenerate        # Regenerate response
GET    /api/v1/sessions/public/{id}/messages   # Get public session messages
```

#### Chat
```
POST /api/v1/chat/send                          # Send message (non-streaming)
```

## 🔒 Security Features

### 1. Prompt Injection Protection

Multi-layered defense system:
- ✅ System prompt enforcement (role and purpose definition)
- ✅ Input validation and sanitization
- ✅ Pattern detection (malicious keywords/phrases)
- ✅ Context window management (last 20 messages)
- ✅ Database logging with severity levels
- ✅ Email alerts to admins (threshold: 3+ attempts)
- ✅ Admin panel for viewing injection logs

### 2. Rate Limiting

- Email verification: 1 request per 60 seconds
- Configurable rate limits per endpoint

### 3. Authentication & Authorization

- JWT-based stateless authentication
- Token expiration and refresh
- Role-based access control (USER, ADMIN levels 0-2)
- Argon2 password hashing

### 4. Account Security

- Email verification required
- Account locking after failed attempts
- Password reset with secure tokens
- Active/inactive account status

## 👑 Admin Panel

### Admin Hierarchy

- **Level 0** (Super Admin) - Full access including logs and tokens
- **Level 1** (Admin) - User and session management
- **Level 2** (Moderator) - Limited content moderation

### Admin Capabilities

#### User Management (9 operations)
- Create, update, delete users
- Activate/deactivate accounts
- Unlock locked accounts
- Reset user passwords
- View user list and details

#### Session Management (6 operations)
- View, delete, archive sessions
- Flag/unflag inappropriate sessions
- Access all user sessions

#### Message Management (6 operations)
- View, delete messages
- Flag/unflag inappropriate content
- Access message history

#### System Monitoring (10 operations)
- View admin activity logs (38 operations tracked)
- Manage verification tokens
- Manage password reset tokens
- View prompt injection logs
- Bulk token cleanup

### Activity Logging

All admin operations are logged with:
- Admin ID and username
- Operation type (CREATE, UPDATE, DELETE, READ, etc.)
- IP address and user agent
- Timestamp and detailed context
- Target resource information

## 📖 Documentation

Comprehensive documentation available in `/backend/docs/`:

- `CHAT_SEARCH_AND_SHARING_FEATURES.md` - New features guide
- `ADMIN_PANEL_COMPLETE_SUMMARY.md` - Admin panel documentation
- `ADMIN_ACTIVITY_LOGGING_FINAL_SUMMARY.md` - Activity logging guide
- `PHASE1_COMPLETE.md` - Phase 1 implementation
- `PHASE2_IMPLEMENTATION_SUMMARY.md` - Phase 2 features
- `PROFILE_FEATURE_README.md` - Profile management
- And 20+ more detailed guides

## 🧪 Testing

### Backend Testing
```bash
cd backend
mvn test
```

### Frontend Testing
```bash
cd frontend
npm test
```

### API Testing
Import Postman collections from `backend/postman_files/` and test all endpoints.

## 🔧 Configuration

### Backend Configuration

Key configuration in `application.properties`:

```properties
# Database
spring.datasource.url=jdbc:mysql://localhost:3306/chatbot_db
spring.datasource.username=your_username
spring.datasource.password=your_password

# JWT
jwt.secret=your-secret-key
jwt.expiration=86400000

# Email
spring.mail.host=smtp.gmail.com
spring.mail.username=your-email@gmail.com
spring.mail.password=your-app-password

# Ollama
ollama.base-url=http://localhost:11434
```

### Frontend Configuration

Configuration in `.env`:

```env
VITE_API_BASE_URL=http://localhost:8080
VITE_APP_NAME=Chatbot AI
```

## 🤝 Contributing

Contributions are welcome! Please follow these steps:

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## 📝 TODO

See `backend/TODO.md` for the complete task list and roadmap.

### Upcoming Features
- [ ] Project grouping for chat sessions
- [ ] Ready-made prompt templates
- [ ] AI persona system (like Gemini Gems)
- [ ] Output filtering for AI responses
- [ ] Advanced filtering options
- [ ] Authentication error logging (401, 403, 404)
- [ ] OpenAI integration option

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 👏 Acknowledgments

- Inspired by [MCP-Server](https://github.com/BilgisayarKavramlari/MCP-Server)
- Built with Spring Boot and React
- AI powered by Ollama and Llama3

## 📧 Contact

For questions or feedback, please open an issue on GitHub.

---

**⭐ If you find this project helpful, please consider giving it a star!**
