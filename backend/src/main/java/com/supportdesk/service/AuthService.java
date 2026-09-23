package com.supportdesk.service;

import com.supportdesk.dto.auth.AgentRegisterRequest;
import com.supportdesk.dto.auth.AuthResponse;
import com.supportdesk.dto.auth.CustomerRegisterRequest;
import com.supportdesk.dto.auth.LoginRequest;
import com.supportdesk.dto.auth.RegisterRequest;
import com.supportdesk.dto.auth.UserSummaryDto;
import com.supportdesk.security.UserPrincipal;

public interface AuthService {
    AuthResponse register(RegisterRequest request);
    AuthResponse registerCustomer(CustomerRegisterRequest request);
    AuthResponse registerAgent(AgentRegisterRequest request);
    AuthResponse login(LoginRequest request);
    AuthResponse adminLogin(LoginRequest request);
    UserSummaryDto getCurrentUser(UserPrincipal userPrincipal);
}
