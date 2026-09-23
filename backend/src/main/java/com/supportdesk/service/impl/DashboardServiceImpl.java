package com.supportdesk.service.impl;

import com.supportdesk.dto.dashboard.DashboardStatsResponse;
import com.supportdesk.enums.Role;
import com.supportdesk.enums.TicketPriority;
import com.supportdesk.enums.TicketStatus;
import com.supportdesk.repository.TicketRepository;
import com.supportdesk.security.UserPrincipal;
import com.supportdesk.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private final TicketRepository ticketRepository;

    @Override
    @Transactional(readOnly = true)
    public DashboardStatsResponse getDashboardStats(UserPrincipal currentUser) {
        if (currentUser.getRole().isCustomer()) {
            Long customerId = currentUser.getId();
            long total = ticketRepository.countByCustomerId(customerId);
            long open = ticketRepository.countByCustomerIdAndStatus(customerId, TicketStatus.OPEN);
            long inProgress = ticketRepository.countByCustomerIdAndStatus(customerId, TicketStatus.IN_PROGRESS);
            long resolved = ticketRepository.countByCustomerIdAndStatus(customerId, TicketStatus.RESOLVED);
            long closed = ticketRepository.countByCustomerIdAndStatus(customerId, TicketStatus.CLOSED);
            long urgent = ticketRepository.countByCustomerIdAndPriority(customerId, TicketPriority.URGENT);

            return DashboardStatsResponse.builder()
                    .totalTickets(total)
                    .openTickets(open)
                    .inProgressTickets(inProgress)
                    .resolvedTickets(resolved)
                    .closedTickets(closed)
                    .highPriorityTickets(urgent)
                    .urgentTickets(urgent)
                    .build();
        }

        // Global metrics for Support Agents and Admins
        long total = ticketRepository.count();
        long open = ticketRepository.countByStatus(TicketStatus.OPEN);
        long inProgress = ticketRepository.countByStatus(TicketStatus.IN_PROGRESS);
        long resolved = ticketRepository.countByStatus(TicketStatus.RESOLVED);
        long closed = ticketRepository.countByStatus(TicketStatus.CLOSED);
        long urgent = ticketRepository.countByPriority(TicketPriority.URGENT);

        return DashboardStatsResponse.builder()
                .totalTickets(total)
                .openTickets(open)
                .inProgressTickets(inProgress)
                .resolvedTickets(resolved)
                .closedTickets(closed)
                .highPriorityTickets(urgent)
                .urgentTickets(urgent)
                .build();
    }
}
