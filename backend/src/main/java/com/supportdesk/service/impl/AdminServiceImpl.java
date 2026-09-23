package com.supportdesk.service.impl;

import com.supportdesk.dto.admin.AdminAgentWorkloadDto;
import com.supportdesk.dto.admin.AdminDashboardStatsResponse;
import com.supportdesk.entity.User;
import com.supportdesk.enums.Role;
import com.supportdesk.enums.TicketCategory;
import com.supportdesk.enums.TicketPriority;
import com.supportdesk.enums.TicketStatus;
import com.supportdesk.repository.TicketRepository;
import com.supportdesk.repository.UserRepository;
import com.supportdesk.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {

    private final TicketRepository ticketRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public AdminDashboardStatsResponse getAdminDashboardStats() {
        long totalTickets = ticketRepository.count();
        long openTickets = ticketRepository.countByStatus(TicketStatus.OPEN);
        long inProgressTickets = ticketRepository.countByStatus(TicketStatus.IN_PROGRESS);
        long resolvedTickets = ticketRepository.countByStatus(TicketStatus.RESOLVED);
        long closedTickets = ticketRepository.countByStatus(TicketStatus.CLOSED);
        long urgentTickets = ticketRepository.countByPriority(TicketPriority.URGENT);
        long highPriorityTickets = ticketRepository.countByPriority(TicketPriority.HIGH);

        long totalCustomers = userRepository.countByRole(Role.ROLE_CUSTOMER);
        long totalAgents = userRepository.countByRoleIn(List.of(Role.ROLE_SUPPORT_AGENT, Role.ROLE_AGENT));
        long unassignedTickets = ticketRepository.countByAssignedAgentIsNull();
        long assignedTickets = ticketRepository.countByAssignedAgentIsNotNull();

        Map<TicketCategory, Long> categoryCounts = new EnumMap<>(TicketCategory.class);
        for (TicketCategory category : TicketCategory.values()) {
            categoryCounts.put(category, ticketRepository.countByCategory(category));
        }

        Map<TicketPriority, Long> priorityCounts = new EnumMap<>(TicketPriority.class);
        for (TicketPriority priority : TicketPriority.values()) {
            priorityCounts.put(priority, ticketRepository.countByPriority(priority));
        }

        return AdminDashboardStatsResponse.builder()
                .totalTickets(totalTickets)
                .openTickets(openTickets)
                .inProgressTickets(inProgressTickets)
                .resolvedTickets(resolvedTickets)
                .closedTickets(closedTickets)
                .urgentTickets(urgentTickets)
                .highPriorityTickets(highPriorityTickets)
                .totalCustomers(totalCustomers)
                .totalAgents(totalAgents)
                .unassignedTickets(unassignedTickets)
                .assignedTickets(assignedTickets)
                .categoryCounts(categoryCounts)
                .priorityCounts(priorityCounts)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<AdminAgentWorkloadDto> getAgentWorkloads() {
        List<User> agents = new ArrayList<>();
        agents.addAll(userRepository.findByRole(Role.ROLE_SUPPORT_AGENT));
        agents.addAll(userRepository.findByRole(Role.ROLE_AGENT));

        List<AdminAgentWorkloadDto> workloads = new ArrayList<>();
        for (User agent : agents) {
            long assigned = ticketRepository.countByAssignedAgentId(agent.getId());
            long open = ticketRepository.countByAssignedAgentIdAndStatus(agent.getId(), TicketStatus.OPEN) +
                    ticketRepository.countByAssignedAgentIdAndStatus(agent.getId(), TicketStatus.IN_PROGRESS);
            long resolved = ticketRepository.countByAssignedAgentIdAndStatus(agent.getId(), TicketStatus.RESOLVED) +
                    ticketRepository.countByAssignedAgentIdAndStatus(agent.getId(), TicketStatus.CLOSED);

            workloads.add(AdminAgentWorkloadDto.builder()
                    .id(agent.getId())
                    .fullName(agent.getFullName())
                    .email(agent.getEmail())
                    .agentId(agent.getAgentId())
                    .assignedTickets(assigned)
                    .openTickets(open)
                    .resolvedTickets(resolved)
                    .build());
        }

        return workloads;
    }
}
