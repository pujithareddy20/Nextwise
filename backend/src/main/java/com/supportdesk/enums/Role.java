package com.supportdesk.enums;

public enum Role {
    ROLE_CUSTOMER,
    ROLE_AGENT,
    ROLE_SUPPORT_AGENT,
    ROLE_ADMIN;

    public boolean isAgent() {
        return this == ROLE_AGENT || this == ROLE_SUPPORT_AGENT || this == ROLE_ADMIN;
    }

    public boolean isCustomer() {
        return this == ROLE_CUSTOMER;
    }
}
