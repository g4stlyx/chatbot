import { useState } from "react";
import { useChat } from "../../context/ChatContext";
import { chatAPI } from "../../services/api";

const MessageInput = () => {
  const { currentSession, addMessage, fetchSessions } = useChat();
  const [input, setInput] = useState("");
  const [loading, setLoading] = useState(false);

  const handleSubmit = async (e) => {
    e.preventDefault();

    if (!input.trim() || loading) return;

    const userMessage = input.trim();
    setInput("");
    setLoading(true);

    // Add user message to UI immediately
    const tempUserMessage = {
      id: Date.now(),
      role: "user",
      content: userMessage,
      timestamp: new Date().toISOString(),
    };
    addMessage(tempUserMessage);

    try {
      // Send message to backend (non-streaming)
      const response = await chatAPI.sendMessage(
        userMessage,
        currentSession?.sessionId
      );

      console.log("Chat response:", response.data); // Debug log

      // Add assistant response
      const assistantMessage = {
        id: response.data.assistantMessageId || Date.now() + 1,
        role: "assistant",
        content: response.data.assistantMessage || "",
        timestamp: new Date().toISOString(),
      };
      addMessage(assistantMessage);

      // Refresh sessions list to show new session if created
      if (!currentSession || response.data.isNewSession) {
        await fetchSessions();
      }
    } catch (error) {
      console.error("Error sending message:", error);

      // Add error message
      addMessage({
        id: Date.now() + 1,
        role: "assistant",
        content: "Sorry, I encountered an error. Please try again.",
        timestamp: new Date().toISOString(),
      });
    } finally {
      setLoading(false);
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
