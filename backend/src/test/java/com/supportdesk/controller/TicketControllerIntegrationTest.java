package com.supportdesk.controller;

import com.supportdesk.dto.ticket.AssignTicketRequest;
import com.supportdesk.dto.ticket.CreateTicketRequest;
import com.supportdesk.dto.ticket.UpdateTicketPriorityRequest;
import com.supportdesk.dto.ticket.UpdateTicketStatusRequest;
import com.supportdesk.entity.Ticket;
import com.supportdesk.entity.User;
import com.supportdesk.enums.TicketCategory;
import com.supportdesk.enums.TicketPriority;
import com.supportdesk.enums.TicketStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("Ticket Controller Integration Tests")
class TicketControllerIntegrationTest extends BaseIntegrationTest {

    @Test
    @DisplayName("Customer should successfully create a ticket")
    void testCustomerCreateTicket() throws Exception {
        String customerToken = getCustomerToken();

        CreateTicketRequest request = CreateTicketRequest.builder()
                .title("Cannot export monthly billing invoice")
                .description("Clicking the PDF export button in settings returns a blank document.")
                .category(TicketCategory.BILLING)
                .priority(TicketPriority.HIGH)
                .build();

        mockMvc.perform(post("/api/tickets")
                        .header("Authorization", "Bearer " + customerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.ticketNumber", startsWith("TCK-")))
                .andExpect(jsonPath("$.data.status").value("OPEN"))
                .andExpect(jsonPath("$.data.priority").value("HIGH"))
                .andExpect(jsonPath("$.data.category").value("BILLING"))
                .andExpect(jsonPath("$.data.customer.email").value("customer@supportdesk.com"));
    }

    @Test
    @DisplayName("Customer GET /api/tickets should only return their own tickets")
    void testCustomerGetTicketsScoped() throws Exception {
        String customerToken = getCustomerToken();

        mockMvc.perform(get("/api/tickets")
                        .header("Authorization", "Bearer " + customerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content", notNullValue()))
                .andExpect(jsonPath("$.data.content[*].customer.email", everyItem(is("customer@supportdesk.com"))));
    }

    @Test
    @DisplayName("Agent GET /api/tickets should view all tickets across customers with filters")
    void testAgentGetTicketsAll() throws Exception {
        String agentToken = getAgentToken();

        mockMvc.perform(get("/api/tickets")
                        .header("Authorization", "Bearer " + agentToken)
                        .param("status", "OPEN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content", notNullValue()));
    }

    @Test
    @DisplayName("Agent can update ticket status to IN_PROGRESS and RESOLVED")
    void testAgentUpdateTicketStatus() throws Exception {
        String agentToken = getAgentToken();

        // Get an existing ticket from repository
        Ticket ticket = ticketRepository.findAll().stream().findFirst()
                .orElseThrow(() -> new IllegalStateException("No tickets found in database"));

        UpdateTicketStatusRequest statusRequest = new UpdateTicketStatusRequest(TicketStatus.IN_PROGRESS);

        mockMvc.perform(patch("/api/tickets/" + ticket.getId() + "/status")
                        .header("Authorization", "Bearer " + agentToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(statusRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("IN_PROGRESS"));
    }

    @Test
    @DisplayName("Agent can update ticket priority")
    void testAgentUpdateTicketPriority() throws Exception {
        String agentToken = getAgentToken();

        Ticket ticket = ticketRepository.findAll().stream().findFirst()
                .orElseThrow(() -> new IllegalStateException("No tickets found in database"));

        UpdateTicketPriorityRequest priorityRequest = new UpdateTicketPriorityRequest(TicketPriority.URGENT);

        mockMvc.perform(patch("/api/tickets/" + ticket.getId() + "/priority")
                        .header("Authorization", "Bearer " + agentToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(priorityRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.priority").value("URGENT"));
    }

    @Test
    @DisplayName("Agent can assign ticket to another agent")
    void testAgentAssignTicket() throws Exception {
        String agentToken = getAgentToken();

        Ticket ticket = ticketRepository.findAll().stream().findFirst()
                .orElseThrow(() -> new IllegalStateException("No tickets found in database"));

        // Assign to second agent
        User secondAgent = userRepository.findByEmail("testagent2@supportdesk.com")
                .orElseGet(() -> {
                    int freeId = 126;
                    if (userRepository.existsByAgentId(freeId)) {
                        freeId = 124;
                    }
                    return userRepository.save(com.supportdesk.entity.User.builder()
                            .fullName("Test Support Agent 2")
                            .email("testagent2@supportdesk.com")
                            .password(passwordEncoder.encode("Agent@123"))
                            .role(com.supportdesk.enums.Role.ROLE_SUPPORT_AGENT)
                            .agentId(freeId)
                            .build());
                });
        Long agent2Id = secondAgent.getId();

        AssignTicketRequest assignRequest = new AssignTicketRequest(agent2Id);

        mockMvc.perform(patch("/api/tickets/" + ticket.getId() + "/assign")
                        .header("Authorization", "Bearer " + agentToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(assignRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.assignedAgent.id").value(agent2Id));
    }

    @Test
    @DisplayName("Customer should be forbidden from assigning tickets (403 Forbidden)")
    void testCustomerForbiddenFromAssigningTicket() throws Exception {
        String customerToken = getCustomerToken();

        Ticket ticket = ticketRepository.findAll().stream().findFirst()
                .orElseThrow(() -> new IllegalStateException("No tickets found in database"));

        AssignTicketRequest assignRequest = new AssignTicketRequest(2L);

        mockMvc.perform(patch("/api/tickets/" + ticket.getId() + "/assign")
                        .header("Authorization", "Bearer " + customerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(assignRequest)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Customer should be forbidden from changing ticket priority (403 Forbidden)")
    void testCustomerForbiddenFromUpdatingPriority() throws Exception {
        String customerToken = getCustomerToken();

        Ticket ticket = ticketRepository.findAll().stream().findFirst()
                .orElseThrow(() -> new IllegalStateException("No tickets found in database"));

        UpdateTicketPriorityRequest priorityRequest = new UpdateTicketPriorityRequest(TicketPriority.URGENT);

        mockMvc.perform(patch("/api/tickets/" + ticket.getId() + "/priority")
                        .header("Authorization", "Bearer " + customerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(priorityRequest)))
                .andExpect(status().isForbidden());
    }
}
