package com.muyulu.mboard.view;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SongStaticView {

    private Long id;
    private String name;
    private Integer trackNo;
    private Integer durationSeconds;
}
