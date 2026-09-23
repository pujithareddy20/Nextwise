package com.supportdesk.dto.message;

import com.supportdesk.dto.auth.UserSummaryDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MessageResponse {
    private Long id;
    private Long ticketId;
    private UserSummaryDto sender;
    private String message;
    private boolean internalNote;
    private LocalDateTime createdAt;
}
