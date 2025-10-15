# Phase 2 Features - Quick Reference Guide

## 🎯 New Features Overview

### Message Actions

**Where:** Hover over any message  
**User Messages:** Edit ✏️ | Delete 🗑️  
**Assistant Messages:** Regenerate 🔄 | Delete 🗑️

### Session Management

**Where:** Three-dot menu (⋮) next to each session  
**Actions:** Rename | Pause | Archive | Resume | Delete

### Session Filters

**Where:** Dropdown above session list  
**Options:** All | Active | Paused | Archived

---

## 📖 Feature Guide

### ✏️ Edit Message

1. Hover over your message
2. Click the **Edit** button
3. Modify text in the modal
4. _(Optional)_ Check "Regenerate assistant response"
5. Click **Save Changes**

**Shortcuts:**

- `Ctrl+Enter` - Save
- `Esc` - Cancel

---

### 🗑️ Delete Message

1. Hover over any message
2. Click the **Delete** button
3. Confirm deletion

**Note:** Deleting a user message also removes the paired assistant response.

---

### 🔄 Regenerate Response

1. Hover over assistant message
2. Click the **Regenerate** button
3. New response replaces the old one

**Use when:** You want a different answer to the same question.

---

### ✏️ Rename Session

1. Click the three-dot menu (⋮) next to session
2. Select **Rename**
3. Type new title
4. Press `Enter` to save or `Esc` to cancel

---

### ⏸️ Pause Session

1. Click the three-dot menu (⋮) next to session
2. Select **Pause**
3. Session status changes to PAUSED (yellow dot)

**Use when:** You want to temporarily pause a conversation.

---

### ▶️ Resume Session

1. Find a PAUSED session (yellow dot)
2. Click the three-dot menu (⋮)
3. Select **Resume**
4. Session status changes to ACTIVE (green dot)

---

### 📦 Archive Session

1. Click the three-dot menu (⋮) next to session
2. Select **Archive**
3. Session status changes to ARCHIVED (gray dot)

**Use when:** You want to hide completed conversations.

---

### 📤 Unarchive Session

1. Filter by "Archived" sessions
2. Find the session you want to restore
3. Click the three-dot menu (⋮)
4. Select **Unarchive**
5. Session status changes to ACTIVE (green dot)

---

### 🔍 Filter Sessions

1. Click the filter dropdown above session list
2. Select:
   - **All** - Show all conversations
   - **Active** - Show only active (green dot)
   - **Paused** - Show only paused (yellow dot)
   - **Archived** - Show only archived (gray dot)

---

## 🎨 Status Indicators

| Status      | Color  | Meaning                       |
| ----------- | ------ | ----------------------------- |
| 🟢 ACTIVE   | Green  | Currently active conversation |
| 🟡 PAUSED   | Yellow | Temporarily paused            |
| ⚪ ARCHIVED | Gray   | Archived (hidden by default)  |

---

## ⌨️ Keyboard Shortcuts

### Edit Message Modal

- `Ctrl+Enter` or `Cmd+Enter` - Save changes
- `Esc` - Close modal without saving

### Session Rename

- `Enter` - Save new title
- `Esc` - Cancel rename

---

## 💡 Tips & Tricks

### Message Editing

- ✅ Edit your message to fix typos
- ✅ Use "Regenerate response" to get a new answer based on edited message
- ✅ Previous version is not saved (no version history yet)

### Session Organization

- 📌 Rename important conversations for easy finding
- ⏸️ Pause sessions you might continue later
- 📦 Archive old conversations to declutter
- 🔍 Use filters to focus on specific session types

### Best Practices

1. **Name your sessions** - Makes finding conversations easier
2. **Archive completed work** - Keeps sidebar clean
3. **Pause ongoing projects** - Distinguishes from active chats
4. **Use regenerate** - If answer isn't helpful, try again

---

## 🐛 Troubleshooting

### Actions not appearing?

- Make sure you're hovering over the message
- Check that messages have finished streaming
- Try refreshing the page

### Can't edit assistant messages?

- This is by design - you can only edit your own messages
- Use "Regenerate" instead to get a new response

### Session filter not working?

- Check that sessions have the correct status
- Try selecting "All" to see all sessions
- Refresh the page if needed

### Rename not saving?

- Press Enter or click outside the input
- Make sure you changed the text
- Check internet connection

---

## 📝 Feature Limitations

### Current Restrictions

- ❌ No undo for deleted messages/sessions
- ❌ No version history for edited messages
- ❌ Cannot edit messages while streaming
- ❌ No bulk actions (select multiple sessions)
- ❌ No search within conversations (yet)

### Coming Soon (Potential)

- 🔜 Message search
- 🔜 Export conversations
- 🔜 Copy message to clipboard
- 🔜 Session search
- 🔜 Undo delete

---

## 🆘 Common Questions

**Q: What happens when I delete a user message?**  
A: Both your message and the assistant's response are deleted.

**Q: What happens when I delete an assistant message?**  
A: Only the assistant's message is deleted. Your message remains.

**Q: Can I recover deleted messages?**  
A: No, deletions are permanent. Be careful!

**Q: What's the difference between Pause and Archive?**  
A: Both hide the conversation, but PAUSED implies you'll come back to it soon, while ARCHIVED is for completed conversations.

**Q: Do archived sessions get deleted automatically?**  
A: No, archived sessions stay forever until you manually delete them.

**Q: Can I have multiple sessions paused?**  
A: Yes! Pause as many as you want.

**Q: Does renaming change the conversation content?**  
A: No, only the title in the sidebar changes.

**Q: Will regenerating delete my previous response?**  
A: Yes, the old response is replaced with the new one.

---

## 🎯 Quick Action Summary

| Want to...                | Do this...                         |
| ------------------------- | ---------------------------------- |
| Fix a typo in my message  | Hover → Edit → Modify → Save       |
| Get a better answer       | Hover on bot response → Regenerate |
| Remove a message          | Hover → Delete                     |
| Organize conversations    | Three-dot menu → Rename            |
| Temporarily stop chatting | Three-dot menu → Pause             |
| Hide completed work       | Three-dot menu → Archive           |
| See only active chats     | Filter dropdown → Active           |
| Delete conversation       | Three-dot menu → Delete            |

---

**For detailed implementation info, see:** `PHASE2_IMPLEMENTATION_SUMMARY.md`
