import React, { useState } from "react";
import "./MessageActions.css";

const MessageActions = ({
  message,
  onEdit,
  onDelete,
  onRegenerate,
  isStreaming,
}) => {
  const [showActions, setShowActions] = useState(false);

  const isUserMessage = message.role === "USER";
  const isAssistantMessage = message.role === "ASSISTANT";

  // Don't show actions while streaming
  if (isStreaming) {
    return null;
  }

  const handleEdit = () => {
    if (onEdit) {
      onEdit(message);
    }
  };

  const handleDelete = () => {
    if (window.confirm("Are you sure you want to delete this message?")) {
      if (onDelete) {
        onDelete(message.id);
      }
    }
  };

  const handleRegenerate = () => {
    if (onRegenerate) {
      onRegenerate(message.sessionId);
    }
  };

  return (
    <div
      className="message-actions"
      onMouseEnter={() => setShowActions(true)}
      onMouseLeave={() => setShowActions(false)}
    >
      {showActions && (
        <div className="message-actions-buttons">
          {/* Edit button - only for user messages */}
          {isUserMessage && (
            <button
              className="action-btn edit-btn"
              onClick={handleEdit}
              title="Edit message"
              aria-label="Edit message"
            >
              <svg
                width="16"
                height="16"
                viewBox="0 0 24 24"
                fill="none"
                stroke="currentColor"
                strokeWidth="2"
              >
                <path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7" />
                <path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z" />
              </svg>
            </button>
          )}

          {/* Delete button - for both user and assistant messages */}
          <button
            className="action-btn delete-btn"
            onClick={handleDelete}
            title="Delete message"
            aria-label="Delete message"
          >
            <svg
              width="16"
              height="16"
              viewBox="0 0 24 24"
              fill="none"
              stroke="currentColor"
              strokeWidth="2"
            >
              <polyline points="3 6 5 6 21 6" />
              <path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2" />
            </svg>
          </button>

          {/* Regenerate button - only for assistant messages */}
          {isAssistantMessage && (
            <button
              className="action-btn regenerate-btn"
              onClick={handleRegenerate}
              title="Regenerate response"
              aria-label="Regenerate response"
            >
              <svg
                width="16"
                height="16"
                viewBox="0 0 24 24"
                fill="none"
                stroke="currentColor"
                strokeWidth="2"
              >
                <polyline points="23 4 23 10 17 10" />
                <polyline points="1 20 1 14 7 14" />
                <path d="M3.51 9a9 9 0 0 1 14.85-3.36L23 10M1 14l4.64 4.36A9 9 0 0 0 20.49 15" />
              </svg>
            </button>
          )}
        </div>
      )}
    </div>
  );
};

export default MessageActions;
