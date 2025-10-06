### user stuff
* user profile, password change
* admin profile, password change
* ✅ chatSession create, read, update, delete (COMPLETED)
    * ✅ message CRUD (COMPLETED - Phase 2)
        * ✅ GET /sessions/{sessionId}/messages - Get conversation history
        * ✅ GET /messages/{messageId} - Get single message
        * ✅ PUT /messages/{messageId} - Edit message (with optional regenerate)
        * ✅ DELETE /messages/{messageId} - Delete message (cascade for user msgs)
        * ✅ POST /sessions/{sessionId}/regenerate - Regenerate last response
    * 🔄 chat sharing? (is_public) - Basic support added, needs testing
    * user a accessing user b's private chat should be tested too

---
### admin panel stuff
* CRUD users
* CRUD admins (staircase style)
* CRUD chatSession info, with their messages
* CRUD messages
* read adminActivityLog
* read passwordResetTokens, verificationTokens (kss'den al)

---
### Frontend