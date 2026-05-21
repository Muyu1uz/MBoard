package com.muyulu.mboard.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@TableName("song_rating_user")
public class SongRatingUser extends BaseEntity {

    @TableId
    private Long id;
    private Long songId;
    private Long userId;
    private Integer star;
}
