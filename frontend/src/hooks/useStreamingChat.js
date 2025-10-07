import { useState, useCallback } from "react";
import { chatAPI } from "../services/api";

export const useStreamingChat = () => {
  const [isStreaming, setIsStreaming] = useState(false);
  const [streamedContent, setStreamedContent] = useState("");

  const sendStreamingMessage = useCallback(
    (message, sessionId, onChunk, onComplete, onError) => {
      setIsStreaming(true);
      setStreamedContent("");

      const url = chatAPI.getStreamUrl(sessionId);
      const eventSource = new EventSource(url);

      // Send the message via POST with the message content
      fetch(url.split("?")[0], {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
          Authorization: `Bearer ${localStorage.getItem("token")}`,
        },
        body: JSON.stringify({ message }),
      }).catch((error) => {
        console.error("Error sending message:", error);
        if (onError) onError(error);
      });

      eventSource.onmessage = (event) => {
        const chunk = event.data;
        setStreamedContent((prev) => prev + chunk);
        if (onChunk) onChunk(chunk);
      };

      eventSource.addEventListener("done", () => {
        eventSource.close();
        setIsStreaming(false);
        if (onComplete) onComplete(streamedContent);
      });

      eventSource.onerror = (error) => {
        console.error("SSE Error:", error);
        eventSource.close();
        setIsStreaming(false);
        if (onError) onError(error);
      };

      return () => {
        eventSource.close();
        setIsStreaming(false);
      };
    },
    [streamedContent]
  );

  return {
    isStreaming,
    streamedContent,
    sendStreamingMessage,
  };
};

export default useStreamingChat;
