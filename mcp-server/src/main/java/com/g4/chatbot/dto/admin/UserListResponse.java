package com.g4.chatbot.dto.admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserListResponse {
    private List<UserManagementDTO> users;
    private int currentPage;
    private int totalPages;
    private long totalItems;
    private int pageSize;
}
