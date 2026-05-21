package com.muyulu.mboard.view;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class CommentView {

    private Long id;
    private String username;
    private String content;
    private Integer ratingStarSnapshot;
    private Integer ratingScoreSnapshot;
    private LocalDateTime createdAt;
}
