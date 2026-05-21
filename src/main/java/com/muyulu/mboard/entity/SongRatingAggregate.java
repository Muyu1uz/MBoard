package com.muyulu.mboard.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@TableName("song_rating_aggregate")
public class SongRatingAggregate extends BaseEntity {

    @TableId
    private Long songId;
    private Long ratingCount;
    private Long ratingStarSum;
    private BigDecimal ratingStarAvg;
}
