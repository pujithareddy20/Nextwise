package com.supportdesk.controller;

import com.supportdesk.dto.ai.AiClassifyTicketRequest;
import com.supportdesk.dto.ai.AiClassifyTicketResponse;
import com.supportdesk.enums.TicketCategory;
import com.supportdesk.enums.TicketPriority;
import com.supportdesk.exception.AiServiceUnavailableException;
import com.supportdesk.service.AiClassificationService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("AI Classification Controller Integration Tests")
class AiClassificationControllerIntegrationTest extends BaseIntegrationTest {

    @MockBean
    private AiClassificationService aiClassificationService;

    @Test
    @DisplayName("Should successfully return AI classification suggestion for customer")
    void testCustomerAiClassifySuccess() throws Exception {
        String customerToken = getCustomerToken();

        AiClassifyTicketRequest request = AiClassifyTicketRequest.builder()
                .title("Cannot export invoices to PDF")
                .description("Whenever I click export in the billing tab, a 500 error is returned and no PDF downloads.")
                .build();

        AiClassifyTicketResponse mockResponse = AiClassifyTicketResponse.builder()
                .category(TicketCategory.BILLING)
                .priority(TicketPriority.HIGH)
                .reason("The issue relates to payment and billing invoice generation, directly impeding financial records.")
                .build();

        when(aiClassificationService.classifyTicket(any())).thenReturn(mockResponse);

        mockMvc.perform(post("/api/ai/suggest-ticket-classification")
                        .header("Authorization", "Bearer " + customerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.category").value("BILLING"))
                .andExpect(jsonPath("$.data.priority").value("HIGH"))
                .andExpect(jsonPath("$.data.reason").value("The issue relates to payment and billing invoice generation, directly impeding financial records."));
    }

    @Test
    @DisplayName("Should also work via alias endpoint /api/tickets/ai-classify")
    void testAliasEndpointSuccess() throws Exception {
        String customerToken = getCustomerToken();

        AiClassifyTicketRequest request = AiClassifyTicketRequest.builder()
                .title("App crashes when opening user settings")
                .description("On iOS mobile safari, clicking profile settings crashes the tab instantly.")
                .build();

        AiClassifyTicketResponse mockResponse = AiClassifyTicketResponse.builder()
                .category(TicketCategory.TECHNICAL)
                .priority(TicketPriority.URGENT)
                .reason("A reproducible crash in core user settings warrants urgent technical triage.")
                .build();

        when(aiClassificationService.classifyTicket(any())).thenReturn(mockResponse);

        mockMvc.perform(post("/api/tickets/ai-classify")
                        .header("Authorization", "Bearer " + customerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.category").value("TECHNICAL"))
                .andExpect(jsonPath("$.data.priority").value("URGENT"));
    }

    @Test
    @DisplayName("Should return 400 Bad Request when title or description is missing/too short")
    void testValidationFailure() throws Exception {
        String customerToken = getCustomerToken();

        AiClassifyTicketRequest invalidRequest = AiClassifyTicketRequest.builder()
                .title("Hi")
                .description("Short")
                .build();

        mockMvc.perform(post("/api/ai/suggest-ticket-classification")
                        .header("Authorization", "Bearer " + customerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("Should return 503 Service Unavailable when AI service is not configured")
    void testAiServiceNotConfigured() throws Exception {
        String customerToken = getCustomerToken();

        AiClassifyTicketRequest request = AiClassifyTicketRequest.builder()
                .title("Cannot export invoices to PDF")
                .description("Whenever I click export in billing tab, an error occurs.")
                .build();

        when(aiClassificationService.classifyTicket(any()))
                .thenThrow(new AiServiceUnavailableException("AI classification service is not configured. Please set GEMINI_API_KEY in the environment."));

        mockMvc.perform(post("/api/ai/suggest-ticket-classification")
                        .header("Authorization", "Bearer " + customerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("AI classification service is not configured. Please set GEMINI_API_KEY in the environment."));
    }

    @Test
    @DisplayName("Should return 401 Unauthorized when unauthenticated")
    void testUnauthenticatedAccess() throws Exception {
        AiClassifyTicketRequest request = AiClassifyTicketRequest.builder()
                .title("Cannot export invoices to PDF")
                .description("Whenever I click export in billing tab, an error occurs.")
                .build();

        mockMvc.perform(post("/api/ai/suggest-ticket-classification")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }
}
