package com.supportdesk.dto.admin;

import com.supportdesk.enums.TicketCategory;
import com.supportdesk.enums.TicketPriority;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminDashboardStatsResponse {

    private long totalTickets;
    private long openTickets;
    private long inProgressTickets;
    private long resolvedTickets;
    private long closedTickets;
    private long urgentTickets;
    private long highPriorityTickets;

    private long totalCustomers;
    private long totalAgents;
    private long unassignedTickets;
    private long assignedTickets;

    private Map<TicketCategory, Long> categoryCounts;
    private Map<TicketPriority, Long> priorityCounts;
}
