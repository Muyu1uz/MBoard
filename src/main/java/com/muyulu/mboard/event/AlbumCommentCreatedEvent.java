package com.muyulu.mboard.event;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class AlbumCommentCreatedEvent {

    private String eventId;
    private Long albumId;
    private Long userId;
    private LocalDateTime eventTime;

    @Builder
    public AlbumCommentCreatedEvent(String eventId, Long albumId, Long userId, LocalDateTime eventTime) {
        this.eventId = eventId;
        this.albumId = albumId;
        this.userId = userId;
        this.eventTime = eventTime;
    }
}
