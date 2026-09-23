package com.supportdesk.config;

import com.supportdesk.entity.Ticket;
import com.supportdesk.entity.TicketMessage;
import com.supportdesk.entity.User;
import com.supportdesk.enums.Role;
import com.supportdesk.repository.TicketMessageRepository;
import com.supportdesk.repository.TicketRepository;
import com.supportdesk.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(DataInitializer.class);

    private final UserRepository userRepository;
    private final TicketRepository ticketRepository;
    private final TicketMessageRepository messageRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {
        // 1. Safely clean up any pre-seeded demo/test agent accounts from previous runs or seed scripts
        cleanupPreSeededServiceAgents();

        // 2. Ensure System Administrator exists
        var adminOpt = userRepository.findByEmail("admin@supportdesk.com");
        if (adminOpt.isEmpty()) {
            userRepository.save(User.builder()
                    .fullName("System Administrator")
                    .email("admin@supportdesk.com")
                    .password(passwordEncoder.encode("Admin@123"))
                    .role(Role.ROLE_ADMIN)
                    .build());
            logger.info("Seeded System Administrator (admin@supportdesk.com)");
        } else {
            User existingAdmin = adminOpt.get();
            existingAdmin.setRole(Role.ROLE_ADMIN);
            existingAdmin.setPassword(passwordEncoder.encode("Admin@123"));
            userRepository.save(existingAdmin);
            logger.info("Synchronized System Administrator credentials (admin@supportdesk.com)");
        }

        // 3. Ensure Customer demo user exists
        if (userRepository.findByEmail("customer@supportdesk.com").isEmpty()) {
            userRepository.save(User.builder()
                    .fullName("John Customer")
                    .email("customer@supportdesk.com")
                    .password(passwordEncoder.encode("Customer@123"))
                    .role(Role.ROLE_CUSTOMER)
                    .build());
            logger.info("Seeded Customer Demo User (customer@supportdesk.com)");
        }

        // 4. Seed sample tickets if empty
        if (ticketRepository.count() == 0) {
            seedSampleTickets();
        }
    }

    private void cleanupPreSeededServiceAgents() {
        Set<String> demoEmails = Set.of("agent1@supportdesk.com", "agent2@supportdesk.com");
        Set<String> demoNames = Set.of(
                "Sarah Agent",
                "David Support",
                "Support Agent 123",
                "Support Agent 124",
                "Support Agent 125",
                "Support Agent 126",
                "Support Agent 127"
        );

        List<User> allUsers = userRepository.findAll();
        for (User user : allUsers) {
            boolean isDemoAgent = demoEmails.contains(user.getEmail().toLowerCase())
                    || demoNames.contains(user.getFullName())
                    || (user.getEmail().toLowerCase().startsWith("agent_12") && user.getEmail().toLowerCase().endsWith("@supportdesk.com"));

            if (isDemoAgent) {
                logger.info("Cleaning up pre-seeded/test agent: {} ({}) with agent_id={}", user.getFullName(), user.getEmail(), user.getAgentId());

                // Detach from any assigned tickets
                List<Ticket> assignedTickets = ticketRepository.findAll();
                for (Ticket ticket : assignedTickets) {
                    if (ticket.getAssignedAgent() != null && ticket.getAssignedAgent().getId().equals(user.getId())) {
                        ticket.setAssignedAgent(null);
                        ticketRepository.save(ticket);
                    }
                }

                // Delete or dissociate messages sent by this agent
                List<TicketMessage> messages = messageRepository.findAll();
                for (TicketMessage msg : messages) {
                    if (msg.getSender() != null && msg.getSender().getId().equals(user.getId())) {
                        messageRepository.delete(msg);
                    }
                }

                // Delete the agent user record to free up Agent ID and remove from database
                userRepository.delete(user);
                logger.info("Successfully removed demo agent user id={}", user.getId());
            }
        }
    }

    private void seedSampleTickets() {
        var customerOpt = userRepository.findByEmail("customer@supportdesk.com");
        if (customerOpt.isEmpty()) return;

        User customer = customerOpt.get();

        // Ticket 1: Open & Unassigned
        Ticket t1 = ticketRepository.save(Ticket.builder()
                .ticketNumber("TCK-1001")
                .title("Cannot access billing invoice from dashboard")
                .description("Whenever I click on 'Download Invoice #INV-2026-09', I get a 404 error page. Please assist with this invoice as our accounting period ends this week.")
                .category(com.supportdesk.enums.TicketCategory.BILLING)
                .priority(com.supportdesk.enums.TicketPriority.HIGH)
                .status(com.supportdesk.enums.TicketStatus.OPEN)
                .customer(customer)
                .assignedAgent(null)
                .build());

        messageRepository.save(com.supportdesk.entity.TicketMessage.builder()
                .ticket(t1)
                .sender(customer)
                .message("Whenever I click on 'Download Invoice #INV-2026-09', I get a 404 error page.")
                .isInternalNote(false)
                .build());

        // Ticket 2: Open & Unassigned
        Ticket t2 = ticketRepository.save(Ticket.builder()
                .ticketNumber("TCK-1002")
                .title("API webhook notification delivery delay")
                .description("Our webhook endpoint is receiving ticket update notifications with a 15-minute delay during peak hours. We require real-time events.")
                .category(com.supportdesk.enums.TicketCategory.TECHNICAL)
                .priority(com.supportdesk.enums.TicketPriority.URGENT)
                .status(com.supportdesk.enums.TicketStatus.OPEN)
                .customer(customer)
                .assignedAgent(null)
                .build());

        messageRepository.save(com.supportdesk.entity.TicketMessage.builder()
                .ticket(t2)
                .sender(customer)
                .message("Our webhook endpoint is receiving ticket update notifications with a 15-minute delay.")
                .isInternalNote(false)
                .build());

        // Ticket 3: Resolved & Unassigned
        Ticket t3 = ticketRepository.save(Ticket.builder()
                .ticketNumber("TCK-1003")
                .title("Request to update account organization name")
                .description("We recently rebranded from Acme Labs to Acme Technologies. Please update our billing company name.")
                .category(com.supportdesk.enums.TicketCategory.ACCOUNT)
                .priority(com.supportdesk.enums.TicketPriority.MEDIUM)
                .status(com.supportdesk.enums.TicketStatus.RESOLVED)
                .customer(customer)
                .assignedAgent(null)
                .resolvedAt(java.time.LocalDateTime.now().minusHours(4))
                .build());

        messageRepository.save(com.supportdesk.entity.TicketMessage.builder()
                .ticket(t3)
                .sender(customer)
                .message("We recently rebranded from Acme Labs to Acme Technologies. Please update our billing company name.")
                .isInternalNote(false)
                .build());
    }
}
