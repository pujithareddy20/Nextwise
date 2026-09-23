package com.supportdesk.service;

import com.supportdesk.dto.auth.UserSummaryDto;

import java.util.List;

public interface UserService {
    List<UserSummaryDto> getSupportAgents();
}
