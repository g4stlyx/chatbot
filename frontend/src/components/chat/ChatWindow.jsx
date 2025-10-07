import { useChat } from "../../context/ChatContext";
import MessageList from "./MessageList";
import MessageInput from "./MessageInput";

const ChatWindow = () => {
  const { currentSession, messages } = useChat();

  return (
    <div className="chat-window">
      <div className="chat-header">
        <h3>{currentSession?.title || "New Conversation"}</h3>
      </div>

      <MessageList messages={messages} />
      <MessageInput />
    </div>
  );
};

export default ChatWindow;
