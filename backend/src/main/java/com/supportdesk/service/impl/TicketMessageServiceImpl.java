package com.supportdesk.service.impl;

import com.supportdesk.dto.auth.UserSummaryDto;
import com.supportdesk.dto.message.CreateMessageRequest;
import com.supportdesk.dto.message.MessageResponse;
import com.supportdesk.entity.Ticket;
import com.supportdesk.entity.TicketMessage;
import com.supportdesk.entity.User;
import com.supportdesk.enums.Role;
import com.supportdesk.enums.TicketStatus;
import com.supportdesk.exception.ResourceNotFoundException;
import com.supportdesk.repository.TicketMessageRepository;
import com.supportdesk.repository.TicketRepository;
import com.supportdesk.repository.UserRepository;
import com.supportdesk.security.UserPrincipal;
import com.supportdesk.service.TicketMessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TicketMessageServiceImpl implements TicketMessageService {

    private final TicketMessageRepository messageRepository;
    private final TicketRepository ticketRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public List<MessageResponse> getMessagesByTicketId(Long ticketId, UserPrincipal currentUser) {
        Ticket ticket = ticketRepository.findWithDetailsById(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found with id: " + ticketId));

        if (currentUser.getRole().isCustomer()) {
            if (!ticket.getCustomer().getId().equals(currentUser.getId())) {
                throw new AccessDeniedException("You do not have permission to view conversation for this ticket");
            }
            return messageRepository.findByTicketIdAndIsInternalNoteFalseOrderByCreatedAtAsc(ticketId)
                    .stream()
                    .map(this::mapToMessageResponse)
                    .collect(Collectors.toList());
        }

        return messageRepository.findByTicketIdOrderByCreatedAtAsc(ticketId)
                .stream()
                .map(this::mapToMessageResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public MessageResponse addMessage(Long ticketId, CreateMessageRequest request, UserPrincipal currentUser) {
        Ticket ticket = ticketRepository.findWithDetailsById(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found with id: " + ticketId));

        boolean isCustomer = currentUser.getRole().isCustomer();

        if (isCustomer && !ticket.getCustomer().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("You do not have permission to reply to this ticket");
        }

        User sender = userRepository.findById(currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Sender user not found"));

        boolean internalNote = !isCustomer && request.isInternalNote();

        TicketMessage message = TicketMessage.builder()
                .ticket(ticket)
                .sender(sender)
                .message(request.getMessage().trim())
                .isInternalNote(internalNote)
                .build();

        TicketMessage savedMessage = messageRepository.save(message);

        // If customer replies to a resolved ticket, automatically reopen to IN_PROGRESS
        if (isCustomer && (ticket.getStatus() == TicketStatus.RESOLVED || ticket.getStatus() == TicketStatus.CLOSED)) {
            ticket.setStatus(TicketStatus.IN_PROGRESS);
            ticket.setResolvedAt(null);
            ticketRepository.save(ticket);
        }

        return mapToMessageResponse(savedMessage);
    }

    private MessageResponse mapToMessageResponse(TicketMessage message) {
        User sender = message.getSender();
        UserSummaryDto senderDto = null;
        if (sender != null) {
            senderDto = UserSummaryDto.builder()
                    .id(sender.getId())
                    .fullName(sender.getFullName())
                    .email(sender.getEmail())
                    .role(sender.getRole())
                    .build();
        }

        return MessageResponse.builder()
                .id(message.getId())
                .ticketId(message.getTicket().getId())
                .sender(senderDto)
                .message(message.getMessage())
                .internalNote(message.isInternalNote())
                .createdAt(message.getCreatedAt())
                .build();
    }
}
