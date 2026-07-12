package com.example.alarm.global.security;

import lombok.Getter;

@Getter
public class UserPrincipal {

    private final String userId;

    private final Role role;

    public UserPrincipal(String userId, Role role) {
        this.userId = userId;
        this.role = role;
    }
}
