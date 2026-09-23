package com.supportdesk.security;

import com.supportdesk.entity.User;
import com.supportdesk.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

@Getter
@AllArgsConstructor
public class UserPrincipal implements UserDetails {

    private final Long id;
    private final String fullName;
    private final String email;
    private final String password;
    private final Role role;
    private final String contactNumber;
    private final Integer agentId;
    private final Collection<? extends GrantedAuthority> authorities;

    public UserPrincipal(Long id, String fullName, String email, String password, Role role, Collection<? extends GrantedAuthority> authorities) {
        this(id, fullName, email, password, role, null, null, authorities);
    }

    public static UserPrincipal create(User user) {
        java.util.List<GrantedAuthority> authorities = new java.util.ArrayList<>();
        String roleName = user.getRole().name();
        authorities.add(new SimpleGrantedAuthority(roleName));

        if (roleName.startsWith("ROLE_")) {
            authorities.add(new SimpleGrantedAuthority(roleName.substring(5)));
        } else {
            authorities.add(new SimpleGrantedAuthority("ROLE_" + roleName));
        }

        // Map both AGENT and SUPPORT_AGENT aliases interchangeably
        if (user.getRole() == Role.ROLE_AGENT || user.getRole() == Role.ROLE_SUPPORT_AGENT) {
            authorities.add(new SimpleGrantedAuthority("ROLE_AGENT"));
            authorities.add(new SimpleGrantedAuthority("AGENT"));
            authorities.add(new SimpleGrantedAuthority("ROLE_SUPPORT_AGENT"));
            authorities.add(new SimpleGrantedAuthority("SUPPORT_AGENT"));
        }

        return new UserPrincipal(
                user.getId(),
                user.getFullName(),
                user.getEmail(),
                user.getPassword(),
                user.getRole(),
                user.getContactNumber(),
                user.getAgentId(),
                authorities
        );
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
