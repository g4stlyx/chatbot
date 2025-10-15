import { useState } from "react";
import { useChat } from "../../context/ChatContext";
import MessageList from "./MessageList";
import MessageInput from "./MessageInput";

const ChatWindow = () => {
  const { currentSession, messages } = useChat();
  const [isStreaming, setIsStreaming] = useState(false);

  return (
    <div className="chat-window">
      <div className="chat-header">
        <h3>{currentSession?.title || "New Conversation"}</h3>
      </div>

      <MessageList messages={messages} isStreaming={isStreaming} />
      <MessageInput onStreamingChange={setIsStreaming} />
    </div>
  );
};

export default ChatWindow;
