package com.supportdesk.service.impl;

import com.supportdesk.dto.auth.UserSummaryDto;
import com.supportdesk.dto.ticket.*;
import com.supportdesk.entity.Ticket;
import com.supportdesk.entity.User;
import com.supportdesk.enums.Role;
import com.supportdesk.enums.TicketCategory;
import com.supportdesk.enums.TicketPriority;
import com.supportdesk.enums.TicketStatus;
import com.supportdesk.exception.BadRequestException;
import com.supportdesk.exception.ResourceNotFoundException;
import com.supportdesk.repository.TicketRepository;
import com.supportdesk.repository.TicketSpecification;
import com.supportdesk.repository.UserRepository;
import com.supportdesk.security.UserPrincipal;
import com.supportdesk.service.TicketService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class TicketServiceImpl implements TicketService {

    private final TicketRepository ticketRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public TicketDetailResponse createTicket(CreateTicketRequest request, UserPrincipal currentUser) {
        User customer = userRepository.findById(currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Ticket ticket = Ticket.builder()
                .title(request.getTitle().trim())
                .description(request.getDescription().trim())
                .category(request.getCategory())
                .priority(request.getPriority())
                .status(TicketStatus.OPEN)
                .customer(customer)
                .build();

        Ticket savedTicket = ticketRepository.save(ticket);
        return mapToDetailResponse(savedTicket);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TicketListResponse> getTickets(
            String search,
            TicketStatus status,
            TicketPriority priority,
            TicketCategory category,
            Long assignedAgentId,
            Boolean unassigned,
            Pageable pageable,
            UserPrincipal currentUser
    ) {
        Long customerFilter = null;
        if (currentUser.getRole().isCustomer()) {
            customerFilter = currentUser.getId();
        }

        Specification<Ticket> spec = TicketSpecification.filterTickets(
                customerFilter,
                search,
                status,
                priority,
                category,
                assignedAgentId,
                unassigned
        );

        return ticketRepository.findAll(spec, pageable).map(this::mapToListResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public TicketDetailResponse getTicketById(Long id, UserPrincipal currentUser) {
        Ticket ticket = ticketRepository.findWithDetailsById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found with id: " + id));

        // Broken Object Level Authorization (BOLA) Check
        if (currentUser.getRole().isCustomer() &&
                !ticket.getCustomer().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("You do not have permission to view this ticket");
        }

        return mapToDetailResponse(ticket);
    }

    @Override
    @Transactional
    public TicketDetailResponse updateTicketStatus(Long id, UpdateTicketStatusRequest request, UserPrincipal currentUser) {
        Ticket ticket = ticketRepository.findWithDetailsById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found with id: " + id));

        // Customers can only close their own tickets once resolved
        if (currentUser.getRole() == Role.ROLE_CUSTOMER) {
            if (!ticket.getCustomer().getId().equals(currentUser.getId())) {
                throw new AccessDeniedException("You do not have permission to update this ticket");
            }
            if (request.getStatus() != TicketStatus.CLOSED) {
                throw new BadRequestException("Customers can only close their resolved tickets");
            }
            if (ticket.getStatus() != TicketStatus.RESOLVED && ticket.getStatus() != TicketStatus.CLOSED) {
                throw new BadRequestException("Ticket must be resolved before it can be closed by the customer");
            }
        }

        ticket.setStatus(request.getStatus());
        if (request.getStatus() == TicketStatus.RESOLVED) {
            ticket.setResolvedAt(LocalDateTime.now());
        } else if (request.getStatus() != TicketStatus.CLOSED) {
            ticket.setResolvedAt(null);
        }

        Ticket updatedTicket = ticketRepository.save(ticket);
        return mapToDetailResponse(updatedTicket);
    }

    @Override
    @Transactional
    public TicketDetailResponse updateTicketPriority(Long id, UpdateTicketPriorityRequest request, UserPrincipal currentUser) {
        if (currentUser.getRole().isCustomer()) {
            throw new AccessDeniedException("Only support agents can modify ticket priority");
        }

        Ticket ticket = ticketRepository.findWithDetailsById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found with id: " + id));

        ticket.setPriority(request.getPriority());
        Ticket updatedTicket = ticketRepository.save(ticket);
        return mapToDetailResponse(updatedTicket);
    }

    @Override
    @Transactional
    public TicketDetailResponse assignTicket(Long id, AssignTicketRequest request, UserPrincipal currentUser) {
        if (currentUser.getRole().isCustomer()) {
            throw new AccessDeniedException("Only support agents can assign tickets");
        }

        Ticket ticket = ticketRepository.findWithDetailsById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found with id: " + id));

        Long targetAgentId = (request != null && request.getAgentId() != null)
                ? request.getAgentId()
                : currentUser.getId();

        User agent = userRepository.findById(targetAgentId)
                .orElseThrow(() -> new ResourceNotFoundException("Agent not found with id: " + targetAgentId));

        if (agent.getRole() == Role.ROLE_CUSTOMER) {
            throw new BadRequestException("Cannot assign ticket to a customer account");
        }

        ticket.setAssignedAgent(agent);
        if (ticket.getStatus() == TicketStatus.OPEN) {
            ticket.setStatus(TicketStatus.IN_PROGRESS);
        }

        Ticket updatedTicket = ticketRepository.save(ticket);
        return mapToDetailResponse(updatedTicket);
    }

    private TicketListResponse mapToListResponse(Ticket ticket) {
        return TicketListResponse.builder()
                .id(ticket.getId())
                .ticketNumber(ticket.getTicketNumber())
                .title(ticket.getTitle())
                .status(ticket.getStatus())
                .priority(ticket.getPriority())
                .category(ticket.getCategory())
                .customerId(ticket.getCustomer() != null ? ticket.getCustomer().getId() : null)
                .customerName(ticket.getCustomer() != null ? ticket.getCustomer().getFullName() : null)
                .customerEmail(ticket.getCustomer() != null ? ticket.getCustomer().getEmail() : null)
                .assignedAgentId(ticket.getAssignedAgent() != null ? ticket.getAssignedAgent().getId() : null)
                .assignedAgentName(ticket.getAssignedAgent() != null ? ticket.getAssignedAgent().getFullName() : null)
                .createdAt(ticket.getCreatedAt())
                .updatedAt(ticket.getUpdatedAt())
                .messageCount(ticket.getMessages() != null ? ticket.getMessages().size() : 0)
                .build();
    }

    private TicketDetailResponse mapToDetailResponse(Ticket ticket) {
        UserSummaryDto customerDto = null;
        if (ticket.getCustomer() != null) {
            customerDto = UserSummaryDto.builder()
                    .id(ticket.getCustomer().getId())
                    .fullName(ticket.getCustomer().getFullName())
                    .email(ticket.getCustomer().getEmail())
                    .role(ticket.getCustomer().getRole())
                    .build();
        }

        UserSummaryDto agentDto = null;
        if (ticket.getAssignedAgent() != null) {
            agentDto = UserSummaryDto.builder()
                    .id(ticket.getAssignedAgent().getId())
                    .fullName(ticket.getAssignedAgent().getFullName())
                    .email(ticket.getAssignedAgent().getEmail())
                    .role(ticket.getAssignedAgent().getRole())
                    .build();
        }

        return TicketDetailResponse.builder()
                .id(ticket.getId())
                .ticketNumber(ticket.getTicketNumber())
                .title(ticket.getTitle())
                .description(ticket.getDescription())
                .status(ticket.getStatus())
                .priority(ticket.getPriority())
                .category(ticket.getCategory())
                .customer(customerDto)
                .assignedAgent(agentDto)
                .createdAt(ticket.getCreatedAt())
                .updatedAt(ticket.getUpdatedAt())
                .resolvedAt(ticket.getResolvedAt())
                .messageCount(ticket.getMessages() != null ? ticket.getMessages().size() : 0)
                .build();
    }
}
