package com.supportdesk.service;

import com.supportdesk.dto.ticket.*;
import com.supportdesk.enums.TicketCategory;
import com.supportdesk.enums.TicketPriority;
import com.supportdesk.enums.TicketStatus;
import com.supportdesk.security.UserPrincipal;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface TicketService {

    TicketDetailResponse createTicket(CreateTicketRequest request, UserPrincipal currentUser);

    Page<TicketListResponse> getTickets(
            String search,
            TicketStatus status,
            TicketPriority priority,
            TicketCategory category,
            Long assignedAgentId,
            Boolean unassigned,
            Pageable pageable,
            UserPrincipal currentUser
    );

    TicketDetailResponse getTicketById(Long id, UserPrincipal currentUser);

    TicketDetailResponse updateTicketStatus(Long id, UpdateTicketStatusRequest request, UserPrincipal currentUser);

    TicketDetailResponse updateTicketPriority(Long id, UpdateTicketPriorityRequest request, UserPrincipal currentUser);

    TicketDetailResponse assignTicket(Long id, AssignTicketRequest request, UserPrincipal currentUser);
}
