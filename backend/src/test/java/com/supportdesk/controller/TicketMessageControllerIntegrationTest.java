package com.supportdesk.controller;

import com.supportdesk.dto.message.CreateMessageRequest;
import com.supportdesk.entity.Ticket;
import com.supportdesk.entity.User;
import com.supportdesk.enums.TicketCategory;
import com.supportdesk.enums.TicketPriority;
import com.supportdesk.enums.TicketStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("Ticket Message Controller Integration Tests")
class TicketMessageControllerIntegrationTest extends BaseIntegrationTest {

    private Ticket ticket;
    private String customerToken;
    private String agentToken;

    @BeforeEach
    void setUpTicket() {
        customerToken = getCustomerToken();
        agentToken = getAgentToken();

        User customer = userRepository.findByEmail("customer@supportdesk.com")
                .orElseThrow(() -> new IllegalStateException("Customer not found"));

        ticket = ticketRepository.save(Ticket.builder()
                .ticketNumber("TCK-MSG-" + System.currentTimeMillis())
                .title("Message Thread Test Ticket")
                .description("Verifying public conversation and agent internal notes.")
                .category(TicketCategory.TECHNICAL)
                .priority(TicketPriority.MEDIUM)
                .status(TicketStatus.OPEN)
                .customer(customer)
                .build());
    }

    @Test
    @DisplayName("Customer can post a public message")
    void testCustomerAddPublicMessage() throws Exception {
        CreateMessageRequest request = CreateMessageRequest.builder()
                .message("Hello support team, I need help with installation.")
                .internalNote(false)
                .build();

        mockMvc.perform(post("/api/tickets/" + ticket.getId() + "/messages")
                        .header("Authorization", "Bearer " + customerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.message").value("Hello support team, I need help with installation."))
                .andExpect(jsonPath("$.data.internalNote").value(false));
    }

    @Test
    @DisplayName("Agent can post an internal note")
    void testAgentAddInternalNote() throws Exception {
        CreateMessageRequest request = CreateMessageRequest.builder()
                .message("Internal Note: User has active Enterprise subscription.")
                .internalNote(true)
                .build();

        mockMvc.perform(post("/api/tickets/" + ticket.getId() + "/messages")
                        .header("Authorization", "Bearer " + agentToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.internalNote").value(true));
    }

    @Test
    @DisplayName("Customer attempting internalNote: true is sanitized to false")
    void testCustomerCannotCreateInternalNote() throws Exception {
        CreateMessageRequest request = CreateMessageRequest.builder()
                .message("Customer trying to mark message as internal note")
                .internalNote(true)
                .build();

        mockMvc.perform(post("/api/tickets/" + ticket.getId() + "/messages")
                        .header("Authorization", "Bearer " + customerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.internalNote").value(false));
    }

    @Test
    @DisplayName("Customer must NOT see internal notes in conversation thread")
    void testCustomerCannotViewInternalNotes() throws Exception {
        // 1. Customer posts message
        CreateMessageRequest pubMsg = CreateMessageRequest.builder()
                .message("Public message from customer")
                .internalNote(false)
                .build();
        mockMvc.perform(post("/api/tickets/" + ticket.getId() + "/messages")
                .header("Authorization", "Bearer " + customerToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(pubMsg)));

        // 2. Agent posts internal note
        CreateMessageRequest intNote = CreateMessageRequest.builder()
                .message("Secret internal note by agent")
                .internalNote(true)
                .build();
        mockMvc.perform(post("/api/tickets/" + ticket.getId() + "/messages")
                .header("Authorization", "Bearer " + agentToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(intNote)));

        // 3. Customer retrieves messages -> internal notes must be excluded
        mockMvc.perform(get("/api/tickets/" + ticket.getId() + "/messages")
                        .header("Authorization", "Bearer " + customerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[*].internalNote", everyItem(is(false))))
                .andExpect(jsonPath("$.data[*].message", not(hasItem("Secret internal note by agent"))));
    }

    @Test
    @DisplayName("Agent CAN view all messages including internal notes")
    void testAgentCanViewInternalNotes() throws Exception {
        // 1. Agent posts internal note
        CreateMessageRequest intNote = CreateMessageRequest.builder()
                .message("Private diagnosis note")
                .internalNote(true)
                .build();
        mockMvc.perform(post("/api/tickets/" + ticket.getId() + "/messages")
                .header("Authorization", "Bearer " + agentToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(intNote)));

        // 2. Agent retrieves messages -> internal note must be present
        mockMvc.perform(get("/api/tickets/" + ticket.getId() + "/messages")
                        .header("Authorization", "Bearer " + agentToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[*].message", hasItem("Private diagnosis note")));
    }
}
