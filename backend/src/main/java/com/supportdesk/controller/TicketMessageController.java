package com.supportdesk.controller;

import com.supportdesk.dto.common.ApiResponse;
import com.supportdesk.dto.message.CreateMessageRequest;
import com.supportdesk.dto.message.MessageResponse;
import com.supportdesk.security.UserPrincipal;
import com.supportdesk.service.TicketMessageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tickets/{ticketId}/messages")
@RequiredArgsConstructor
public class TicketMessageController {

    private final TicketMessageService messageService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<MessageResponse>>> getMessages(
            @PathVariable Long ticketId,
            @AuthenticationPrincipal UserPrincipal currentUser
    ) {
        List<MessageResponse> messages = messageService.getMessagesByTicketId(ticketId, currentUser);
        return ResponseEntity.ok(ApiResponse.ok(messages));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<MessageResponse>> addMessage(
            @PathVariable Long ticketId,
            @Valid @RequestBody CreateMessageRequest request,
            @AuthenticationPrincipal UserPrincipal currentUser
    ) {
        MessageResponse response = messageService.addMessage(ticketId, request, currentUser);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Message sent successfully", response));
    }
}
