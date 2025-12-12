# Backend Project Analysis & Summary

**Project:** Chatbot AI - Spring Boot Backend (MCP Server)  
**Version:** 0.0.1-SNAPSHOT  
**Last Updated:** December 12, 2025  
**Status:** ✅ Production-Ready - All Major Features Complete

---

## 🎯 Project Overview

Enterprise-grade Spring Boot backend for an AI-powered chatbot application. Integrates Ollama/Llama3 for AI responses, provides comprehensive user/admin management, session handling, email verification, password recovery, project organization, two-factor authentication, prompt injection protection, and extensive admin panel capabilities with activity logging.

---

## 🛠️ Technology Stack

### Core Framework
- **Framework:** Spring Boot 3.4.4
- **Java Version:** 17
- **Build Tool:** Maven
- **Architecture:** RESTful API with MVC pattern

### Spring Boot Starters
- **spring-boot-starter-web** - REST API
- **spring-boot-starter-webflux** - Reactive WebClient & SSE streaming
- **spring-boot-starter-data-jpa** - ORM with Hibernate
- **spring-boot-starter-security** - Authentication & Authorization
- **spring-boot-starter-validation** - Bean validation
- **spring-boot-starter-mail** - Email service
- **spring-boot-starter-thymeleaf** - Email templates
- **spring-boot-starter-data-redis** - Redis caching
- **spring-boot-starter-cache** - Cache abstraction
- **spring-boot-starter-actuator** - Health checks & monitoring
- **spring-boot-docker-compose** - Docker integration

### Database & Persistence
- **MySQL Connector J** - MySQL database driver
- **Hibernate/JPA** - ORM layer
- **Redis** - Caching and session storage

### Security
- **Argon2-JVM** (2.12) - Password hashing
- **Bouncy Castle** (1.80) - Cryptography provider
- **JJWT** (0.12.6) - JWT token generation/validation
  - jjwt-api, jjwt-impl, jjwt-jackson
- **TOTP** - Time-based One-Time Password for 2FA

### Utilities
- **Project Lombok** (edge-SNAPSHOT) - Boilerplate reduction
- **Spring Dotenv** (4.0.0) - Environment variable management

### Monitoring & Metrics
- **Micrometer Registry Prometheus** - Metrics export
- **Spring Boot Actuator** - Application monitoring

### AI Integration
- **Ollama/Llama3** - Local LLM (localhost:11434)
- **WebClient** - Non-blocking HTTP client for Ollama communication

### Infrastructure
- **Docker Compose** - MySQL + Redis containerization

---

## 📁 Project Structure

```
mcp-server/
├── src/main/java/com/g4/chatbot/
│   ├── config/                          # Configuration classes (11 files)
│   │   ├── SecurityConfig.java          # Spring Security config
│   │   ├── JwtConfig.java               # JWT configuration
│   │   ├── OllamaConfig.java            # Ollama WebClient config
│   │   ├── RedisConfig.java             # Redis configuration
│   │   ├── RateLimitConfig.java         # Rate limiting config
│   │   ├── SystemPromptConfig.java      # AI system prompt config
│   │   ├── ScheduledTasks.java          # Scheduled tasks (backup, cleanup)
│   │   └── ...
│   ├── controllers/                     # REST endpoints (17 controllers)
│   │   ├── AuthController.java          # Auth endpoints
│   │   ├── UserProfileController.java   # User profile
│   │   ├── ChatController.java          # Chat/LLM endpoints
│   │   ├── ChatSessionController.java   # Session management
│   │   ├── MessageController.java       # Message CRUD
│   │   ├── ProjectController.java       # Project management
│   │   ├── TwoFactorAuthController.java # 2FA endpoints
│   │   ├── DatabaseBackupController.java # DB backup
│   │   ├── AdminProfileController.java
│   │   ├── UserManagementController.java
│   │   ├── AdminManagementController.java
│   │   ├── AdminSessionController.java
│   │   ├── AdminMessageController.java
│   │   ├── AdminActivityLogController.java
│   │   ├── AdminTokenManagementController.java
│   │   ├── AdminPromptInjectionController.java
│   │   └── AdminAuthErrorController.java
│   ├── dto/                             # Data Transfer Objects (64 files)
│   │   ├── auth/                        # Auth DTOs
│   │   ├── chat/                        # Chat DTOs
│   │   ├── profile/                     # Profile DTOs
│   │   ├── session/                     # Session DTOs
│   │   ├── messages/                    # Message DTOs
│   │   ├── projects/                    # Project DTOs
│   │   ├── admin/                       # Admin DTOs
│   │   ├── security/                    # Security DTOs
│   │   ├── two_factor/                  # 2FA DTOs
│   │   └── ollama/                      # Ollama API DTOs
│   ├── models/                          # JPA Entities (11 entities)
│   │   ├── User.java                    # User entity
│   │   ├── Admin.java                   # Admin entity (with 2FA fields)
│   │   ├── ChatSession.java             # Session entity
│   │   ├── Message.java                 # Message entity
│   │   ├── MessageFlag.java             # Message flag entity
│   │   ├── Project.java                 # Project entity
│   │   ├── AdminActivityLog.java        # Activity log entity
│   │   ├── PasswordResetToken.java      # Password reset token
│   │   ├── VerificationToken.java       # Email verification token
│   │   ├── PromptInjectionLog.java      # Prompt injection log
│   │   └── AuthenticationErrorLog.java  # Auth error log
│   ├── repos/                           # JPA Repositories (11 repos)
│   ├── services/                        # Business Logic (26 services)
│   │   ├── auth/
│   │   │   ├── AuthService.java         # Authentication logic
│   │   │   └── EmailService.java        # Email sending
│   │   ├── user/
│   │   │   ├── UserProfileService.java
│   │   │   └── ChatSessionService.java
│   │   ├── admin/
│   │   │   ├── AdminProfileService.java
│   │   │   ├── UserManagementService.java
│   │   │   ├── AdminManagementService.java
│   │   │   ├── AdminSessionManagementService.java
│   │   │   ├── AdminMessageManagementService.java
│   │   │   ├── AdminActivityLogService.java
│   │   │   ├── AdminTokenManagementService.java
│   │   │   ├── AdminPromptInjectionService.java
│   │   │   └── AdminAuthErrorService.java
│   │   ├── ChatService.java             # Chat orchestration
│   │   ├── MessageService.java          # Message CRUD
│   │   ├── OllamaService.java           # LLM communication
│   │   ├── ProjectService.java          # Project management
│   │   ├── TwoFactorAuthService.java    # 2FA logic
│   │   ├── DatabaseBackupService.java   # DB backup
│   │   ├── PromptValidationService.java # Input validation
│   │   ├── OutputValidationService.java # Output filtering
│   │   ├── SecurityLogService.java      # Security logging
│   │   ├── AuthErrorLogService.java     # Auth error logging
│   │   ├── RateLimitService.java        # Rate limiting
│   │   ├── PasswordService.java         # Password handling
│   │   └── AdminActivityLogger.java     # Async activity logging
│   ├── security/                        # Security utilities (4 files)
│   │   ├── JwtUtils.java                # JWT helper
│   │   ├── CustomUserDetailsService.java
│   │   └── ...
│   ├── exception/                       # Custom exceptions (6 files)
│   │   ├── GlobalExceptionHandler.java
│   │   ├── ResourceNotFoundException.java
│   │   └── ...
│   └── McpServerApplication.java        # Main application
├── src/main/resources/
│   ├── application.properties           # Main configuration
│   ├── templates/                       # Email templates (Thymeleaf)
│   └── ...
├── src/test/                            # Test classes
├── docs/                                # Documentation (31 files)
├── postman_files/                       # API testing collections (8 files)
│   ├── 0auth.postman_collection.json
│   ├── 1chat_sessions.postman_collection.json
│   ├── 2chatbot(llama)_api_phase1.postman_collection.json
│   ├── 3messages_phase2.postman_collection.json
│   ├── 4profiles.postman_collection.json
│   ├── 5admin_panel_api.postman_collection.json
│   ├── 6projects.postman_collection.json
│   └── 7database_backup.postman_collection.json
├── compose.yaml                         # Docker Compose (MySQL + Redis)
├── pom.xml                             # Maven dependencies
├── README.md                           # Project documentation
├── TODO.md                             # Task tracking
└── SETUP.md                            # Setup instructions
```

---

## ✅ Completed Features

### 1. **Authentication & Authorization** ✅
- User registration with validation
- User/Admin login with JWT tokens
- Email verification system with tokens
- Password recovery (forgot/reset) with tokens
- Token expiration and refresh
- Role-based access control (USER, ADMIN)
- Admin hierarchy system (Level 0, 1, 2)
- Argon2 password hashing
- Email uniqueness validation across user/admin tables

### 2. **Two-Factor Authentication (2FA)** ✅ (NEW)
- TOTP-based 2FA for admin accounts
- QR code generation for authenticator apps
- 2FA setup and verification endpoints
- 2FA disable with code verification
- 2FA status checking
- 2FA login verification flow

### 3. **User Profile Management** ✅
- View user profile
- Update profile (email, firstName, lastName, profilePicture)
- Change password with validation
- Account deactivation/reactivation (self-service)
- Profile picture support
- Last login tracking
- Email verification status

### 4. **Admin Profile Management** ✅
- View admin profile
- View other admin profiles (hierarchy-based)
- Update admin profile
- Change admin password
- Deactivate/reactivate admin accounts (permission-based)
- Admin level and permission display

### 5. **Chat System (AI Integration)** ✅
- **Ollama/Llama3 Integration** - Local LLM on localhost:11434
- **Non-streaming chat** - Complete response delivery
- **Streaming chat (SSE)** - Real-time response generation
- **Context-aware conversations** - Full message history maintained
- **Auto-session creation** - No manual session setup needed
- **Message persistence** - All conversations saved to database
- **Session statistics** - Token usage and message counts
- **Prompt Injection Protection** - 8-layer defense system

**Chat Endpoints:**
- `POST /api/v1/chat` - Non-streaming
- `POST /api/v1/chat/stream` - Streaming (SSE)
- `POST /api/v1/chat/sessions/{id}` - Chat for specific session
- `POST /api/v1/chat/sessions/{id}/stream` - Streaming for specific session

### 6. **Chat Session Management** ✅
- Create new sessions
- List all user sessions
- Get session by ID
- Update session (rename, title)
- Delete sessions
- Archive/Unarchive sessions
- Pause/Resume sessions
- Session filtering by status
- Session statistics (message count, tokens)
- **Search sessions by title** ✅ (NEW)
- **Public/Private visibility toggle** ✅ (NEW)
- **Public session listing** ✅ (NEW)
- **Copy public sessions** ✅ (NEW)

### 7. **Message Management (Phase 2)** ✅
- **Get conversation history** - List all messages in session
- **Get single message** - Message details by ID
- **Edit messages** - Update user messages with optional regenerate
- **Delete messages** - Remove messages (cascade for user messages)
- **Regenerate responses** - Request new AI response for last message
- Message timestamps and metadata
- Role tracking (USER, ASSISTANT)

**Message Endpoints:**
- `GET /api/v1/sessions/{sessionId}/messages`
- `GET /api/v1/messages/{messageId}`
- `PUT /api/v1/messages/{messageId}`
- `DELETE /api/v1/messages/{messageId}`
- `POST /api/v1/sessions/{sessionId}/regenerate`

### 8. **Project Management** ✅ (NEW)
- Create projects to group chat sessions
- Project CRUD operations
- Customize projects with colors and icons
- Archive/Unarchive projects without deletion
- Add/remove sessions to/from projects
- Search projects by name
- Session count auto-management
- Pagination and sorting support

**Project Endpoints:**
- `POST /api/v1/projects` - Create project
- `GET /api/v1/projects` - List projects
- `GET /api/v1/projects/{id}` - Get project
- `PUT /api/v1/projects/{id}` - Update project
- `DELETE /api/v1/projects/{id}` - Delete project
- `POST /api/v1/projects/{id}/archive` - Archive
- `POST /api/v1/projects/{id}/unarchive` - Unarchive
- `POST /api/v1/projects/{id}/sessions/{sessionId}` - Add session
- `DELETE /api/v1/projects/sessions/{sessionId}` - Remove session
- `GET /api/v1/projects/search` - Search projects

### 9. **Prompt Injection Protection** ✅ (NEW)
- **System prompt enforcement** - Role and purpose definition
- **Input validation/sanitization** - Pattern detection
- **Malicious keyword filtering** - Block harmful phrases
- **Context window management** - Last 20 messages only
- **Database logging** - Severity levels (LOW, MEDIUM, HIGH, CRITICAL)
- **Email alerts** - Notify admins on threshold (3+ attempts)
- **Admin panel API** - View and manage injection logs
- **Output filtering** - Prevent system prompt leakage

### 10. **Authentication Error Logging** ✅ (NEW)
- Log 401, 403, 404 authentication errors
- Track IP address and user agent
- Track user ID if available from token
- Admin panel endpoints for viewing logs
- Filter by user, error type, IP address
- Statistics endpoint for error analysis
- Level 0 admin can delete logs

### 11. **Admin Panel - User Management** ✅
- List all users (paginated, filtered)
- Get user by ID
- Create user (admin-created accounts)
- Update user profile
- Delete user
- Activate/Deactivate user
- Reset user password
- Unlock user account
- Verify user email manually
- Filter by email verification, active status, locked status
- Activity logging for all operations

### 12. **Admin Panel - Admin Management** ✅
- List all admins (paginated, filtered)
- Get admin by ID
- Create admin (level-based permissions)
- Update admin profile
- Delete admin (staircase hierarchy)
- Activate/Deactivate admin
- Reset admin password
- Unlock admin account
- Admin level filtering (0, 1, 2)
- Staircase permission model (higher level manages lower)
- Activity logging for all operations

### 13. **Admin Panel - Session Management** ✅
- View all chat sessions across users
- Get session details by ID
- Delete sessions (any user)
- Archive/Unarchive sessions
- Flag/Unflag sessions (moderation)
- Toggle public/private visibility
- Filter by user, status, archived state
- Pagination and sorting
- Activity logging for all operations

### 14. **Admin Panel - Message Management** ✅
- View all messages across sessions
- Get message by ID
- Get messages by session ID
- Delete messages (any message)
- Flag/Unflag messages (moderation)
- Filter by session, role, flagged state
- Pagination and sorting
- Activity logging for all operations

### 15. **Admin Panel - Activity Logging** ✅ (Level 0 Only)
- **Comprehensive logging** - All admin actions tracked
- **38 operations logged:**
  - **23 CUD operations** (Create, Update, Delete)
  - **15 READ operations** (Data access monitoring)
- **Logged entities:**
  - AdminManagementService: 7 operations
  - UserManagementService: 9 operations
  - AdminSessionManagementService: 6 operations
  - AdminMessageManagementService: 6 operations
  - AdminTokenManagementService: 8 operations
  - AdminActivityLogService: 2 operations (self-referential)
- **Rich context tracking:**
  - IP address
  - User agent
  - Admin details (ID, username, email, level)
  - Action type (CREATE, UPDATE, DELETE, etc.)
  - Resource type (USER, ADMIN, SESSION, MESSAGE, etc.)
  - Detailed operation context
  - Timestamp
- **Async processing** - Non-blocking with fail-safe error handling
- **Filtering & pagination** - By admin, action, resource, date range
- **Activity statistics** - Per-admin action counts

### 16. **Admin Panel - Token Management** ✅ (Level 0 Only)
- **Password Reset Tokens:**
  - List all tokens (paginated)
  - Get token by ID
  - Delete/Invalidate token (cleanup)
  - Filter by user type, expiration
- **Verification Tokens:**
  - List all tokens (paginated)
  - Get token by ID
  - Delete/Invalidate token (cleanup)
  - Filter by user type, expiration
- **Features:**
  - Include/exclude expired tokens
  - Enriched data (username, email)
  - Security monitoring
  - Bulk cleanup capabilities

### 17. **Admin Panel - Prompt Injection Logs** ✅ (NEW)
- View all prompt injection attempts
- Get log by ID
- Delete logs (Level 0 only)
- Filter by user, severity
- Statistics endpoint
- Pagination and sorting

### 18. **Admin Panel - Auth Error Logs** ✅ (NEW)
- View all authentication error logs
- Get log by ID
- Get logs by user ID
- Get logs by IP address
- Delete logs (Level 0 only)
- Statistics endpoint
- Filter by error type, user, IP

### 19. **Database Backup System** ✅ (NEW)
- Manual backup trigger via API
- Scheduled automatic backups (daily at 3:00 AM)
- Backup status endpoint
- Email notification with backup file
- Admin-only access

### 20. **Email System** ✅
- Email verification on registration
- Password reset emails
- Resend verification email
- HTML email templates (Thymeleaf)
- Email rate limiting
- Token-based verification
- Configurable SMTP settings
- Security alert emails (prompt injection threshold)

### 21. **Security Features** ✅
- JWT-based authentication
- Argon2 password hashing
- Role-based authorization
- Protected endpoints
- Token expiration handling
- CORS configuration
- SQL injection prevention (JPA)
- XSS protection
- CSRF protection for state-changing operations
- Two-factor authentication (2FA)
- Prompt injection protection (8 layers)
- Output filtering for AI responses
- Authentication error logging

### 22. **Monitoring & Health** ✅
- Spring Boot Actuator endpoints
- Prometheus metrics export
- Application health checks
- Custom metrics tracking
- Redis health monitoring
- Database health monitoring
- Ollama health monitoring

---

## 🔌 API Endpoints Summary

### Authentication (Public)
- `POST /api/v1/auth/register` - User registration
- `POST /api/v1/auth/login` - User/Admin login
- `POST /api/v1/auth/forgot-password` - Request password reset
- `POST /api/v1/auth/reset-password` - Reset with token
- `POST /api/v1/auth/resend-verification` - Resend verification
- `GET /api/v1/auth/verify` - Email verification

### User Profile (Authenticated)
- `GET /api/v1/user/profile` - Get profile
- `PUT /api/v1/user/profile` - Update profile
- `POST /api/v1/user/profile/change-password` - Change password
- `POST /api/v1/user/profile/deactivate` - Deactivate account
- `POST /api/v1/user/profile/reactivate` - Reactivate account

### Chat (Authenticated)
- `POST /api/v1/chat` - Non-streaming chat
- `POST /api/v1/chat/stream` - Streaming chat
- `POST /api/v1/chat/sessions/{id}` - Session chat
- `POST /api/v1/chat/sessions/{id}/stream` - Session streaming

### Sessions (Authenticated)
- `GET /api/v1/sessions` - List sessions
- `POST /api/v1/sessions` - Create session
- `GET /api/v1/sessions/{id}` - Get session
- `PUT /api/v1/sessions/{id}` - Update session
- `DELETE /api/v1/sessions/{id}` - Delete session
- `POST /api/v1/sessions/{id}/archive` - Archive
- `POST /api/v1/sessions/{id}/pause` - Pause
- `POST /api/v1/sessions/{id}/activate` - Resume
- `GET /api/v1/sessions/active` - Active sessions
- `GET /api/v1/sessions/search` - Search by title
- `PATCH /api/v1/sessions/{id}/visibility` - Toggle public/private
- `GET /api/v1/sessions/public` - List public sessions
- `GET /api/v1/sessions/public/{id}` - Get public session
- `POST /api/v1/sessions/public/{id}/copy` - Copy public session

### Messages (Authenticated)
- `GET /api/v1/sessions/{sessionId}/messages` - List messages
- `GET /api/v1/messages/{messageId}` - Get message
- `PUT /api/v1/messages/{messageId}` - Edit message
- `DELETE /api/v1/messages/{messageId}` - Delete message
- `POST /api/v1/sessions/{sessionId}/regenerate` - Regenerate response

### Projects (Authenticated)
- `POST /api/v1/projects` - Create project
- `GET /api/v1/projects` - List projects
- `GET /api/v1/projects/{id}` - Get project
- `PUT /api/v1/projects/{id}` - Update project
- `DELETE /api/v1/projects/{id}` - Delete project
- `POST /api/v1/projects/{id}/archive` - Archive
- `POST /api/v1/projects/{id}/unarchive` - Unarchive
- `POST /api/v1/projects/{id}/sessions/{sessionId}` - Add session
- `DELETE /api/v1/projects/sessions/{sessionId}` - Remove session
- `GET /api/v1/projects/search` - Search projects

### 2FA (Admin)
- `POST /api/v1/admin/2fa/setup` - Setup 2FA
- `POST /api/v1/admin/2fa/verify` - Verify and enable
- `POST /api/v1/admin/2fa/disable` - Disable 2FA
- `GET /api/v1/admin/2fa/status` - Check status
- `POST /api/v1/admin/2fa/verify-login` - Verify during login

### Admin Profile (Admin)
- `GET /api/v1/admin/profile` - Get own profile
- `GET /api/v1/admin/profile/{adminId}` - Get admin profile
- `PUT /api/v1/admin/profile` - Update profile
- `POST /api/v1/admin/profile/change-password` - Change password
- `POST /api/v1/admin/profile/{adminId}/deactivate` - Deactivate
- `POST /api/v1/admin/profile/{adminId}/reactivate` - Reactivate

### Admin - User Management (Admin)
- `GET /api/v1/admin/users` - List users
- `GET /api/v1/admin/users/search` - Search users
- `POST /api/v1/admin/users` - Create user
- `GET /api/v1/admin/users/{userId}` - Get user
- `PUT /api/v1/admin/users/{userId}` - Update user
- `DELETE /api/v1/admin/users/{userId}` - Delete user
- `POST /api/v1/admin/users/{userId}/activate` - Activate
- `POST /api/v1/admin/users/{userId}/deactivate` - Deactivate
- `POST /api/v1/admin/users/{userId}/reset-password` - Reset password
- `POST /api/v1/admin/users/{userId}/unlock` - Unlock account
- `POST /api/v1/admin/users/{userId}/verify-email` - Verify email

### Admin - Admin Management (Admin, Staircase)
- `GET /api/v1/admin/admins` - List admins
- `POST /api/v1/admin/admins` - Create admin
- `GET /api/v1/admin/admins/{adminId}` - Get admin
- `PUT /api/v1/admin/admins/{adminId}` - Update admin
- `DELETE /api/v1/admin/admins/{adminId}` - Delete admin
- `POST /api/v1/admin/admins/{adminId}/activate` - Activate
- `POST /api/v1/admin/admins/{adminId}/deactivate` - Deactivate
- `POST /api/v1/admin/admins/{adminId}/reset-password` - Reset password
- `POST /api/v1/admin/admins/{adminId}/unlock` - Unlock

### Admin - Session Management (Admin)
- `GET /api/v1/admin/sessions` - List all sessions
- `GET /api/v1/admin/sessions/{sessionId}` - Get session
- `DELETE /api/v1/admin/sessions/{sessionId}` - Delete session
- `POST /api/v1/admin/sessions/{sessionId}/archive` - Archive
- `POST /api/v1/admin/sessions/{sessionId}/flag` - Flag session
- `POST /api/v1/admin/sessions/{sessionId}/unflag` - Unflag session
- `POST /api/v1/admin/sessions/{sessionId}/toggle-public` - Toggle visibility

### Admin - Message Management (Admin)
- `GET /api/v1/admin/messages` - List all messages
- `GET /api/v1/admin/messages/{messageId}` - Get message
- `GET /api/v1/admin/messages/session/{sessionId}` - Session messages
- `DELETE /api/v1/admin/messages/{messageId}` - Delete message
- `POST /api/v1/admin/messages/{messageId}/flag` - Flag message
- `POST /api/v1/admin/messages/{messageId}/unflag` - Unflag message

### Admin - Activity Logs (Level 0 Only)
- `GET /api/v1/admin/activity-logs` - List logs
- `GET /api/v1/admin/activity-logs/{logId}` - Get log

### Admin - Token Management (Level 0 Only)
- `GET /api/v1/admin/tokens/password-reset` - List reset tokens
- `GET /api/v1/admin/tokens/password-reset/{tokenId}` - Get reset token
- `DELETE /api/v1/admin/tokens/password-reset/{tokenId}` - Delete reset token
- `POST /api/v1/admin/tokens/password-reset/{tokenId}/invalidate` - Invalidate
- `GET /api/v1/admin/tokens/verification` - List verification tokens
- `GET /api/v1/admin/tokens/verification/{tokenId}` - Get verification token
- `DELETE /api/v1/admin/tokens/verification/{tokenId}` - Delete verification token
- `POST /api/v1/admin/tokens/verification/{tokenId}/invalidate` - Invalidate

### Admin - Prompt Injection Logs (Admin)
- `GET /api/v1/admin/prompt-injection-logs` - List logs
- `GET /api/v1/admin/prompt-injection-logs/{id}` - Get log
- `DELETE /api/v1/admin/prompt-injection-logs/{id}` - Delete log (Level 0)
- `GET /api/v1/admin/prompt-injection-logs/statistics` - Statistics

### Admin - Auth Error Logs (Admin)
- `GET /api/v1/admin/auth-error-logs` - List logs
- `GET /api/v1/admin/auth-error-logs/{id}` - Get log
- `GET /api/v1/admin/auth-error-logs/user/{userId}` - Logs by user
- `GET /api/v1/admin/auth-error-logs/ip/{ipAddress}` - Logs by IP
- `GET /api/v1/admin/auth-error-logs/statistics` - Statistics
- `DELETE /api/v1/admin/auth-error-logs/{id}` - Delete log (Level 0)

### Database Backup (Admin)
- `POST /api/v1/admin/database-backup/create` - Trigger backup
- `GET /api/v1/admin/database-backup/status` - Get status

**Total API Endpoints:** 90+

---

## 🚀 How to Run

### Prerequisites
- Java 17+
- Maven 3.8+
- Docker & Docker Compose
- Ollama with Llama3 model

### Setup

1. **Start Infrastructure (MySQL + Redis)**
   ```bash
   cd mcp-server
   docker-compose up -d
   ```

2. **Configure Environment**
   - Set up `.env` file with database, JWT, email credentials

3. **Install Ollama & Llama3**
   ```bash
   ollama pull llama3
   ollama serve
   ```

4. **Run Application**
   ```bash
   ./mvnw spring-boot:run
   ```

5. **Access API**
   - Base URL: `http://localhost:8080`
   - Health: `http://localhost:8080/actuator/health`

### Testing
- Import Postman collections from `postman_files/` directory (8 collections)
- All endpoints documented with examples
- Pre-configured variables for easy testing

---

## 📊 Database Schema

### Core Entities
- **users** - User accounts
- **admins** - Admin accounts with levels and 2FA
- **chat_sessions** - Conversation sessions
- **messages** - Chat messages
- **message_flags** - Message moderation flags
- **projects** - Session grouping
- **password_reset_tokens** - Password recovery tokens
- **verification_tokens** - Email verification tokens
- **admin_activity_logs** - Admin action audit trail
- **prompt_injection_logs** - Security attempt logs
- **authentication_error_logs** - Auth failure logs

### Relationships
- User → ChatSessions (1:N)
- User → Projects (1:N)
- Project → ChatSessions (1:N)
- ChatSession → Messages (1:N)
- Message → MessageFlags (1:N)
- Admin → ActivityLogs (1:N)
- User/Admin → Tokens (1:N)

---

## 📈 Performance Considerations

### Caching
- Redis for session data
- Spring Cache abstraction
- Token caching for faster validation

### Database
- Indexed columns for fast queries
- Pagination for large datasets
- Connection pooling (HikariCP)

### Async Processing
- Activity logging runs async
- Email sending async
- Non-blocking LLM communication with WebClient
- Database backup runs in background thread

### Security
- Argon2 optimized for performance
- JWT with reasonable expiration
- Rate limiting on email endpoints
- Prompt validation with caching

---

## 🚧 Known Limitations & Future Improvements

### High Priority
- [ ] **Ready-made prompt templates** - User and admin managed
- [ ] **AI persona system** - Like Gemini Gems

### Medium Priority
- [ ] **OpenAI/Claude/Gemini integration** - Multiple model options
- [ ] **Bulk operations** - Bulk user/admin actions
- [ ] **Advanced analytics** - Usage statistics, metrics dashboards
- [ ] **Audit trail export** - Export activity logs (CSV, JSON)

### Low Priority
- [ ] **WebSocket support** - Alternative to SSE for chat
- [ ] **File attachments** - Support file uploads in chat
- [ ] **Chat export** - Export conversations (PDF, JSON)
- [ ] **Multi-language support** - i18n for email templates

### Infrastructure
- [ ] **CI/CD pipeline** - Automated testing and deployment
- [ ] **Docker production image** - Optimized production Dockerfile
- [ ] **Kubernetes manifests** - K8s deployment configs
- [ ] **Log aggregation** - Centralized logging (ELK stack)

---

## 🔐 Security Best Practices

### Implemented
- ✅ Argon2 password hashing
- ✅ JWT token-based authentication
- ✅ Role-based authorization (RBAC)
- ✅ Admin hierarchy (staircase model)
- ✅ Email verification
- ✅ Password reset with secure tokens
- ✅ CORS configuration
- ✅ SQL injection prevention (JPA/Hibernate)
- ✅ Activity logging for audit trails
- ✅ Token management and cleanup
- ✅ Two-factor authentication (2FA)
- ✅ Prompt injection protection (8 layers)
- ✅ Output filtering for AI responses
- ✅ Authentication error logging
- ✅ Failed login attempt tracking
- ✅ IP-based logging

### Recommended Additions
- [ ] API key management for third-party integrations
- [ ] Session management (concurrent login limits)
- [ ] IP-based rate limiting (general)

---

**Status:** Production-ready with comprehensive features. Suitable for enterprise deployment with additional infrastructure setup.
