package com.supportdesk.controller;

import com.supportdesk.dto.auth.UserSummaryDto;
import com.supportdesk.dto.common.ApiResponse;
import com.supportdesk.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/agents")
    @PreAuthorize("hasAnyRole('AGENT', 'ROLE_AGENT', 'SUPPORT_AGENT', 'ROLE_SUPPORT_AGENT', 'ADMIN', 'ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<List<UserSummaryDto>>> getSupportAgents() {
        List<UserSummaryDto> agents = userService.getSupportAgents();
        return ResponseEntity.ok(ApiResponse.ok(agents));
    }
}
