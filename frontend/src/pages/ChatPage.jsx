import { useEffect } from "react";
import { useAuth } from "../context/AuthContext";
import { useChat } from "../context/ChatContext";
import Sidebar from "../components/chat/Sidebar";
import ChatWindow from "../components/chat/ChatWindow";
import "./ChatPage.css";

const ChatPage = () => {
  const { user, logout } = useAuth();
  const { fetchSessions } = useChat();

  useEffect(() => {
    fetchSessions();
  }, [fetchSessions]);

  return (
    <div className="chat-page">
      <Sidebar user={user} onLogout={logout} />
      <ChatWindow />
    </div>
  );
};

export default ChatPage;
