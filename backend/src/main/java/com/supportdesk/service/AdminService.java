package com.supportdesk.service;

import com.supportdesk.dto.admin.AdminAgentWorkloadDto;
import com.supportdesk.dto.admin.AdminDashboardStatsResponse;

import java.util.List;

public interface AdminService {
    AdminDashboardStatsResponse getAdminDashboardStats();
    List<AdminAgentWorkloadDto> getAgentWorkloads();
}
