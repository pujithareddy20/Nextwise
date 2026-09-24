package com.supportdesk.service.impl;

import com.supportdesk.dto.auth.AgentRegisterRequest;
import com.supportdesk.dto.auth.AuthResponse;
import com.supportdesk.dto.auth.CustomerRegisterRequest;
import com.supportdesk.dto.auth.LoginRequest;
import com.supportdesk.dto.auth.RegisterRequest;
import com.supportdesk.dto.auth.UserSummaryDto;
import com.supportdesk.entity.User;
import com.supportdesk.enums.Role;
import com.supportdesk.exception.BadRequestException;
import com.supportdesk.exception.ConflictException;
import com.supportdesk.repository.UserRepository;
import com.supportdesk.security.JwtTokenProvider;
import com.supportdesk.security.UserPrincipal;
import com.supportdesk.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail().trim().toLowerCase())) {
            throw new BadRequestException("An account with this email already exists");
        }

        User user = User.builder()
                .fullName(request.getFullName().trim())
                .email(request.getEmail().trim().toLowerCase())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Role.ROLE_CUSTOMER) // Default registration role
                .build();

        User savedUser = userRepository.save(user);

        String token = tokenProvider.generateTokenFromUser(
                savedUser.getEmail(),
                savedUser.getId(),
                savedUser.getRole().name(),
                savedUser.getFullName()
        );

        UserSummaryDto userSummary = UserSummaryDto.builder()
                .id(savedUser.getId())
                .fullName(savedUser.getFullName())
                .email(savedUser.getEmail())
                .role(savedUser.getRole())
                .build();

        return AuthResponse.builder()
                .accessToken(token)
                .tokenType("Bearer")
                .user(userSummary)
                .build();
    }

    @Override
    @Transactional
    public AuthResponse registerCustomer(CustomerRegisterRequest request) {
        String email = request.getEmail().trim().toLowerCase();
        if (userRepository.existsByEmail(email)) {
            throw new ConflictException("An account with this email already exists.");
        }

        User user = User.builder()
                .fullName(request.getFullName().trim())
                .email(email)
                .password(passwordEncoder.encode(request.getPassword()))
                .contactNumber(request.getContactNumber().trim())
                .role(Role.ROLE_CUSTOMER)
                .build();

        User savedUser = userRepository.save(user);

        String token = tokenProvider.generateTokenFromUser(
                savedUser.getEmail(),
                savedUser.getId(),
                savedUser.getRole().name(),
                savedUser.getFullName()
        );

        UserSummaryDto userSummary = UserSummaryDto.builder()
                .id(savedUser.getId())
                .fullName(savedUser.getFullName())
                .email(savedUser.getEmail())
                .contactNumber(savedUser.getContactNumber())
                .role(savedUser.getRole())
                .build();

        return AuthResponse.builder()
                .accessToken(token)
                .tokenType("Bearer")
                .user(userSummary)
                .build();
    }

    @Override
    @Transactional
    public AuthResponse registerAgent(AgentRegisterRequest request) {
        Integer agentId = request.getAgentId();
        if (agentId == null || agentId < 123 || agentId > 127) {
            throw new BadRequestException("Incorrect Agent ID. Please enter a valid authorized Agent ID.");
        }

        if (userRepository.existsByAgentId(agentId)) {
            throw new ConflictException("This Agent ID is already registered.");
        }

        String email = request.getEmail().trim().toLowerCase();
        if (userRepository.existsByEmail(email)) {
            throw new ConflictException("An account with this email already exists.");
        }

        User user = User.builder()
                .fullName(request.getFullName().trim())
                .email(email)
                .password(passwordEncoder.encode(request.getPassword()))
                .agentId(agentId)
                .role(Role.ROLE_SUPPORT_AGENT)
                .build();

        User savedUser = userRepository.save(user);

        String token = tokenProvider.generateTokenFromUser(
                savedUser.getEmail(),
                savedUser.getId(),
                savedUser.getRole().name(),
                savedUser.getFullName()
        );

        UserSummaryDto userSummary = UserSummaryDto.builder()
                .id(savedUser.getId())
                .fullName(savedUser.getFullName())
                .email(savedUser.getEmail())
                .agentId(savedUser.getAgentId())
                .role(savedUser.getRole())
                .build();

        return AuthResponse.builder()
                .accessToken(token)
                .tokenType("Bearer")
                .user(userSummary)
                .build();
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail().trim().toLowerCase(),
                        request.getPassword()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        String token = tokenProvider.generateToken(authentication);

        UserSummaryDto userSummary = UserSummaryDto.builder()
                .id(userPrincipal.getId())
                .fullName(userPrincipal.getFullName())
                .email(userPrincipal.getEmail())
                .role(userPrincipal.getRole())
                .contactNumber(userPrincipal.getContactNumber())
                .agentId(userPrincipal.getAgentId())
                .build();

        return AuthResponse.builder()
                .accessToken(token)
                .tokenType("Bearer")
                .user(userSummary)
                .build();
    }

    @Override
    public UserSummaryDto getCurrentUser(UserPrincipal userPrincipal) {
        return UserSummaryDto.builder()
                .id(userPrincipal.getId())
                .fullName(userPrincipal.getFullName())
                .email(userPrincipal.getEmail())
                .role(userPrincipal.getRole())
                .contactNumber(userPrincipal.getContactNumber())
                .agentId(userPrincipal.getAgentId())
                .build();
    }
}
