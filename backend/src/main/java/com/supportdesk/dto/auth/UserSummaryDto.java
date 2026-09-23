package com.supportdesk.dto.auth;

import com.supportdesk.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserSummaryDto {
    private Long id;
    private String fullName;
    private String email;
    private Role role;
    private String contactNumber;
    private Integer agentId;
}
