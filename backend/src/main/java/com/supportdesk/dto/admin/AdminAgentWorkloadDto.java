package com.supportdesk.dto.admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminAgentWorkloadDto {

    private Long id;
    private String fullName;
    private String email;
    private Integer agentId;
    private long assignedTickets;
    private long openTickets;
    private long resolvedTickets;
}
