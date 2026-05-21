package com.muyulu.mboard.service.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AlbumQueryRequest {

    private String genre;
    private String keyword;
    private long page = 1;
    private long size = 12;
}
