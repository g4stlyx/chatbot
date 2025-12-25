import { useState } from "react";
import { useChat } from "../../context/ChatContext";
import { chatAPI } from "../../services/api";

const MessageInput = ({ onStreamingChange }) => {
  const { currentSession, addMessage, updateMessage, fetchSessions } =
    useChat();
  const [input, setInput] = useState("");
  const [loading, setLoading] = useState(false);

  const handleSubmit = async (e) => {
    e.preventDefault();

    if (!input.trim() || loading) return;

    const userMessage = input.trim();
    setInput("");
    setLoading(true);
    onStreamingChange?.(true);

    // Add user message to UI immediately
    const tempUserMessage = {
      id: Date.now(),
      role: "user",
      content: userMessage,
      timestamp: new Date().toISOString(),
    };
    addMessage(tempUserMessage);

    // Add placeholder for assistant message
    const assistantMsgId = Date.now() + 1;
    const assistantMessage = {
      id: assistantMsgId,
      role: "assistant",
      content: "",
      timestamp: new Date().toISOString(),
      isStreaming: true,
    };
    addMessage(assistantMessage);

    try {
      // Use streaming endpoint
      const response = await chatAPI.sendMessageStream(
        userMessage,
        currentSession?.sessionId,
        (chunk) => {
          // Update message content as chunks arrive
          updateMessage(assistantMsgId, (prev) => ({
            ...prev,
            content: prev.content + chunk,
            isStreaming: true, // Explicitly keep streaming flag during updates
          }));
        }
      );

      // Mark streaming as complete - use setTimeout to ensure it happens after last chunk update
      setTimeout(() => {
        updateMessage(assistantMsgId, (prev) => ({
          ...prev,
          isStreaming: false,
        }));
      }, 0);

      // Refresh sessions list to show new session if created
      if (!currentSession || response.isNewSession) {
        await fetchSessions();
      }
    } catch (error) {
      console.error("Error sending message:", error);

      // Update with error message
      updateMessage(assistantMsgId, {
        id: assistantMsgId,
        role: "assistant",
        content: "Sorry, I encountered an error. Please try again.",
        timestamp: new Date().toISOString(),
        isStreaming: false,
      });
    } finally {
      setLoading(false);
      onStreamingChange?.(false);
    }
  };

  const handleKeyDown = (e) => {
    if (e.key === "Enter" && !e.shiftKey) {
      e.preventDefault();
      handleSubmit(e);
    }
  };

  return (
    <div className="input-area">
      <form onSubmit={handleSubmit} className="input-form">
        <div className="input-wrapper">
          <textarea
            className="message-input"
            placeholder="Type your message... (Shift+Enter for new line)"
            value={input}
            onChange={(e) => setInput(e.target.value)}
            onKeyDown={handleKeyDown}
            disabled={loading}
            rows={1}
          />
        </div>
        <button
          type="submit"
          className="send-btn"
          disabled={loading || !input.trim()}
        >
          {loading ? "..." : "Send"}
        </button>
      </form>
    </div>
  );
};

export default MessageInput;
