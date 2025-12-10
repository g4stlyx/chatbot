import { createContext, useState, useContext, useCallback } from "react";
import { sessionAPI, messageAPI } from "../services/api";

const ChatContext = createContext(null);

export const ChatProvider = ({ children }) => {
  const [sessions, setSessions] = useState([]);
  const [currentSession, setCurrentSession] = useState(null);
  const [messages, setMessages] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  // Fetch all sessions
  const fetchSessions = useCallback(async () => {
    try {
      setLoading(true);
      const response = await sessionAPI.getSessions();
      // Backend returns paginated response with sessions array
      setSessions(response.data.sessions || []);
      setError(null);
    } catch (err) {
      console.error("Failed to fetch sessions:", err);
      setError(err.response?.data?.message || "Failed to fetch sessions");
      setSessions([]); // Set empty array on error
    } finally {
      setLoading(false);
    }
  }, []);

  // Fetch messages for a session
  const fetchMessages = useCallback(async (sessionId) => {
    try {
      setLoading(true);
      const response = await messageAPI.getMessages(sessionId);
      setMessages(response.data.messages || []);
      setError(null);
    } catch (err) {
      setError(err.response?.data?.message || "Failed to fetch messages");
    } finally {
      setLoading(false);
    }
  }, []);

  // Select a session
  const selectSession = useCallback(
    async (sessionId) => {
      try {
        const response = await sessionAPI.getSession(sessionId);
        setCurrentSession(response.data);
        await fetchMessages(sessionId);
      } catch (err) {
        setError(err.response?.data?.message || "Failed to load session");
      }
    },
    [fetchMessages]
  );

  // Create new session (happens automatically on first message)
  const createNewSession = useCallback(() => {
    setCurrentSession(null);
    setMessages([]);
  }, []);

  // Delete session
  const deleteSession = useCallback(
    async (sessionId) => {
      try {
        await sessionAPI.deleteSession(sessionId);
        setSessions((prev) => prev.filter((s) => s.sessionId !== sessionId));

        if (currentSession?.sessionId === sessionId) {
          setCurrentSession(null);
          setMessages([]);
        }
      } catch (err) {
        setError(err.response?.data?.message || "Failed to delete session");
      }
    },
    [currentSession]
  );

  // Add message to current conversation
  const addMessage = useCallback((message) => {
    setMessages((prev) => [...prev, message]);
  }, []);

  // Update message in conversation
  const updateMessage = useCallback((messageId, updates) => {
    setMessages((prev) =>
      prev.map((msg) => {
        if (msg.id === messageId) {
          // Support both object and function updater
          const newData =
            typeof updates === "function" ? updates(msg) : updates;
          return { ...msg, ...newData };
        }
        return msg;
      })
    );
  }, []);

  // Delete message
  const deleteMessage = useCallback(async (messageId) => {
    try {
      await messageAPI.deleteMessage(messageId);
      setMessages((prev) => prev.filter((msg) => msg.id !== messageId));
    } catch (err) {
      setError(err.response?.data?.message || "Failed to delete message");
    }
  }, []);

  // Edit message
  const editMessage = useCallback(
    async (messageId, content, regenerateResponse = false) => {
      try {
        const response = await messageAPI.editMessage(
          messageId,
          content,
          regenerateResponse
        );

        if (regenerateResponse) {
          // If regenerating, fetch fresh messages to get the new assistant response
          if (currentSession) {
            await fetchMessages(currentSession.sessionId);
          }
        } else {
          // Just update the edited message
          setMessages((prev) =>
            prev.map((msg) =>
              msg.id === messageId ? { ...msg, content, isEdited: true } : msg
            )
          );
        }

        return response.data;
      } catch (err) {
        setError(err.response?.data?.message || "Failed to edit message");
        throw err;
      }
    },
    [currentSession, fetchMessages]
  );

  // Regenerate response
  const regenerateResponse = useCallback(
    async (sessionId) => {
      try {
        setLoading(true);
        await messageAPI.regenerateResponse(sessionId);
        // Fetch fresh messages to get the new response
        await fetchMessages(sessionId);
      } catch (err) {
        setError(
          err.response?.data?.message || "Failed to regenerate response"
        );
        throw err;
      } finally {
        setLoading(false);
      }
    },
    [fetchMessages]
  );

  // Rename session
  const renameSession = useCallback(
    async (sessionId, title) => {
      try {
        const response = await sessionAPI.renameSession(sessionId, title);
        setSessions((prev) =>
          prev.map((s) => (s.sessionId === sessionId ? { ...s, title } : s))
        );

        if (currentSession?.sessionId === sessionId) {
          setCurrentSession((prev) => ({ ...prev, title }));
        }

        return response.data;
      } catch (err) {
        setError(err.response?.data?.message || "Failed to rename session");
        throw err;
      }
    },
    [currentSession]
  );

  // Archive session
  const archiveSession = useCallback(
    async (sessionId) => {
      try {
        await sessionAPI.archiveSession(sessionId);
        setSessions((prev) =>
          prev.map((s) =>
            s.sessionId === sessionId ? { ...s, status: "ARCHIVED" } : s
          )
        );

        if (currentSession?.sessionId === sessionId) {
          setCurrentSession((prev) => ({ ...prev, status: "ARCHIVED" }));
        }
      } catch (err) {
        setError(err.response?.data?.message || "Failed to archive session");
        throw err;
      }
    },
    [currentSession]
  );

  // Pause session
  const pauseSession = useCallback(
    async (sessionId) => {
      try {
        await sessionAPI.pauseSession(sessionId);
        setSessions((prev) =>
          prev.map((s) =>
            s.sessionId === sessionId ? { ...s, status: "PAUSED" } : s
          )
        );

        if (currentSession?.sessionId === sessionId) {
          setCurrentSession((prev) => ({ ...prev, status: "PAUSED" }));
        }
      } catch (err) {
        setError(err.response?.data?.message || "Failed to pause session");
        throw err;
      }
    },
    [currentSession]
  );

  // Activate/Resume session
  const activateSession = useCallback(
    async (sessionId) => {
      try {
        await sessionAPI.activateSession(sessionId);
        setSessions((prev) =>
          prev.map((s) =>
            s.sessionId === sessionId ? { ...s, status: "ACTIVE" } : s
          )
        );

        if (currentSession?.sessionId === sessionId) {
          setCurrentSession((prev) => ({ ...prev, status: "ACTIVE" }));
        }
      } catch (err) {
        setError(err.response?.data?.message || "Failed to activate session");
        throw err;
      }
    },
    [currentSession]
  );

  const value = {
    sessions,
    currentSession,
    messages,
    loading,
    error,
    fetchSessions,
    fetchMessages,
    selectSession,
    createNewSession,
    deleteSession,
    addMessage,
    updateMessage,
    deleteMessage,
    editMessage,
    regenerateResponse,
    renameSession,
    archiveSession,
    pauseSession,
    activateSession,
  };

  return <ChatContext.Provider value={value}>{children}</ChatContext.Provider>;
};

export const useChat = () => {
  const context = useContext(ChatContext);
  if (!context) {
    throw new Error("useChat must be used within a ChatProvider");
  }
  return context;
};

export default ChatContext;
