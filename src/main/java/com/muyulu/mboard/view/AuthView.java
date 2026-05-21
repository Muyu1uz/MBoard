package com.muyulu.mboard.view;

import com.muyulu.mboard.enums.RoleType;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AuthView {

    private Long userId;
    private String username;
    private String displayName;
    private RoleType role;
    private String token;
}
