package com.supportdesk.service;

import com.supportdesk.dto.message.CreateMessageRequest;
import com.supportdesk.dto.message.MessageResponse;
import com.supportdesk.security.UserPrincipal;

import java.util.List;

public interface TicketMessageService {
    List<MessageResponse> getMessagesByTicketId(Long ticketId, UserPrincipal currentUser);
    MessageResponse addMessage(Long ticketId, CreateMessageRequest request, UserPrincipal currentUser);
}
