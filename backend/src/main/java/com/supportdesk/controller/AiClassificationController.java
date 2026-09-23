package com.supportdesk.controller;

import com.supportdesk.dto.ai.AiClassifyTicketRequest;
import com.supportdesk.dto.ai.AiClassifyTicketResponse;
import com.supportdesk.dto.common.ApiResponse;
import com.supportdesk.service.AiClassificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping
@RequiredArgsConstructor
public class AiClassificationController {

    private final AiClassificationService aiClassificationService;

    @PostMapping({"/api/ai/suggest-ticket-classification", "/api/tickets/ai-classify"})
    @PreAuthorize("hasAnyRole('CUSTOMER', 'ROLE_CUSTOMER', 'AGENT', 'ROLE_AGENT', 'SUPPORT_AGENT', 'ROLE_SUPPORT_AGENT', 'ADMIN', 'ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<AiClassifyTicketResponse>> classifyTicket(
            @Valid @RequestBody AiClassifyTicketRequest request
    ) {
        AiClassifyTicketResponse response = aiClassificationService.classifyTicket(request);
        return ResponseEntity.ok(ApiResponse.ok("AI classification generated successfully", response));
    }

    @org.springframework.web.bind.annotation.GetMapping({"/api/ai/status", "/api/tickets/ai-status"})
    @PreAuthorize("hasAnyRole('CUSTOMER', 'ROLE_CUSTOMER', 'AGENT', 'ROLE_AGENT', 'SUPPORT_AGENT', 'ROLE_SUPPORT_AGENT', 'ADMIN', 'ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<java.util.Map<String, Object>>> getAiStatus() {
        return ResponseEntity.ok(ApiResponse.ok("AI status checked", aiClassificationService.getStatus()));
    }
}
