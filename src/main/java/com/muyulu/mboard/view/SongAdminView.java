package com.muyulu.mboard.view;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SongAdminView {

    private Long id;
    private Long albumId;
    private String name;
    private Integer trackNo;
    private Integer durationSeconds;
}
