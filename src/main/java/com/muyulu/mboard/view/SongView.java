package com.muyulu.mboard.view;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SongView {

    private Long id;
    private String name;
    private Integer trackNo;
    private Integer durationSeconds;
    private Integer ratingCount;
    private Double ratingScore10;
    private Integer currentUserStar;
    private Integer currentUserScore10;
}
