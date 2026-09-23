package com.supportdesk.repository;

import com.supportdesk.entity.Ticket;
import com.supportdesk.enums.TicketCategory;
import com.supportdesk.enums.TicketPriority;
import com.supportdesk.enums.TicketStatus;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class TicketSpecification {

    public static Specification<Ticket> filterTickets(
            Long customerId,
            String search,
            TicketStatus status,
            TicketPriority priority,
            TicketCategory category,
            Long assignedAgentId,
            Boolean unassigned
    ) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // 1. Customer Scoping (if customerId is present, strictly restrict to this customer)
            if (customerId != null) {
                predicates.add(cb.equal(root.get("customer").get("id"), customerId));
            }

            // 2. Search by Ticket Number or Title
            if (StringUtils.hasText(search)) {
                String searchPattern = "%" + search.trim().toLowerCase() + "%";
                Predicate titleMatch = cb.like(cb.lower(root.get("title")), searchPattern);
                Predicate numberMatch = cb.like(cb.lower(root.get("ticketNumber")), searchPattern);
                predicates.add(cb.or(titleMatch, numberMatch));
            }

            // 3. Filter by Status
            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }

            // 4. Filter by Priority
            if (priority != null) {
                predicates.add(cb.equal(root.get("priority"), priority));
            }

            // 5. Filter by Category
            if (category != null) {
                predicates.add(cb.equal(root.get("category"), category));
            }

            // 6. Filter by Assigned Agent
            if (assignedAgentId != null) {
                predicates.add(cb.equal(root.get("assignedAgent").get("id"), assignedAgentId));
            } else if (Boolean.TRUE.equals(unassigned)) {
                predicates.add(cb.isNull(root.get("assignedAgent")));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
