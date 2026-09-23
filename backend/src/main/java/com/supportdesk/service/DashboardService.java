package com.supportdesk.service;

import com.supportdesk.dto.dashboard.DashboardStatsResponse;
import com.supportdesk.security.UserPrincipal;

public interface DashboardService {
    DashboardStatsResponse getDashboardStats(UserPrincipal currentUser);
}
