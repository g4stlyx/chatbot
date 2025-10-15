import React, { useState, useEffect } from "react";
import "./EditMessageModal.css";

const EditMessageModal = ({ message, onSave, onClose }) => {
  const [editedContent, setEditedContent] = useState("");
  const [regenerateResponse, setRegenerateResponse] = useState(true);
  const [isSaving, setIsSaving] = useState(false);
  const [error, setError] = useState("");

  useEffect(() => {
    if (message) {
      setEditedContent(message.content);
    }
  }, [message]);

  const handleSave = async () => {
    if (!editedContent.trim()) {
      setError("Message cannot be empty");
      return;
    }

    setIsSaving(true);
    setError("");

    try {
      await onSave({
        messageId: message.id,
        content: editedContent.trim(),
        regenerateResponse,
      });
      onClose();
    } catch (err) {
      setError(err.response?.data?.message || "Failed to save message");
    } finally {
      setIsSaving(false);
    }
  };

  const handleKeyDown = (e) => {
    if (e.key === "Escape") {
      onClose();
    }
    // Ctrl/Cmd + Enter to save
    if ((e.ctrlKey || e.metaKey) && e.key === "Enter") {
      handleSave();
    }
  };

  if (!message) return null;

  return (
    <div className="modal-overlay" onClick={onClose}>
      <div className="modal-content" onClick={(e) => e.stopPropagation()}>
        <div className="modal-header">
          <h2>Edit Message</h2>
          <button
            className="close-btn"
            onClick={onClose}
            aria-label="Close modal"
          >
            <svg
              width="24"
              height="24"
              viewBox="0 0 24 24"
              fill="none"
              stroke="currentColor"
              strokeWidth="2"
            >
              <line x1="18" y1="6" x2="6" y2="18" />
              <line x1="6" y1="6" x2="18" y2="18" />
            </svg>
          </button>
        </div>

        <div className="modal-body">
          <div className="form-group">
            <label htmlFor="message-content">Message Content</label>
            <textarea
              id="message-content"
              className="message-textarea"
              value={editedContent}
              onChange={(e) => setEditedContent(e.target.value)}
              onKeyDown={handleKeyDown}
              placeholder="Enter your message..."
              rows={8}
              autoFocus
              disabled={isSaving}
            />
          </div>

          <div className="form-group checkbox-group">
            <label className="checkbox-label">
              <input
                type="checkbox"
                checked={regenerateResponse}
                onChange={(e) => setRegenerateResponse(e.target.checked)}
                disabled={isSaving}
              />
              <span>Regenerate assistant response after editing</span>
            </label>
            <p className="help-text">
              When enabled, the assistant will generate a new response based on
              your edited message.
            </p>
          </div>

          {error && <div className="error-message">{error}</div>}
        </div>

        <div className="modal-footer">
          <button
            className="btn btn-secondary"
            onClick={onClose}
            disabled={isSaving}
          >
            Cancel
          </button>
          <button
            className="btn btn-primary"
            onClick={handleSave}
            disabled={isSaving}
          >
            {isSaving ? "Saving..." : "Save Changes"}
          </button>
        </div>

        <div className="modal-hint">
          <small>Press ESC to cancel • Ctrl+Enter to save</small>
        </div>
      </div>
    </div>
  );
};

export default EditMessageModal;
