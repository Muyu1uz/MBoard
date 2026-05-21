package com.muyulu.mboard.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@TableName("artist")
public class Artist extends BaseEntity {

    @TableId
    private Long id;
    private String name;
    private String avatarUrl;
    private String bio;
}
