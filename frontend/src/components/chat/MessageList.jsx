import { useEffect, useRef } from "react";
import { format } from "date-fns";

const MessageList = ({ messages }) => {
  const messagesEndRef = useRef(null);

  const scrollToBottom = () => {
    messagesEndRef.current?.scrollIntoView({ behavior: "smooth" });
  };

  useEffect(() => {
    scrollToBottom();
  }, [messages]);

  if (messages.length === 0) {
    return (
      <div className="messages-container">
        <div className="empty-state">
          <h3>👋 Start a Conversation</h3>
          <p>Ask me anything! I'm here to help.</p>
        </div>
      </div>
    );
  }

  return (
    <div className="messages-container">
      {messages.map((message) => (
        <div key={message.id} className={`message ${message.role}`}>
          <div className="message-avatar">
            {message.role === "user" ? "👤" : "🤖"}
          </div>
          <div>
            <div className="message-content">{message.content}</div>
            {message.timestamp && (
              <div className="message-time">
                {format(new Date(message.timestamp), "HH:mm")}
              </div>
            )}
          </div>
        </div>
      ))}
      <div ref={messagesEndRef} />
    </div>
  );
};

export default MessageList;
