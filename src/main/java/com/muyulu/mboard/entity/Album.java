package com.muyulu.mboard.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@TableName("album")
public class Album extends BaseEntity {

    @TableId
    private Long id;
    private Long artistId;
    private String name;
    private String genre;
    private String coverUrl;
    private String summary;
    private LocalDate releaseDate;
    private boolean trending;
    private boolean published;
}
