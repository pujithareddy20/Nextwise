package com.supportdesk.controller;

import com.supportdesk.dto.auth.LoginRequest;
import com.supportdesk.dto.auth.RegisterRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("Auth Controller Integration Tests")
class AuthControllerIntegrationTest extends BaseIntegrationTest {

    @Test
    @DisplayName("Should successfully register a new customer user")
    void testRegisterSuccess() throws Exception {
        RegisterRequest request = new RegisterRequest("New User", "newuser_" + System.currentTimeMillis() + "@test.com", "Password@123");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message", containsString("User registered successfully")));
    }

    @Test
    @DisplayName("Should return 400 Bad Request when registering duplicate email")
    void testRegisterDuplicateEmail() throws Exception {
        RegisterRequest request = new RegisterRequest("Duplicate User", "customer@supportdesk.com", "Password@123");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message", containsString("already exists")));
    }

    @Test
    @DisplayName("Should return 400 Bad Request when validation constraints fail")
    void testRegisterValidationFailure() throws Exception {
        RegisterRequest request = new RegisterRequest("", "invalid-email", "123");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.validationErrors.fullName").exists())
                .andExpect(jsonPath("$.validationErrors.email").exists())
                .andExpect(jsonPath("$.validationErrors.password").exists());
    }

    @Test
    @DisplayName("Should successfully login and return JWT token")
    void testLoginSuccess() throws Exception {
        LoginRequest request = new LoginRequest("customer@supportdesk.com", "Customer@123");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.accessToken", notNullValue()))
                .andExpect(jsonPath("$.data.user.email").value("customer@supportdesk.com"))
                .andExpect(jsonPath("$.data.user.role").value("ROLE_CUSTOMER"));
    }

    @Test
    @DisplayName("Should return 401 Unauthorized for invalid password")
    void testLoginBadCredentials() throws Exception {
        LoginRequest request = new LoginRequest("customer@supportdesk.com", "WrongPassword@123");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message", containsString("Invalid email or password")));
    }

    @Test
    @DisplayName("Should successfully register customer with contact number")
    void testRegisterCustomerSuccess() throws Exception {
        com.supportdesk.dto.auth.CustomerRegisterRequest request = com.supportdesk.dto.auth.CustomerRegisterRequest.builder()
                .fullName("Alice Customer")
                .email("alice_" + System.currentTimeMillis() + "@test.com")
                .password("Password@123")
                .contactNumber("+1 555-123-4567")
                .build();

        mockMvc.perform(post("/api/auth/register/customer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.user.role").value("ROLE_CUSTOMER"))
                .andExpect(jsonPath("$.data.user.contactNumber").value("+1 555-123-4567"));
    }

    @Test
    @DisplayName("Should return 409 Conflict when registering customer with duplicate email")
    void testRegisterCustomerDuplicateEmail() throws Exception {
        com.supportdesk.dto.auth.CustomerRegisterRequest request = com.supportdesk.dto.auth.CustomerRegisterRequest.builder()
                .fullName("Duplicate Customer")
                .email("customer@supportdesk.com")
                .password("Password@123")
                .contactNumber("9876543210")
                .build();

        mockMvc.perform(post("/api/auth/register/customer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message", containsString("already exists")));
    }

    @Test
    @DisplayName("Should return 400 Bad Request when customer contact number format is invalid")
    void testRegisterCustomerInvalidContactNumber() throws Exception {
        com.supportdesk.dto.auth.CustomerRegisterRequest request = com.supportdesk.dto.auth.CustomerRegisterRequest.builder()
                .fullName("Invalid Phone Customer")
                .email("invalidphone_" + System.currentTimeMillis() + "@test.com")
                .password("Password@123")
                .contactNumber("123") // too short
                .build();

        mockMvc.perform(post("/api/auth/register/customer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.validationErrors.contactNumber").exists());
    }

    @Test
    @DisplayName("Should successfully register service agent with valid Agent ID 123")
    void testRegisterAgentSuccess() throws Exception {
        com.supportdesk.dto.auth.AgentRegisterRequest request = com.supportdesk.dto.auth.AgentRegisterRequest.builder()
                .fullName("Agent Jack")
                .email("jack_" + System.currentTimeMillis() + "@test.com")
                .password("Password@123")
                .agentId(123)
                .build();

        mockMvc.perform(post("/api/auth/register/agent")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.user.role").value("ROLE_SUPPORT_AGENT"))
                .andExpect(jsonPath("$.data.user.agentId").value(123));
    }

    @Test
    @DisplayName("Should return 400 Bad Request when Agent ID is outside 123-127")
    void testRegisterAgentInvalidAgentId() throws Exception {
        // Test ID 122
        com.supportdesk.dto.auth.AgentRegisterRequest request122 = com.supportdesk.dto.auth.AgentRegisterRequest.builder()
                .fullName("Invalid Agent 122")
                .email("agent122_" + System.currentTimeMillis() + "@test.com")
                .password("Password@123")
                .agentId(122)
                .build();

        mockMvc.perform(post("/api/auth/register/agent")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request122)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message", org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsString("123 and 127"))));

        // Test ID 128
        com.supportdesk.dto.auth.AgentRegisterRequest request128 = com.supportdesk.dto.auth.AgentRegisterRequest.builder()
                .fullName("Invalid Agent 128")
                .email("agent128_" + System.currentTimeMillis() + "@test.com")
                .password("Password@123")
                .agentId(128)
                .build();

        mockMvc.perform(post("/api/auth/register/agent")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request128)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message", org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsString("123 and 127"))));
    }

    @Test
    @DisplayName("Should return 409 Conflict when registering duplicate Agent ID")
    void testRegisterAgentDuplicateAgentId() throws Exception {
        // Register with Agent ID 125
        com.supportdesk.dto.auth.AgentRegisterRequest request1 = com.supportdesk.dto.auth.AgentRegisterRequest.builder()
                .fullName("Agent Unique 125")
                .email("agent125_" + System.currentTimeMillis() + "@test.com")
                .password("Password@123")
                .agentId(125)
                .build();

        mockMvc.perform(post("/api/auth/register/agent")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request1)))
                .andExpect(status().isCreated());

        // Try registering again with the same Agent ID 125
        com.supportdesk.dto.auth.AgentRegisterRequest duplicateAgent = com.supportdesk.dto.auth.AgentRegisterRequest.builder()
                .fullName("Duplicate Agent 125")
                .email("diffemail_" + System.currentTimeMillis() + "@test.com")
                .password("Password@123")
                .agentId(125)
                .build();

        mockMvc.perform(post("/api/auth/register/agent")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(duplicateAgent)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message", containsString("This Agent ID is already registered.")));
    }
}
