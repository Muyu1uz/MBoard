package com.muyulu.mboard.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@TableName("rating_event_consume_log")
public class RatingEventConsumeLog extends BaseEntity {

    @TableId
    private String eventId;
    private String topic;
}
