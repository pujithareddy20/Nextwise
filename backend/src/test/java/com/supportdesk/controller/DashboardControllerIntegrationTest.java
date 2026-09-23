package com.supportdesk.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("Dashboard Controller Integration Tests")
class DashboardControllerIntegrationTest extends BaseIntegrationTest {

    @Test
    @DisplayName("Customer dashboard stats should return personal ticket metrics")
    void testCustomerDashboardStats() throws Exception {
        String customerToken = getCustomerToken();

        mockMvc.perform(get("/api/dashboard/stats")
                        .header("Authorization", "Bearer " + customerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalTickets", greaterThanOrEqualTo(0)))
                .andExpect(jsonPath("$.data.openTickets", greaterThanOrEqualTo(0)))
                .andExpect(jsonPath("$.data.inProgressTickets", greaterThanOrEqualTo(0)))
                .andExpect(jsonPath("$.data.resolvedTickets", greaterThanOrEqualTo(0)));
    }

    @Test
    @DisplayName("Agent dashboard stats should return platform-wide ticket metrics")
    void testAgentDashboardStats() throws Exception {
        String agentToken = getAgentToken();

        mockMvc.perform(get("/api/dashboard/stats")
                        .header("Authorization", "Bearer " + agentToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalTickets", greaterThanOrEqualTo(0)))
                .andExpect(jsonPath("$.data.highPriorityTickets", greaterThanOrEqualTo(0)))
                .andExpect(jsonPath("$.data.closedTickets", greaterThanOrEqualTo(0)));
    }

    @Test
    @DisplayName("Unauthenticated request to dashboard stats returns 401 Unauthorized")
    void testUnauthenticatedDashboardFails() throws Exception {
        mockMvc.perform(get("/api/dashboard/stats"))
                .andExpect(status().isUnauthorized());
    }
}
