package com.supportdesk.repository;

import com.supportdesk.entity.Ticket;
import com.supportdesk.enums.TicketPriority;
import com.supportdesk.enums.TicketStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.Optional;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, Long>, JpaSpecificationExecutor<Ticket> {

    @EntityGraph(attributePaths = {"customer", "assignedAgent"})
    Optional<Ticket> findWithDetailsById(Long id);

    @EntityGraph(attributePaths = {"customer", "assignedAgent"})
    Page<Ticket> findAll(Specification<Ticket> spec, Pageable pageable);

    // Dynamic count queries for Agent / Admin (Global system metrics)
    long countByStatus(TicketStatus status);

    long countByPriority(TicketPriority priority);

    long countByPriorityIn(Collection<TicketPriority> priorities);

    // Dynamic count queries for Customer (Scoped to customer's own tickets)
    long countByCustomerId(Long customerId);

    long countByCustomerIdAndStatus(Long customerId, TicketStatus status);

    long countByCustomerIdAndPriority(Long customerId, TicketPriority priority);

    long countByCustomerIdAndPriorityIn(Long customerId, Collection<TicketPriority> priorities);

    long countByCategory(com.supportdesk.enums.TicketCategory category);

    long countByAssignedAgentIsNull();

    long countByAssignedAgentIsNotNull();

    long countByAssignedAgentId(Long agentId);

    long countByAssignedAgentIdAndStatus(Long agentId, TicketStatus status);
}
