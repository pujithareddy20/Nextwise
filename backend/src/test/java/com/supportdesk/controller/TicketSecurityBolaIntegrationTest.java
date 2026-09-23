package com.supportdesk.controller;

import com.supportdesk.dto.message.CreateMessageRequest;
import com.supportdesk.dto.ticket.CreateTicketRequest;
import com.supportdesk.entity.Ticket;
import com.supportdesk.entity.User;
import com.supportdesk.enums.TicketCategory;
import com.supportdesk.enums.TicketPriority;
import com.supportdesk.enums.TicketStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("Ticket Security & BOLA / IDOR Defense Integration Tests")
class TicketSecurityBolaIntegrationTest extends BaseIntegrationTest {

    private Ticket customer1Ticket;
    private String customer2Token;

    @BeforeEach
    void setupSecurityContext() {
        // Customer 1
        User customer1 = userRepository.findByEmail("customer@supportdesk.com")
                .orElseThrow(() -> new IllegalStateException("Customer 1 not found"));

        // Create a unique ticket belonging to Customer 1
        customer1Ticket = ticketRepository.save(Ticket.builder()
                .ticketNumber("TCK-BOLA-" + System.currentTimeMillis())
                .title("Customer 1 Confidential Billing Dispute")
                .description("Sensitive billing records and bank information.")
                .category(TicketCategory.BILLING)
                .priority(TicketPriority.HIGH)
                .status(TicketStatus.OPEN)
                .customer(customer1)
                .build());

        // Create Customer 2 (an attacker or separate customer)
        User customer2 = createSecondCustomer("customer2_bola@example.com", "Customer Two");
        customer2Token = createTokenForUser(customer2);
    }

    @Test
    @DisplayName("BOLA Defense: Customer 2 must NOT access Customer 1's ticket (403 Forbidden)")
    void testCustomerCannotAccessAnotherCustomerTicket() throws Exception {
        mockMvc.perform(get("/api/tickets/" + customer1Ticket.getId())
                        .header("Authorization", "Bearer " + customer2Token))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("You do not have permission to view this ticket"));
    }

    @Test
    @DisplayName("BOLA Defense: Customer 2 must NOT post messages to Customer 1's ticket (403 Forbidden)")
    void testCustomerCannotPostMessageToAnotherCustomerTicket() throws Exception {
        CreateMessageRequest request = CreateMessageRequest.builder()
                .message("Malicious inject attempt on Customer 1 ticket")
                .internalNote(false)
                .build();

        mockMvc.perform(post("/api/tickets/" + customer1Ticket.getId() + "/messages")
                        .header("Authorization", "Bearer " + customer2Token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("You do not have permission to reply to this ticket"));
    }

    @Test
    @DisplayName("Agent CAN access Customer 1's ticket for resolution (200 OK)")
    void testAgentCanAccessCustomerTicket() throws Exception {
        String agentToken = getAgentToken();

        mockMvc.perform(get("/api/tickets/" + customer1Ticket.getId())
                        .header("Authorization", "Bearer " + agentToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(customer1Ticket.getId()));
    }

    @Test
    @DisplayName("Unauthenticated request to ticket endpoint must return 401 Unauthorized")
    void testUnauthenticatedAccessRejected() throws Exception {
        mockMvc.perform(get("/api/tickets/" + customer1Ticket.getId()))
                .andExpect(status().isUnauthorized());
    }
}
