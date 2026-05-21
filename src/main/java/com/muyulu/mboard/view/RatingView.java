package com.muyulu.mboard.view;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class RatingView {

    private Integer previousStar;
    private Integer currentStar;
    private Integer ratingCount;
    private Double ratingScore10;
}
