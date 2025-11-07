package com.g4.chatbot.dto.messages;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessageHistoryResponse {
    private String sessionId;
    private Integer totalMessages;
    private List<MessageResponse> messages;
}
