package com.supportdesk.dto.ticket;

import com.supportdesk.enums.TicketPriority;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateTicketPriorityRequest {

    @NotNull(message = "Priority is required")
    private TicketPriority priority;
}
