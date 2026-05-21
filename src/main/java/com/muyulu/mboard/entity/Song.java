package com.muyulu.mboard.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@TableName("song")
public class Song extends BaseEntity {

    @TableId
    private Long id;
    private Long albumId;
    private String name;
    private Integer trackNo;
    private Integer durationSeconds;
}
