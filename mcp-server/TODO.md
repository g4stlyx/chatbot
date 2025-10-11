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
* admin aktivitesi loglanmıyor olabilir bak (middleware gibi bir şeyle yap, fonksiyonu admin işlemlerine ekle -event streaming? apache kafka?)
* verification ve password reset tokenleri kaydediliyor mu test et (muhtemelen ediliyor ama db boş o yüzden bakmak lazım)
* filtering everywhere (e.g by level for admin management, by emailVerified/active/lockedUntil for user management)
* title ile chat arama (hem BE hem FE'e)
* 🔄 chat sharing? (is_public) - Basic support added, needs testing
    * user a accessing user b's private chat should be tested too

### FE updates
* option for streaming or non-streaming answer on messages
* admin panel stuff