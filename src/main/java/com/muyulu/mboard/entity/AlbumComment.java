package com.muyulu.mboard.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@TableName("album_comment")
public class AlbumComment extends BaseEntity {

    @TableId
    private Long id;
    private Long albumId;
    private Long userId;
    private String username;
    private String content;
    private Integer ratingStarSnapshot;
    private Integer ratingScoreSnapshot;
}
