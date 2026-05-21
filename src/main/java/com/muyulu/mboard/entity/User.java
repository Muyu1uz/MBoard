package com.muyulu.mboard.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.muyulu.mboard.enums.RoleType;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@TableName("mboard_user")
public class User extends BaseEntity {

    @TableId
    private Long id;
    private String username;
    private String passwordHash;
    private String displayName;
    private RoleType role;
}
