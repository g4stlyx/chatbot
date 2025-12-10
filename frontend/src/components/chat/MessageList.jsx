import { useEffect, useRef, useState } from "react";
import { format } from "date-fns";
import ReactMarkdown from "react-markdown";
import remarkGfm from "remark-gfm";
import { Prism as SyntaxHighlighter } from "react-syntax-highlighter";
import { vscDarkPlus } from "react-syntax-highlighter/dist/esm/styles/prism";
import MessageActions from "./MessageActions";
import EditMessageModal from "./EditMessageModal";
import { useChat } from "../../context/ChatContext";

const MessageList = ({ messages, isStreaming }) => {
  const messagesEndRef = useRef(null);
  const [editingMessage, setEditingMessage] = useState(null);
  const { editMessage, deleteMessage, regenerateResponse } = useChat();

  const scrollToBottom = () => {
    messagesEndRef.current?.scrollIntoView({ behavior: "smooth" });
  };

  useEffect(() => {
    scrollToBottom();
  }, [messages]);

  const handleEdit = (message) => {
    setEditingMessage(message);
  };

  const handleSaveEdit = async ({
    messageId,
    content,
    regenerateResponse: shouldRegenerate,
  }) => {
    await editMessage(messageId, content, shouldRegenerate);
  };

  const handleDelete = async (messageId) => {
    await deleteMessage(messageId);
  };

  const handleRegenerate = async (sessionId) => {
    await regenerateResponse(sessionId);
  };

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
            {message.role === "USER" ? "👤" : "🤖"}
          </div>
          <div className="message-body">
            <div className="message-content">
              {message.isStreaming ? (
                <div style={{ whiteSpace: "pre-wrap" }}>
                  {message.content}
                  <span className="streaming-cursor">▊</span>
                </div>
              ) : (
                <ReactMarkdown
                  remarkPlugins={[remarkGfm]}
                  components={{
                    code({ node, inline, className, children, ...props }) {
                      const match = /language-(\w+)/.exec(className || "");
                      return !inline && match ? (
                        <SyntaxHighlighter
                          style={vscDarkPlus}
                          language={match[1]}
                          PreTag="div"
                          {...props}
                        >
                          {String(children).replace(/\n$/, "")}
                        </SyntaxHighlighter>
                      ) : (
                        <code className={className} {...props}>
                          {children}
                        </code>
                      );
                    },
                  }}
                >
                  {message.content}
                </ReactMarkdown>
              )}
              {message.isEdited && (
                <span className="edited-badge" title="This message was edited">
                  (edited)
                </span>
              )}
            </div>
            <div className="message-footer">
              {message.timestamp && (
                <div className="message-time">
                  {format(new Date(message.timestamp), "HH:mm")}
                </div>
              )}
              <MessageActions
                message={message}
                onEdit={handleEdit}
                onDelete={handleDelete}
                onRegenerate={handleRegenerate}
                isStreaming={isStreaming}
              />
            </div>
          </div>
        </div>
      ))}
      <div ref={messagesEndRef} />

      {/* Edit Message Modal */}
      {editingMessage && (
        <EditMessageModal
          message={editingMessage}
          onSave={handleSaveEdit}
          onClose={() => setEditingMessage(null)}
        />
      )}
    </div>
  );
};

export default MessageList;
