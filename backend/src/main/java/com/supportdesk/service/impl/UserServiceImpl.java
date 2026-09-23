package com.supportdesk.service.impl;

import com.supportdesk.dto.auth.UserSummaryDto;
import com.supportdesk.entity.User;
import com.supportdesk.enums.Role;
import com.supportdesk.repository.UserRepository;
import com.supportdesk.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public List<UserSummaryDto> getSupportAgents() {
        List<User> agents = userRepository.findByRole(Role.ROLE_SUPPORT_AGENT);

        return agents.stream()
                .map(user -> UserSummaryDto.builder()
                        .id(user.getId())
                        .fullName(user.getFullName())
                        .email(user.getEmail())
                        .role(user.getRole())
                        .build())
                .collect(Collectors.toList());
    }
}
