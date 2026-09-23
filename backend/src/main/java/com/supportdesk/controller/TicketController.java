package com.supportdesk.controller;

import com.supportdesk.dto.common.ApiResponse;
import com.supportdesk.dto.ticket.*;
import com.supportdesk.enums.TicketCategory;
import com.supportdesk.enums.TicketPriority;
import com.supportdesk.enums.TicketStatus;
import com.supportdesk.security.UserPrincipal;
import com.supportdesk.service.TicketService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/tickets")
@RequiredArgsConstructor
public class TicketController {

    private final TicketService ticketService;

    @PostMapping
    @PreAuthorize("hasAnyRole('CUSTOMER', 'ROLE_CUSTOMER', 'AGENT', 'ROLE_AGENT', 'SUPPORT_AGENT', 'ROLE_SUPPORT_AGENT', 'ADMIN', 'ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<TicketDetailResponse>> createTicket(
            @Valid @RequestBody CreateTicketRequest request,
            @AuthenticationPrincipal UserPrincipal currentUser
    ) {
        TicketDetailResponse response = ticketService.createTicket(request, currentUser);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Ticket created successfully", response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<TicketListResponse>>> getTickets(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) TicketStatus status,
            @RequestParam(required = false) TicketPriority priority,
            @RequestParam(required = false) TicketCategory category,
            @RequestParam(required = false) Long assignedAgentId,
            @RequestParam(required = false) Boolean unassigned,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @AuthenticationPrincipal UserPrincipal currentUser
    ) {
        Sort.Direction direction = sortDir.equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));

        Page<TicketListResponse> tickets = ticketService.getTickets(
                search, status, priority, category, assignedAgentId, unassigned, pageable, currentUser
        );
        return ResponseEntity.ok(ApiResponse.ok(tickets));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<TicketDetailResponse>> getTicketById(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal currentUser
    ) {
        TicketDetailResponse ticket = ticketService.getTicketById(id, currentUser);
        return ResponseEntity.ok(ApiResponse.ok(ticket));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<TicketDetailResponse>> updateTicketStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateTicketStatusRequest request,
            @AuthenticationPrincipal UserPrincipal currentUser
    ) {
        TicketDetailResponse updatedTicket = ticketService.updateTicketStatus(id, request, currentUser);
        return ResponseEntity.ok(ApiResponse.ok("Ticket status updated successfully", updatedTicket));
    }

    @PatchMapping("/{id}/priority")
    @PreAuthorize("hasAnyRole('AGENT', 'ROLE_AGENT', 'SUPPORT_AGENT', 'ROLE_SUPPORT_AGENT', 'ADMIN', 'ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<TicketDetailResponse>> updateTicketPriority(
            @PathVariable Long id,
            @Valid @RequestBody UpdateTicketPriorityRequest request,
            @AuthenticationPrincipal UserPrincipal currentUser
    ) {
        TicketDetailResponse updatedTicket = ticketService.updateTicketPriority(id, request, currentUser);
        return ResponseEntity.ok(ApiResponse.ok("Ticket priority updated successfully", updatedTicket));
    }

    @PatchMapping("/{id}/assign")
    @PreAuthorize("hasAnyRole('AGENT', 'ROLE_AGENT', 'SUPPORT_AGENT', 'ROLE_SUPPORT_AGENT', 'ADMIN', 'ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<TicketDetailResponse>> assignTicket(
            @PathVariable Long id,
            @RequestBody(required = false) AssignTicketRequest request,
            @AuthenticationPrincipal UserPrincipal currentUser
    ) {
        TicketDetailResponse updatedTicket = ticketService.assignTicket(id, request, currentUser);
        return ResponseEntity.ok(ApiResponse.ok("Ticket assigned successfully", updatedTicket));
    }
}
