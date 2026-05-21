package com.muyulu.mboard.event;

import com.muyulu.mboard.enums.TargetType;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class RatingChangedEvent {

    private String eventId;
    private Long userId;
    private String username;
    private TargetType targetType;
    private Long targetId;
    private Integer oldStar;
    private Integer newStar;
    private Long ratingCount;
    private Long ratingStarSum;
    private Double ratingStarAvg;
    private LocalDateTime eventTime;

    @Builder
    public RatingChangedEvent(String eventId,
                              Long userId,
                              String username,
                              TargetType targetType,
                              Long targetId,
                              Integer oldStar,
                              Integer newStar,
                              Long ratingCount,
                              Long ratingStarSum,
                              Double ratingStarAvg,
                              LocalDateTime eventTime) {
        this.eventId = eventId;
        this.userId = userId;
        this.username = username;
        this.targetType = targetType;
        this.targetId = targetId;
        this.oldStar = oldStar;
        this.newStar = newStar;
        this.ratingCount = ratingCount;
        this.ratingStarSum = ratingStarSum;
        this.ratingStarAvg = ratingStarAvg;
        this.eventTime = eventTime;
    }
}
