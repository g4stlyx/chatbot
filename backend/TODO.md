### user stuff
* ✅ user profile, password change
* ✅ admin profile, password change
* ✅ chatSession create, read, update, delete (COMPLETED)
    * ✅ message CRUD (COMPLETED - Phase 2)
        * ✅ GET /sessions/{sessionId}/messages - Get conversation history
        * ✅ GET /messages/{messageId} - Get single message
        * ✅ PUT /messages/{messageId} - Edit message (with optional regenerate)
        * ✅ DELETE /messages/{messageId} - Delete message (cascade for user msgs)
        * ✅ POST /sessions/{sessionId}/regenerate - Regenerate last response

---
### admin panel stuff
* ✅ CRUD users
* ✅ CRUD admins (staircase style)
* ✅ CRUD chatSession info, with their messages
* ✅ CRUD messages
* ✅ read adminActivityLog (only level 0 admins)
* ✅ read passwordResetTokens, verificationTokens (only level 0 admins)

---
### Extras
* ✅ email uniqueness on register
* ✅ isLocked, isVerified and isActive should be checked on login.
* ✅ admin aktivitesini logla (COMPLETE - AdminActivityLogger implemented for ALL management services with READ operations)
    * ✅ AdminManagementService: 7 operations (CREATE, UPDATE, DELETE, ACTIVATE, DEACTIVATE, READ list, READ by ID)
    * ✅ UserManagementService: 9 operations (CREATE, UPDATE, DELETE, ACTIVATE, DEACTIVATE, RESET_PASSWORD, UNLOCK, READ list, READ by ID)
    * ✅ AdminSessionManagementService: 6 operations (DELETE, ARCHIVE, FLAG, UNFLAG, READ list, READ by ID)
    * ✅ AdminMessageManagementService: 6 operations (DELETE, FLAG, UNFLAG, READ list, READ by ID, READ by session)
    * ✅ AdminTokenManagementService: 8 operations (DELETE password reset token, DELETE verification token, bulk cleanup, READ password reset tokens list, READ password reset by ID, READ verification tokens list, READ verification by ID)
    * ✅ AdminActivityLogService: 2 operations (READ list, READ by ID) - Note: Creates self-referential logs
    * ✅ All operations log IP address, user agent, and detailed context
    * ✅ Async processing with fail-safe error handling
    * ✅ READ operations logged for all admin data access including sensitive tokens and activity logs
    * ✅ Total: 38 operations logged (23 CUD + 15 READ)
* ✅ verification ve password reset tokenleri kaydediliyor mu test et
* log the auth. errors like 403 or 401 (or even 404s). who tried (if req. has a token), ip, etc. info (again with async processing)
* filtering everywhere (e.g by level for admin management, by emailVerified/active/lockedUntil for user management)
* ✅ searching chats by title
* ✅ chat sharing? (toggle is_public)
    * ✅ user a accessing user b's private chat should be tested too
* projects kısmı, chatleri gruplandırmak için (gptdeki gibi)
* hazır prompt şablonları, kullanıcı ekleyebilir veya admin panelden yönetilecek şekilde olabilir (tuğberk hocanın repodaki gibi)
    * gemini'daki gem'ler tarzı bir şey olabilir
        * ismi, açıklaması, system promptu (talimatları) var. bunun üstüne prompt giriliyor.
* ✅ prompt injection protection: (COMPLETE - All Priorities + Database + Admin Panel + Output Filtering)
    * ✅ 1. system prompt to define ai's role and purpose, set clear rules (cannot be overriden by user input)
    * ✅ 2. input validation and sanitization to detect common prompt inj. patterns, filter malicious keywords/phrases, sanitizes special characters, validates message structure
    * ✅ 3. context window management to prevent context window exploitation (only sent last 20 messages)
    * ✅ 4. security exception handling with detailed logging
    * ✅ 5. database persistence (prompt_injection_logs table) with severity levels
    * ✅ 6. email alerts to admins on threshold (3+ attempts)
    * ✅ 7. admin panel API for viewing/managing injection logs
    * ✅ 8. output filtering to check if AI is revealing the system prompt, broke character etc.


### FE updates
* option for streaming or non-streaming answer on messages
* admin panel stuff