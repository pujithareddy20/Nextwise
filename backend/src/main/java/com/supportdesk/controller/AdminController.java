package com.supportdesk.controller;

import com.supportdesk.dto.admin.AdminAgentWorkloadDto;
import com.supportdesk.dto.admin.AdminDashboardStatsResponse;
import com.supportdesk.dto.common.ApiResponse;
import com.supportdesk.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'ROLE_ADMIN')")
public class AdminController {

    private final AdminService adminService;

    @GetMapping("/dashboard/stats")
    public ResponseEntity<ApiResponse<AdminDashboardStatsResponse>> getAdminDashboardStats() {
        AdminDashboardStatsResponse stats = adminService.getAdminDashboardStats();
        return ResponseEntity.ok(ApiResponse.ok(stats));
    }

    @GetMapping("/agents")
    public ResponseEntity<ApiResponse<List<AdminAgentWorkloadDto>>> getAgentWorkloads() {
        List<AdminAgentWorkloadDto> workloads = adminService.getAgentWorkloads();
        return ResponseEntity.ok(ApiResponse.ok(workloads));
    }
}
