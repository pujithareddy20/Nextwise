package com.supportdesk.controller;

import com.supportdesk.dto.auth.LoginRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("Admin Controller & Security Integration Tests")
class AdminControllerIntegrationTest extends BaseIntegrationTest {

    @Test
    @DisplayName("Admin should successfully log in via /api/auth/admin/login")
    void testAdminLoginSuccess() throws Exception {
        LoginRequest request = LoginRequest.builder()
                .email("admin@supportdesk.com")
                .password("Admin@123")
                .build();

        mockMvc.perform(post("/api/auth/admin/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.accessToken").isString())
                .andExpect(jsonPath("$.data.user.role").value("ROLE_ADMIN"))
                .andExpect(jsonPath("$.data.user.email").value("admin@supportdesk.com"));
    }

    @Test
    @DisplayName("Customer credentials should be rejected on /api/auth/admin/login (403 Forbidden)")
    void testCustomerLoginToAdminRejected() throws Exception {
        LoginRequest request = LoginRequest.builder()
                .email("customer@supportdesk.com")
                .password("Customer@123")
                .build();

        mockMvc.perform(post("/api/auth/admin/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Access denied. Administrator credentials required."));
    }

    @Test
    @DisplayName("Support Agent credentials should be rejected on /api/auth/admin/login (403 Forbidden)")
    void testAgentLoginToAdminRejected() throws Exception {
        getAgentToken(); // ensures agent exists

        LoginRequest request = LoginRequest.builder()
                .email("testagent@supportdesk.com")
                .password("Agent@123")
                .build();

        mockMvc.perform(post("/api/auth/admin/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Access denied. Administrator credentials required."));
    }

    @Test
    @DisplayName("Invalid admin credentials should return 401 Unauthorized")
    void testInvalidAdminCredentials() throws Exception {
        LoginRequest request = LoginRequest.builder()
                .email("admin@supportdesk.com")
                .password("WrongPassword@123")
                .build();

        mockMvc.perform(post("/api/auth/admin/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Admin should successfully access /api/admin/dashboard/stats")
    void testAdminGetDashboardStats() throws Exception {
        String adminToken = getAdminToken();

        mockMvc.perform(get("/api/admin/dashboard/stats")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalTickets").isNumber())
                .andExpect(jsonPath("$.data.openTickets").isNumber())
                .andExpect(jsonPath("$.data.categoryCounts").isMap())
                .andExpect(jsonPath("$.data.priorityCounts").isMap())
                .andExpect(jsonPath("$.data.totalCustomers").isNumber());
    }

    @Test
    @DisplayName("Customer should be blocked from /api/admin/dashboard/stats (403 Forbidden)")
    void testCustomerBlockedFromAdminStats() throws Exception {
        String customerToken = getCustomerToken();

        mockMvc.perform(get("/api/admin/dashboard/stats")
                        .header("Authorization", "Bearer " + customerToken))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Support Agent should be blocked from /api/admin/dashboard/stats (403 Forbidden)")
    void testAgentBlockedFromAdminStats() throws Exception {
        String agentToken = getAgentToken();

        mockMvc.perform(get("/api/admin/dashboard/stats")
                        .header("Authorization", "Bearer " + agentToken))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Admin should successfully access /api/admin/agents")
    void testAdminGetAgents() throws Exception {
        String adminToken = getAdminToken();

        mockMvc.perform(get("/api/admin/agents")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    @DisplayName("Customer should be blocked from /api/admin/agents (403 Forbidden)")
    void testCustomerBlockedFromAdminAgents() throws Exception {
        String customerToken = getCustomerToken();

        mockMvc.perform(get("/api/admin/agents")
                        .header("Authorization", "Bearer " + customerToken))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Unauthenticated request to /api/admin/dashboard/stats should return 401 Unauthorized")
    void testUnauthenticatedBlocked() throws Exception {
        mockMvc.perform(get("/api/admin/dashboard/stats"))
                .andExpect(status().isUnauthorized());
    }
}
