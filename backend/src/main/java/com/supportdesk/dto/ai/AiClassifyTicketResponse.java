package com.supportdesk.dto.ai;

import com.supportdesk.enums.TicketCategory;
import com.supportdesk.enums.TicketPriority;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AiClassifyTicketResponse {

    private TicketCategory category;
    private TicketPriority priority;
    private String reason;
}
