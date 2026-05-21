package com.muyulu.mboard.common.security;

import com.muyulu.mboard.enums.RoleType;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AuthUser {

    private Long userId;
    private String username;
    private RoleType role;
}
