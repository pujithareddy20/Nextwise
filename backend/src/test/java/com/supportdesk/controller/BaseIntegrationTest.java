package com.supportdesk.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.supportdesk.entity.User;
import com.supportdesk.enums.Role;
import com.supportdesk.repository.TicketMessageRepository;
import com.supportdesk.repository.TicketRepository;
import com.supportdesk.repository.UserRepository;
import com.supportdesk.security.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
public abstract class BaseIntegrationTest {

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    @Autowired
    protected JwtTokenProvider jwtTokenProvider;

    @Autowired
    protected UserRepository userRepository;

    @Autowired
    protected TicketRepository ticketRepository;

    @Autowired
    protected TicketMessageRepository messageRepository;

    @Autowired
    protected PasswordEncoder passwordEncoder;

    protected String getCustomerToken() {
        User user = userRepository.findByEmail("customer@supportdesk.com")
                .orElseThrow(() -> new IllegalStateException("Customer user not found"));
        return jwtTokenProvider.generateTokenFromUser(user.getEmail(), user.getId(), user.getRole().name(), user.getFullName());
    }

    protected String getAgentToken() {
        User user = userRepository.findAll().stream()
                .filter(u -> u.getRole() == Role.ROLE_SUPPORT_AGENT)
                .findFirst()
                .orElseGet(() -> userRepository.save(User.builder()
                        .fullName("Test Support Agent")
                        .email("testagent@supportdesk.com")
                        .password(passwordEncoder.encode("Agent@123"))
                        .role(Role.ROLE_SUPPORT_AGENT)
                        .agentId(127)
                        .build()));
        return jwtTokenProvider.generateTokenFromUser(user.getEmail(), user.getId(), user.getRole().name(), user.getFullName());
    }

    protected String getAdminToken() {
        User user = userRepository.findByEmail("admin@supportdesk.com")
                .orElseThrow(() -> new IllegalStateException("Admin user not found"));
        return jwtTokenProvider.generateTokenFromUser(user.getEmail(), user.getId(), user.getRole().name(), user.getFullName());
    }

    protected String createTokenForUser(User user) {
        return jwtTokenProvider.generateTokenFromUser(user.getEmail(), user.getId(), user.getRole().name(), user.getFullName());
    }

    protected User createSecondCustomer(String email, String fullName) {
        return userRepository.findByEmail(email).orElseGet(() ->
                userRepository.save(User.builder()
                        .fullName(fullName)
                        .email(email)
                        .password(passwordEncoder.encode("Password@123"))
                        .role(Role.ROLE_CUSTOMER)
                        .build())
        );
    }
}
