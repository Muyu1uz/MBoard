package com.muyulu.mboard.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@TableName("album_rating_user")
public class AlbumRatingUser extends BaseEntity {

    @TableId
    private Long id;
    private Long albumId;
    private Long userId;
    private Integer star;
}
